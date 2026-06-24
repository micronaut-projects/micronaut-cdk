package data.cdk.impl.resourceSpec;

import io.micronaut.cdk.component.Component;

public class TestResource<N> extends Component<N> {
    public TestResource(N cloudComponent, String cloudId, String cdkId) {
        super(cloudComponent, cloudId, cdkId);
    }
}
