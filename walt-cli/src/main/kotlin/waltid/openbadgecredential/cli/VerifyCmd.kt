package waltid.openbadgecredential.cli

import picocli.CommandLine
import picocli.CommandLine.Command

@Command(
        name = "verify",
        mixinStandardHelpOptions = true,
        description = ["Presents a verifiable credential for verification"]
)
class VerifyCmd : Runnable {

    @CommandLine.Parameters(paramLabel = "vp", description = ["???"])
    lateinit var vp: String;
    override fun run() {
        val vc = OpenBadgeService().verifyVC(vp)
        println("------------------------------------------------------------------")
        println("Verification:")
        println("------------------------------------------------------------------")
        println(vc)
    }
}
