package data.cdk.impl.resourceSpec.delete.raw.five;

import io.micronaut.cdk.component.Component;

public class TestResource5<N> extends Component<N> {
    public TestResource5(N cloudComponent, String cloudId, String cdkId) {
        super(cloudComponent, cloudId, cdkId);
    }
}
