package io.micronaut.cdk.impl

import data.cdk.impl.resourceSpec.delete.generics.one.TestResource1
import data.cdk.impl.resourceSpec.delete.generics.one.TestResource1DeleteSpec
import data.cdk.impl.resourceSpec.delete.generics.one.TestResource1Spec
import data.cdk.impl.resourceSpec.delete.generics.two.TestResource2
import data.cdk.impl.resourceSpec.delete.generics.two.TestResource2Spec
import spock.lang.Specification

class ComponentsCacheSpec extends Specification {

    private ComponentsCache cache = new ComponentsCache()

    private TestResource1Spec spec1 = TestResource1Spec.builder('res1').build()
    private TestResource1Spec spec1Copy = TestResource1Spec.builder('res1').build()
    private TestResource1 res1 = new TestResource1('cloud-res1', 'cloud:res1', 'res1')
    private TestResource1DeleteSpec del1 = TestResource1DeleteSpec.testBuilder('res1', true).build()
    private TestResource1DeleteSpec del1Cloud = TestResource1DeleteSpec.testBuilder('cloud:res1', false).build()

    private TestResource1Spec spec1New = TestResource1Spec.builder('res1').build()
    private TestResource1 res1New = new TestResource1('cloud-res1', 'cloud:res1-new', 'res1')

    private TestResource2Spec spec2 = TestResource2Spec.builder('res2').build()
    private TestResource2 res2 = new TestResource2(List.of('cloud-res12'), 'cloud:res2', 'res2')

    private TestResource1Spec spec1B = TestResource1Spec.builder('res1B').build()
    private TestResource1 res1B = new TestResource1('cloud-res1B', 'cloud:res1B', 'res1B')
    private TestResource1DeleteSpec del1B = TestResource1DeleteSpec.testBuilder('res1B', true).build()

    void 'Existing Components resolved'() {
        given:
        [res1, res2, res1B].each { cache.registerResolved(it) }

        expect:
        cache.findDeployedComponent(res1.cdkId, true, TestResource1).get() == res1
        cache.findDeployedComponent(res2.cdkId, true, TestResource2).get() == res2
        cache.findDeployedComponent(res2.cdkId, true, TestResource1).isEmpty()

        // check cloud component
        cache.findDeployed(res2.cdkId, true, List).get() == res2.cloudComponent
        cache.findDeployed(res2.cdkId, true, String).isEmpty()

        cache.findDeployedComponent(res1B.cdkId, true, TestResource1).get() == res1B
    }

    void 'Simple Deploy'() {
        given:
        [res1, res2, res1B].each { cache.registerResolved(it) }

        when:
        cache.registerDeployed(spec1, res1)
        cache.registerDeployed(spec2, res2)
        cache.registerUndeployed(del1B)

        then:
        cache.findDeployedComponent(spec1.cdkId, true, TestResource1).get() == res1
        cache.findDeployedComponent(spec2.cdkId, true, TestResource2).get() == res2

        cache.findDeployedComponent(spec1B.cdkId, true, TestResource1).isEmpty()
        cache.findDeployedComponent(res1B.cloudId, false, TestResource1).isEmpty()

        cache.findRevertedComponent(spec1B).isEmpty()
        cache.findRevertedComponent(del1B).isEmpty()

        cache.undeployed == List.of(del1B)
        cache.getUndeployed(del1) == null
        cache.getUndeployed(del1B) == res1B
    }

    void 'Register multiple components per spec'() {
        when:
        cache.registerDeployed(spec1, res1)

        then:
        cache.findPredecessor(spec1).isEmpty()

        when:
        cache.registerDeployed(spec1, res1B)

        then:
        thrown(IllegalArgumentException)
    }

    void 'Deploy twice the same from different specs'() {
        given:
        cache.registerDeployed(spec1, res1)

        when:
        cache.registerDeployed(spec1Copy, res1)

        then:
        cache.findPredecessor(spec1Copy).get() == spec1
        cache.getAllPredecessors(spec1Copy) == List.of(spec1)
    }

    void 'Deploy by CDK, undeploy by CloudID'() {
        given:
        cache.registerDeployed(spec1, res1)

        when:
        cache.registerUndeployed(del1Cloud)

        then:
        cache.findDeployedComponent(spec1.cdkId, true, TestResource1).isEmpty()
        cache.findRevertedComponent(spec1).isPresent()

        cache.undeployed == List.of(del1Cloud)
        cache.findPredecessor(del1Cloud).get() == spec1

        cache.getUndeployed(del1Cloud) == res1
        cache.getUndeployed(spec1) == res1
    }

    void 'Undeploy something not deployed'() {
        given:
        cache.registerResolved(res1)

        when:
        cache.registerUndeployed(del1)

        then:
        cache.findDeployedComponent(spec1.cdkId, true, TestResource1).isEmpty()
        cache.findDeployedComponent(del1Cloud.id, false, TestResource1).isEmpty()

        cache.findPredecessor(del1).isEmpty()
        cache.getUndeployed(del1) == res1
    }

    void 'Undeploy something even not ever resolved'() {
        when:
        cache.registerUndeployed(del1)

        then:
        cache.findDeployedComponent(spec1.cdkId, true, TestResource1).isEmpty()
        cache.findDeployedComponent(del1Cloud.id, false, TestResource1).isEmpty()

        cache.findPredecessor(del1).isEmpty()
        cache.getUndeployed(del1) == null
    }

    void 'Check resolved components'() {
        when:
        cache.registerResolved(res1)

        then:
        cache.resolved.size() == 1
    }

    void 'Check published components'() {
        expect:
        cache.components.isEmpty()

        when:
        cache.registerDeployed(spec1, res1)

        then:
        cache.components.size() == 1

        when:
        cache.registerDeployed(spec1New, res1)

        then:
        cache.components.size() == 1

        when:
        cache.registerDeployed(spec2, res2)

        then:
        cache.components.size() == 2
    }

    void 'Undeploy and Redeploy New'() {
        given:
        [res1, res2, res1B].each { cache.registerResolved(it) }

        when:
        cache.registerUndeployed(del1)

        then:
        cache.findDeployedComponent(spec1.cdkId, true, TestResource1).isEmpty()
        cache.findDeployedComponent(res1.cloudId, false, TestResource1).isEmpty()
        cache.getUndeployed(del1) == res1
        cache.findRevertedComponent(spec1).isEmpty()

        cache.getUndeployed(del1) == res1

        when:
        cache.registerDeployed(spec1, res1New)
        then:
        cache.findDeployedComponent(spec1.cdkId, true, TestResource1).isPresent()
        // old cloudID is not present
        cache.findDeployedComponent(res1.cloudId, false, TestResource1).isEmpty()
        // new cloudID is present
        cache.findDeployedComponent(res1New.cloudId, false, TestResource1).get() == res1New

        // check cloud component
        cache.findDeployed(spec1.cdkId, true, String).get() == res1.cloudComponent
        cache.findDeployed(spec1.cdkId, true, List).isEmpty()

        cache.actionsAndComponents.get(del1) == null
        cache.actionsAndComponents.get(spec1) == res1New
        cache.getUndeployed(del1) == null

        cache.findRevertedComponent(spec1).isEmpty()
        cache.findRevertedComponent(del1).get() == res1New
    }

    /**
     * Rather an exception - the component is resolvable after it has been reportedly undeployed.
     * The Cache should track the undeploy operation as reverted after resolving the component again.
     */
    void 'Resolved after undeploy'() {
        given:
        [res1, res2, res1B].each { cache.registerResolved(it) }

        when:
        cache.registerUndeployed(del1)

        then:
        cache.findDeployedComponent(spec1.cdkId, true, TestResource1).isEmpty()
        cache.findDeployedComponent(res1.cloudId, false, TestResource1).isEmpty()

        when:
        cache.registerResolved(res1)

        then:
        cache.findDeployedComponent(spec1.cdkId, true, TestResource1).isPresent()
        cache.findDeployedComponent(res1.cloudId, false, TestResource1).isPresent()

    }

    void 'Deploy and discard'() {
        when:
        cache.registerDeployed(spec1B, res1B)

        then:
        cache.findDeployedComponent(spec1B.cdkId, true, TestResource1).isPresent()
        cache.findDeployedComponent(res1B.cloudId, false, TestResource1).isPresent()
        cache.actionsAndComponents.get(spec1B) == res1B

        when:
        cache.registerUndeployed(del1B)

        then:
        cache.findDeployedComponent(spec1B.cdkId, true, TestResource1).isEmpty()
        cache.findDeployedComponent(res1B.cloudId, false, TestResource1).isEmpty()
        cache.getUndeployed(del1B) == res1B

        cache.actionsAndComponents.get(spec1B) == null

        cache.findRevertedComponent(del1B).isEmpty()
        cache.findRevertedComponent(spec1B).isPresent()
    }

    void 'Deploy and Redeploy'() {
        when:
        cache.registerDeployed(spec1, res1)

        then:
        cache.findDeployedComponent(spec1.cdkId, true, TestResource1).isPresent()
        cache.findDeployedComponent(res1.cloudId, false, TestResource1).isPresent()
        cache.actionsAndComponents.get(spec1) == res1

        when:
        cache.registerUndeployed(del1)

        then:
        cache.findDeployedComponent(spec1.cdkId, true, TestResource1).isEmpty()
        cache.findDeployedComponent(res1.cloudId, false, TestResource1).isEmpty()
        cache.findRevertedComponent(del1).isEmpty()
        cache.findRevertedComponent(spec1).get() == res1

        when:
        cache.registerDeployed(spec1New, res1New)

        then:
        cache.actionsAndComponents.get(spec1) == null
        cache.actionsAndComponents.get(spec1New) == res1New
        cache.findRevertedComponent(spec1).get() == res1
        cache.findRevertedComponent(del1).get() == res1New
    }
}
