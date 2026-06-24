package io.micronaut.cdk.action.ssh

import io.micronaut.cdk.ExecutionContext
import io.micronaut.cdk.action.Actions
import io.micronaut.cdk.test.AbstractCdkUnitSpec

class SSHExecSpec extends AbstractCdkUnitSpec {

    private final String cdkId = 'SSHExecTests'
    private final String command = 'ls -l'
    private final String password = 'pass'
    private final String remote = '~/remote'

    void 'test builder'() {
        when:
        SSHExec.builder(cdkId, remote).build()

        then:
        IllegalStateException e = thrown()
        e.message == 'at least one command is required'

        when:
        SSHExec.builder(cdkId, remote)
                .command(command)
                .serverAliveCountMax(0)
                .build()

        then:
        e = thrown()
        e.message == 'serverAliveCountMax cannot be negative or zero'

        when:
        SSHExec.builder(cdkId, remote)
                .command(command)
                .serverAliveIntervalSeconds(-1)
                .build()

        then:
        e = thrown()
        e.message == 'serverAliveIntervalSeconds cannot be negative'

        when:
        SSHExec.builder(cdkId, remote)
                .command(command)
                .knownHostsPath('/not/a/file')
                .build()

        then:
        e = thrown()
        e.message == '''File /not/a/file doesn't exist'''

        when:
        SSHExec.builder(cdkId, remote)
                .command(command)
                .build()

        then:
        noExceptionThrown()

        when:
        SSHExec.builder(cdkId, remote)
                .commands(List.of(command))
                .password(password)
                .build()

        then:
        noExceptionThrown()

        when:
        SSHExec.builder(cdkId, remote)
                .command(command)
                .password(password)
                .port(2222)
                .trustAllCertificates(true)
                .build()

        then:
        noExceptionThrown()
    }

    void 'test init'() {
        given:
        String username = 'user'
        ExecutionContext ec = new ExecutionContext(true)
        ec.actions = new Actions()

        when:
        SSHExec.builder(cdkId, remote)
                .command(command)
                .username(username)
                .passphrase('hello')
                .build()
                .init(ec)

        then:
        IllegalStateException ise = thrown()
        ise.message == 'password or privateKeyPath is required'

        when:
        SSHExec.builder(cdkId, remote)
                .command(command)
                .password(password)
                .build()
                .init(ec)

        then:
        IllegalArgumentException iae = thrown()
        iae.message == 'username must be specified or loaded from SSH config'

        when:
        SSHExec.builder(cdkId, remote)
                .command(command)
                .password(password)
                .privateKeyPath('/not/a/file2')
                .username(username)
                .build()
                .init(ec)

        then:
        ise = thrown()
        ise.message == '''File /not/a/file2 doesn't exist'''

        when:
        SSHExec.builder(cdkId, remote)
                .command(command)
                .privateKeyPath('/not/a/file3')
                .username(username)
                .build()
                .init(ec)

        then:
        ise = thrown()
        ise.message == '''File /not/a/file3 doesn't exist'''

        when:
        SSHExec.builder(cdkId, remote)
                .command(command)
                .sshConfigPath('/not/a/file4')
                .username(username)
                .build()
                .init(ec)

        then:
        ise = thrown()
        ise.message == '''File /not/a/file4 doesn't exist'''

        when:
        SSHExec.builder(cdkId, remote)
                .command(command)
                .password(password)
                .username(username)
                .build()
                .init(ec)

        then:
        noExceptionThrown()
    }
}
