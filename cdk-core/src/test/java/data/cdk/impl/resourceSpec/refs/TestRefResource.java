package data.cdk.impl.resourceSpec.refs;

import io.micronaut.cdk.component.Component;

public final class TestRefResource<N> extends Component<N> {
    public TestRefResource(N cloudComponent, String cloudId, String cdkId) {
        super(cloudComponent, cloudId, cdkId);
    }
}
