package data.cdk.impl.resourceSpec.delete.generics.two;

import io.micronaut.cdk.component.Component;

public class TestResource2<N> extends Component<N> {
    public TestResource2(N cloudComponent, String cloudId, String cdkId) {
        super(cloudComponent, cloudId, cdkId);
    }
}
