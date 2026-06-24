package data.cdk.impl.resourceSpec.delete.generics.one;

import io.micronaut.cdk.action.Spec;
import io.micronaut.cdk.annotations.ResourceSpec;
import org.spockframework.util.Assert;

import java.util.Collection;

@ResourceSpec(component = TestResource1.class, deleteSpec = TestResource1DeleteSpec.class)
public abstract class AbstractTestResource1Spec<S extends AbstractTestResource1Spec<S>> extends Spec<S> {

    /**
     * Constructor.
     *
     * @param cdkId  CDK ID
     * @param stacks stacks
     */
    protected AbstractTestResource1Spec(String cdkId, Collection<String> stacks) {
        super(cdkId, stacks);
    }

    public static <D extends TestResource1DeleteSpec<D>, B extends TestResource1DeleteSpec.Builder<D, B>, N> TestResource1DeleteSpec.Builder<D, B> deleteBuilder(TestResource1<N> spec) {
        return new TestResource1DeleteSpec.Builder<>(spec.getCloudId(), false);
    }

    public static <D extends TestResource1DeleteSpec<D>, B extends TestResource1DeleteSpec.Builder<D, B>> TestResource1DeleteSpec.Builder<D, B> deleteBuilder(String id, boolean cdkId) {
        Assert.fail("Should not be called");
        return new TestResource1DeleteSpec.Builder<>(id, cdkId);
    }
}
