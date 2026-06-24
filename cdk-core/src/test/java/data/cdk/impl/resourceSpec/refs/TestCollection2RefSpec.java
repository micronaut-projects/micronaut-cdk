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

import static io.micronaut.cdk.Cloud.AZURE;

/**
 * Specification with multiple referenced components. Contains multiple component-referencing properties.
 *
 * @param <S> spec type
 */
@ResourceSpec(component = TestRefResource.class, deleteSpec = DeleteSpec.class)
@CloudSpecific(AZURE)
public final class TestCollection2RefSpec<S extends TestCollection2RefSpec<S>> extends Spec<S> {

    private final List<ComponentReference> refs;
    private final ComponentReference part;

    private TestCollection2RefSpec(@NonNull String cdkId,
                                   @NonNull Collection<String> stacks,
                                   List<ComponentReference> refs, ComponentReference part) {
        super(cdkId, stacks);
        this.refs = refs;
        this.part = part;
    }

    public List<ComponentReference> getComponents() {
        return refs;
    }

    public ComponentReference getPart() {
        return part;
    }

    public static <S extends TestCollection2RefSpec<S>, B extends TestCollection2RefSpec.Builder<S, B>> TestCollection2RefSpec.Builder<S, B> builder(String cdkId) {
        return new TestCollection2RefSpec.Builder<>(cdkId);
    }

    public static final class Builder<S extends TestCollection2RefSpec<S>, B extends TestCollection2RefSpec.Builder<S, B>> extends Spec.Builder<S, B> {
        List<ComponentReference> references = new ArrayList<>();
        ComponentReference part;

        Builder(String cdkId) {
            super(cdkId);
        }

        public B addComponent(@NonNull ComponentReference reference) {
            references.add(reference);
            return self();
        }

        public B withPart(@NonNull ComponentReference part) {
            this.part = part;
            return self();
        }

        @Override
        @SuppressWarnings("unchecked")
        protected S doBuild() throws InvalidActionException {
            return (S) new TestCollection2RefSpec<S>(getCdkId(), getStacks(), references, part);
        }
    }
}
