package data.cdk.impl.resourceSpec.delete.generics.three;

import io.micronaut.cdk.action.InvalidActionException;
import io.micronaut.cdk.action.delete.DeleteSpec;
import io.micronaut.cdk.component.Component;
import org.spockframework.util.Assert;

import java.util.Collection;

public class TestResource3DeleteSpec<D extends TestResource3DeleteSpec<D>> extends DeleteSpec<D> {

    protected TestResource3DeleteSpec(String id,
                                      Collection<String> stacks,
                                      boolean byCdkId,
                                      boolean errorIfNotFound,
                                      String newName,
                                      Class<? extends Component> componentClass) {
        super(id, componentClass, stacks, byCdkId, errorIfNotFound, newName);
    }

    public static <D extends TestResource3DeleteSpec<D>, B extends Builder<D, B>, N> Builder<D, B> builder(TestResource3<N> spec) {
        return new Builder<>(spec.getCloudId(), false);
    }

    public static <D extends TestResource3DeleteSpec<D>, B extends Builder<D, B>> Builder<D, B> builder(String id, boolean cdkId) {
        Assert.fail("Should not be called");
        return new Builder<>(id, cdkId);
    }

    public static class Builder<D extends TestResource3DeleteSpec<D>, B extends Builder<D, B>> extends DeleteSpec.Builder<D, B> {

        Builder(String id, boolean byCdkId) {
            super(id, byCdkId);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected D doBuild() throws InvalidActionException {
            return (D) new TestResource3DeleteSpec<>(getId(), getStacks(), isByCdkId(), isErrorIfNotFound(), getNewName(),
                    TestResource3.class);
        }
    }
}
