package waltid.openbadgecredential.cli

import picocli.CommandLine.*
import picocli.CommandLine.Model.CommandSpec

@Command(name = "issue", 
         mixinStandardHelpOptions = true,
         description = ["Issues a verifiable credential"])
class IssueCmd : Runnable {

    @Spec
    var spec: CommandSpec? = null

    override fun run() {
        println("VC issued.")
    }
}