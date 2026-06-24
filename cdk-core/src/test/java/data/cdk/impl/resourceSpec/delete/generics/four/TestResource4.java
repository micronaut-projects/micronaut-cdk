package data.cdk.impl.resourceSpec.delete.generics.four;

import io.micronaut.cdk.component.Component;

public class TestResource4<N> extends Component<N> {
    public TestResource4(N cloudComponent, String cloudId, String cdkId) {
        super(cloudComponent, cloudId, cdkId);
    }
}
