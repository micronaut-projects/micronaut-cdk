package data.cdk.impl.resourceSpec.delete.generics.four;

import io.micronaut.cdk.action.InvalidActionException;
import io.micronaut.cdk.action.delete.DeleteSpec;
import io.micronaut.cdk.component.Component;

import java.util.Collection;

public class TestResource4DeleteSpec<D extends TestResource4DeleteSpec<D>> extends DeleteSpec<D> {

    protected TestResource4DeleteSpec(String id,
                                      Collection<String> stacks,
                                      boolean byCdkId,
                                      boolean errorIfNotFound,
                                      String newName,
                                      Class<? extends Component> componentClass) {
        super(id, componentClass, stacks, byCdkId, errorIfNotFound, newName);
    }

    public static <D extends TestResource4DeleteSpec<D>, B extends Builder<D, B>> Builder<D, B> builder(String id, boolean cdkId) {
        return new Builder<>(id, cdkId);
    }

    public static class Builder<D extends TestResource4DeleteSpec<D>, B extends Builder<D, B>> extends DeleteSpec.Builder<D, B> {
        Builder(String id, boolean byCdkId) {
            super(id, byCdkId);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected D doBuild() throws InvalidActionException {
            return (D) new TestResource4DeleteSpec<>(getId(), getStacks(), isByCdkId(), isErrorIfNotFound(), getNewName(),
                    TestResource4.class);
        }
    }
}
