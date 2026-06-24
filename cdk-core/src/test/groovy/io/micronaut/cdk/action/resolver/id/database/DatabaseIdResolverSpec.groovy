package io.micronaut.cdk.action.resolver.id.database

import io.micronaut.cdk.action.IdResolver
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import jakarta.inject.Named
import spock.lang.Specification

@MicronautTest
class DatabaseIdResolverSpec extends Specification {

    @Inject
    @Named('database')
    IdResolver idResolver

    @Inject
    CdkIdentityRepository repository

    void 'check types'() {
        expect:
        idResolver instanceof DatabaseIdResolver
        repository instanceof TestCdkIdentityRepository
    }

    void 'test associate'() {
        given:
        String cdkId = 'cdk1'
        String cloudId = 'cloud1'

        when:
        idResolver.associate cdkId, cloudId

        then:
        idResolver.findCdkId(cloudId, null, null).get() == cdkId
        idResolver.findCloudId(cdkId, null).get() == cloudId
    }
}
