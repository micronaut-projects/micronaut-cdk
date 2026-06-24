package data.cdk.impl.resourceSpec;

import io.micronaut.cdk.action.InvalidActionException;
import io.micronaut.cdk.action.delete.DeleteSpec;
import io.micronaut.cdk.component.Component;

import java.util.Collection;

public class TestResourceDeleteSpec extends DeleteSpec<TestResourceDeleteSpec> {
    protected TestResourceDeleteSpec(String id, Collection<String> stacks, boolean byCdkId, boolean errorIfNotFound, String newName, Class<? extends Component> componentClass) {
        super(id, componentClass, stacks, byCdkId, errorIfNotFound, newName);
    }

    public static Builder builder(TestResource spec) {
        return new Builder(spec.getCloudId(), false);
    }

    public static Builder builder(String id, boolean cdkId) {
        return new Builder(id, cdkId);
    }

    public static class Builder extends DeleteSpec.Builder<TestResourceDeleteSpec, TestResourceDeleteSpec.Builder> {
        Builder(String id, boolean byCdkId) {
            super(id, byCdkId, TestResource.class);
        }

        @Override
        protected TestResourceDeleteSpec doBuild() throws InvalidActionException {
            return null;
        }
    }
}
