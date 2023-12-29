package waltid.openbadgecredential.cli

import picocli.CommandLine
import picocli.CommandLine.Command

@Command(
        name = "present",
        mixinStandardHelpOptions = true,
        description = ["Presents a verifiable credential for verification"]
)
class PresentCmd : Runnable {

    @CommandLine.Parameters(paramLabel = "jwt", description = ["The JWT-encoded credential to be presented."])
    lateinit var jwt: String;
    override fun run() {
        val vc = OpenBadgeService().presentVC(jwt)
        println("------------------------------------------------------------------")
        println("Presentation:")
        println("------------------------------------------------------------------")
        println(vc)
    }
}
