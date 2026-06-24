package data.cdk.impl.resourceSpec.delete.generics.one;

import io.micronaut.cdk.action.InvalidActionException;
import io.micronaut.cdk.action.Spec;
import io.micronaut.cdk.annotations.CloudSpecific;
import io.micronaut.cdk.annotations.ResourceSpec;
import org.spockframework.util.Assert;

import java.util.Collection;

import static io.micronaut.cdk.Cloud.AZURE;

@ResourceSpec(component = TestResource1.class, deleteSpec = TestResource1CDeleteSpec.class)
@CloudSpecific(AZURE)
public class TestResource1CSpec<S extends TestResource1CSpec<S>> extends Spec<S> {

    /**
     * Constructor.
     *
     * @param cdkId  CDK ID
     * @param stacks stacks
     */
    protected TestResource1CSpec(String cdkId, Collection<String> stacks) {
        super(cdkId, stacks);
    }

    public static <S extends TestResource1CSpec<S>, B extends TestResource1CSpec.Builder<S, B>> TestResource1CSpec.Builder<S, B> builder(String cdkId) {
        return new TestResource1CSpec.Builder<>(cdkId);
    }

    /**
     * This is a bait that should NOT be called by the generated code when just 'component' is passed - see 'Prefer Spec over Component for #name'. As null
     * is passed for Resource, the generated code should NOT call this method, instead it should select (delete)builder(TestResource1), if available - and it is,
     * on TestResource1DeleteSpec
     *
     * @param spec specification
     * @param <S>  spec type
     * @param <D>  delete spec type
     * @param <B>  builder type
     * @return builder instance
     */
    public static <S extends TestResource1CSpec<S>, D extends TestResource1CDeleteSpec<D>, B extends TestResource1CDeleteSpec.Builder<D, B>> TestResource1CDeleteSpec.Builder<D, B> deleteBuilder(S spec) {
        Assert.fail("Should not be called");
        return new TestResource1CDeleteSpec.Builder<>(spec.getCdkId(), true);
    }

    public static <D extends TestResource1CDeleteSpec<D>, B extends TestResource1CDeleteSpec.Builder<D, B>> TestResource1CDeleteSpec.Builder<D, B> deleteBuilder(String id, boolean cdkId) {
        Assert.fail("Should not be called");
        return new TestResource1CDeleteSpec.Builder<>(id, cdkId);
    }

    public static class Builder<S extends TestResource1CSpec<S>, B extends TestResource1CSpec.Builder<S, B>> extends Spec.Builder<S, B> {
        Builder(String cdkId) {
            super(cdkId);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected S doBuild() throws InvalidActionException {
            return (S) new TestResource1CSpec<S>(getCdkId(), getStacks());
        }
    }
}
