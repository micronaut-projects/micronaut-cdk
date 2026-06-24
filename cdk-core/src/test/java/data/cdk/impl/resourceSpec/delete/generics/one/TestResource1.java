package data.cdk.impl.resourceSpec.delete.generics.one;

import io.micronaut.cdk.component.Component;

public class TestResource1<N> extends Component<N> {
    public TestResource1(N cloudComponent, String cloudId, String cdkId) {
        super(cloudComponent, cloudId, cdkId);
    }
}
