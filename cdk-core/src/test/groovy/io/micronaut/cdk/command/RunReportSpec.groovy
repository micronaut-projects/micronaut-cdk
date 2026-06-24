package io.micronaut.cdk.command

import io.micronaut.cdk.ExecutionContext
import io.micronaut.cdk.action.Actions
import io.micronaut.cdk.action.InvalidActionException
import io.micronaut.cdk.action.Spec
import io.micronaut.cdk.component.Component
import io.micronaut.cdk.component.ExistingComponents
import io.micronaut.cdk.delta.Delta
import io.micronaut.cdk.security.UserInfo
import io.micronaut.cdk.test.AbstractCdkUnitSpec
import io.micronaut.cdk.util.StringCleaner

import static io.micronaut.cdk.Cloud.AWS
import static io.micronaut.cdk.action.Stack.DEFAULT_STACK
import static io.micronaut.cdk.command.Command.Status.SUCCESSFUL

class RunReportSpec extends AbstractCdkUnitSpec {

    private final StringCleaner stringCleaner = it -> it

    void 'toString() with exception'() {
        when:
        var actions = new Actions(new TestSpec('testToStringWithException', List.of(DEFAULT_STACK)))
        var ec = new ExecutionContext(true)
        ec.actions = actions

        var reportAsString = new RunReport(ec, stringCleaner, new InvalidActionException('cdkId123', 'abc')).toString()

        then:
        reportAsString.startsWith 'RunReport:'
        reportAsString.contains 'Dry run: true'
        reportAsString.contains 'Cloud: AWS'
        reportAsString.contains 'Actions (not sorted):'
        reportAsString.contains "TestSpec: cdkId='testToStringWithException'"
        reportAsString.contains 'Deployed component: None'
        reportAsString.contains '''Failure: Invalid action 'cdkId123': abc'''
        reportAsString.contains '''io.micronaut.cdk.action.InvalidActionException: Invalid action 'cdkId123': abc'''
    }

    void 'toString() without exception'() {
        when:
        var actions = new Actions(new TestSpec('testToStringWithoutException', List.of(DEFAULT_STACK)))
        var ec = new ExecutionContext(true)
        ec.actions = actions

        var reportAsString = new RunReport(ec, stringCleaner, null).toString()

        then:
        reportAsString.startsWith 'RunReport:'
        reportAsString.contains 'Dry run: true'
        reportAsString.contains 'Cloud: AWS'
        reportAsString.contains 'Actions (not sorted):'
        reportAsString.contains "TestSpec: cdkId='testToStringWithoutException'"
        reportAsString.contains 'Deployed component: None'
        reportAsString.contains 'Failure: None'
    }

    void 'toString() with deployed components'() {
        when:
        var cdkId = 'testToStringWithDeployedComponents'
        var action = new TestSpec(cdkId, List.of(DEFAULT_STACK))

        var ec = new ExecutionContext(true)
        ec.actions = new Actions(action)
        ec.existingComponents = new ExistingComponents(List.of())
        ec.delta = new Delta(Set.of(action), Map.of(), Map.of(), List.of(), List.of(), List.of())

        var command = Command.create(action)
        var commands = new Commands(command)
        ec.commands = commands

//        ec.sortedCommands = new DefaultDependencyResolver().sort(commands)

        ec.registerDeployed action, new TestComponent(new CloudThing('cloud thing 42'), cdkId)
        command.commandStatus = SUCCESSFUL

        var reportAsString = new RunReport(ec, stringCleaner, null).toString()

        then:
        reportAsString.startsWith 'RunReport:'
        reportAsString.contains 'Dry run: true'
        reportAsString.contains 'Cloud: AWS'
//        reportAsString.contains 'Actions (sorted):'
        reportAsString.contains "TestSpec: cdkId='testToStringWithDeployedComponents'"
        reportAsString.contains 'Deployed component: CloudThing: cloud thing 42'
        reportAsString.contains 'Command(create): TestSpec(testToStringWithDeployedComponents): SUCCESSFUL'
        reportAsString.contains 'Failure: None'
    }

    void 'toString() with failed deployed CommandRunException'() {
        when:
        var cdkId = 'testToStringWithDeployedComponents'
        var action = new TestSpec(cdkId, List.of(DEFAULT_STACK))

        var ec = new ExecutionContext(true)
        ec.actions = new Actions(action)
        ec.existingComponents = new ExistingComponents(List.of())
        ec.delta = new Delta(Set.of(action), Map.of(), Map.of(), List.of(), List.of(), List.of())

        var command = Command.create(action)
        var commands = new Commands(command)
        ec.commands = commands

        ec.registerFailedCommand command, new CommandRunException('cannot run the create command')

        var reportAsString = new RunReport(ec, stringCleaner, null).toString()

        then:
        reportAsString.startsWith 'RunReport:'
        reportAsString.contains 'Dry run: true'
        reportAsString.contains 'Cloud: AWS'
        reportAsString.contains "TestSpec: cdkId='testToStringWithDeployedComponents'"
        reportAsString.contains 'Deployed component: None'
        reportAsString.contains 'Command(create): TestSpec(testToStringWithDeployedComponents): FAILED'
        reportAsString.contains 'Failure:'
        reportAsString.contains "Error running 'Command(create): TestSpec(testToStringWithDeployedComponents)'."
        reportAsString.contains '> io.micronaut.cdk.command.CommandRunException: cannot run the create command'
    }

    void setup() {
        login()
    }

    @Override
    protected UserInfo getUserInfo() {
        () -> AWS
    }

    private static class TestSpec extends Spec<TestSpec> {
        TestSpec(String cdkId,
                 Collection<String> stacks) {
            super(cdkId, stacks)
        }
    }

    private static class TestComponent extends Component<CloudThing> {
        TestComponent(CloudThing cloudComponent, String cdkId) {
            super(cloudComponent, 'cloud id', cdkId)
        }
    }

    private static final class CloudThing {
        private final String name

        private CloudThing(String name) {
            this.name = name
        }

        @Override
        String toString() {
            'CloudThing: ' + name
        }
    }
}
