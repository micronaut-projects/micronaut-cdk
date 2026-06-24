package io.micronaut.cdk.test

import io.micronaut.cdk.AbstractClients
import io.micronaut.cdk.ExecutionContext
import io.micronaut.cdk.action.ActionProcessor
import io.micronaut.cdk.action.DefaultActionProcessor
import io.micronaut.cdk.action.DefaultExistingComponentResolver
import io.micronaut.cdk.action.ExistingComponentResolver
import io.micronaut.cdk.action.validation.ActionsValidator
import io.micronaut.cdk.action.validation.DefaultActionsValidator
import io.micronaut.cdk.command.CommandBuilder
import io.micronaut.cdk.command.CommandRunner
import io.micronaut.cdk.command.DefaultCommandBuilder
import io.micronaut.cdk.command.DefaultCommandRunner
import io.micronaut.cdk.command.DefaultExecutableRunner
import io.micronaut.cdk.command.ExecutableRunner
import io.micronaut.cdk.delta.DefaultDeltaResolver
import io.micronaut.cdk.delta.DeltaResolver
import io.micronaut.cdk.graph.DefaultDependencyResolver
import io.micronaut.cdk.graph.DependencyResolver
import io.micronaut.cdk.retry.CdkRetryEventLogger
import io.micronaut.cdk.util.StringCleaner
import io.micronaut.cdk.validation.DefaultLimitValidator
import io.micronaut.cdk.validation.LimitValidator
import io.micronaut.context.exceptions.NoSuchBeanException

/**
 * Sanity checks.
 */
class CoreApplicationContextSpec extends AbstractCdkIntegrationSpec {

    void 'expected beans'() {
        expect:
        applicationContext.getBean(ActionProcessor) instanceof DefaultActionProcessor
        applicationContext.getBean(ActionsValidator) instanceof DefaultActionsValidator
        applicationContext.getBean(CommandBuilder) instanceof DefaultCommandBuilder
        applicationContext.getBean(CommandRunner) instanceof DefaultCommandRunner
        applicationContext.getBean(DeltaResolver) instanceof DefaultDeltaResolver
        applicationContext.getBean(DependencyResolver) instanceof DefaultDependencyResolver
        applicationContext.getBean(ExecutableRunner) instanceof DefaultExecutableRunner
        applicationContext.getBean(ExistingComponentResolver) instanceof DefaultExistingComponentResolver
        applicationContext.getBean(LimitValidator) instanceof DefaultLimitValidator

        applicationContext.getBean CdkRetryEventLogger
        applicationContext.getBean ExecutionContext
    }

    void 'not expected beans'() {
        when:
        applicationContext.getBean AbstractClients

        then:
        thrown NoSuchBeanException

        when:
        applicationContext.getBean StringCleaner

        then:
        thrown NoSuchBeanException
    }
}
