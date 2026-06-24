package data.cdk.impl.resourceSpec.delete.generics.two;

import io.micronaut.cdk.action.InvalidActionException;
import io.micronaut.cdk.action.Spec;
import io.micronaut.cdk.annotations.CloudSpecific;
import io.micronaut.cdk.annotations.ResourceSpec;

import java.util.Collection;

import static io.micronaut.cdk.Cloud.AWS;

/**
 * Spec that use full generics, a contains deleteBuilder generic factory methods. The matching DeleteSpec
 * also uses full templates. The deleteBuilder(String, boolean) should be selected. This is a variant
 * that explicitly specifies the DeleteSpec class.
 *
 * @param <S>
 */
@CloudSpecific(AWS)
@ResourceSpec(component = TestResource2.class, deleteSpec = TestResource2DeleteSpec.class)
public class TestResource2ASpec<S extends TestResource2ASpec<S>> extends AbstractTestResource2Spec<S> {

    /**
     * Constructor.
     *
     * @param cdkId  CDK ID
     * @param stacks stacks
     */
    protected TestResource2ASpec(String cdkId, Collection<String> stacks) {
        super(cdkId, stacks);
    }

    public static <S extends TestResource2ASpec<S>, B extends Builder<S, B>> Builder<S, B> builder(String cdkId) {
        return new Builder<>(cdkId);
    }

    public static <D extends TestResource2DeleteSpec<D>, B extends TestResource2DeleteSpec.Builder<D, B>> TestResource2DeleteSpec.Builder<D, B> deleteBuilder(String id, boolean cdkId) {
        return new TestResource2DeleteSpec.Builder<>(id, cdkId);
    }

    public static class Builder<S extends TestResource2ASpec<S>, B extends Builder<S, B>> extends Spec.Builder<S, B> {

        Builder(String cdkId) {
            super(cdkId);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected S doBuild() throws InvalidActionException {
            return (S) new TestResource2ASpec<S>(getCdkId(), getStacks());
        }
    }
}
