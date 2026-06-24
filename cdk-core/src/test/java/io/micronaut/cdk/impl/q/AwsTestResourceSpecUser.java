package io.micronaut.cdk.impl.q;

import io.micronaut.cdk.action.InvalidActionException;
import io.micronaut.cdk.action.Spec;
import io.micronaut.cdk.annotations.CloudSpecific;
import io.micronaut.cdk.annotations.ResourceSpec;
import io.micronaut.cdk.component.User;
import data.cdk.impl.resourceSpec.TestResourceDeleteSpec;

import java.util.Collection;

import static io.micronaut.cdk.Cloud.AWS;

/**
 * Test Aws User Spec.
 */
@CloudSpecific(AWS)
@ResourceSpec(component = User.class, deleteSpec = TestResourceDeleteSpec.class)
public class AwsTestResourceSpecUser extends Spec<AwsTestResourceSpecUser> {

    /**
     * Constructor.
     *
     * @param cdkId  CDK ID
     * @param stacks stacks
     */
    protected AwsTestResourceSpecUser(String cdkId, Collection<String> stacks) {
        super(cdkId, stacks);
    }

    public static Builder builder(String cdkId) {
        return new Builder(cdkId);
    }

    /**
     * Builder.
     */
    public static class Builder extends Spec.Builder<AwsTestResourceSpecUser, Builder> {
        Builder(String cdkId) {
            super(cdkId);
        }

        @Override
        protected AwsTestResourceSpecUser doBuild() throws InvalidActionException {
            return new AwsTestResourceSpecUser(getCdkId(), getStacks());
        }
    }
}
