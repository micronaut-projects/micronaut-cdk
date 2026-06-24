package data.cdk.impl.resourceSpec.delete.raw.five;

import io.micronaut.cdk.action.InvalidActionException;
import io.micronaut.cdk.action.Spec;
import io.micronaut.cdk.annotations.CloudSpecific;
import io.micronaut.cdk.annotations.ResourceSpec;
import org.spockframework.util.Assert;

import java.util.Collection;

import static io.micronaut.cdk.Cloud.OCI;

/**
 * Spec that does not use generic, deleteBuilder(spec) should be selected.
 */
@ResourceSpec(component = TestResource5.class)
@CloudSpecific(OCI)
public class TestResource5Spec extends Spec<TestResource5Spec> {

    /**
     * Constructor.
     *
     * @param cdkId  CDK ID
     * @param stacks stacks
     */
    protected TestResource5Spec(String cdkId, Collection<String> stacks) {
        super(cdkId, stacks);
    }

    public static Builder builder(String cdkId) {
        return new Builder(cdkId);
    }

    public static <N> TestResource5DeleteSpec.Builder deleteBuilder(TestResource5<N> spec) {
        return new TestResource5DeleteSpec.Builder(spec.getCloudId(), false);
    }

    public static TestResource5DeleteSpec.Builder deleteBuilder(String id, boolean cdkId) {
        Assert.fail("Should not be called");
        return new TestResource5DeleteSpec.Builder(id, cdkId);
    }

    public static class Builder extends Spec.Builder<TestResource5Spec, Builder> {
        Builder(String cdkId) {
            super(cdkId);
        }

        @Override
        protected TestResource5Spec doBuild() throws InvalidActionException {
            return new TestResource5Spec(getCdkId(), getStacks());
        }
    }
}
