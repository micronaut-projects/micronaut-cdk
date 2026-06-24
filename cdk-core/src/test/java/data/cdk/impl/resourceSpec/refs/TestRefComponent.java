package data.cdk.impl.resourceSpec.refs;

import io.micronaut.cdk.component.Component;
import io.micronaut.cdk.component.ComponentReference;

import java.util.List;

/**
 * Testing component that references other components.
 *
 * @param <N> cloud data.
 */
public final class TestRefComponent<N> extends Component<N> {
    private final List<ComponentReference> refs;

    public TestRefComponent(N cloudComponent, String cloudId, String cdkId, List<ComponentReference> refs) {
        super(cloudComponent, cloudId, cdkId);
        this.refs = refs;
    }

    public List<ComponentReference> getRefs() {
        return refs;
    }
}
