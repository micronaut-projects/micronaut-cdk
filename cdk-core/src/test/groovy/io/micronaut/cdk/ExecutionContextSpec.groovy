package io.micronaut.cdk

import io.micronaut.cdk.action.Actions
import data.cdk.impl.resourceSpec.delete.generics.one.TestResource1Spec
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

import static io.micronaut.cdk.ExecutionContext.Step.COMMANDS

@MicronautTest(rebuildContext = true)
class ExecutionContextSpec extends Specification {

    @Inject
    ExecutionContext context

    void 'Initial round finished'() {
        when:
        var r = context.currentRound

        then:
        !r.hasStarted()
        r.hasFinished()
        r.actions.isEmpty()
    }

    void 'Actions define Round'() {
        given:
        var spec = TestResource1Spec.builder('frodo').build()
        context.actions = new Actions(spec)

        when:
        var r = context.currentRound

        then:
        !r.hasStarted()
        !r.hasFinished()
        !r.actions.isEmpty()
    }

    void 'Round not switched if not done'() {
        given:
        var spec = TestResource1Spec.builder('frodo').build()
        context.actions = new Actions(spec)
        context.actions = new Actions(spec)

        when:
        var r1 = context.nextRound()
        var r2 = context.nextRound()

        then:
        r1 == r2
    }

    void 'Final round retained if done'() {
        given:
        var spec = TestResource1Spec.builder('frodo').build()
        context.actions = new Actions(spec)
        var r1 = context.nextRound()

        when:

        r1.close()
        var r2 = context.nextRound()

        then:
        r1 == r2
    }

    void 'Finished round switched for new'() {
        given:
        var spec = TestResource1Spec.builder('frodo').build()
        context.actions = new Actions(spec)
        context.actions = new Actions(spec)
        var r1 = context.nextRound()

        when:

        r1.close()
        var r2 = context.nextRound()

        then:
        r1 != r2
    }

    void 'Variables defined after round'() {
        given:
        context.actions = new Actions()
        context.currentRound.step = COMMANDS
        context.registerVariable('frodo', 'variable', 'value')

        when:
        var r = context.nextRound()

        then:
        context.definedVariables.isEmpty()

        when:
        r.close()

        then:
        !context.definedVariables.isEmpty()
    }
}
