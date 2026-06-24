package data.cdk.impl.resourceSpec.delete.raw.seven;

import io.micronaut.cdk.component.Component;

public class TestResource7<N> extends Component<N> {
    public TestResource7(N cloudComponent, String cloudId, String cdkId) {
        super(cloudComponent, cloudId, cdkId);
    }
}
