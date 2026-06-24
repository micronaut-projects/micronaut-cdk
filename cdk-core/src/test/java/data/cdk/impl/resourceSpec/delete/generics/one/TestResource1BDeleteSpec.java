package data.cdk.impl.resourceSpec.delete.generics.one;

import io.micronaut.cdk.action.InvalidActionException;
import io.micronaut.cdk.action.delete.DeleteSpec;
import io.micronaut.cdk.component.Component;
import data.cdk.impl.resourceSpec.TestResource;
import org.spockframework.util.Assert;

import java.util.Collection;

public class TestResource1BDeleteSpec<D extends TestResource1BDeleteSpec<D>> extends DeleteSpec<D> {

    protected TestResource1BDeleteSpec(String id,
                                       Collection<String> stacks,
                                       boolean byCdkId,
                                       boolean errorIfNotFound,
                                       String newName,
                                       Class<? extends Component> componentClass) {
        super(id, componentClass, stacks, byCdkId, errorIfNotFound, newName);
    }

    public static <D extends TestResource1BDeleteSpec<D>, B extends TestResource1BDeleteSpec.Builder<D, B>> TestResource1BDeleteSpec.Builder<D, B> builder(TestResource1 spec) {
        Assert.fail("Should not be called");
        return new Builder<>(spec.getCloudId(), false);
    }

    public static <D extends TestResource1BDeleteSpec<D>, B extends TestResource1BDeleteSpec.Builder<D, B>> TestResource1BDeleteSpec.Builder<D, B> builder(String id, boolean cdkId) {
        Assert.fail("Should not be called");
        return new Builder<>(id, cdkId);
    }

    public static class Builder<D extends TestResource1BDeleteSpec<D>, B extends Builder<D, B>> extends DeleteSpec.Builder<D, B> {
        Builder(String id, boolean byCdkId) {
            super(id, byCdkId, TestResource.class);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected D doBuild() throws InvalidActionException {
            return (D) new TestResource1BDeleteSpec<>(getId(), getStacks(), isByCdkId(), isErrorIfNotFound(), getNewName(),
                    TestResource.class);
        }
    }

    public static class Builder2<D extends TestResource1BDeleteSpec<D>, B extends Builder2<D, B>> extends DeleteSpec.Builder<D, B> {
        Builder2(String id, boolean byCdkId) {
            super(id, byCdkId, TestResource.class);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected D doBuild() throws InvalidActionException {
            return (D) new TestResource1BDeleteSpec<>(getId(), getStacks(), isByCdkId(), isErrorIfNotFound(), getNewName(),
                    TestResource.class);
        }
    }
}
