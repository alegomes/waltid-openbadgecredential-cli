package waltid.openbadgecredential.cli

import picocli.CommandLine
import picocli.CommandLine.Command
import kotlin.system.exitProcess

import picocli.CommandLine.*
import picocli.CommandLine.Model.CommandSpec


@Command(name = "walt", 
        mixinStandardHelpOptions = true, // adds -h and -v 
        version = ["0.1"],
        description = ["Command line tool for verifiable credentials handling."],
        subcommands = [IssueCmd::class]) // walt issue
class WaltCmd : Runnable {

    @Spec
    var spec: CommandSpec? = null

    override fun run() {
        spec?.commandLine()?.usage(System.out)
    }
}

fun main(args: Array<String>) : Unit = exitProcess(CommandLine(WaltCmd()).execute(*args))