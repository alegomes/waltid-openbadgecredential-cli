package waltid.openbadgecredential.cli

import picocli.CommandLine.*
import picocli.CommandLine.Model.CommandSpec

import waltid.openbadgecredential.cli.utils.issueVC
@Command(
        name = "issue",
        mixinStandardHelpOptions = true,
        description = ["Issues a verifiable credential"]
)
class IssueCmd : Runnable {

    override fun run() {
        val vc = issueVC()
        println("------------------------")
        println("Issued VC:")
        println(vc)
    }
}
