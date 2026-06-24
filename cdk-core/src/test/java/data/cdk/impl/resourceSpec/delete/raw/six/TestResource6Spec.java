package data.cdk.impl.resourceSpec.delete.raw.six;

import io.micronaut.cdk.action.InvalidActionException;
import io.micronaut.cdk.action.Spec;
import io.micronaut.cdk.annotations.CloudSpecific;
import io.micronaut.cdk.annotations.ResourceSpec;

import java.util.Collection;

import static io.micronaut.cdk.Cloud.OCI;

/**
 * Spec that does not use generic, deleteBuilder(String, boolean) should be selected.
 */
@ResourceSpec(component = TestResource6.class)
@CloudSpecific(OCI)
public class TestResource6Spec extends Spec<TestResource6Spec> {

    /**
     * Constructor.
     *
     * @param cdkId  CDK ID
     * @param stacks stacks
     */
    protected TestResource6Spec(String cdkId, Collection<String> stacks) {
        super(cdkId, stacks);
    }

    public static Builder builder(String cdkId) {
        return new Builder(cdkId);
    }

    public static TestResource6DeleteSpec.Builder deleteBuilder(String id, boolean cdkId) {
        return new TestResource6DeleteSpec.Builder(id, cdkId);
    }

    public static class Builder extends Spec.Builder<TestResource6Spec, Builder> {
        Builder(String cdkId) {
            super(cdkId);
        }

        @Override
        protected TestResource6Spec doBuild() throws InvalidActionException {
            return new TestResource6Spec(getCdkId(), getStacks());
        }
    }
}
