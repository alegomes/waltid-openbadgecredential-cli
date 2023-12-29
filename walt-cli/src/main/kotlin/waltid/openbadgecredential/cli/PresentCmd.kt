package waltid.openbadgecredential.cli

import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import java.io.File
import waltid.openbadgecredential.cli.utils.toPrettyJson

@Command(
        name = "present",
        mixinStandardHelpOptions = true,
        description = ["Presents a verifiable credential for verification"]
)
class PresentCmd : Runnable {

//    @CommandLine.ArgGroup(exclusive = true, multiplicity = "1")
//    lateinit var jwt : JWTSource

//    class JWTSource {
        @Option(names = [ "-f", "--file" ],
                defaultValue = "jwt.json",
                description =   ["File with a JWT-encoded credential to be presented. Defaults to 'jwt.json'"])
        lateinit var fileSource : File

        @Option(names = ["-t", "--jwt" ],
                required = false,
                description = ["The JWT-encoded credential to be presented. Takes precedence over -f option."])
        lateinit var inlineSource: String;
//    }


    override fun run() {

//        val jwt = inlineSource ?: fileSource.readText()
        var jwt : String

        if (this::inlineSource.isInitialized && inlineSource != null) {
            println("Getting JWT from the command line argument.")
            jwt = inlineSource
        } else {
            println("Getting JWT from $fileSource file.")
            jwt = fileSource.readText()
        }

        println("")
        println("+-------------+")
        println("| Encoded JWT |")
        println("+-------------+")
        println(jwt)
        println()

        val vc = OpenBadgeService().presentVC(jwt)
        println("------------------------------------------------------------------")
        println("Presentation saved in presentation.json file")
        println("------------------------------------------------------------------")
        println(toPrettyJson(vc))
    }
}
