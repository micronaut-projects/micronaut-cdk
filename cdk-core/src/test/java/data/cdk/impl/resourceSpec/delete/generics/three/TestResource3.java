package data.cdk.impl.resourceSpec.delete.generics.three;

import io.micronaut.cdk.component.Component;

public class TestResource3<N> extends Component<N> {
    public TestResource3(N cloudComponent, String cloudId, String cdkId) {
        super(cloudComponent, cloudId, cdkId);
    }
}
