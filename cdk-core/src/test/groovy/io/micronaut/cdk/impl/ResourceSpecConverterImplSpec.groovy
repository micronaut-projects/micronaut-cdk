package io.micronaut.cdk.impl

import io.micronaut.cdk.action.ResourceSpecConverter
import io.micronaut.cdk.component.ComponentReference
import data.cdk.impl.resourceSpec.TestResource
import data.cdk.impl.resourceSpec.TestResourceSpec
import data.cdk.impl.resourceSpec.delete.generics.four.TestResource4
import data.cdk.impl.resourceSpec.delete.generics.four.TestResource4DeleteSpec
import data.cdk.impl.resourceSpec.delete.generics.one.TestResource1
import data.cdk.impl.resourceSpec.delete.generics.one.TestResource1ADeleteSpec
import data.cdk.impl.resourceSpec.delete.generics.one.TestResource1ASpec
import data.cdk.impl.resourceSpec.delete.generics.one.TestResource1DeleteSpec
import data.cdk.impl.resourceSpec.delete.generics.one.TestResource1Spec
import data.cdk.impl.resourceSpec.delete.generics.two.TestResource2
import data.cdk.impl.resourceSpec.delete.generics.two.TestResource2Spec
import data.cdk.impl.resourceSpec.refs.TestCollection2RefSpec
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

import static io.micronaut.cdk.Cloud.AWS
import static io.micronaut.cdk.Cloud.AZURE
import static io.micronaut.cdk.Cloud.NAME_OCI
import static io.micronaut.cdk.Cloud.OCI
import static io.micronaut.cdk.util.Constants.CLOUD_KEY

@MicronautTest
@Property(name = CLOUD_KEY, value = NAME_OCI)
class ResourceSpecConverterImplSpec extends Specification {

    @Inject
    ResourceSpecConverter converter

    void 'Delete Builder created for Spec'() {
        given:
        def res = new TestResource1('foo', 'cloudId', '1')
        ComponentAccessor.instance.setCloud(res, OCI)

        when:
        def delSpec = converter.deleteComponent(res, null)

        then:
        delSpec != null
    }

    void 'DeleteSpec CDK ID is the same'() {
        given:
        def res = new TestResource1('foo', 'cloudId', '1')
        ComponentAccessor.instance.setCloud(res, OCI)

        when:
        def delSpec = converter.deleteComponent(res, null)

        then:
        delSpec != null
        delSpec.id == res.cloudId
    }

    void 'Unknown resource returns null DeleteSpec'() {
        given:
        def res = new TestResource('foo', 'cloudId', '1')
        ComponentAccessor.instance.setCloud(res, OCI)

        when:
        def delSpec = converter.deleteComponent(res, null)

        then:
        delSpec == null
    }

    void 'Component type for Spec'() {
        given:
        def spec = TestResource1Spec.builder('AAA').build()

        when:
        def comp = converter.findComponentClass(spec)

        then:
        comp == TestResource1
    }

    void 'Component type for DeleteSpec'() {
        given:
        // must call TestResource4, which has .builder(String, boolean) enabled
        def spec = TestResource4DeleteSpec.builder('AAA', true).build()

        when:
        def comp = converter.findComponentClass(spec)

        then:
        comp == TestResource4
    }

    void 'Spec without @ResourceSpec fails'() {
        given:
        def spec = TestResourceSpec.builder('CCC').build()

        when:
        def comp = converter.findComponentClass(spec)

        then:
        def exc = thrown(UnsupportedOperationException)
        exc.message.contains('CCC')
    }

    void 'Component type for inherited Spec'() {
        given:
        def spec = TestResource2Spec.builder('AAA').build()

        when:
        def comp = converter.findComponentClass(spec)

        then:
        comp == TestResource2
    }

    void 'Spec type for resource'() {
        expect:
        converter.findSpecClass(TestResource1, OCI) == TestResource1Spec
        converter.findSpecClass(TestResource1, AWS) == TestResource1ASpec
    }

    void 'DeleteSpec type for resource'() {
        converter.findDeleteSpecClass(TestResource1, OCI) == TestResource1DeleteSpec
        converter.findDeleteSpecClass(TestResource1, AWS) == TestResource1ADeleteSpec
    }

    void 'Spec type for unregistered type'() {
        converter.findDeleteSpecClass(TestResource, OCI) == null
    }

    void 'References contained in Spec'() {
        given:
        var cref1 = ComponentReference.forCdkId('x')
        var cref2 = ComponentReference.forCdkId('y')
        var spec = TestCollection2RefSpec.builder('b')
                .addComponent(cref1)
                .addComponent(cref2)
                .build()

        when:
        Set<ComponentReference> refs = converter.findReferencedComponents(spec, AZURE)

        then:
        refs == Set.of(cref1, cref2)
    }
}
