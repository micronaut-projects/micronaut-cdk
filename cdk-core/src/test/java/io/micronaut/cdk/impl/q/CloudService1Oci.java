package io.micronaut.cdk.impl.q;

import io.micronaut.cdk.annotations.CloudSpecific;
import io.micronaut.cdk.annotations.ComponentService;
import io.micronaut.cdk.component.Project;
import jakarta.inject.Singleton;

import static io.micronaut.cdk.Cloud.OCI;

/**
 * Test service specific for a Cloud + Component type.
 */
@Singleton
@CloudSpecific(OCI)
@ComponentService(Project.class)
public class CloudService1Oci implements CloudService1 {
}
