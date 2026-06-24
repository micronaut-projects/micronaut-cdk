package io.micronaut.cdk.component

import spock.lang.Specification
import spock.lang.Unroll

class ComponentReferenceSpec extends Specification {

    @Unroll
    void 'parse supports #value'() {
        when:
        def result = ComponentReference.parse(value)

        then:
        if (expectedReference != null) {
            assert result.is(expectedReference)
        } else {
            assert !result.is(ComponentReference.ANY)
            assert !result.is(ComponentReference.NONE)
        }
        result.cdkId == cdkId
        result.cloudId == cloudId

        where:
        value                   | expectedReference       | cdkId      | cloudId
        ' cdk:user1-id '        | null                    | 'user1-id' | null
        'cloud:ocid1.user.oc1'  | null                    | null       | 'ocid1.user.oc1'
        ' any '                 | ComponentReference.ANY  | null       | null
        'none'                  | ComponentReference.NONE | null       | null
    }

    @Unroll
    void 'parse rejects invalid value #value'() {
        when:
        ComponentReference.parse(value)

        then:
        def e = thrown(IllegalArgumentException)
        e.message.contains(messageFragment)

        where:
        value      | messageFragment
        ''         | 'cannot be blank'
        '   '      | 'cannot be blank'
        'foo'      | 'Unsupported component reference value'
        'cdk:'     | "must include a CDK ID"
        'cloud:  ' | "must include a cloud ID"
    }
}
