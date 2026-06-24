package data.cdk.impl.resourceSpec.delete.generics.four;

import io.micronaut.cdk.action.InvalidActionException;
import io.micronaut.cdk.action.Spec;
import io.micronaut.cdk.annotations.CloudSpecific;
import io.micronaut.cdk.annotations.ResourceSpec;

import java.util.Collection;

import static io.micronaut.cdk.Cloud.OCI;

/**
 * Spec that use full generics, a contains deleteBuilder generic factory methods. The matching DeleteSpec
 * also uses full templates. The builder(String, boolean) from the DeleteSpec class should be selected.
 *
 * @param <S>
 */
@ResourceSpec(component = TestResource4.class)
@CloudSpecific(OCI)
public class TestResource4Spec<S extends TestResource4Spec<S>> extends Spec<S> {

    /**
     * Constructor.
     *
     * @param cdkId  CDK ID
     * @param stacks stacks
     */
    protected TestResource4Spec(String cdkId, Collection<String> stacks) {
        super(cdkId, stacks);
    }

    public static <S extends TestResource4Spec<S>, B extends Builder<S, B>> Builder<S, B> builder(String cdkId) {
        return new Builder<>(cdkId);
    }

    public static class Builder<S extends TestResource4Spec<S>, B extends Builder<S, B>> extends Spec.Builder<S, B> {
        Builder(String cdkId) {
            super(cdkId);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected S doBuild() throws InvalidActionException {
            return (S) new TestResource4Spec<S>(getCdkId(), getStacks());
        }
    }
}
