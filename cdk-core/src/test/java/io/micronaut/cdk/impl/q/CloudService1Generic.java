package io.micronaut.cdk.impl.q;

import io.micronaut.cdk.annotations.ComponentService;
import io.micronaut.cdk.component.Component;
import jakarta.inject.Singleton;

/**
 * Test service accepting any component and any cloud.
 */
@Singleton
@ComponentService(Component.class)
public class CloudService1Generic implements CloudService1 {
}
