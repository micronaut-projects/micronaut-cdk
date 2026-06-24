package data.cdk.impl.resourceSpec;

import io.micronaut.cdk.action.InvalidActionException;
import io.micronaut.cdk.action.Spec;
import io.micronaut.cdk.annotations.CloudSpecific;
import io.micronaut.cdk.annotations.ResourceSpec;

import java.util.Collection;

import static io.micronaut.cdk.Cloud.OCI;

/**
 * This class is intentionally NOT annotated by {@link ResourceSpec}.
 */
@CloudSpecific(OCI)
public class TestResourceSpec extends Spec<TestResourceSpec> {

    /**
     * Constructor.
     *
     * @param cdkId  CDK ID
     * @param stacks stacks
     */
    protected TestResourceSpec(String cdkId, Collection<String> stacks) {
        super(cdkId, stacks);
    }

    public static Builder builder(String cdkId) {
        return new Builder(cdkId);
    }

    public static TestResourceDeleteSpec.Builder deleteBuilder(TestResource component) {
        return new TestResourceDeleteSpec.Builder(component.getCloudId(), false);
    }

    public static TestResourceDeleteSpec.Builder deleteBuilder(String cdkId, boolean byCdkId) {
        return new TestResourceDeleteSpec.Builder(cdkId, byCdkId);
    }

    public static class Builder extends Spec.Builder<TestResourceSpec, Builder> {
        Builder(String cdkId) {
            super(cdkId);
        }

        @Override
        protected TestResourceSpec doBuild() throws InvalidActionException {
            return new TestResourceSpec(getCdkId(), getStacks());
        }
    }
}
