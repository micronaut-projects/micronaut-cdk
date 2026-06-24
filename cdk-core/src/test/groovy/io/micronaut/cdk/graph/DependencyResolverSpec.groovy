package io.micronaut.cdk.graph

import io.micronaut.cdk.action.Spec
import io.micronaut.cdk.command.Command
import io.micronaut.cdk.command.Commands
import io.micronaut.cdk.test.AbstractCdkIntegrationSpec

import static io.micronaut.cdk.action.Stack.DEFAULT_STACK

class DependencyResolverSpec extends AbstractCdkIntegrationSpec {

    private DependencyResolver dependencyResolver

    void setup() {
        dependencyResolver = applicationContext.getBean(DependencyResolver)
    }

    void 'happy path'() {
        when:
        var a = new VmSpec('a', List.of(DEFAULT_STACK))           // VM1
        var b = new OrclDbSpec('b', List.of(DEFAULT_STACK))       // ORCL DB
        var c = new NetworkSpec('c', List.of(DEFAULT_STACK))      // Network
        var d = new KvStoreSpec('d', List.of(DEFAULT_STACK))      // K/V store
        var e = new OtherSpec('e', List.of(DEFAULT_STACK))        // other
        var f = new VmSpec('f', List.of(DEFAULT_STACK))           // VM2
        var g = new LoadBalancerSpec('g', List.of(DEFAULT_STACK)) // load balancer

        a.addDependency(b) // 'VM1 needs an Oracle database'
        a.addDependency(c) // 'VM1 needs a network'
        a.addDependency(g) // 'VM1 needs a load balancer'

        f.addDependency(d) // 'VM2 needs key/value storage'
        f.addDependency(c) // 'VM2 needs a network'
        f.addDependency(g) // 'VM2 needs a load balancer'

        g.addDependency(d) // 'load balancer needs a network'

        var commands = new Commands(
                Command.create(a),
                Command.create(b),
                Command.create(c),
                Command.create(d),
                Command.create(e),
                Command.create(f),
                Command.create(g))

        List<String> sortedIds = dependencyResolver.sort(commands).commands.collect { it.action.cdkId }

        then:
        sortedIds == ['b', 'c', 'd', 'g', 'a', 'e', 'f']
    }

    void 'cycle'() {
        when:
        var a = new VmSpec('a', List.of(DEFAULT_STACK))
        var b = new OrclDbSpec('b', List.of(DEFAULT_STACK))
        var c = new NetworkSpec('c', List.of(DEFAULT_STACK))

        a.addDependency(b)
        b.addDependency(c)
        c.addDependency(a)

        var commands = new Commands(Command.create(a), Command.create(b), Command.create(c))
        dependencyResolver.sort(commands)

        then:
        thrown DependencyCycleException
    }

    private static class VmSpec extends Spec<VmSpec> {
        VmSpec(String cdkId, Collection<String> stacks) {
            super(cdkId, stacks)
        }
    }

    private static class OrclDbSpec extends Spec<OrclDbSpec> {
        OrclDbSpec(String cdkId, Collection<String> stacks) {
            super(cdkId, stacks)
        }
    }

    private static class NetworkSpec extends Spec<NetworkSpec> {
        NetworkSpec(String cdkId, Collection<String> stacks) {
            super(cdkId, stacks)
        }
    }

    private static class KvStoreSpec extends Spec<KvStoreSpec> {
        KvStoreSpec(String cdkId, Collection<String> stacks) {
            super(cdkId, stacks)
        }
    }

    private static class LoadBalancerSpec extends Spec<LoadBalancerSpec> {
        LoadBalancerSpec(String cdkId, Collection<String> stacks) {
            super(cdkId, stacks)
        }
    }

    private static class OtherSpec extends Spec<OtherSpec> {
        OtherSpec(String cdkId, Collection<String> stacks) {
            super(cdkId, stacks)
        }
    }
}
