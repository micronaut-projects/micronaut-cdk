package io.micronaut.cdk.action.resolver.id

import io.micronaut.cdk.action.IdResolver
import io.micronaut.cdk.action.resolver.id.database.DatabaseIdResolver
import io.micronaut.cdk.action.resolver.id.redis.RedisIdResolver
import io.micronaut.context.ApplicationContext
import io.micronaut.inject.BeanDefinition
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest
class IdResolverSpec extends Specification {

    @Inject
    ApplicationContext ctx

    void beans() {
        when:
        Collection<BeanDefinition<IdResolver>> defs = ctx.getBeanDefinitions(IdResolver)

        then:
        defs.size() == 4

        when:
        Collection<Class> types = defs.collect { it.declaringType.get() }

        then:
        types.contains DatabaseIdResolver
        types.contains DefaultIdResolver
        types.contains RedisIdResolver
        types.contains IdResolverFactory
    }
}
