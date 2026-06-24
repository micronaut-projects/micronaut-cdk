package io.micronaut.cdk

import io.micronaut.cdk.action.Action
import io.micronaut.cdk.action.IdResolver
import io.micronaut.context.ApplicationContext
import io.micronaut.context.exceptions.BeanInstantiationException
import spock.lang.Specification

import static io.micronaut.cdk.action.resolver.id.ResolverName.DATABASE
import static io.micronaut.cdk.action.resolver.id.ResolverName.DEFAULT
import static io.micronaut.cdk.action.resolver.id.ResolverName.REDIS

class CdkConfigurationPropertiesSpec extends Specification {

    ApplicationContext ctx

    void 'retry defaults'() {
        given:
        start()

        when:
        var configProperties = ctx.getBean(CdkConfigurationProperties)

        then:
        configProperties.retry.attempts == '5'
        configProperties.retry.delay == '1s'
        configProperties.retry.maxdelay == '30s'
        configProperties.retry.multiplier == '2'
    }

    void 'retry overrides'() {
        given:
        start(['cdk.retry.attempts'  : 'a',
               'cdk.retry.delay'     : 'b',
               'cdk.retry.maxdelay'  : 'c',
               'cdk.retry.multiplier': 'd'])

        when:
        var configProperties = ctx.getBean(CdkConfigurationProperties)

        then:
        configProperties.retry.attempts == 'a'
        configProperties.retry.delay == 'b'
        configProperties.retry.maxdelay == 'c'
        configProperties.retry.multiplier == 'd'
    }

    void 'id resolver default'() {
        given:
        start()

        when:
        var configProperties = ctx.getBean(CdkConfigurationProperties)

        then:
        configProperties.idresolver.name == DEFAULT
    }

    void 'id resolver choose existing'() {
        given:
        start('cdk.idresolver.name': 'database')

        when:
        var configProperties = ctx.getBean(CdkConfigurationProperties)

        then:
        configProperties.idresolver.name == DATABASE
    }

    void 'id resolver custom, exists'() {
        given:
        start('cdk.idresolver.className': TestResolver.name)

        when:
        var resolver = ctx.getBean(IdResolver)

        then:
        resolver instanceof TestResolver
    }

    void 'id resolver custom, does not exist'() {
        given:
        start('cdk.idresolver.className': LinkedList.name)

        when:
        ctx.getBean(IdResolver)

        then:
        BeanInstantiationException e = thrown()
        e.message.contains 'Error instantiating bean of type  [io.micronaut.cdk.action.IdResolver]'
        e.message.contains 'class java.util.LinkedList cannot be cast to class io.micronaut.cdk.action.IdResolver'
    }

    void 'Redis URI'() {
        given:
        start(['cdk.idresolver.name': 'redis',
               'cdk.redis.uri'      : 'redis://user:pass@localhost:6379/0?timeout=1s&clientName=myClient&libraryName=myLibrary'])

        when:
        var configProperties = ctx.getBean(CdkConfigurationProperties)

        then:
        configProperties.idresolver.name == REDIS
        configProperties.redis.uri.host == 'localhost'
        configProperties.redis.uri.port == 6379
        configProperties.redis.uri.database == 0
        configProperties.redis.uri.clientName == 'myClient'
        configProperties.redis.uri.credentialsProvider.resolveCredentials().block().username == 'user'
        configProperties.redis.uri.credentialsProvider.resolveCredentials().block().password.toString() == 'pass'
        configProperties.redis.uri.libraryName == 'myLibrary'
    }

    void 'number of threads'() {
        given:
        int threads = 5
        start('cdk.threads': threads)

        when:
        var configProperties = ctx.getBean(CdkConfigurationProperties)

        then:
        configProperties.threads == threads
    }

    void cleanup() {
        ctx?.close()
    }

    private void start(Map config = [:]) {
        ctx = ApplicationContext.run(config)
    }
}

class TestResolver implements IdResolver {

    Optional<String> findCdkId(String cloudId, Action action, component) {}

    Optional<String> findCloudId(String cdkId, Action action) {}

    void associate(String cdkId, String cloudId) {}
}
