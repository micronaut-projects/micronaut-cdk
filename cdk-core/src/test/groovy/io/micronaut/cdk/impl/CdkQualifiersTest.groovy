package io.micronaut.cdk.impl

import io.micronaut.cdk.annotations.CloudSpecific
import io.micronaut.cdk.annotations.ComponentService
import io.micronaut.cdk.component.Project
import io.micronaut.cdk.impl.q.CloudService1
import io.micronaut.cdk.impl.q.CloudService1Aws
import io.micronaut.cdk.impl.q.CloudService1Generic
import io.micronaut.cdk.impl.q.CloudService1Oci
import io.micronaut.cdk.impl.q.CloudService1User
import io.micronaut.cdk.impl.q.CloudService2Aws
import io.micronaut.context.ApplicationContext
import io.micronaut.inject.BeanDefinition
import io.micronaut.inject.qualifiers.Qualifiers
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

import java.util.stream.Collectors

import static io.micronaut.cdk.Cloud.AWS
import static io.micronaut.cdk.Cloud.OCI

@MicronautTest
class CdkQualifiersTest extends Specification {

    @Inject
    ApplicationContext applicationContext

    void 'Optional cloud + default'() {
        given:
        def qualifier = CdkQualifiers.optionally(CloudSpecific,
                CdkQualifiers.byAnnotationValue(CloudSpecific, OCI))

        when:
        def beans = applicationContext.getBeansOfType(CloudService1, qualifier)

        then:
        // Generic and OCI services
        beans.size() == 2
        beans.any { it instanceof CloudService1Generic }
        beans.any { it instanceof CloudService1Oci }
    }

    void 'Optional other cloud + default'() {
        given:
        def qualifier = CdkQualifiers.optionally(CloudSpecific,
                CdkQualifiers.byAnnotationValue(CloudSpecific, AWS))

        when:
        def beans = applicationContext.getBeansOfType(CloudService1, qualifier)

        then:
        // Generic and AWS services
        beans.size() == 3
        beans.any { it instanceof CloudService1Generic }
        beans.any { it instanceof CloudService1Aws }
        beans.any { it instanceof CloudService2Aws }
    }

    void 'Optional class value'() {
        given:
        def qualifier = CdkQualifiers.optionally(ComponentService,
                CdkQualifiers.byAnnotationValue(ComponentService, Project))

        when:
        def beans = applicationContext.getBeansOfType(CloudService1, qualifier)

        then:
        // Generic and OCI services
        beans.size() == 2
        beans.any { it instanceof CloudService1Oci }
        beans.any { it instanceof CloudService1Aws }
    }

    void 'Optional polymorphic value'() {
        given:
        def qualifier = CdkQualifiers.optionally(ComponentService,
                CdkQualifiers.acceptsType(ComponentService, 'value', Project))

        when:
        def beans = applicationContext.getBeansOfType(CloudService1, qualifier)

        then:
        // Generic and OCI services
        beans.size() == 3
        beans.any { it instanceof CloudService1Oci }
        beans.any { it instanceof CloudService1Generic }
        beans.any { it instanceof CloudService1Aws }
    }

    void 'Optional polymorphic value 2'() {
        given:
        def qualifier = CdkQualifiers.preferAcceptsType(ComponentService, 'value', Project)

        when:
        def beans = applicationContext.getBeansOfType(CloudService1, qualifier)

        then:
        // Generic and OCI services
        beans.size() == 3
        (beans[0] instanceof CloudService1Oci)
        (beans[1] instanceof CloudService1Generic)
        (beans[2] instanceof CloudService1Aws)
    }

    void 'Required polymorphic value'() {
        given:
        def qualifier = CdkQualifiers.acceptsType(ComponentService, 'value', Project)

        when:
        def beans = applicationContext.getBeansOfType(CloudService1, qualifier)

        then:
        // Generic and OCI services
        beans.size() == 2
        (beans[0] instanceof CloudService1Oci)
        (beans[1] instanceof CloudService1Generic)
    }

    void 'Optional polymorphic in cloud'() {
        given:
        def qualifier =
                Qualifiers.byQualifiers(
                        CdkQualifiers.byAnnotationValue(CloudSpecific, OCI),
                        CdkQualifiers.preferAcceptsType(ComponentService, 'value', Project)
                )

        when:
        def beans = applicationContext.getBeansOfType(CloudService1, qualifier)

        then:
        // Generic and OCI services
        beans.size() == 1
        beans[0] instanceof CloudService1Oci
    }

    void 'Optional polymorphic with optional cloud'() {
        given:
        def qualifier =
                Qualifiers.byQualifiers(
                        CdkQualifiers.optionally(CloudSpecific,
                                CdkQualifiers.byAnnotationValue(CloudSpecific, OCI)),
                        CdkQualifiers.preferAcceptsType(ComponentService, 'value', Project)
                )

        when:
        def beans = applicationContext.getBeansOfType(CloudService1, qualifier)

        then:
        // Generic and OCI services
        beans.size() == 2
        (beans[0] instanceof CloudService1Oci)
        (beans[1] instanceof CloudService1Generic)
    }

    void 'Reject qualifier'() {
        given:
        def qualifier = CdkQualifiers.reject(CloudSpecific)

        when:
        def beans = applicationContext.getBeansOfType(CloudService1, qualifier)
        List<Class> beanClasses = beans*.getClass()

        then:
        beanClasses == [CloudService1Generic]
    }

    void 'Reject qualifier value'() {
        given:
        def qualifier = CdkQualifiers.reject(CloudSpecific, OCI)

        when:
        def beans = applicationContext.getBeansOfType(CloudService1, qualifier)
        Set<Class> beanClasses = beans*.getClass()

        then:
        beanClasses == Set.of(CloudService1Generic, CloudService1Aws, CloudService1User, CloudService2Aws)
    }

    void 'Reject qualifier reduce'() {
        given:
        def qualifier = CdkQualifiers.reject(CloudSpecific, OCI)
        Collection<BeanDefinition> beans = applicationContext.getBeanDefinitions(CloudService1)

        when:
        Set<Class> reduced = qualifier.reduce(CloudService1, beans.stream()).map(BeanDefinition::getBeanType).collect(Collectors.toSet())

        then:
        reduced == Set.of(CloudService1Generic, CloudService1Aws, CloudService1User, CloudService2Aws)
    }
}
