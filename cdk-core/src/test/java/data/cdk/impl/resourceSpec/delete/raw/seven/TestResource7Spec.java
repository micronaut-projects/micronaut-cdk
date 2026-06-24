package data.cdk.impl.resourceSpec.delete.raw.seven;

import io.micronaut.cdk.action.InvalidActionException;
import io.micronaut.cdk.action.Spec;
import io.micronaut.cdk.annotations.CloudSpecific;
import io.micronaut.cdk.annotations.ResourceSpec;

import java.util.Collection;

import static io.micronaut.cdk.Cloud.OCI;

/**
 * Spec that does not use generic, deleteBuilder(spec) from the DeleteSpec classshould be selected.
 */
@ResourceSpec(component = TestResource7.class)
@CloudSpecific(OCI)
public class TestResource7Spec extends Spec<TestResource7Spec> {

    /**
     * Constructor.
     *
     * @param cdkId  CDK ID
     * @param stacks stacks
     */
    protected TestResource7Spec(String cdkId, Collection<String> stacks) {
        super(cdkId, stacks);
    }

    public static Builder builder(String cdkId) {
        return new Builder(cdkId);
    }

    public static class Builder extends Spec.Builder<TestResource7Spec, Builder> {
        Builder(String cdkId) {
            super(cdkId);
        }

        @Override
        protected TestResource7Spec doBuild() throws InvalidActionException {
            return new TestResource7Spec(getCdkId(), getStacks());
        }
    }
}
