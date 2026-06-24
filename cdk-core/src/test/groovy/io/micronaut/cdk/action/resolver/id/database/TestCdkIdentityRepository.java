package io.micronaut.cdk.action.resolver.id.database;

import io.micronaut.data.jdbc.annotation.JdbcRepository;

import static io.micronaut.data.model.query.builder.sql.Dialect.H2;

@JdbcRepository(dialect = H2)
interface TestCdkIdentityRepository extends CdkIdentityRepository {
}
