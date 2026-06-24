package io.micronaut.cdk.impl.q;

import io.micronaut.cdk.annotations.CloudSpecific;
import jakarta.inject.Singleton;

import static io.micronaut.cdk.Cloud.AWS;

/**
 * Test service specific for cloud only accepting all components.
 */
@Singleton
@CloudSpecific(AWS)
public class CloudService1Aws implements CloudService1 {
}
