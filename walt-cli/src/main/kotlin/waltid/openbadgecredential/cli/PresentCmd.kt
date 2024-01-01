package waltid.openbadgecredential.cli

import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import waltid.openbadgecredential.cli.utils.toPrettyJson
import java.io.File
import kotlin.system.exitProcess

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
                defaultValue = "vc-jws.json",
                description =   ["File with a JWT-encoded credential to be presented. Defaults to 'jwt.json'"])
        lateinit var fileSource : File

        @Option(names = ["-i", "--inline" ],
                required = false,
                description = ["The JWT-encoded credential to be presented. Takes precedence over -f option."])
        lateinit var inlineSource: String;
//    }

    @CommandLine.Spec
    var spec: CommandLine.Model.CommandSpec? = null

    override fun run() {

//        val jwt = inlineSource ?: fileSource.readText()
        var jws : String

        if (this::inlineSource.isInitialized && inlineSource != null) {
            println("Getting VC JWT from the command line argument.")
            jws = inlineSource
        } else {

            if (fileSource.exists()) {
                println("Getting VC JWT from $fileSource file.")
                jws = fileSource.readText()
            } else {
                println("No option provided. Default to '-f $fileSource', but file doesn't exist. It seems you haven't issued a credential yet. Check `walt issue` command")
                spec?.parent()?.commandLine()?.usage(System.err);

                exitProcess(-1)
            }

        }

        println("")
        println("+-------------+")
        println("| Encoded JWT |")
        println("+-------------+")
        println(jws)
        println()

        val vpJWS = OpenBadgeService().presentVC(jws)
        println("------------------------------------------------------------------")
        println("Presentation saved in vp-jws.json file")
        println("------------------------------------------------------------------")
        println(toPrettyJson(vpJWS))
    }
}
