package io.micronaut.cdk.impl.q;

import io.micronaut.cdk.action.InvalidActionException;
import io.micronaut.cdk.action.Spec;
import io.micronaut.cdk.annotations.CloudSpecific;
import io.micronaut.cdk.annotations.ResourceSpec;
import io.micronaut.cdk.component.Project;
import data.cdk.impl.resourceSpec.TestResourceDeleteSpec;

import java.util.Collection;

import static io.micronaut.cdk.Cloud.OCI;

/**
 * Test Oci Project Spec.
 */
@CloudSpecific(OCI)
@ResourceSpec(component = Project.class, deleteSpec = TestResourceDeleteSpec.class)
public class OciTestResourceSpecProject extends Spec<OciTestResourceSpecProject> {

    /**
     * Constructor.
     *
     * @param cdkId  CDK ID
     * @param stacks stacks
     */
    protected OciTestResourceSpecProject(String cdkId, Collection<String> stacks) {
        super(cdkId, stacks);
    }

    public static Builder builder(String cdkId) {
        return new Builder(cdkId);
    }

    /**
     * Builder.
     */
    public static class Builder extends Spec.Builder<OciTestResourceSpecProject, Builder> {
        Builder(String cdkId) {
            super(cdkId);
        }

        @Override
        protected OciTestResourceSpecProject doBuild() throws InvalidActionException {
            return new OciTestResourceSpecProject(getCdkId(), getStacks());
        }
    }
}
