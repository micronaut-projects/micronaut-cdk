package data.cdk.impl.resourceSpec.refs;

import io.micronaut.cdk.action.InvalidActionException;
import io.micronaut.cdk.action.Spec;
import io.micronaut.cdk.action.delete.DeleteSpec;
import io.micronaut.cdk.annotations.CloudSpecific;
import io.micronaut.cdk.annotations.ResourceSpec;
import io.micronaut.cdk.component.ComponentReference;
import io.micronaut.core.annotation.NonNull;

import java.util.Collection;
import java.util.List;

import static io.micronaut.cdk.Cloud.AWS;

/**
 * Specification with multiple referenced components.
 *
 * @param <S> spec type
 */
@ResourceSpec(component = TestRefResource.class, deleteSpec = DeleteSpec.class)
@CloudSpecific(AWS)
public final class TestCollectionRefSpec<S extends TestCollectionRefSpec<S>> extends Spec<S> {

    private final List<ComponentReference> refs;

    private TestCollectionRefSpec(@NonNull String cdkId,
                                  @NonNull Collection<String> stacks,
                                  List<ComponentReference> refs) {
        super(cdkId, stacks);
        this.refs = refs;
    }

    public List<ComponentReference> getComponents() {
        return refs;
    }

    public static <S extends TestCollectionRefSpec<S>, B extends TestCollectionRefSpec.Builder<S, B>> TestCollectionRefSpec.Builder<S, B> builder(String cdkId) {
        return new TestCollectionRefSpec.Builder<>(cdkId);
    }

    public static final class Builder<S extends TestCollectionRefSpec<S>, B extends TestCollectionRefSpec.Builder<S, B>> extends Spec.Builder<S, B> {

        private ComponentReference reference;

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
            return (S) new TestCollectionRefSpec<S>(getCdkId(), getStacks(), List.of(reference));
        }
    }
}
