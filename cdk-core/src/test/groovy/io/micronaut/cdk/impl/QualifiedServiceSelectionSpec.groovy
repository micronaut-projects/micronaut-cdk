package io.micronaut.cdk.impl

import io.micronaut.cdk.ExecutionContext
import io.micronaut.cdk.action.QualifiedServiceProvider
import io.micronaut.cdk.impl.q.AwsTestResourceSpecUser
import io.micronaut.cdk.impl.q.AzureTestResourceSpecUser
import io.micronaut.cdk.impl.q.CloudService1
import io.micronaut.cdk.impl.q.CloudService1Oci
import io.micronaut.cdk.impl.q.CloudService1User
import io.micronaut.cdk.impl.q.OciTestResourceSpecProject
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

import static io.micronaut.cdk.Cloud.AWS
import static io.micronaut.cdk.Cloud.AZURE
import static io.micronaut.cdk.Cloud.OCI

@MicronautTest
class QualifiedServiceSelectionSpec extends Specification {

    @Inject
    QualifiedServiceProvider serviceProvider

    @Inject
    ExecutionContext ec

    void 'select CloudService1 that operate with Project and Oci'() {
        given:
        def spec = OciTestResourceSpecProject.builder('test-a').build()
        ec.defaultCloud = OCI

        when:
        def res = serviceProvider.findServices(CloudService1, spec)

        then:
        res.size() == 1
        res[0] instanceof CloudService1Oci
    }

    void 'select CloudService1 that operate with User and Azure'() {
        given:
        def spec = AzureTestResourceSpecUser.builder('test-a').build()
        ec.defaultCloud = AZURE

        when:
        def res = serviceProvider.findServices(CloudService1, spec)

        then:
        res.size() == 1
        res[0] instanceof CloudService1User
    }

    void 'select fallback CloudService1 for User and Aws'() {
        given:
        def spec = AwsTestResourceSpecUser.builder('test-a').build()
        ec.defaultCloud = AWS

        when:
        def res = serviceProvider.findServices(CloudService1, spec)

        then:
        // there's one service that accepts all cloud components
        // should not found CloudService2Aws.
        res.size() == 1
    }

}
