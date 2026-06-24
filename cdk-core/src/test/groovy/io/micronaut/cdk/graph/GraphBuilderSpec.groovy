package io.micronaut.cdk.graph

import io.micronaut.cdk.action.Actions
import io.micronaut.cdk.action.GroupAction
import io.micronaut.cdk.action.ThingSpec
import io.micronaut.cdk.command.Command
import io.micronaut.cdk.command.Commands
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification

@MicronautTest
class GraphBuilderSpec extends Specification {

    void 'simple graph builder'() {
        given:
        var spec1 = ThingSpec.builder('spec1').build()
        var spec2 = ThingSpec.builder('spec2').build()
        var spec3 = ThingSpec.builder('spec3').build()

        spec1.addDependency(spec2)
        spec1.addDependency(spec3)
        spec3.addDependency(spec2)

        var graphBuilder = new GraphBuilder(new Commands(
                Command.create(spec1),
                Command.create(spec2),
                Command.create(spec3)))

        when:
        var graph = graphBuilder.build()

        then:
        graph.initialNodes.size() == 1
        graph.nodes.size() == 3

    }

    void 'graph is cyclic, throw DependencyCycleException'() {
        given:
        var spec1 = ThingSpec.builder('spec1').build()
        var spec2 = ThingSpec.builder('spec2').build()
        var spec3 = ThingSpec.builder('spec3').build()

        spec1.addDependency(spec2)
        spec2.addDependency(spec3)
        spec3.addDependency(spec1)

        var graphBuilder = new GraphBuilder(new Commands(
                Command.create(spec1),
                Command.create(spec2),
                Command.create(spec3)))

        when:
        graphBuilder.build()

        then:
        thrown(DependencyCycleException)
    }

    void 'action self dependency, throw DependencyCycleException'() {
        given:
        var spec1 = ThingSpec.builder('spec1').build()
        var spec2 = ThingSpec.builder('spec2').build()

        spec1.addDependency(spec1)

        var graphBuilder = new GraphBuilder(new Commands(
                Command.create(spec1),
                Command.create(spec2)))

        when:
        graphBuilder.build()

        then:
        thrown(DependencyCycleException)
    }

    void 'duplicate actions, throws IllegalStateException'() {
        given:
        var spec1 = ThingSpec.builder('spec1').build()
        var spec2 = ThingSpec.builder('spec2').build()

        var graphBuilder = new GraphBuilder(new Commands(
                Command.create(spec1),
                Command.create(spec2),
                Command.create(spec1)))

        when:
        graphBuilder.build()

        then:
        var e = thrown(IllegalStateException)
        e.message == "Duplicate action detected: Action 'ThingSpec: cdkId='spec1', dependencies=[], stacks=[default__stack__]' must be unique."
    }

    void 'Grouped actions unroll into Actions'() {
        given:
        var spec1 = ThingSpec.builder('spec1').build()
        var spec2 = ThingSpec.builder('spec2').build()
        var g = GroupAction.builder('g1').add(spec1, spec2).build()

        when:
        Actions actions = new Actions([g])

        then:
        actions.actions.containsAll([spec1, spec2, g])

        when:
        actions = new Actions([spec1, g])

        then:
        actions.size() == 3
        actions.actions as List == [spec1, g, spec2]
    }

    void 'Sorted group orders actions'() {
        given:
        var spec1 = ThingSpec.builder('spec1').build()
        var spec2 = ThingSpec.builder('spec2').build()
        var g = GroupAction.builder('g1').add(spec2, spec1).ordered(true).build()
        var commands = new Commands([Command.create(spec1), Command.create(spec2), Command.noop(g)])

        when:
        spec2.addDependency(g)
        var graph = new GraphBuilder(commands).build()
        var sorted = graph.sort()

        then:
        sorted*.action == [g, spec2, spec1]
    }

    void 'Unit group orders actions in middle'() {
        given:
        var spec1 = ThingSpec.builder('spec1').build()
        var spec2 = ThingSpec.builder('spec2').build()
        var spec3 = ThingSpec.builder('spec3').build()
        var spec4 = ThingSpec.builder('spec4').build()

        spec2.addDependency(spec1)
        spec1.addDependency(spec3)
        spec4.addDependency(spec2)
        var g = GroupAction.builder('g1').add(spec1, spec2).ordered(true).setUnit(true).build()
        var commands = new Commands([Command.create(spec4), Command.create(spec3),
                                     Command.create(spec2), Command.create(spec1), Command.noop(g)])

        when:
        var graph = new GraphBuilder(commands).build()
        var sorted = graph.sort()

        then:
        sorted*.action == [spec3, g, spec1, spec2, spec4]
    }

}
