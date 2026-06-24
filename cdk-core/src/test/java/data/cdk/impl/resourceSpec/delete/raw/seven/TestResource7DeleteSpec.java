package data.cdk.impl.resourceSpec.delete.raw.seven;

import io.micronaut.cdk.action.InvalidActionException;
import io.micronaut.cdk.action.delete.DeleteSpec;
import io.micronaut.cdk.component.Component;
import org.spockframework.util.Assert;

import java.util.Collection;

public class TestResource7DeleteSpec extends DeleteSpec<TestResource7DeleteSpec> {

    protected TestResource7DeleteSpec(String id,
                                      Collection<String> stacks,
                                      boolean byCdkId,
                                      boolean errorIfNotFound,
                                      String newName,
                                      Class<? extends Component> componentClass) {
        super(id, componentClass, stacks, byCdkId, errorIfNotFound, newName);
    }

    public static <N> Builder builder(TestResource7<N> spec) {
        return new Builder(spec.getCloudId(), false);
    }

    public static Builder builder(String id, boolean cdkId) {
        Assert.fail("Should not be called");
        return new Builder(id, cdkId);
    }

    public static class Builder extends DeleteSpec.Builder<TestResource7DeleteSpec, Builder> {

        Builder(String id, boolean byCdkId) {
            super(id, byCdkId);
        }

        @Override
        protected TestResource7DeleteSpec doBuild() throws InvalidActionException {
            return new TestResource7DeleteSpec(getId(), getStacks(), isByCdkId(), isErrorIfNotFound(), getNewName(), TestResource7.class);
        }
    }
}
