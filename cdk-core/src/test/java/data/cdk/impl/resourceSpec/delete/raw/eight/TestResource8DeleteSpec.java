package data.cdk.impl.resourceSpec.delete.raw.eight;

import io.micronaut.cdk.action.InvalidActionException;
import io.micronaut.cdk.action.delete.DeleteSpec;
import io.micronaut.cdk.component.Component;

import java.util.Collection;

public class TestResource8DeleteSpec extends DeleteSpec<TestResource8DeleteSpec> {
    protected TestResource8DeleteSpec(String id, Collection<String> stacks, boolean byCdkId, boolean errorIfNotFound, String newName, Class<? extends Component> componentClass) {
        super(id, componentClass, stacks, byCdkId, errorIfNotFound, newName);
    }

    public static Builder builder(String id, boolean cdkId) {
        return new Builder(id, cdkId);
    }

    public static class Builder extends DeleteSpec.Builder<TestResource8DeleteSpec, Builder> {
        Builder(String id, boolean byCdkId) {
            super(id, byCdkId);
        }

        @Override
        protected TestResource8DeleteSpec doBuild() throws InvalidActionException {
            return new TestResource8DeleteSpec(getId(), getStacks(), isByCdkId(), isErrorIfNotFound(), getNewName(), TestResource8.class);
        }
    }
}
