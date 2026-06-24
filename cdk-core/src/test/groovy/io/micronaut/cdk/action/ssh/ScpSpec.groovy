package io.micronaut.cdk.action.ssh

import io.micronaut.cdk.ExecutionContext
import io.micronaut.cdk.action.Actions
import io.micronaut.cdk.test.AbstractCdkUnitSpec

class ScpSpec extends AbstractCdkUnitSpec {

    private final String cdkId = 'ScpTests'
    private final File file = new File('foo')
    private final String remote = '~/remote'

    void 'test builder'() {
        when:
        Scp.builder(cdkId, remote).build()

        then:
        IllegalStateException ise = thrown()
        ise.message == 'at least one File is required'

        when:
        Scp.builder(cdkId, remote)
                .file(file, remote)
                .serverAliveCountMax(0)
                .build()

        then:
        ise = thrown()
        ise.message == 'serverAliveCountMax cannot be negative or zero'

        when:
        Scp.builder(cdkId, remote)
                .file(file, remote)
                .serverAliveIntervalSeconds(-1)
                .build()

        then:
        ise = thrown()
        ise.message == 'serverAliveIntervalSeconds cannot be negative'

        when:
        Scp.builder(cdkId, remote)
                .file(file, remote)
                .fileMode('abc')
                .build()

        then:
        NumberFormatException nfe = thrown()
        nfe.message.startsWith 'For input string: "abc"'

        when:
        Scp.builder(cdkId, remote)
                .file(file, remote)
                .knownHostsPath('/not/a/file')
                .build()

        then:
        ise = thrown()
        ise.message == '''File /not/a/file doesn't exist'''

        when:
        Scp.builder(cdkId, remote)
                .file(file, remote)
                .build()

        then:
        noExceptionThrown()

        when:
        Scp.builder(cdkId, remote)
                .files(Map.of(file, remote))
                .fileMode(644)
                .build()

        then:
        noExceptionThrown()

        when:
        Scp.builder(cdkId, remote)
                .compress(true)
                .file(file, remote)
                .port(2222)
                .preserveLastModified(true)
                .trustAllCertificates(true)
                .build()

        then:
        noExceptionThrown()
    }

    void 'test init'() {
        given:
        String password = 'pass'
        String username = 'user'
        ExecutionContext ec = new ExecutionContext(true)
        ec.actions = new Actions()

        when:
        Scp.builder(cdkId, remote)
                .file(file, remote)
                .privateKeyPath('/not/a/file2')
                .build()
                .init(ec)

        then:
        IllegalStateException ise = thrown()
        '''File /not/a/file2 doesn't exist''' == ise.message

        when:
        Scp.builder(cdkId, remote)
                .file(file, remote)
                .sshConfigPath('/not/a/file3')
                .build()
                .init(ec)

        then:
        ise = thrown()
        '''File /not/a/file3 doesn't exist''' == ise.message

        when:
        Scp.builder(cdkId, remote)
                .file(file, remote)
                .build()
                .init(ec)

        then:
        ise = thrown()
        'password or privateKeyPath is required' == ise.message

        when:
        Scp.builder(cdkId, remote)
                .file(file, remote)
                .password(password)
                .build()
                .init(ec)

        then:
        IllegalArgumentException iae = thrown()
        'username must be specified or loaded from SSH config' == iae.message

        when:
        Scp.builder(cdkId, remote)
                .file(file, remote)
                .password(password)
                .username(username)
                .build()
                .init(ec)

        then:
        noExceptionThrown()
    }
}
