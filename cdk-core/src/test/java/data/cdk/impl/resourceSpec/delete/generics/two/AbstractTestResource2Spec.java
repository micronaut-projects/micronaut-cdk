package data.cdk.impl.resourceSpec.delete.generics.two;

import io.micronaut.cdk.action.Spec;
import io.micronaut.cdk.annotations.ResourceSpec;

import java.util.Collection;

@ResourceSpec(component = TestResource2.class, deleteSpec = TestResource2DeleteSpec.class)
public abstract class AbstractTestResource2Spec<S extends AbstractTestResource2Spec<S>> extends Spec<S> {

    public AbstractTestResource2Spec(String cdkId, Collection<String> stacks) {
        super(cdkId, stacks);
    }

    public static <D extends TestResource2DeleteSpec<D>, B extends TestResource2DeleteSpec.Builder<D, B>> TestResource2DeleteSpec.Builder<D, B> deleteBuilder(String id, boolean cdkId) {
        return new TestResource2DeleteSpec.Builder<>(id, cdkId);
    }

}
