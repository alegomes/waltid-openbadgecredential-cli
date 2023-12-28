package waltid.openbadgecredential.cli

import picocli.CommandLine.Command

@Command(
        name = "issue",
        mixinStandardHelpOptions = true,
        description = ["Issues a verifiable credential"]
)
class IssueCmd : Runnable {

    override fun run() {
        val vc = OpenBadgeService().issueVC()
        println("------------------------------------------------------------------")
        println("JWT-encoded version of the newly created (and signed?) credential:")
        println("------------------------------------------------------------------")
        println(vc)
    }
}
