package data.cdk.impl.resourceSpec.delete.raw.six;

import io.micronaut.cdk.component.Component;

public class TestResource6<N> extends Component<N> {
    public TestResource6(N cloudComponent, String cloudId, String cdkId) {
        super(cloudComponent, cloudId, cdkId);
    }
}
