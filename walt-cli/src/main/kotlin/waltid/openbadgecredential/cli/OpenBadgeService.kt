package waltid.openbadgecredential.cli

import id.walt.credentials.PresentationBuilder
import id.walt.credentials.issuance.Issuer.mergingJwtIssue
import id.walt.credentials.schemes.JwsSignatureScheme
import id.walt.credentials.vc.vcs.W3CVC
import id.walt.credentials.verification.PolicyRunner
import id.walt.credentials.verification.PolicyRunner.runPolicyRequest
import id.walt.credentials.verification.models.PolicyRequest
import id.walt.credentials.verification.models.PolicyRequest.Companion.parsePolicyRequests
import id.walt.credentials.verification.models.PresentationVerificationResponse
import id.walt.credentials.verification.policies.JwtSignaturePolicy
import id.walt.crypto.utils.JsonUtils.toJsonElement
import id.walt.crypto.utils.JsonUtils.toJsonObject
import id.walt.crypto.utils.JwsUtils
import id.walt.crypto.utils.JwsUtils.decodeJws
import id.walt.did.helpers.WaltidServices
import id.walt.sdjwt.*
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.serialization.json.*
import waltid.openbadgecredential.cli.model.OpenBadge
import waltid.openbadgecredential.cli.model.Profile
import waltid.openbadgecredential.cli.utils.FileNames
import waltid.openbadgecredential.cli.utils.toPrettyJson
import java.io.File
import kotlin.time.Duration.Companion.days



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
        val file = File(FileNames.VC)
        file.writeText(vc.toPrettyJson())

        println("-------------------------------------------------------")
        println("Human-readable version of the newly created credential ")
        println("saved at ${file.absolutePath}.")
        println("-------------------------------------------------------")
        println(vc.toPrettyJson())

        // Issue VC
        val signedVC = signVC(vc)

        // Save the newly created VC in vc.json file
        File(FileNames.VC_JWS).writeText(signedVC)

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

        // If no data has to be updated.
        // val jws = runBlocking { vc.signJws(
        //         issuerKey = issuer.key,
        //         issuerDid = issuer.did,
        //         subjectDid = holder.did)}

        // Overwrites issuedOn and expires field and sign it.
        val jws = runBlocking {
            vc.mergingJwtIssue(
                    issuerKey = issuer.key,
                    issuerDid = issuer.did,
                    subjectDid = holder.did,
                    mappings = dataOverwrites,
                    additionalJwtHeader = emptyMap(),
                    additionalJwtOptions = emptyMap(),
            )
        }

        return jws
    }


    fun presentVC(jwt : String) : String {

        // waltid-verifier-api Main.kt
//        DidService.apply {
//            registerResolver(LocalResolver())
//            updateResolversForMethods()
//        }
//        PolicyManager.registerPolicies(PresentationDefinitionPolicy())

        // JWT as defined in https://www.iana.org/assignments/jwt/jwt.xhtml
        val decodedJWT = jwt.decodeJws(withSignature = true)
        prettyPrint(decodedJWT)

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

        println("---------------------------------------------------------")
        println("Human-readable version of the newly created presentation.")
        println("saved at ${FileNames.VP}.")
        println("---------------------------------------------------------")
        println(toPrettyJson(vp))

        // Now, let's generate a signed token from this the newly created presentation

        // 1st try: failed.
        // PresentationBuilder.buidlAndSign not returning the generated signed token.
        // val vpJWS = runBlocking {
        //     builder.buildAndSign(issuer.key)
        // }

        // 2nd try: failed.
        // java.lang.IllegalArgumentException: No `type` supplied: {"iss":"did:key:z6Mkw54TW4dCtVqzR9pwWPELAupDaVQPNQrnrBLH87qVzpVL","sub":"did:key:z6MkfPHPVCo1ZUNvL6JbV9VBr7xy3YvuN4oiDf29jmRec26Z","vc":{"sub":"did:key:z6MkmpNWgxpZsH4SPytAJn1ezeYMEjWYEXCC2NHxm7QW9xHu","nbf":1704142692,"iat":1704142752,"jti":"urn:uuid:b20604ad-8217-4d2e-aaa9-de419389705f","iss":"did:key:z6MkmpNWgxpZsH4SPytAJn1ezeYMEjWYEXCC2NHxm7QW9xHu","nonce":"ABC123DEF456GHI789JKL","vp":{"@context":["https://www.w3.org/2018/credentials/v1"],"type":["VerifiablePresentation"],"id":"urn:uuid:b20604ad-8217-4d2e-aaa9-de419389705f","holder":"did:key:z6MkmpNWgxpZsH4SPytAJn1ezeYMEjWYEXCC2NHxm7QW9xHu","verifiableCredential":[{"context":"https://w3id.org/openbadges/v2","id":"urn:uuid:8ee4802b-0d00-469d-a500-8bea43488188","type":"Assertion","recipient":{"type":"email","identity":"alegomes@gmail.com","hashed":false},"badge":{"type":"BadgeClass","id":"urn:uuid:f9afd6ad-8618-4357-9c42-532e79961192","name":"Coffee Lover","description":"A true lover of good coffee","image":"https://www.teepublic.com/magnet/4067432-certified-coffee-lover-caffeine-addict","criteria":{"type":"Criteria","narrative":"Able to carry out detailed sensory analysis of different coffee tastings."},"issuer":{"id":"did:key:z6MkmpNWgxpZsH4SPytAJn1ezeYMEjWYEXCC2NHxm7QW9xHu","type":"Profile","name":"The Coffee Palace","url":"https://en.wikipedia.org/wiki/Coffee_palace","email":"alegomes@gmail.com"}},"verification":{"type":"hosted"},"issuedOn":"2023-12-31T22:22:11.784085Z","expires":"2024-06-28T22:22:11.784085Z"}]}}}
        // VP encoded in an unexpected structure. Maybe because I'm using W3CVC?
        // val vpJWS = runBlocking { W3CVC.fromJson(vp.toString()).signJws(
        //                 issuerKey = issuer.key,
        //                 issuerDid = issuer.did,
        //                 subjectDid = holder.did)}

        // 3rd try: failed
        // java.lang.IllegalArgumentException: No `type` supplied: {"iss":"did:key:z6MkgtsqyseDUrcwGbwECbA1Vs8z7JMx8NaK3LvBwTKdkj6Z","sub":"did:key:z6MkrJMax5qDbjkJV3k7DapDPSC2z9e8u1rBareZjrsbPD3S","vc":{"sub":"did:key:z6MkmpNWgxpZsH4SPytAJn1ezeYMEjWYEXCC2NHxm7QW9xHu","nbf":1704146690,"iat":1704146750,"jti":"urn:uuid:7ad1bc7f-158e-4bbf-9614-95faf5c139ff","iss":"did:key:z6MkmpNWgxpZsH4SPytAJn1ezeYMEjWYEXCC2NHxm7QW9xHu","nonce":"ABC123DEF456GHI789JKL","vp":{"@context":["https://www.w3.org/2018/credentials/v1"],"type":["VerifiablePresentation"],"id":"urn:uuid:7ad1bc7f-158e-4bbf-9614-95faf5c139ff","holder":"did:key:z6MkmpNWgxpZsH4SPytAJn1ezeYMEjWYEXCC2NHxm7QW9xHu","verifiableCredential":[{"context":"https://w3id.org/openbadges/v2","id":"urn:uuid:8ee4802b-0d00-469d-a500-8bea43488188","type":"Assertion","recipient":{"type":"email","identity":"alegomes@gmail.com","hashed":false},"badge":{"type":"BadgeClass","id":"urn:uuid:f9afd6ad-8618-4357-9c42-532e79961192","name":"Coffee Lover","description":"A true lover of good coffee","image":"https://www.teepublic.com/magnet/4067432-certified-coffee-lover-caffeine-addict","criteria":{"type":"Criteria","narrative":"Able to carry out detailed sensory analysis of different coffee tastings."},"issuer":{"id":"did:key:z6MkmpNWgxpZsH4SPytAJn1ezeYMEjWYEXCC2NHxm7QW9xHu","type":"Profile","name":"The Coffee Palace","url":"https://en.wikipedia.org/wiki/Coffee_palace","email":"alegomes@gmail.com"}},"verification":{"type":"hosted"},"issuedOn":"2023-12-31T22:22:11.784085Z","expires":"2024-06-28T22:22:11.784085Z"}]}}}
        // Same :-( VP encoded in an unexpected structure.
        // Expected: jwt["type"] or jwt["vc"]["type"] or jws["vp"]["type"]
        // Provided: jwt["vc"]["vp"]["type"]
        val vpJWS = runBlocking {
            JwsSignatureScheme().sign(
                    data = vp.jsonObject,
                    key = issuer.key,
                    jwtHeaders = mapOf(JwsSignatureScheme.JwsHeader.KEY_ID to issuer.did),
                    jwtOptions = mapOf(
                            JwsSignatureScheme.JwsOption.ISSUER to JsonPrimitive(issuer.did),
                            JwsSignatureScheme.JwsOption.SUBJECT to JsonPrimitive(holder.did),
                    ),
            )
        }

        // TODO()
        //  - Using a random key

        // Save presentation for later use
        File(FileNames.VP).writeText(toPrettyJson(vp))
        File(FileNames.VP_JWS).writeText(vpJWS)

        return vpJWS
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

    // Example from https://docs.oss.walt.id/verifier/sdks/verify-single-element
    fun verifySignature(jws : String) :  Result<Any>{

        // JWT as defined in https://www.iana.org/assignments/jwt/jwt.xhtml
        val decodedJWS = jws.decodeJws(withSignature = true)
        prettyPrint(decodedJWS)

        val dataToVerify: JsonElement = JsonPrimitive(jws)

        val policyRequest = PolicyRequest(JwtSignaturePolicy())

        // optionally provide some context for specific Verification Policies (e.g. presentationDefinition, challenge)
        // context ignored in JwtSignaturePolicy
        val context = emptyMap<String, Any>()

        val result: Result<Any> = runBlocking {
            policyRequest.runPolicyRequest(dataToVerify, context)
        } // result type depends on Policy

        return result
    }

    // Example from https://docs.oss.walt.id/verifier/sdks/verify-presentation
    fun verifyMultiplePolicies(jws : String) : PresentationVerificationResponse {

        // val vpPolicies = listOf(PolicyRequest(JwtSignaturePolicy()))
        // val vcPolicies = listOf(PolicyRequest(JwtSignaturePolicy()))

        // configure the validation policies
        val vcPolicies = Json.parseToJsonElement(
        """
           [
              "signature",
              "expired",
              "not-before"
            ] 
        """
        ).jsonArray.parsePolicyRequests()

        val vpPolicies = Json.parseToJsonElement(
        """
            [
              "signature",
              "expired",
              "not-before"
            ]
        """
        ).jsonArray.parsePolicyRequests()

        val specificPolicies = Json.parseToJsonElement(
        """
           {
              "OpenBadgeCredential": [
                  {
                    "policy": "schema",
                    "args": {
                        "type": "object",
                        "required": ["issuer"],
                        "properties": {
                            "issuer": {
                                "type": "object"
                            }
                        }
                    }
                }
              ]
           } 
        """
        ).jsonObject.mapValues { it.value.jsonArray.parsePolicyRequests() }

        // validate verifiable presentation against the configured policies
        // Assumes jws.payload["vp"]["verifiableCredential"] or jws.payload["verifiableCredential"]
        val validationResult = runBlocking {
            PolicyRunner.verifyPresentation(
                    vpTokenJwt = jws,
                    vpPolicies = vpPolicies,
                    globalVcPolicies = vcPolicies,
                    specificCredentialPolicies = specificPolicies,
                    presentationContext = mapOf(
                            // "presentationDefinition" to presentationDefinition,
                            "presentationSubmission" to JsonObject(emptyMap()),
                            "challenge" to "abc"
                    )
            )
        }

        return validationResult
    }

//     fun verifyVC(jwt : String) : String {
//
//         val sharedSecret = "ef23f749-7238-481a-815c-f0c2157dfa8e"
//
//         // Create SimpleJWTCryptoProvider with MACSigner and MACVerifier
//         val cryptoProvider = SimpleJWTCryptoProvider(JWSAlgorithm.HS256, jwsSigner = null, jwsVerifier = MACVerifier(sharedSecret))
//
//
//         // val cryptoProvider = SimpleJWTCryptoProvider(JWSAlgorithm.RS256, jwsSigner = null, jwsVerifier = MACVerifier(sharedSecret))
// //
// ////    val undisclosedJwt = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI0NTYiLCJfc2QiOlsiaGx6ZmpmMDRvNVpzTFIyNWhhNGMtWS05SFcyRFVseGNnaU1ZZDMyNE5nWSJdfQ.2fsLqzujWt0hS0peLS8JLHyyo3D5KCDkNnHcBYqQwVo~"
// //
//         // verify and parse presented SD-JWT with all fields undisclosed, throws Exception if verification fails!
//         // If KeyType.Ed25519 is used, "com.nimbusds.jose.JOSEException: Unsupported JWS algorithm EdDSA, must be HS256, HS384 or HS512"
//         val parsedVerifiedUndisclosedJwt = SDJwt.verifyAndParse(jwt, cryptoProvider)
// //
// //        // print full payload with disclosed fields only
// //        println("Undisclosed JWT payload:")
// //        println(parsedVerifiedUndisclosedJwt.sdJwt.fullPayload)
// ////    println(parsedVerifiedUndisclosedJwt.sdPayload.fullPayload.toString())
// //
// //        // alternatively parse and verify in 2 steps:
// //        val parsedUndisclosedJwt = SDJwt.parse(jwt)
// //        val isValid = parsedUndisclosedJwt.verify(cryptoProvider)
// //        println("Undisclosed SD-JWT verified: $isValid")
// //
// //        val parsedVerifiedDisclosedJwt = SDJwt.verifyAndParse(
// //                "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI0NTYiLCJfc2QiOlsiaGx6ZmpmMDRvNVpzTFIyNWhhNGMtWS05SFcyRFVseGNnaU1ZZDMyNE5nWSJdfQ.2fsLqzujWt0hS0peLS8JLHyyo3D5KCDkNnHcBYqQwVo~WyJ4RFk5VjBtOG43am82ZURIUGtNZ1J3Iiwic3ViIiwiMTIzIl0~",
// //                cryptoProvider
// //        )
// //        // print full payload with disclosed fields
// //        println("Disclosed JWT payload:")
// ////    println(parsedVerifiedDisclosedJwt.sdPayload.fullPayload.toString())
// //        println(parsedVerifiedDisclosedJwt.sdJwt.fullPayload.toString())
//
//     return parsedVerifiedUndisclosedJwt.toString()
//     }

}

private fun prettyPrint(decodedJWT: JwsUtils.JwsParts) {
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
}