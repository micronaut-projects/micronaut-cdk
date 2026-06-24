package data.cdk.impl.resourceSpec.delete.generics.one;

import io.micronaut.cdk.action.InvalidActionException;
import io.micronaut.cdk.action.Spec;
import io.micronaut.cdk.annotations.CloudSpecific;
import io.micronaut.cdk.annotations.ResourceSpec;
import org.spockframework.util.Assert;

import java.util.Collection;

import static io.micronaut.cdk.Cloud.AWS;

/**
 * The deleteBuilder(spec) should be selected. In addition,the desired DeleteSpec class is specified,
 * so the correct factory method (not badBuilder) should be selected.
 *
 * @param <S> type of the spec
 */
@ResourceSpec(component = TestResource1.class, deleteSpec = TestResource1ADeleteSpec.class)
@CloudSpecific(AWS)
public class TestResource1ASpec<S extends TestResource1ASpec<S>> extends Spec<S> {

    /**
     * Constructor.
     *
     * @param cdkId  CDK ID
     * @param stacks stacks
     */
    protected TestResource1ASpec(String cdkId, Collection<String> stacks) {
        super(cdkId, stacks);
    }

    public static <S extends TestResource1ASpec<S>, B extends Builder<S, B>> Builder<S, B> builder(String cdkId) {
        return new Builder<>(cdkId);
    }

    public static <D extends TestResource1ADeleteSpec<D>, B extends TestResource1ADeleteSpec.Builder<D, B>> TestResource1ADeleteSpec.Builder<D, B> deleteBuilder(TestResource1 spec) {
        return new TestResource1ADeleteSpec.Builder<>(spec.getCloudId(), false);
    }

    public static <D extends TestResource1DeleteSpec<D>, B extends TestResource1DeleteSpec.Builder<D, B>> TestResource1DeleteSpec.Builder<D, B> badBuilder(TestResource1 spec) {
        Assert.fail("Should not be called");
        return new TestResource1DeleteSpec.Builder<>(spec.getCloudId(), false);
    }

    public static <D extends TestResource1ADeleteSpec<D>, B extends TestResource1ADeleteSpec.Builder<D, B>> TestResource1ADeleteSpec.Builder<D, B> deleteBuilder(String id, boolean cdkId) {
        Assert.fail("Should not be called");
        return new TestResource1ADeleteSpec.Builder<>(id, cdkId);
    }

    public static class Builder<S extends TestResource1ASpec<S>, B extends Builder<S, B>> extends Spec.Builder<S, B> {

        Builder(String cdkId) {
            super(cdkId);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected S doBuild() throws InvalidActionException {
            return (S) new TestResource1ASpec<S>(getCdkId(), getStacks());
        }
    }
}
