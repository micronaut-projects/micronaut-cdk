package io.micronaut.cdk.impl.q;

import io.micronaut.cdk.action.InvalidActionException;
import io.micronaut.cdk.action.Spec;
import io.micronaut.cdk.annotations.CloudSpecific;
import io.micronaut.cdk.annotations.ResourceSpec;
import io.micronaut.cdk.component.User;
import data.cdk.impl.resourceSpec.TestResourceDeleteSpec;

import java.util.Collection;

import static io.micronaut.cdk.Cloud.AZURE;

/**
 * Test Azure User Spec.
 */
@CloudSpecific(AZURE)
@ResourceSpec(component = User.class, deleteSpec = TestResourceDeleteSpec.class)
public class AzureTestResourceSpecUser extends Spec<AzureTestResourceSpecUser> {

    /**
     * Constructor.
     *
     * @param cdkId  CDK ID
     * @param stacks stacks
     */
    protected AzureTestResourceSpecUser(String cdkId, Collection<String> stacks) {
        super(cdkId, stacks);
    }

    public static Builder builder(String cdkId) {
        return new Builder(cdkId);
    }

    /**
     * Builder.
     */
    public static class Builder extends Spec.Builder<AzureTestResourceSpecUser, Builder> {
        Builder(String cdkId) {
            super(cdkId);
        }

        @Override
        protected AzureTestResourceSpecUser doBuild() throws InvalidActionException {
            return new AzureTestResourceSpecUser(getCdkId(), getStacks());
        }
    }
}
