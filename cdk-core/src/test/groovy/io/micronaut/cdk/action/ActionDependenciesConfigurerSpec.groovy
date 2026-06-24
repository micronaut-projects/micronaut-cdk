package io.micronaut.cdk.action

import io.micronaut.cdk.component.ComponentReference
import spock.lang.Specification

class ActionDependenciesConfigurerSpec extends Specification {

    private final ActionDependenciesConfigurer configurer = new ActionDependenciesConfigurer()

    void 'adds dependency inferred from component reference'() {
        given:
        def domain = new DomainSpec('domain-id', ['default'])
        def app = new AppSpec('app-id', ['default'], ComponentReference.forCdkId('domain-id'))

        when:
        configurer.configure([app, domain])

        then:
        app.dependencies as List == [domain]
    }

    void 'does not duplicate an existing dependency'() {
        given:
        def domain = new DomainSpec('domain-id', ['default'])
        def app = new AppSpec('app-id', ['default'], ComponentReference.forCdkId('domain-id'))
        app.addDependency(domain)

        when:
        configurer.configure([app, domain])

        then:
        app.dependencies as List == [domain]
    }

    private static final class DomainSpec extends Spec<DomainSpec> {
        DomainSpec(String cdkId, Collection<String> stacks) {
            super(cdkId, stacks)
        }
    }

    private static final class AppSpec extends Spec<AppSpec> {
        private final ComponentReference identityDomainReference

        AppSpec(String cdkId, Collection<String> stacks, ComponentReference identityDomainReference) {
            super(cdkId, stacks)
            this.identityDomainReference = identityDomainReference
        }

        ComponentReference getIdentityDomainReference() {
            identityDomainReference
        }
    }
}
