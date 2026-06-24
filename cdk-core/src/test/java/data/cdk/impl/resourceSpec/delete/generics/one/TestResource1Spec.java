package data.cdk.impl.resourceSpec.delete.generics.one;

import io.micronaut.cdk.action.InvalidActionException;
import io.micronaut.cdk.action.Spec;
import io.micronaut.cdk.annotations.CloudSpecific;
import io.micronaut.cdk.annotations.ResourceSpec;
import org.spockframework.util.Assert;

import java.util.Collection;

import static io.micronaut.cdk.Cloud.OCI;

/**
 * Spec that use full generics, a contains deleteBuilder generic factory methods. The matching DeleteSpec
 * also uses full templates. The deleteBuilder(spec) should be selected.
 *
 * @param <S>
 */
@ResourceSpec(component = TestResource1.class)
@CloudSpecific(OCI)
public class TestResource1Spec<S extends TestResource1Spec<S>> extends AbstractTestResource1Spec<S> {

    /**
     * Constructor.
     *
     * @param cdkId  CDK ID
     * @param stacks stacks
     */
    protected TestResource1Spec(String cdkId, Collection<String> stacks) {
        super(cdkId, stacks);
    }

    public static <S extends TestResource1Spec<S>, B extends Builder<S, B>> Builder<S, B> builder(String cdkId) {
        return new Builder<>(cdkId);
    }

    public static <D extends TestResource1DeleteSpec<D>, B extends TestResource1DeleteSpec.Builder<D, B>, N> TestResource1DeleteSpec.Builder<D, B> ociDeleteBuilder(TestResource1<N> spec) {
        return new TestResource1DeleteSpec.Builder<>(spec.getCloudId(), false);
    }

    public static <D extends TestResource1DeleteSpec<D>, B extends TestResource1DeleteSpec.Builder<D, B>> TestResource1DeleteSpec.Builder<D, B> ociDeleteBuilder(String id, boolean cdkId) {
        Assert.fail("Should not be called");
        return new TestResource1DeleteSpec.Builder<>(id, cdkId);
    }

    public static class Builder<S extends TestResource1Spec<S>, B extends Builder<S, B>> extends Spec.Builder<S, B> {
        Builder(String cdkId) {
            super(cdkId);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected S doBuild() throws InvalidActionException {
            return (S) new TestResource1Spec<S>(getCdkId(), getStacks());
        }
    }
}
