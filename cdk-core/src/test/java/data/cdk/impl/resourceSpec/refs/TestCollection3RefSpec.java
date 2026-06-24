package data.cdk.impl.resourceSpec.refs;

import io.micronaut.cdk.action.InvalidActionException;
import io.micronaut.cdk.action.Spec;
import io.micronaut.cdk.action.delete.DeleteSpec;
import io.micronaut.cdk.annotations.CloudSpecific;
import io.micronaut.cdk.annotations.ResourceSpec;
import io.micronaut.cdk.component.ComponentReference;
import io.micronaut.core.annotation.NonNull;

import java.util.ArrayList;
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
public final class TestCollection3RefSpec<S extends TestCollection3RefSpec<S>> extends Spec<S> {

    private final List<ComponentReference> refs;

    private TestCollection3RefSpec(@NonNull String cdkId,
                                   @NonNull Collection<String> stacks,
                                   List<ComponentReference> refs) {
        super(cdkId, stacks);
        this.refs = refs;
    }

    public List<ComponentReference> getComponents() {
        return refs;
    }

    public static <S extends TestCollection3RefSpec<S>, B extends TestCollection3RefSpec.Builder<S, B>> TestCollection3RefSpec.Builder<S, B> builder(String cdkId) {
        return new TestCollection3RefSpec.Builder<>(cdkId);
    }

    public static final class Builder<S extends TestCollection3RefSpec<S>, B extends TestCollection3RefSpec.Builder<S, B>> extends Spec.Builder<S, B> {

        private Collection<ComponentReference> references;

        Builder(String cdkId) {
            super(cdkId);
        }

        public B setComponents(@NonNull Collection<ComponentReference> references) {
            this.references = references;
            return self();
        }

        @Override
        @SuppressWarnings("unchecked")
        protected S doBuild() throws InvalidActionException {
            return (S) new TestCollection3RefSpec<S>(getCdkId(), getStacks(), new ArrayList<>(references));
        }
    }
}
