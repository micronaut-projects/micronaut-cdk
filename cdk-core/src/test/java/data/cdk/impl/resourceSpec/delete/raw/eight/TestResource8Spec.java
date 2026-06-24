package data.cdk.impl.resourceSpec.delete.raw.eight;

import io.micronaut.cdk.action.InvalidActionException;
import io.micronaut.cdk.action.Spec;
import io.micronaut.cdk.annotations.CloudSpecific;
import io.micronaut.cdk.annotations.ResourceSpec;

import java.util.Collection;

import static io.micronaut.cdk.Cloud.OCI;

/**
 * Spec that does not use generic, deleteBuilder(String, boolean) from the DeleteSpec class should be selected.
 */
@ResourceSpec(component = TestResource8.class)
@CloudSpecific(OCI)
public class TestResource8Spec extends Spec<TestResource8Spec> {

    /**
     * Constructor.
     *
     * @param cdkId  CDK ID
     * @param stacks stacks
     */
    protected TestResource8Spec(String cdkId, Collection<String> stacks) {
        super(cdkId, stacks);
    }

    public static Builder builder(String cdkId) {
        return new Builder(cdkId);
    }

    public static class Builder extends Spec.Builder<TestResource8Spec, Builder> {
        Builder(String cdkId) {
            super(cdkId);
        }

        @Override
        protected TestResource8Spec doBuild() throws InvalidActionException {
            return new TestResource8Spec(getCdkId(), getStacks());
        }
    }
}
