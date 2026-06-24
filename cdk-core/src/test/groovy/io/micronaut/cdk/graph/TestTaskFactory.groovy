package io.micronaut.cdk.graph

import io.micronaut.cdk.command.Command
import io.micronaut.cdk.command.CommandRunException
import io.micronaut.cdk.command.TaskFactory
import groovy.transform.CompileStatic

import java.util.concurrent.Callable

import static io.micronaut.cdk.command.Command.Status.RUNNING

@CompileStatic
class TestTaskFactory implements TaskFactory<Command> {

    @Override
    Callable<Command> createTask(Command command) {
        new TestTask(command)
    }
}

class TestTask implements Callable<Command> {

    final Command command

    TestTask(Command command) {
        this.command = command
    }

    @Override
    Command call() {
        command.commandStatus = RUNNING
        return command
    }
}

class TestExceptionTask implements Callable<Command> {

    final Command command

    TestExceptionTask(Command command) {
        this.command = command
    }

    @Override
    Command call() {
        command.commandStatus = RUNNING
        if (command.action.cdkId == 'lb') {
            throw new CommandRunException('Test Exception.')
        }

        return command
    }
}
