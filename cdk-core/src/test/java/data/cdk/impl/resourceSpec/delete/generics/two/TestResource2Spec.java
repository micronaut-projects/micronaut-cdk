package data.cdk.impl.resourceSpec.delete.generics.two;

import io.micronaut.cdk.action.InvalidActionException;
import io.micronaut.cdk.action.Spec;
import io.micronaut.cdk.annotations.CloudSpecific;

import java.util.Collection;

import static io.micronaut.cdk.Cloud.OCI;

/**
 * Spec that use full generics, a contains deleteBuilder generic factory methods. The matching DeleteSpec
 * also uses full templates. The deleteBuilder(String, boolean) should be selected.
 *
 * @param <S>
 */
@CloudSpecific(OCI)
public class TestResource2Spec<S extends TestResource2Spec<S>> extends AbstractTestResource2Spec<S> {

    /**
     * Constructor.
     *
     * @param cdkId  CDK ID
     * @param stacks stacks
     */
    protected TestResource2Spec(String cdkId, Collection<String> stacks) {
        super(cdkId, stacks);
    }

    public static <D extends TestResource2DeleteSpec<D>, B extends TestResource2DeleteSpec.Builder<D, B>> TestResource2DeleteSpec.Builder<D, B> deleteBuilder(String id, boolean cdkId) {
        return new TestResource2DeleteSpec.Builder<>(id, cdkId);
    }

    public static <S extends TestResource2Spec<S>, B extends Builder<S, B>> Builder<S, B> builder(String cdkId) {
        return new Builder<>(cdkId);
    }

    public static class Builder<S extends TestResource2Spec<S>, B extends Builder<S, B>> extends Spec.Builder<S, B> {
        Builder(String cdkId) {
            super(cdkId);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected S doBuild() throws InvalidActionException {
            return (S) new TestResource2Spec<S>(getCdkId(), getStacks());
        }
    }
}
