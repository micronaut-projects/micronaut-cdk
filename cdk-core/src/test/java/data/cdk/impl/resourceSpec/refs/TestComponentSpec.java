package data.cdk.impl.resourceSpec.refs;

import io.micronaut.cdk.action.InvalidActionException;
import io.micronaut.cdk.action.Spec;
import io.micronaut.cdk.action.delete.DeleteSpec;
import io.micronaut.cdk.annotations.CloudSpecific;
import io.micronaut.cdk.annotations.ResourceSpec;
import io.micronaut.core.annotation.NonNull;

import java.util.Collection;

import static io.micronaut.cdk.Cloud.OCI;

@ResourceSpec(component = TestRefComponent.class, deleteSpec = DeleteSpec.class)
@CloudSpecific(OCI)
public final class TestComponentSpec<S extends TestComponentSpec<S>> extends Spec<S> {

    private TestComponentSpec(@NonNull String cdkId, @NonNull Collection<String> stacks) {
        super(cdkId, stacks);
    }

    public static <S extends TestComponentSpec<S>, B extends TestComponentSpec.Builder<S, B>> TestComponentSpec.Builder<S, B> builder(String cdkId) {
        return new TestComponentSpec.Builder<>(cdkId);
    }

    public static final class Builder<S extends TestComponentSpec<S>, B extends TestComponentSpec.Builder<S, B>> extends Spec.Builder<S, B> {
        Builder(String cdkId) {
            super(cdkId);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected S doBuild() throws InvalidActionException {
            return (S) new TestComponentSpec<S>(getCdkId(), getStacks());
        }
    }
}
