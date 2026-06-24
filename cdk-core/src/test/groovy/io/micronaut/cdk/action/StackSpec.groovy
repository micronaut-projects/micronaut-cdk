package io.micronaut.cdk.action

import io.micronaut.cdk.test.AbstractCdkUnitSpec

import static Stack.DEFAULT_STACK

class StackSpec extends AbstractCdkUnitSpec {

    void 'add spec to a stack'() {
        when:
        var vm = ThingSpec.builder('test')
                .name('name')
                .stack('stack-1')
                .build()

        then:
        vm.stacks.size() == 1
        Stack.stacks.size() == 2
        vm.stacks.contains('stack-1')
        Stack.get('stack-1').actions.contains(vm)

        cleanup:
        Stack.reset()
    }

    void 'add spec to the default stack'() {
        when:
        var vm = ThingSpec.builder('test')
                .name('name')
                .build()

        then:
        vm.stacks.size() == 1
        vm.stacks.contains(DEFAULT_STACK)
        Stack.get(DEFAULT_STACK).actions.contains(vm)

        cleanup:
        Stack.reset()
    }

    void 'add action to multiple stacks'() {
        when:
        var vm = ThingSpec.builder('test')
                .name('name')
                .stacks('stack-1', 'stack-2', 'stack-3')
                .build()

        List<String> stacks = List.of('stack-1', 'stack-2', 'stack-3')
        var vm2 = ThingSpec.builder('test')
                .name('name')
                .stacks(stacks)
                .build()

        var vm3 = ThingSpec.builder('test')
                .name('name')
                .stack('stack-1')
                .stack('stack-2')
                .build()

        then:
        vm.stacks.size() == 3
        Stack.stacks.size() == 4
        vm.stacks.size() == vm2.stacks.size()
        vm.stacks.size() != vm3.stacks.size()

        vm.stacks.contains('stack-1')
        vm.stacks.contains('stack-2')
        vm.stacks.contains('stack-3')

        Stack.get('stack-1').actions.contains(vm)
        Stack.get('stack-1').actions.contains(vm2)
        Stack.get('stack-1').actions.contains(vm3)

        Stack.get('stack-2').actions.contains(vm)
        Stack.get('stack-2').actions.contains(vm2)
        Stack.get('stack-2').actions.contains(vm3)

        Stack.get('stack-3').actions.contains(vm)
        Stack.get('stack-3').actions.contains(vm2)
        !Stack.get('stack-3').actions.contains(vm3)

        cleanup:
        Stack.reset()
    }

    void 'add duplicate stack names'() {
        when:
        var vm = ThingSpec.builder('test')
                .name('name')
                .stacks('stack-1', 'stack-1', 'stack-1', 'stack-1')
                .build()

        then:
        vm.stacks.size() == 1
        Stack.stacks.size() == 2
        Stack.get('stack-1').actions.size() == 1
        Stack.get('stack-1').actions.contains(vm)

        cleanup:
        Stack.reset()
    }

    void 'wrong stack name'() {
        when:
        var vm = ThingSpec.builder('test')
                .name('name')
                .stacks('stack-1')
                .build()

        Stack.get('stack-2')

        then:
        IllegalStateException e = thrown()
        e.message.contains "stack 'stack-2' Not Found!"

        cleanup:
        Stack.reset()
    }
}

class ThingSpec<S extends ThingSpec<S>> extends Spec<S> {

    final String name

    ThingSpec(String id, Collection<String> stacks, String name) {
        super(id, stacks)
        this.name = name
    }

    static <S extends ThingSpec<S>, B extends Builder<S, B>> Builder<S, B> builder(String cdkId) {
        new Builder<>(cdkId)
    }

    static class Builder<S extends ThingSpec<S>, B extends Builder<S, B>> extends Spec.Builder<S, B> {

        private String name

        Builder(String cdkId) {
            super(cdkId)
        }

        @SuppressWarnings('unchecked')
        protected S doBuild() throws InvalidActionException {
            return (S) new ThingSpec<>(getCdkId(), getStacks(), getName())
        }

        B name(String name) {
            this.name = name
            return self()
        }

        String getName() {
            name
        }
    }
}
