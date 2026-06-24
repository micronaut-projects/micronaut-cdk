package io.micronaut.cdk.graph

import io.micronaut.cdk.ExecutionContext
import io.micronaut.cdk.action.Actions
import io.micronaut.cdk.action.ThingSpec
import io.micronaut.cdk.command.CdkThreadFactory
import io.micronaut.cdk.command.Command
import io.micronaut.cdk.command.Commands
import io.micronaut.cdk.component.ExistingComponents
import io.micronaut.cdk.delta.Delta
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import org.awaitility.Awaitility
import spock.lang.Specification

import java.util.concurrent.Callable
import java.util.concurrent.Executors

import static io.micronaut.cdk.FailureMode.LAZY_FAILURE
import static io.micronaut.cdk.command.Command.Status.FAILED
import static io.micronaut.cdk.command.Command.Status.INITIAL
import static io.micronaut.cdk.command.Command.Status.SKIPPED
import static io.micronaut.cdk.command.Command.Status.SUCCESSFUL
import static io.micronaut.cdk.graph.GraphExecutor.State.FINISHED
import static io.micronaut.cdk.graph.GraphExecutor.State.READY
import static java.util.concurrent.TimeUnit.SECONDS

@MicronautTest
class GraphExecutorSpec extends Specification {

    void 'test concurrent task execution'() {
        given:
        var vm = VmSpec.builder('vm')
                .name('name')
                .stack('stack-1')
                .build()
        var db = DbSpec.builder('db')
                .name('name')
                .stack('stack-1')
                .build()
        var obj = ObjectStorageSpec.builder('object-storage')
                .name('name')
                .stack('stack-1')
                .build()
        var lb = LbSpec.builder('lb')
                .name('name')
                .stack('stack-1')
                .build()
        var subnet = SubnetSpec.builder('subnet')
                .name('name')
                .stack('stack-1')
                .build()
        var vpc = VpcSpec.builder('vpc')
                .name('name')
                .stack('stack-1')
                .build()

        subnet.addDependency(vpc)
        lb.addDependency(subnet)
        vm.addDependency(subnet)
        vm.addDependency(lb)
        db.addDependency(subnet)
        db.addDependency(vm)

        var ec = new ExecutionContext(true)
        ec.actions = new Actions(vm, db, obj, lb, subnet, vpc)
        ec.existingComponents = new ExistingComponents(List.of())
        ec.delta = new Delta(Set.of(vm, db, obj, lb, subnet, vpc), Map.of(), Map.of(), List.of(), List.of(), List.of())

        var commands = new Commands(
                Command.create(vm),
                Command.create(db),
                Command.create(obj),
                Command.create(lb),
                Command.create(subnet),
                Command.create(vpc))

        ec.commands = commands

        Graph graph = new GraphBuilder(commands).build()

        var executor = Executors.newFixedThreadPool(10, new CdkThreadFactory())
        TestTaskFactory factory = new TestTaskFactory()

        when:
        var graphExecutor = new GraphExecutor(graph, executor, ec, factory)

        then:
        graphExecutor.getNodeCountWithState(READY) == 2

        when:
        graphExecutor.run()

        then:
        Awaitility.await().atMost(5, SECONDS)
                .until(() -> graphExecutor.getNodeCountWithState(FINISHED) == commands.size())

        graphExecutor.getNodeCountWithState(FINISHED) == commands.size()

        cleanup:
        executor.shutdown()
    }

    void 'task execution with exception, LAZY_FAILURE'() {
        given:
        var vm = VmSpec.builder('vm')
                .name('name')
                .stack('stack-1')
                .build()
        var db = DbSpec.builder('db')
                .name('name')
                .stack('stack-1')
                .build()
        var obj = ObjectStorageSpec.builder('object-storage')
                .name('name')
                .stack('stack-1')
                .build()
        var lb = LbSpec.builder('lb')
                .name('name')
                .stack('stack-1')
                .build()
        var subnet = SubnetSpec.builder('subnet')
                .name('name')
                .stack('stack-1')
                .build()
        var vpc = VpcSpec.builder('vpc')
                .name('name')
                .stack('stack-1')
                .build()

        subnet.addDependency(vpc)
        lb.addDependency(subnet)
        vm.addDependency(subnet)
        vm.addDependency(lb)
        db.addDependency(subnet)
        db.addDependency(vm)

        var ec = new ExecutionContext(true)
        ec.actions = new Actions(vm, db, obj, lb, subnet, vpc)
        ec.existingComponents = new ExistingComponents(List.of())
        ec.delta = new Delta(Set.of(vm, db, obj, lb, subnet, vpc), Map.of(), Map.of(), List.of(), List.of(), List.of())

        var vmCommand = Command.create(vm)
        var dbCommand = Command.create(db)
        var objCommand = Command.create(obj)
        var lbCommand = Command.create(lb)
        var subnetCommand = Command.create(subnet)
        var vpcCommand = Command.create(vpc)
        var commands = new Commands(
                vmCommand,
                dbCommand,
                objCommand,
                lbCommand,
                subnetCommand,
                vpcCommand)

        ec.commands = commands

        Graph graph = new GraphBuilder(commands).build()

        var executor = Executors.newFixedThreadPool(10, new CdkThreadFactory())
        TestTaskFactory factory = new TestTaskFactory() {
            @Override
            Callable<Command> createTask(Command command) {
                new TestExceptionTask(command)
            }
        }

        when:
        ec.failureMode = LAZY_FAILURE
        var graphExecutor = new GraphExecutor(graph, executor, ec, factory)

        then:
        graphExecutor.getNodeCountWithState(READY) == 2
        vmCommand.commandStatus == INITIAL
        dbCommand.commandStatus == INITIAL
        lbCommand.commandStatus == INITIAL
        subnetCommand.commandStatus == INITIAL
        objCommand.commandStatus == INITIAL
        vpcCommand.commandStatus == INITIAL

        when:
        graphExecutor.run()

        then:
        Awaitility.await().atMost(5, SECONDS)
                .until(() -> executor.isShutdown())

        executor.isTerminated()
        graphExecutor.getNodeCountWithState(FINISHED) == commands.size()

        ec.failedCommands.size() == 1
        vmCommand.failureCause == null
        dbCommand.failureCause == null
        objCommand.failureCause == null
        lbCommand.failureCause.message.contains('Test Exception.')
        subnetCommand.failureCause == null
        vpcCommand.failureCause == null

        ec.commands.findAll(it -> it.commandStatus == FAILED).size() == 1
        ec.commands.findAll(it -> it.commandStatus == SUCCESSFUL).size() == 3
        ec.commands.findAll(it -> it.commandStatus == SKIPPED).size() == 2
        vmCommand.commandStatus == SKIPPED
        dbCommand.commandStatus == SKIPPED
        lbCommand.commandStatus == FAILED
        subnetCommand.commandStatus == SUCCESSFUL
        objCommand.commandStatus == SUCCESSFUL
        vpcCommand.commandStatus == SUCCESSFUL

        cleanup:
        executor.shutdown()
    }

}

class VmSpec extends ThingSpec {
    VmSpec(String id, Collection stacks, String name) {
        super(id, stacks, name)
    }
}

class DbSpec extends ThingSpec {
    DbSpec(String id, Collection stacks, String name) {
        super(id, stacks, name)
    }
}

class ObjectStorageSpec extends ThingSpec {
    ObjectStorageSpec(String id, Collection stacks, String name) {
        super(id, stacks, name)
    }
}

class LbSpec extends ThingSpec {
    LbSpec(String id, Collection stacks, String name) {
        super(id, stacks, name)
    }
}

class SubnetSpec extends ThingSpec {
    SubnetSpec(String id, Collection stacks, String name) {
        super(id, stacks, name)
    }
}

class VpcSpec extends ThingSpec {
    VpcSpec(String id, Collection stacks, String name) {
        super(id, stacks, name)
    }
}
