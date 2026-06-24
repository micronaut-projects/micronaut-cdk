package data.cdk.impl.resourceSpec.delete.raw.five;

import io.micronaut.cdk.action.InvalidActionException;
import io.micronaut.cdk.action.delete.DeleteSpec;
import io.micronaut.cdk.component.Component;
import org.spockframework.util.Assert;

import java.util.Collection;

public class TestResource5DeleteSpec extends DeleteSpec<TestResource5DeleteSpec> {
    protected TestResource5DeleteSpec(String id, Collection<String> stacks, boolean byCdkId, boolean errorIfNotFound, String newName, Class<? extends Component> componentClass) {
        super(id, componentClass, stacks, byCdkId, errorIfNotFound, newName);
    }

    public static <N> Builder builder(TestResource5<N> spec) {
        Assert.fail("Should not be called");
        return new Builder(spec.getCdkId(), true);
    }

    public static Builder builder(String id, boolean cdkId) {
        Assert.fail("Should not be called");
        return new Builder(id, cdkId);
    }

    public static class Builder extends DeleteSpec.Builder<TestResource5DeleteSpec, Builder> {
        Builder(String id, boolean byCdkId) {
            super(id, byCdkId);
        }

        @Override
        protected TestResource5DeleteSpec doBuild() throws InvalidActionException {
            return new TestResource5DeleteSpec(getId(), getStacks(), isByCdkId(), isErrorIfNotFound(), getNewName(), TestResource5.class);
        }
    }
}
