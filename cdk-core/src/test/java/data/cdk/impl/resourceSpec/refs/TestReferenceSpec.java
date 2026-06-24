package data.cdk.impl.resourceSpec.refs;

import io.micronaut.cdk.action.InvalidActionException;
import io.micronaut.cdk.action.Spec;
import io.micronaut.cdk.action.delete.DeleteSpec;
import io.micronaut.cdk.annotations.CloudSpecific;
import io.micronaut.cdk.annotations.ResourceSpec;
import io.micronaut.cdk.component.ComponentReference;
import io.micronaut.core.annotation.NonNull;

import java.util.Collection;

import static io.micronaut.cdk.Cloud.OCI;

/**
 * Resource specification with a single referenced component.
 *
 * @param <S> spec type.
 */
@ResourceSpec(component = TestRefResource.class, deleteSpec = DeleteSpec.class)
@CloudSpecific(OCI)
public final class TestReferenceSpec<S extends TestReferenceSpec<S>> extends Spec<S> {
    private final ComponentReference ref;

    private TestReferenceSpec(@NonNull String cdkId, @NonNull Collection<String> stacks, ComponentReference ref) {
        super(cdkId, stacks);
        this.ref = ref;
    }

    public ComponentReference getComponent() {
        return ref;
    }

    public static <S extends TestReferenceSpec<S>, B extends TestReferenceSpec.Builder<S, B>> TestReferenceSpec.Builder<S, B> builder(String cdkId) {
        return new TestReferenceSpec.Builder<>(cdkId);
    }

    public static final class Builder<S extends TestReferenceSpec<S>, B extends TestReferenceSpec.Builder<S, B>> extends Spec.Builder<S, B> {
        ComponentReference reference;

        Builder(String cdkId) {
            super(cdkId);
        }

        public B setComponent(@NonNull ComponentReference reference) {
            this.reference = reference;
            return self();
        }

        @Override
        @SuppressWarnings("unchecked")
        protected S doBuild() throws InvalidActionException {
            return (S) new TestReferenceSpec<S>(getCdkId(), getStacks(), reference);
        }
    }
}
