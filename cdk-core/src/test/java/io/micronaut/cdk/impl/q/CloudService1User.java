package io.micronaut.cdk.impl.q;

import io.micronaut.cdk.annotations.CloudSpecific;
import io.micronaut.cdk.annotations.ComponentService;
import io.micronaut.cdk.component.User;
import jakarta.inject.Singleton;

import static io.micronaut.cdk.Cloud.AZURE;

/**
 * Test service accepting just Azure Users.
 */
@Singleton
@CloudSpecific(AZURE)
@ComponentService(User.class)
public class CloudService1User implements CloudService1 {
}
