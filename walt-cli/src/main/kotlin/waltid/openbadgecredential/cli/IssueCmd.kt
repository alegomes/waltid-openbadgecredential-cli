package waltid.openbadgecredential.cli

import picocli.CommandLine.Command
import waltid.openbadgecredential.cli.utils.FileNames

@Command(
        name = "issue",
        mixinStandardHelpOptions = true,
        description = ["Issues a verifiable credential"]
)
class IssueCmd : Runnable {

    override fun run() {
        val vc = OpenBadgeService().issueVC()
        println("------------------------------------------------------------------")
        println("JWT-encoded version of the newly created (and signed?) credential ")
        println("saved in file ${FileNames.VC_JWS} to be verified later.")
        println("------------------------------------------------------------------")
        println(vc)
    }
}
