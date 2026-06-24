package io.micronaut.cdk.impl

import io.micronaut.cdk.action.delete.DeleteSpec
import io.micronaut.cdk.impl.processing.deleteFactories.AwsTestResource1ADeleteComponentBuilderFactory
import io.micronaut.cdk.impl.processing.deleteFactories.AwsTestResource2ADeleteComponentBuilderFactory
import io.micronaut.cdk.impl.processing.deleteFactories.AzureTestResource1CDeleteComponentBuilderFactory
import io.micronaut.cdk.impl.processing.deleteFactories.GoogleTestResource1BDeleteComponentBuilderFactory
import io.micronaut.cdk.impl.processing.deleteFactories.OciTestResource1DeleteComponentBuilderFactory
import io.micronaut.cdk.impl.processing.deleteFactories.OciTestResource2DeleteComponentBuilderFactory
import io.micronaut.cdk.impl.processing.deleteFactories.OciTestResource3DeleteComponentBuilderFactory
import io.micronaut.cdk.impl.processing.deleteFactories.OciTestResource4DeleteComponentBuilderFactory
import io.micronaut.cdk.impl.processing.deleteFactories.OciTestResource5DeleteComponentBuilderFactory
import io.micronaut.cdk.impl.processing.deleteFactories.OciTestResource6DeleteComponentBuilderFactory
import io.micronaut.cdk.impl.processing.deleteFactories.OciTestResource7DeleteComponentBuilderFactory
import io.micronaut.cdk.impl.processing.deleteFactories.OciTestResource8DeleteComponentBuilderFactory
import io.micronaut.cdk.impl.processing.deleteFactories.OciTestResource9DeleteComponentBuilderFactory
import io.micronaut.cdk.test.AbstractCdkUnitSpec
import data.cdk.impl.resourceSpec.delete.generics.four.TestResource4
import data.cdk.impl.resourceSpec.delete.generics.one.TestResource1
import data.cdk.impl.resourceSpec.delete.generics.one.TestResource1BSpec
import data.cdk.impl.resourceSpec.delete.generics.three.TestResource3
import data.cdk.impl.resourceSpec.delete.generics.two.TestResource2
import data.cdk.impl.resourceSpec.delete.other.nine.TestResource9
import data.cdk.impl.resourceSpec.delete.raw.eight.TestResource8
import data.cdk.impl.resourceSpec.delete.raw.five.TestResource5
import data.cdk.impl.resourceSpec.delete.raw.seven.TestResource7
import data.cdk.impl.resourceSpec.delete.raw.six.TestResource6
import io.micronaut.context.ApplicationContext
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import jakarta.inject.Inject

import static io.micronaut.cdk.Cloud.NAME_OCI
import static io.micronaut.cdk.test.TestConstants.TEST_NAME_KEY
import static io.micronaut.cdk.util.Constants.CLOUD_KEY

@MicronautTest
class ResourceSpecProcessorSpec extends AbstractCdkUnitSpec implements TestPropertyProvider {

    @Inject
    ApplicationContext applicationContext

    void 'Create DeleteSpec factory for #name'() {
        when:
        def resource = builder('Foo', 'cloudId', '1')

        and:
        def deleteBuilder = applicationContext.getBean(factory).deleteBuilder(resource, null).build() as DeleteSpec

        then:
        deleteBuilder != null
        deleteBuilder.id == resource.cloudId

        where:
        name                            | factory                                          | builder
        'Generic deleteBuilder(Res)'    | OciTestResource1DeleteComponentBuilderFactory    | TestResource1::new
        'Generic deleteBuilder(id)'     | OciTestResource2DeleteComponentBuilderFactory    | TestResource2::new
        'Generic builder(Res)'          | OciTestResource3DeleteComponentBuilderFactory    | TestResource3::new
        'Generic builder(id)'           | OciTestResource4DeleteComponentBuilderFactory    | TestResource4::new

        'Generic explicit builder(Res)' | AwsTestResource1ADeleteComponentBuilderFactory   | TestResource1::new
        'Generic explicit builder(id)'  | AwsTestResource2ADeleteComponentBuilderFactory   | TestResource2::new

        'Generic builder(Spec)'         | AzureTestResource1CDeleteComponentBuilderFactory | TestResource1::new

        'Raw deleteBuilder(Res)'        | OciTestResource5DeleteComponentBuilderFactory    | TestResource5::new
        'Raw deleteBuilder(id)'         | OciTestResource6DeleteComponentBuilderFactory    | TestResource6::new
        'Raw builder(Res)'              | OciTestResource7DeleteComponentBuilderFactory    | TestResource7::new
        'Raw builder(id)'               | OciTestResource8DeleteComponentBuilderFactory    | TestResource8::new
        'Explicit generic builder(Res)' | OciTestResource9DeleteComponentBuilderFactory    | TestResource9::new
    }

    void 'Prefer Spec over Component for #name'() {
        when:
        def resource = builder('Foo', 'cloudId', '1')
        def specInstance = spec != null ? spec('Foo').build() : null

        and:
        def deleteBuilder = applicationContext.getBean(factory).deleteBuilder(resource, specInstance).build() as DeleteSpec

        then:
        deleteBuilder != null
        deleteBuilder.id == expectId

        where:
        name                       | factory                                           | builder            | spec                        | expectId
        'deleteBuilder(Spec)'      | GoogleTestResource1BDeleteComponentBuilderFactory | TestResource1::new | TestResource1BSpec::builder | 'Foo'
        'deleteBuilder(Component)' | AzureTestResource1CDeleteComponentBuilderFactory  | TestResource1::new | null                        | 'cloudId'
    }

    @Override
    Map<String, String> getProperties() {
        [(TEST_NAME_KEY): getClass().simpleName,
         (CLOUD_KEY)    : NAME_OCI]
    }
}
