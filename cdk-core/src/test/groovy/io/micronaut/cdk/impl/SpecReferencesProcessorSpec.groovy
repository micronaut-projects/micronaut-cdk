package io.micronaut.cdk.impl

import io.micronaut.cdk.component.ComponentReference
import io.micronaut.cdk.impl.processing.references.TestCollection2RefSpecReferencesImpl
import io.micronaut.cdk.impl.processing.references.TestCollection3RefSpecReferencesImpl
import io.micronaut.cdk.impl.processing.references.TestReferenceSpecReferencesImpl
import data.cdk.impl.resourceSpec.refs.TestCollection2RefSpec
import data.cdk.impl.resourceSpec.refs.TestCollection3RefSpec
import data.cdk.impl.resourceSpec.refs.TestReferenceSpec
import spock.lang.Specification

class SpecReferencesProcessorSpec extends Specification {

    void 'Single referenced Component'() {
        given:
        var cref = ComponentReference.forCdkId('x')
        var spec = TestReferenceSpec.builder('a')
                .setComponent(cref)
                .build()

        when:
        var provider = new TestReferenceSpecReferencesImpl()

        then:
        provider.findReferences(spec) as List == [cref]
    }

    void 'Single reference not filled'() {
        given:
        var spec = TestReferenceSpec.builder('a')
                .build()

        when:
        var provider = new TestReferenceSpecReferencesImpl()

        then:
        provider.findReferences(spec).isEmpty()
    }

    void 'Multiple references'() {
        given:
        var cref1 = ComponentReference.forCdkId('x')
        var cref2 = ComponentReference.forCdkId('y')
        var spec = TestCollection2RefSpec.builder('b')
                .addComponent(cref1)
                .withPart(cref2)
                .build()

        when:
        var provider = new TestCollection2RefSpecReferencesImpl()

        then:
        provider.findReferences(spec) as Set == Set.of(cref1, cref2)
    }

    void 'Multiple partially filled'() {
        given:
        var cref1 = ComponentReference.forCdkId('x')
        var cref2 = ComponentReference.forCdkId('y')
        var spec = TestCollection2RefSpec.builder('b')
                .addComponent(cref1)
                .addComponent(cref2)
                .build()

        when:
        var provider = new TestCollection2RefSpecReferencesImpl()

        then:
        provider.findReferences(spec) as Set == Set.of(cref1, cref2)
    }

    void 'Duplicate references eliminated'() {
        given:
        var cref1 = ComponentReference.forCdkId('x')
        var cref2 = ComponentReference.forCdkId('y')
        var spec = TestCollection2RefSpec.builder('b')
                .addComponent(cref1)
                .addComponent(cref2)
                .withPart(cref2)
                .build()

        when:
        var provider = new TestCollection2RefSpecReferencesImpl()

        then:
        provider.findReferences(spec) as Set == Set.of(cref1, cref2)
    }

    void 'Accept collection-only property'() {
        given:
        var cref1 = ComponentReference.forCdkId('x')
        var cref2 = ComponentReference.forCdkId('y')
        var spec = TestCollection3RefSpec.builder('b')
                .setComponents(List.of(cref1, cref2))
                .build()

        when:
        var provider = new TestCollection3RefSpecReferencesImpl()

        then:
        provider.findReferences(spec) as Set == Set.of(cref1, cref2)
    }
}
