package waltid.openbadgecredential.cli

import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Model.CommandSpec
import picocli.CommandLine.Spec
import waltid.openbadgecredential.cli.utils.FileNames
import java.io.File
import kotlin.system.exitProcess

@Command(
        name = "verify",
        mixinStandardHelpOptions = true,
        description = ["Presents a verifiable credential for verification"]
)
class VerifyCmd : Runnable {

    @CommandLine.Option(names = [ "-f", "--file" ],
            defaultValue = FileNames.VP_JWS,
            description =   ["File with a signed JWT-encoded credential to be presented. Defaults to 'jwt.json'"])
    lateinit var fileSource : File

    @CommandLine.Option(names = ["-i", "--inline" ],
            required = false,
            description = ["The signed JWT-encoded credential to be presented. Takes precedence over -f option."])
    lateinit var inlineSource: String;

    @Spec
    var spec: CommandSpec? = null

    override fun run() {

        var jws : String

        if (this::inlineSource.isInitialized && inlineSource != null) {
            println("Getting JWT from the command line argument.")
            jws = inlineSource
        } else {

            if (fileSource.exists()) {
                println("Getting JWT from $fileSource file.")
                jws = fileSource.readText()
            } else {
                println("No option provided. Default to '-f $fileSource', but file doesn't exist. It seems you haven't created a presentation yet. Check `walt present` command")
                spec?.parent()?.commandLine()?.usage(System.err);

                exitProcess(-1)
            }

        }

        println("")
        println("+-------------------------------+")
        println("| Encoded VP JWS to be verified |")
        println("+-------------------------------+")
        println(jws)
        println()

        // EdDSA
        // val validJWSWithEdDSA = "eyJhbGciOiJFZERTQSIsInR5cCI6IkpXVCIsImtpZCI6ImRpZDprZXk6ejZNa2hhYWRaUndyb0FORFZ1ZWRoOVhyTGZKQk5KVkhyTkRZVEZrNmJZSGRLTEVoIn0.eyJpc3MiOiJkaWQ6a2V5Ono2TWtoYWFkWlJ3cm9BTkRWdWVkaDlYckxmSkJOSlZIck5EWVRGazZiWUhkS0xFaCIsInN1YiI6ImRpZDprZXk6ejZNa3JXREZaOEpKeVJ4V2dlQjNvUlAxOTlHdEJGOXBGV1R0akZRTVNuUkpHTld3IiwidmMiOnsiY29udGV4dCI6Imh0dHBzOi8vdzNpZC5vcmcvb3BlbmJhZGdlcy92MiIsImlkIjoidXJuOnV1aWQ6ZGVlZDAyMTgtY2ExNi00MmU3LTkyOGQtYmY5YzUyZjE0NzdmIiwidHlwZSI6IkFzc2VydGlvbiIsInJlY2lwaWVudCI6eyJ0eXBlIjoiZW1haWwiLCJpZGVudGl0eSI6ImFsZWdvbWVzQGdtYWlsLmNvbSIsImhhc2hlZCI6ZmFsc2V9LCJiYWRnZSI6eyJ0eXBlIjoiQmFkZ2VDbGFzcyIsImlkIjoidXJuOnV1aWQ6M2YxZTRiMWEtYjQ5ZS00OGExLTlkZDItOGIwMWFmMjcyMzExIiwibmFtZSI6IkNvZmZlZSBMb3ZlciIsImRlc2NyaXB0aW9uIjoiQSB0cnVlIGxvdmVyIG9mIGdvb2QgY29mZmVlIiwiaW1hZ2UiOiJodHRwczovL3d3dy50ZWVwdWJsaWMuY29tL21hZ25ldC80MDY3NDMyLWNlcnRpZmllZC1jb2ZmZWUtbG92ZXItY2FmZmVpbmUtYWRkaWN0IiwiY3JpdGVyaWEiOnsidHlwZSI6IkNyaXRlcmlhIiwibmFycmF0aXZlIjoiQWJsZSB0byBjYXJyeSBvdXQgZGV0YWlsZWQgc2Vuc29yeSBhbmFseXNpcyBvZiBkaWZmZXJlbnQgY29mZmVlIHRhc3RpbmdzLiJ9LCJpc3N1ZXIiOnsiaWQiOiJkaWQ6a2V5Ono2TWtoYWFkWlJ3cm9BTkRWdWVkaDlYckxmSkJOSlZIck5EWVRGazZiWUhkS0xFaCIsInR5cGUiOiJQcm9maWxlIiwibmFtZSI6IlRoZSBDb2ZmZWUgUGFsYWNlIiwidXJsIjoiaHR0cHM6Ly9lbi53aWtpcGVkaWEub3JnL3dpa2kvQ29mZmVlX3BhbGFjZSIsImVtYWlsIjoiYWxlZ29tZXNAZ21haWwuY29tIn19LCJ2ZXJpZmljYXRpb24iOnsidHlwZSI6Imhvc3RlZCJ9LCJpc3N1ZWRPbiI6IjIwMjMtMTItMjlUMjE6NDU6MTUuNjY0ODIyWiIsImV4cGlyZXMiOiIyMDI0LTA2LTI2VDIxOjQ1OjE1LjY2NDgyMloifSwianRpIjoidXJuOnV1aWQ6ZGVlZDAyMTgtY2ExNi00MmU3LTkyOGQtYmY5YzUyZjE0NzdmIn0.4oRvm68fsIUtzGtvweuJnUtQeEV_ZoCt-Nvb_SIYl-vnjmif3nWzq9aqRERfUFBXtOzptApXCXlP0bFXl_HtDw"
        // val tamperedJWSWithEdDSA = "eyJhbGciOiJFZERTQSIsInR5cCI6IkpXVCIsImtpZCI6ImRpZDprZXk6ejZNa2hhYWRaUndyb0FORFZ1ZWRoOVhyTGZKQk5KVkhyTkRZVEZrNmJZSGRLTEVoIn0.eyJpc3MiOiJkaWQ6a2V5Ono2TWtoYWFkWlJ3cm9BTkRWdWVkaDlYckxmSkJOSlZIck5EWVRGazZiWUhkS0xFaCIsInN1YiI6ImRpZDprZXk6ejZNa3JXREZaOEpKeVJ4V2dlQjNvUlAxOTlHdEJGOXBGV1R0akZRTVNuUkpHTld3IiwidmMiOnsiY29udGV4dCI6Imh0dHBzOi8vdzNpZC5vcmcvb3BlbmJhZGdlcy92MiIsImlkIjoidXJuOnV1aWQ6ZGVlZDAyMTgtY2ExNi00MmU3LTkyOGQtYmY5YzUyZjE0NzdmIiwidHlwZSI6IkFzc2VydGlvbiIsInJlY2lwaWVudCI6eyJ0eXBlIjoiZW1haWwiLCJpZGVudGl0eSI6ImFsZWdvbWVzQGdtYWlsLmNvbSIsImhhc2hlZCI6ZmFsc2V9LCJiYWRnZSI6eyJ0eXBlIjoiQmFkZ2VDbGFzcyIsImlkIjoidXJuOnV1aWQ6M2YxZTRiMWEtYjQ5ZS00OGExLTlkZDItOGIwMWFmMjcyMzExIiwibmFtZSI6IkNvZmZlZSBMb3ZlciIsImRlc2NyaXB0aW9uIjoiQSB0cnVlIGxvdmVyIG9mIGdvb2QgY29mZmVlIiwiaW1hZ2UiOiJodHRwczovL3d3dy50ZWVwdWJsaWMuY29tL21hZ25ldC80MDY3NDMyLWNlcnRpZmllZC1jb2ZmZWUtbG92ZXItY2FmZmVpbmUtYWRkaWN0IiwiY3JpdGVyaWEiOnsidHlwZSI6IkNyaXRlcmlhIiwibmFycmF0aXZlIjoiQWJsZSB0byBjYXJyeSBvdXQgZGV0YWlsZWQgc2Vuc29yeSBhbmFseXNpcyBvZiBkaWZmZXJlbnQgY29mZmVlIHRhc3RpbmdzLiJ9LCJpc3N1ZXIiOnsiaWQiOiJkaWQ6a2V5Ono2TWtoYWFkWlJ3cm9BTkRWdWVkaDlYckxmSkJOSlZIck5EWVRGazZiWUhkS0xFaCIsInR5cGUiOiJQcm9maWxlIiwibmFtZSI6IlRoZSBDb2ZmZWUgUGFsYWNlIiwidXJsIjoiaHR0cHM6Ly9lbi53aWtpcGVkaWEub3JnL3dpa2kvQ29mZmVlX3BhbGFjZSIsImVtYWlsIjoiYWxlZ29tZXNAZ21haWwuY29tIn19LCJ2ZXJpZmljYXRpb24iOnsidHlwZSI6Imhvc3RlZCJ9LCJpc3N1ZWRPbiI6IjIwMjMtMTItMjlUMjE6NDU6MTUuNjY0ODIyWiIsImV4cGlyZXMiOiIyMDI0LTA2LTI2VDIxOjQ1OjE1LjY2NDgyMloifSwianRpIjoidXJuOnV1aWQ6ZGVlZDAyMTgtY2ExNi00MmU3LTkyOGQtYmY5YzUyZjE0NzdmIn0.4oRvm68fsIUtzGtvweuJnUtQeEV_ZoCt-Nvb_SIYl-vnjmif3nWzq9aqRERfUFBXtOzptApXCXlP1bFXl_HtDw"

        // RSA
        // Unsupported JWS algorithm RS256, must be HS256, HS384 or HS512
        // val validJWSWithRSA = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImRpZDprZXk6ejRNWGoxd0J6aTlqVXN0eVA5VHNtTlFVYTl4QTNIQnd3bUJEbUFqMXhNa1oyUFVoZUZLUEtDVVluMnFhVmZjdm1vWXV1cGthVVJhd2djanRYRVREeGJZNndLelY4WkNFeGJ0VHRtRzVvSDRZb1N1ZEdSSGhSRGVlQnJhcERXNlBzd0hoQVZjTWJhSDh0VVZRVG8zWmFLcE42Ym1CVnpmSll2bWlZQTEzeUNxcFQ1bllGYUNwR2doeDFqUTNNYTFGNmRIUUJzMXU1b05wQ29ZU0pWTUExWEdYUGp2QmY5Z1NhZnh2a2tMVm53Y2ZkdlRjQXF1cHdWek1qaDM3MnZITVZGWUc5M2JNS3Y0aHVaNkJqTjhLU2lFRnRYdkJHc1g2bjc5TXdraXFFRWVYRG5GWEpEWndIRFdERUhFTlRrRWF5NE1DWGlONWNoWjFERndhVkt0WXI1aFJTZEdBdVV4eVJUaFBjMjJLS3NzbmIydWt6YjE0TCJ9.eyJpc3MiOiJkaWQ6a2V5Ono0TVhqMXdCemk5alVzdHlQOVRzbU5RVWE5eEEzSEJ3d21CRG1BajF4TWtaMlBVaGVGS1BLQ1VZbjJxYVZmY3Ztb1l1dXBrYVVSYXdnY2p0WEVURHhiWTZ3S3pWOFpDRXhidFR0bUc1b0g0WW9TdWRHUkhoUkRlZUJyYXBEVzZQc3dIaEFWY01iYUg4dFVWUVRvM1phS3BONmJtQlZ6ZkpZdm1pWUExM3lDcXBUNW5ZRmFDcEdnaHgxalEzTWExRjZkSFFCczF1NW9OcENvWVNKVk1BMVhHWFBqdkJmOWdTYWZ4dmtrTFZud2NmZHZUY0FxdXB3VnpNamgzNzJ2SE1WRllHOTNiTUt2NGh1WjZCak44S1NpRUZ0WHZCR3NYNm43OU13a2lxRUVlWERuRlhKRFp3SERXREVIRU5Ua0VheTRNQ1hpTjVjaFoxREZ3YVZLdFlyNWhSU2RHQXVVeHlSVGhQYzIyS0tzc25iMnVremIxNEwiLCJzdWIiOiJkaWQ6a2V5Ono0TVhqMXdCemk5alVzdHlRMmVTdG1yNmtnZ3V4dW45ZkttWVpCY1hSODJ4Y01peUdqcE1wR3VKaFhoOUhmN0hEa0c5cDgyMVRLUmlxVWVtaDJkeWV0TjNqVDJrd044ZkZoSFRBSGRFRXFCRm8zamtoMkQ3TlM2OWdTMlpHMlc3ZEZhYTdxOUVOamZHS0V1Y3dhU1Z3R2RDN3dHZlRCTWhoelk0a0ZpTXRZemlTY3dmcnhUVFFBcnhKSzlQWUphU1hIRkMzeXpic1NBdzh2d3V2TXg3TXF2c1h0SDgxYWdzOUtOUGQzSFBudHVnclhFMXlLVURtcGZZOFhXaDJNaUtyR0VmVDdMTDh0U3JLQkNEMmpncWd6Y0U2dG5ydW1vc05ZZFcxRGJRQWFXVHlYVHd5Y0JzZWVtZUFuWDVUZEQ1NGtKdnM2S3J6dFZNM1RzY2VmMnZkelBNeVdqUGhLdzVpeDhhRWJDUHJFa0ZBOFVVcng2eEciLCJ2YyI6eyJjb250ZXh0IjoiaHR0cHM6Ly93M2lkLm9yZy9vcGVuYmFkZ2VzL3YyIiwiaWQiOiJ1cm46dXVpZDpmMDA5YzgyYy0yNWFjLTQyM2ItOTMwMy0xZDc2YWY0NDZhNTQiLCJ0eXBlIjoiQXNzZXJ0aW9uIiwicmVjaXBpZW50Ijp7InR5cGUiOiJlbWFpbCIsImlkZW50aXR5IjoiYWxlZ29tZXNAZ21haWwuY29tIiwiaGFzaGVkIjpmYWxzZX0sImJhZGdlIjp7InR5cGUiOiJCYWRnZUNsYXNzIiwiaWQiOiJ1cm46dXVpZDowNzBhMWQ3MS0yNTIwLTQzMmYtOWYyYi1mNzBlNzU1OTExZGQiLCJuYW1lIjoiQ29mZmVlIExvdmVyIiwiZGVzY3JpcHRpb24iOiJBIHRydWUgbG92ZXIgb2YgZ29vZCBjb2ZmZWUiLCJpbWFnZSI6Imh0dHBzOi8vd3d3LnRlZXB1YmxpYy5jb20vbWFnbmV0LzQwNjc0MzItY2VydGlmaWVkLWNvZmZlZS1sb3Zlci1jYWZmZWluZS1hZGRpY3QiLCJjcml0ZXJpYSI6eyJ0eXBlIjoiQ3JpdGVyaWEiLCJuYXJyYXRpdmUiOiJBYmxlIHRvIGNhcnJ5IG91dCBkZXRhaWxlZCBzZW5zb3J5IGFuYWx5c2lzIG9mIGRpZmZlcmVudCBjb2ZmZWUgdGFzdGluZ3MuIn0sImlzc3VlciI6eyJpZCI6ImRpZDprZXk6ejRNWGoxd0J6aTlqVXN0eVA5VHNtTlFVYTl4QTNIQnd3bUJEbUFqMXhNa1oyUFVoZUZLUEtDVVluMnFhVmZjdm1vWXV1cGthVVJhd2djanRYRVREeGJZNndLelY4WkNFeGJ0VHRtRzVvSDRZb1N1ZEdSSGhSRGVlQnJhcERXNlBzd0hoQVZjTWJhSDh0VVZRVG8zWmFLcE42Ym1CVnpmSll2bWlZQTEzeUNxcFQ1bllGYUNwR2doeDFqUTNNYTFGNmRIUUJzMXU1b05wQ29ZU0pWTUExWEdYUGp2QmY5Z1NhZnh2a2tMVm53Y2ZkdlRjQXF1cHdWek1qaDM3MnZITVZGWUc5M2JNS3Y0aHVaNkJqTjhLU2lFRnRYdkJHc1g2bjc5TXdraXFFRWVYRG5GWEpEWndIRFdERUhFTlRrRWF5NE1DWGlONWNoWjFERndhVkt0WXI1aFJTZEdBdVV4eVJUaFBjMjJLS3NzbmIydWt6YjE0TCIsInR5cGUiOiJQcm9maWxlIiwibmFtZSI6IlRoZSBDb2ZmZWUgUGFsYWNlIiwidXJsIjoiaHR0cHM6Ly9lbi53aWtpcGVkaWEub3JnL3dpa2kvQ29mZmVlX3BhbGFjZSIsImVtYWlsIjoiYWxlZ29tZXNAZ21haWwuY29tIn19LCJ2ZXJpZmljYXRpb24iOnsidHlwZSI6Imhvc3RlZCJ9LCJpc3N1ZWRPbiI6IjIwMjMtMTItMjlUMjI6NDE6NDcuNjU5NDE1WiIsImV4cGlyZXMiOiIyMDI0LTA2LTI2VDIyOjQxOjQ3LjY1OTQxNVoifSwianRpIjoidXJuOnV1aWQ6ZjAwOWM4MmMtMjVhYy00MjNiLTkzMDMtMWQ3NmFmNDQ2YTU0In0.ouiBNOqWyHKLeW4IyJIrmfAlBoAw6TLIUVfsVjcMjJgqu_nrC_roxEvTf-qKlBTFeD_QhVNx4CYidcGuEPme7R2qi4GYDUqVhykD-snpHFTyyrm8t6cDY43-Y0uQRGfOJlGfePMOQbEGf5x3yj71Y2YTdvgVo1JouY7qNqFDwb63x1FjGY4QtVBWs-iUDkc7zZjxS0uY0GZ2v3I3XTMJdqJ711Dk-BDcytaW7urFlbXa2IdV-8DfNftk7TNbQSAxgr4ebxfcwrAtkY6i5cjYaHnFtZuqztZBVWV3W7j-OsxsLIMuFBllQ70YMoOS6JdXSoQKf5-Cbgtvr4jTGQvkkA"

        // secp256r1
        // Unsupported JWS algorithm ES256, must be HS256, HS384 or HS512
        // val validJWSWithSecp256r1 = "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImRpZDprZXk6eldtdTlSQ3JTM2hCaEdTeWhLZk5TeXd1VFdGQ01qTXhKZUtoTVB0NmtDaFMxdDNmdFdEcVdad3VWN3FRajY2dlJGVHFYWGRHaG9GZkVMYTRtWmFGRFNqZDUxb3VUTFhOcWdoVDhSZThGNVFMV2p4Rm9YOWNNckc5eHVVaFk0eGYifQ.eyJpc3MiOiJkaWQ6a2V5OnpXbXU5UkNyUzNoQmhHU3loS2ZOU3l3dVRXRkNNak14SmVLaE1QdDZrQ2hTMXQzZnRXRHFXWnd1VjdxUWo2NnZSRlRxWFhkR2hvRmZFTGE0bVphRkRTamQ1MW91VExYTnFnaFQ4UmU4RjVRTFdqeEZvWDljTXJHOXh1VWhZNHhmIiwic3ViIjoiZGlkOmtleTp6V211OVJDclMzaEJoR1N5aEtmTlN5d3VUV0ZDTWpNeEplS2hNUHQ2akhGRk40YVFQeTdGNkpLaHdXODMzR3h0dVhhcmNqRG5EUDltM0FXdU54a0ViY0JyTE5SZEhBMWNUaEttZXFCZXdnaDY4VnlIWTJkbkpaaFlaYk1uSlRFWiIsInZjIjp7ImNvbnRleHQiOiJodHRwczovL3czaWQub3JnL29wZW5iYWRnZXMvdjIiLCJpZCI6InVybjp1dWlkOjI2NjYwYmU4LTcwNjMtNDA0MS1hNjczLTdmNzIyY2NmMGVhYSIsInR5cGUiOiJBc3NlcnRpb24iLCJyZWNpcGllbnQiOnsidHlwZSI6ImVtYWlsIiwiaWRlbnRpdHkiOiJhbGVnb21lc0BnbWFpbC5jb20iLCJoYXNoZWQiOmZhbHNlfSwiYmFkZ2UiOnsidHlwZSI6IkJhZGdlQ2xhc3MiLCJpZCI6InVybjp1dWlkOjZlYzc0Zjk2LTQ1OWEtNDQ2Mi04MWVhLTFhNDVjYWEzZWI4ZiIsIm5hbWUiOiJDb2ZmZWUgTG92ZXIiLCJkZXNjcmlwdGlvbiI6IkEgdHJ1ZSBsb3ZlciBvZiBnb29kIGNvZmZlZSIsImltYWdlIjoiaHR0cHM6Ly93d3cudGVlcHVibGljLmNvbS9tYWduZXQvNDA2NzQzMi1jZXJ0aWZpZWQtY29mZmVlLWxvdmVyLWNhZmZlaW5lLWFkZGljdCIsImNyaXRlcmlhIjp7InR5cGUiOiJDcml0ZXJpYSIsIm5hcnJhdGl2ZSI6IkFibGUgdG8gY2Fycnkgb3V0IGRldGFpbGVkIHNlbnNvcnkgYW5hbHlzaXMgb2YgZGlmZmVyZW50IGNvZmZlZSB0YXN0aW5ncy4ifSwiaXNzdWVyIjp7ImlkIjoiZGlkOmtleTp6V211OVJDclMzaEJoR1N5aEtmTlN5d3VUV0ZDTWpNeEplS2hNUHQ2a0NoUzF0M2Z0V0RxV1p3dVY3cVFqNjZ2UkZUcVhYZEdob0ZmRUxhNG1aYUZEU2pkNTFvdVRMWE5xZ2hUOFJlOEY1UUxXanhGb1g5Y01yRzl4dVVoWTR4ZiIsInR5cGUiOiJQcm9maWxlIiwibmFtZSI6IlRoZSBDb2ZmZWUgUGFsYWNlIiwidXJsIjoiaHR0cHM6Ly9lbi53aWtpcGVkaWEub3JnL3dpa2kvQ29mZmVlX3BhbGFjZSIsImVtYWlsIjoiYWxlZ29tZXNAZ21haWwuY29tIn19LCJ2ZXJpZmljYXRpb24iOnsidHlwZSI6Imhvc3RlZCJ9LCJpc3N1ZWRPbiI6IjIwMjMtMTItMjlUMjI6NDU6MDEuMDc0NTMwWiIsImV4cGlyZXMiOiIyMDI0LTA2LTI2VDIyOjQ1OjAxLjA3NDUzMFoifSwianRpIjoidXJuOnV1aWQ6MjY2NjBiZTgtNzA2My00MDQxLWE2NzMtN2Y3MjJjY2YwZWFhIn0.iebkH9UaD99C7Eoc4XXfoNmIveljYwsnMkRqfKu_c2JUuJNd07tEE_zBuOyAvPaqZzzitWJ_YH9paFXUokqdLQ"

        // secp256k1
        // Unsupported JWS algorithm ES256K, must be HS256, HS384 or HS512
        // val validJWSWithSecp256k1 = "eyJhbGciOiJFUzI1NksiLCJ0eXAiOiJKV1QiLCJraWQiOiJkaWQ6a2V5OnpkQ3J1MzlHUlZUajdZNmdLUmJUOWF4YkVycFI5eEFxOUdtUW9nVks4Q1RYR29pdTFqSHZ3QUJoSDN5SDR1NWJ4WmN3NGlaNTFoZDR1TktyczRaTmliNGdDM0JUM0Fqa0NwS3FpemZTOTZtZlBYMlMyeXkxV2FRY2ZIdUQifQ.eyJpc3MiOiJkaWQ6a2V5OnpkQ3J1MzlHUlZUajdZNmdLUmJUOWF4YkVycFI5eEFxOUdtUW9nVks4Q1RYR29pdTFqSHZ3QUJoSDN5SDR1NWJ4WmN3NGlaNTFoZDR1TktyczRaTmliNGdDM0JUM0Fqa0NwS3FpemZTOTZtZlBYMlMyeXkxV2FRY2ZIdUQiLCJzdWIiOiJkaWQ6a2V5OnpkQ3J1MzlHUlZUajdZNmdLUmJUOWF4YkVycFI5eEFxOUdtUW1TUUVlcVRwallrbjR4bmJFR1lvTW9WeUdVRTkyM2FkVFg2RmYzdjgxcG56eFBobjIzd3FZS0g2b0JuS3ppY21kUWtVQ1VhbkZkSlJ6VHZiU1lXaWM0NGQiLCJ2YyI6eyJjb250ZXh0IjoiaHR0cHM6Ly93M2lkLm9yZy9vcGVuYmFkZ2VzL3YyIiwiaWQiOiJ1cm46dXVpZDo5MWM2ZWE2OC00MTU5LTRkYWUtYjYzNy0zZWQ3MDNmYmMyOWMiLCJ0eXBlIjoiQXNzZXJ0aW9uIiwicmVjaXBpZW50Ijp7InR5cGUiOiJlbWFpbCIsImlkZW50aXR5IjoiYWxlZ29tZXNAZ21haWwuY29tIiwiaGFzaGVkIjpmYWxzZX0sImJhZGdlIjp7InR5cGUiOiJCYWRnZUNsYXNzIiwiaWQiOiJ1cm46dXVpZDo1NGMzYTEzYS1lYTAxLTRjZGYtOWZhMS04NTEyOWNlNWMyMzMiLCJuYW1lIjoiQ29mZmVlIExvdmVyIiwiZGVzY3JpcHRpb24iOiJBIHRydWUgbG92ZXIgb2YgZ29vZCBjb2ZmZWUiLCJpbWFnZSI6Imh0dHBzOi8vd3d3LnRlZXB1YmxpYy5jb20vbWFnbmV0LzQwNjc0MzItY2VydGlmaWVkLWNvZmZlZS1sb3Zlci1jYWZmZWluZS1hZGRpY3QiLCJjcml0ZXJpYSI6eyJ0eXBlIjoiQ3JpdGVyaWEiLCJuYXJyYXRpdmUiOiJBYmxlIHRvIGNhcnJ5IG91dCBkZXRhaWxlZCBzZW5zb3J5IGFuYWx5c2lzIG9mIGRpZmZlcmVudCBjb2ZmZWUgdGFzdGluZ3MuIn0sImlzc3VlciI6eyJpZCI6ImRpZDprZXk6emRDcnUzOUdSVlRqN1k2Z0tSYlQ5YXhiRXJwUjl4QXE5R21Rb2dWSzhDVFhHb2l1MWpIdndBQmhIM3lINHU1YnhaY3c0aVo1MWhkNHVOS3JzNFpOaWI0Z0MzQlQzQWprQ3BLcWl6ZlM5Nm1mUFgyUzJ5eTFXYVFjZkh1RCIsInR5cGUiOiJQcm9maWxlIiwibmFtZSI6IlRoZSBDb2ZmZWUgUGFsYWNlIiwidXJsIjoiaHR0cHM6Ly9lbi53aWtpcGVkaWEub3JnL3dpa2kvQ29mZmVlX3BhbGFjZSIsImVtYWlsIjoiYWxlZ29tZXNAZ21haWwuY29tIn19LCJ2ZXJpZmljYXRpb24iOnsidHlwZSI6Imhvc3RlZCJ9LCJpc3N1ZWRPbiI6IjIwMjMtMTItMjlUMjI6NDg6MjEuMjM4MTQ3WiIsImV4cGlyZXMiOiIyMDI0LTA2LTI2VDIyOjQ4OjIxLjIzODE0N1oifSwianRpIjoidXJuOnV1aWQ6OTFjNmVhNjgtNDE1OS00ZGFlLWI2MzctM2VkNzAzZmJjMjljIn0.zJ-EWU1lQV8fMSIU-lpbqf2Z81C_0G7tRcx2Gum6jt4xpSAjFQdrZ-Ek-YPXN7o8eQdtniUj416sb2WcWhLILQ"

        println()
        println("+-------------------------------------------------------------------------+")
        println("|                       Verifying token signature...                      |")
        println("+-------------------------------------------------------------------------+")
        println()
        val signatureVerification = OpenBadgeService().verifySignature(jws)

        println()
        println("+-------------------------------------------------------------------------+")
        println("|                       Verifying other policies...                       |")
        println("+-------------------------------------------------------------------------+")
        println()
        val otherVerification =  try {
            OpenBadgeService().verifyMultiplePolicies(jws)
        } catch (e : IllegalArgumentException) {
            // PresentationVerificationResponse(listOf(PresentationResultEntry()))
            println()
            println(" >>>> ERROR: Other policies failed: ${e.toString()} !!!" )
            println()
            null
        }

        println("------------------------------------------------------------------")
        println("Verification Result:")
        println("------------------------------------------------------------------")

        print("Signature: ")
        if (signatureVerification.isSuccess) println("Success!")
        else println("Failed :-( - ${signatureVerification.exceptionOrNull()?.message}")

        print("Others: ")
        if (otherVerification != null && otherVerification.overallSuccess()) println("Success!")
        else println("Failed - ${otherVerification?.policiesFailed()}")

    }

}
