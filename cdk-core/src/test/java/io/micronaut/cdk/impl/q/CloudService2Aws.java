package io.micronaut.cdk.impl.q;

import io.micronaut.cdk.annotations.CloudSpecific;
import io.micronaut.cdk.annotations.ComponentService;
import io.micronaut.cdk.component.UserGroup;
import jakarta.inject.Singleton;

import static io.micronaut.cdk.Cloud.AWS;

/**
 * Test service specific for cloud that accepts certain unrelated component.
 */
@Singleton
@CloudSpecific(AWS)
@ComponentService(UserGroup.class)
public class CloudService2Aws implements CloudService1 {
}
