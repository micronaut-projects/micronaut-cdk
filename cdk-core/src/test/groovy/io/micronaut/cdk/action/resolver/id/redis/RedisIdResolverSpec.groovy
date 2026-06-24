package io.micronaut.cdk.action.resolver.id.redis

import io.micronaut.cdk.action.IdResolver
import io.lettuce.core.RedisURI
import io.lettuce.core.api.StatefulRedisConnection
import io.micronaut.context.ApplicationContext
import redis.embedded.RedisServer
import spock.lang.IgnoreIf
import spock.lang.Shared
import spock.lang.Specification

import static io.lettuce.core.RedisURI.DEFAULT_REDIS_PORT

@IgnoreIf(value = { System.getenv('CI') },
        reason = 'need to add Docker support in CI')
class RedisIdResolverSpec extends Specification {

    @Shared
    RedisServer redisServer

    final RedisURI REDIS_URI = RedisURI.Builder.redis('localhost', 6379).build()

    ApplicationContext ctx

    void setupSpec() {
        redisServer = RedisServer.newRedisServer()
                .bind('localhost')
                .port(DEFAULT_REDIS_PORT)
                .build()
        redisServer.start()
    }

    void setup() {
        ctx = ApplicationContext.run(['cdk.idresolver.name': 'redis',
                                      'cdk.redis.uri'      : REDIS_URI.toString()])
    }

    void 'test redis embedded server'() {
        given:
        var client = ctx.getBean(StatefulRedisConnection)
        var command = client.sync()

        expect:
        command.set 'foo', 'bar'
        command.get('foo') == 'bar'
    }

    void 'check types'() {
        given:
        var idResolver = ctx.getBean(IdResolver)

        expect:
        idResolver instanceof RedisIdResolver
    }

    void 'test associate'() {
        given:
        String cdkId = 'cdk1'
        String cloudId = 'cloud1'
        var idResolver = ctx.getBean(IdResolver)

        when:
        idResolver.associate cdkId, cloudId

        then:
        idResolver.findCdkId(cloudId, null, null).get() == cdkId
        idResolver.findCloudId(cdkId, null).get() == cloudId
    }

    void cleanup() {
        ctx?.close()
    }

    void cleanupSpec() {
        redisServer.stop()
    }
}
