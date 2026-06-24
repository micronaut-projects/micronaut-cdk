package data.cdk.impl.resourceSpec.delete.generics.one;

import io.micronaut.cdk.action.InvalidActionException;
import io.micronaut.cdk.action.Spec;
import io.micronaut.cdk.annotations.CloudSpecific;
import io.micronaut.cdk.annotations.ResourceSpec;
import org.spockframework.util.Assert;

import java.util.Collection;

import static io.micronaut.cdk.Cloud.GOOGLE;

@ResourceSpec(component = TestResource1.class, deleteSpec = TestResource1BDeleteSpec.class)
@CloudSpecific(GOOGLE)
public class TestResource1BSpec<S extends TestResource1BSpec<S>> extends Spec<S> {

    /**
     * Constructor.
     *
     * @param cdkId  CDK ID
     * @param stacks stacks
     */
    protected TestResource1BSpec(String cdkId, Collection<String> stacks) {
        super(cdkId, stacks);
    }

    public static <S extends TestResource1BSpec<S>, B extends TestResource1BSpec.Builder<S, B>> TestResource1BSpec.Builder<S, B> builder(String cdkId) {
        return new TestResource1BSpec.Builder<>(cdkId);
    }

    public static <S extends TestResource1BSpec<S>, D extends TestResource1BDeleteSpec<D>, B extends TestResource1BDeleteSpec.Builder<D, B>> TestResource1BDeleteSpec.Builder<D, B> deleteBuilder(S spec) {
        return new TestResource1BDeleteSpec.Builder<>(spec.getCdkId(), true);
    }

    public static <D extends TestResource1BDeleteSpec<D>, B extends TestResource1BDeleteSpec.Builder<D, B>> TestResource1BDeleteSpec.Builder<D, B> deleteBuilder(String id, boolean cdkId) {
        Assert.fail("Should not be called");
        return new TestResource1BDeleteSpec.Builder<>(id, cdkId);
    }

    public static class Builder<S extends TestResource1BSpec<S>, B extends TestResource1BSpec.Builder<S, B>> extends Spec.Builder<S, B> {
        Builder(String cdkId) {
            super(cdkId);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected S doBuild() throws InvalidActionException {
            return (S) new TestResource1BSpec<S>(getCdkId(), getStacks());
        }
    }
}
