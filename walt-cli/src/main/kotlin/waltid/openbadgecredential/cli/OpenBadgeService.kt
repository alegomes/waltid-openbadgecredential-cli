package waltid.openbadgecredential.cli

import id.walt.credentials.PresentationBuilder
import id.walt.credentials.issuance.Issuer.mergingJwtIssue
import id.walt.credentials.vc.vcs.W3CVC
import id.walt.crypto.utils.JsonUtils.toJsonElement
import id.walt.crypto.utils.JsonUtils.toJsonObject
import id.walt.crypto.utils.JwsUtils.decodeJws
import id.walt.did.helpers.WaltidServices
import id.walt.sdjwt.*
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.serialization.json.JsonElement
import waltid.openbadgecredential.cli.model.OpenBadge
import waltid.openbadgecredential.cli.model.Profile
import kotlin.time.Duration.Companion.days

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jose.crypto.MACVerifier
import id.walt.crypto.utils.JsonUtils.printAsJson
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import waltid.openbadgecredential.cli.utils.prettyJson
import java.io.File
import waltid.openbadgecredential.cli.utils.toPrettyJson


class OpenBadgeService {

    // Ideally, both the issuer and the holder should already exist and
    // have their own identifiers when a new credential is issued.

    private val issuer : Profile by lazy {
        Profile()
    }
    private val holder : Profile by lazy {
        Profile()
    }

    init {
        runBlocking {
            WaltidServices.init()
        }
    }

    fun issueVC(): String {

        // Creates a fake OpenBadgeCredential v2.0 structure
        val assertion = OpenBadge(issuer).fakeIt()

        // Create VC
        val vc = this.createVC(assertion)

        // Save the newly created VC in vc.json file
        val fileName = "vc.json"
        val file = File(fileName)
        file.writeText(vc.toPrettyJson())

        println("-------------------------------------------------------")
        println("Human-readable version of the newly created credential ")
        println("saved at ${file.absolutePath}.")
        println("-------------------------------------------------------")
        println(vc.toPrettyJson())

        // Issue VC
        val signedVC = signVC(vc)

        // Save the newly created VC in vc.json file
        File("jwt.json").writeText(signedVC)

        return signedVC
    }


    private fun createVC(data : Map<String, Any>): W3CVC {

        // Convert Map<string, Any> to Map<String, JsonElement>
        val credential = W3CVC(data.mapValues { it.value.toJsonElement() })

        return credential
    }

    private fun signVC(vc: W3CVC): String {

        // Update issue date and time
        val issuanceDate = Clock.System.now()
        val expirationDate = issuanceDate + (6*30).days

        val dataOverwrites = mapOf(
                "issuedOn" to issuanceDate.toString(),
                "expires" to expirationDate.toString(),
        ).toJsonObject()

        val jwt = runBlocking {
            vc.mergingJwtIssue(
                    issuerKey = issuer.key,
                    issuerDid = issuer.did,
                    subjectDid = holder.did,
                    mappings = dataOverwrites,
                    additionalJwtHeader = emptyMap(),
                    additionalJwtOptions = emptyMap(),
            )
        }

        return jwt
    }


    fun presentVC(jwt : String) : JsonElement {

        // waltid-verifier-api Main.kt
//        DidService.apply {
//            registerResolver(LocalResolver())
//            updateResolversForMethods()
//        }
//        PolicyManager.registerPolicies(PresentationDefinitionPolicy())

        // JWT as defined in https://www.iana.org/assignments/jwt/jwt.xhtml
        val decodedJWT = jwt.decodeJws(withSignature = true)
        println("")
        println("+-------------+")
        println("| Decoded JWT |")
        println("+-------------+")
        println("\n---- Header")
        println(toPrettyJson(decodedJWT.header))
        println("\n---- Payload")
        println(toPrettyJson(decodedJWT.payload))
        println("\n---- Signature")
        println(toPrettyJson(decodedJWT.signature))

        val decodedVC = decodedJWT.payload["vc"]

        val builder = PresentationBuilder().apply {

            // Same DID being used for the Issuer, the Subject and the VP Holder. Not cool :-/
            did = (decodedJWT.payload["iss"] as JsonPrimitive).contentOrNull

            /* nbf, iat, jti set automatically to sane default values */

            nonce = "ABC123DEF456GHI789JKL"

            /* vp.context, vp.type, vp.id set automatically to sane default values */

            addCredential(decodedVC.toJsonElement())

        }

        val vp = builder.buildPresentationJson()

        // TODO()
        //  - Using a random key
        //  - buildAndSign returns nothing
//        val vp2 = runBlocking { builder.buildAndSign(issuer.key) }

        // Save presentation for later use
        File("presentation.json").writeText(toPrettyJson(vp))

        return vp

    }

//    private fun presentSDJwt(jwt : String) {
//        // parse previously created SD-JWT
//        val sdJwt = SDJwt.parse(jwt)
//
//        // present without disclosing SD fields
//        val presentedUndisclosedJwt = sdJwt.present(discloseAll = false)
//        println("------------------------")
//        println("Undisclosed")
//        println(presentedUndisclosedJwt)
//
//        // present disclosing all SD fields
//        val presentedDisclosedJwt = sdJwt.present(discloseAll = true)
//        println("------------------------")
//        println("Disclosed")
//        println(presentedDisclosedJwt)
//
//        // present disclosing selective fields, using SDMap
//        val presentedSelectiveJwt = sdJwt.present(mapOf(
//                "context" to SDField(true)
//        ).toSDMap())
//        println("------------------------")
//        println("Selective")
//        println(presentedSelectiveJwt)
//
//        // present disclosing fields, using JSON paths
//        val presentedSelectiveJwt2 = sdJwt.present(
//                SDMap.generateSDMap(listOf("id"))
//        )
//        println("------------------------")
//        println("Selective with JSON")
//        println(presentedSelectiveJwt2)
//
//    }
//
    fun verifyVC(jwt : String) : String {

        val sharedSecret = "ef23f749-7238-481a-815c-f0c2157dfa8e"

        // Create SimpleJWTCryptoProvider with MACSigner and MACVerifier
        val cryptoProvider = SimpleJWTCryptoProvider(JWSAlgorithm.HS256, jwsSigner = null, jwsVerifier = MACVerifier(sharedSecret))
//
////    val undisclosedJwt = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI0NTYiLCJfc2QiOlsiaGx6ZmpmMDRvNVpzTFIyNWhhNGMtWS05SFcyRFVseGNnaU1ZZDMyNE5nWSJdfQ.2fsLqzujWt0hS0peLS8JLHyyo3D5KCDkNnHcBYqQwVo~"
//
        // verify and parse presented SD-JWT with all fields undisclosed, throws Exception if verification fails!
        val parsedVerifiedUndisclosedJwt = SDJwt.verifyAndParse(jwt, cryptoProvider)
//
//        // print full payload with disclosed fields only
//        println("Undisclosed JWT payload:")
//        println(parsedVerifiedUndisclosedJwt.sdJwt.fullPayload)
////    println(parsedVerifiedUndisclosedJwt.sdPayload.fullPayload.toString())
//
//        // alternatively parse and verify in 2 steps:
//        val parsedUndisclosedJwt = SDJwt.parse(jwt)
//        val isValid = parsedUndisclosedJwt.verify(cryptoProvider)
//        println("Undisclosed SD-JWT verified: $isValid")
//
//        val parsedVerifiedDisclosedJwt = SDJwt.verifyAndParse(
//                "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI0NTYiLCJfc2QiOlsiaGx6ZmpmMDRvNVpzTFIyNWhhNGMtWS05SFcyRFVseGNnaU1ZZDMyNE5nWSJdfQ.2fsLqzujWt0hS0peLS8JLHyyo3D5KCDkNnHcBYqQwVo~WyJ4RFk5VjBtOG43am82ZURIUGtNZ1J3Iiwic3ViIiwiMTIzIl0~",
//                cryptoProvider
//        )
//        // print full payload with disclosed fields
//        println("Disclosed JWT payload:")
////    println(parsedVerifiedDisclosedJwt.sdPayload.fullPayload.toString())
//        println(parsedVerifiedDisclosedJwt.sdJwt.fullPayload.toString())

    return parsedVerifiedUndisclosedJwt.toString()
    }

}