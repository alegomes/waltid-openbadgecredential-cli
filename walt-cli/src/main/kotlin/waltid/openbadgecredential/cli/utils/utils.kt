package waltid.openbadgecredential.cli.utils

import id.walt.credentials.issuance.Issuer
import id.walt.credentials.vc.vcs.W3CVC
import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.LocalKey
import id.walt.crypto.utils.JsonUtils.toJsonElement
import id.walt.crypto.utils.JsonUtils.toJsonObject
import id.walt.did.dids.DidService
import id.walt.did.helpers.WaltidServices
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID
import id.walt.credentials.issuance.Issuer.baseIssue
import id.walt.crypto.keys.Key
import id.walt.sdjwt.SimpleJWTCryptoProvider
//import id.walt.sdjwt.JWTClaimsSet
import id.walt.sdjwt.SDPayload
import id.walt.sdjwt.SDJwt
import id.walt.sdjwt.SDField
import id.walt.sdjwt.SDMap
import id.walt.sdjwt.toSDMap
import com.nimbusds.jwt.SignedJWT
import id.walt.credentials.issuance.Issuer.mergingJwtIssue


import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jose.crypto.MACVerifier
import kotlin.math.sign

class IssuerKey private constructor() {
    companion object {
        val instance : Key by lazy {
            runBlocking {
                LocalKey.generate(KeyType.Ed25519)
            }
        }
    }
}

class HolderKey private constructor() {
    companion object {
        val instance : Key by lazy {
            runBlocking {
                LocalKey.generate(KeyType.Ed25519)
            }
        }
    }
}

val issuerDid by lazy {
    runBlocking {
        DidService.registerByKey("key", IssuerKey.instance).did
    }
}

val holderDid by lazy {
    runBlocking {
        DidService.registerByKey("key", HolderKey.instance).did
    }
}

fun issueVC(): String {

    runBlocking {
        WaltidServices.init()
    }

    val assertion = createAssertionDataModel()

    // Create VC
    val vc = createVC(assertion)

    // Issue VC
    val signedVC = signVC(assertion, vc)

    presentSDJwt(signedVC)
//    verifyVC(signedVC)

    return signedVC
}

private fun createAssertionDataModel() : Map<String, Any> {

    val credentialContext = "https://w3id.org/openbadges/v2"
    val credentialId = "urn:uuid:${UUID.generateUUID()}"
    val credentialType = "Assertion" // "OpenBadgeCredential"

    // generate an asymmetric key of type EdDSA ED25519
    // val keyId = keyService.generate(KeyAlgorithm.EdDSA_Ed25519)

    val issuerType = "Profile"
    val issuerName = "The Coffee Palace"
    val issuerUrl = "https://en.wikipedia.org/wiki/Coffee_palace"
    val issuerEmail = "alegomes@gmail.com"

    // As in https://www.imsglobal.org/sites/default/files/Badges/OBv2p0Final/index.html#Profile
    val issuer = mapOf(
            "id" to issuerDid,
            "type" to issuerType,
            "name" to issuerName,
            "url" to issuerUrl,
            "email" to issuerEmail,
    )

    // As in https://www.imsglobal.org/sites/default/files/Badges/OBv2p0Final/index.html#IdentityObject
    val recipient = mapOf(
            "type" to "email",
            "identity" to issuerEmail,
            "hashed" to false
    ).toJsonObject()

    // As in https://www.imsglobal.org/sites/default/files/Badges/OBv2p0Final/index.html#BadgeClass
    val badge = mapOf(
            "type" to "BadgeClass",
            "id" to "urn:uuid:${UUID.generateUUID()}",
            "name" to "Coffee Lover",
            "description" to "A true lover of good coffee",
            "image" to "https://www.teepublic.com/magnet/4067432-certified-coffee-lover-caffeine-addict",
            "criteria" to mapOf(
                    "type" to "Criteria",
                    "narrative" to "Able to carry out detailed sensory analysis of different coffee tastings."
            ),
            "issuer" to issuer
    )

    // As in https://www.imsglobal.org/sites/default/files/Badges/OBv2p0Final/index.html#VerificationObject
    val verification = mapOf(
            "type" to "hosted",
    )

    val issuanceDate = Clock.System.now()
    val expirationDate = issuanceDate + (6*30).days

    // As in https://www.imsglobal.org/sites/default/files/Badges/OBv2p0Final/index.html#Assertion
    val assertion = mapOf(
            "context" to credentialContext,
            "id" to credentialId, // Generated automagically?
            "type" to credentialType,
            "recipient" to recipient,
            "badge" to badge,
            "verification" to verification,
            "issuedOn" to issuanceDate.toString(),
            "expires" to expirationDate.toString(),
    )

    return assertion
}

private fun createVC(data : Map<String, Any>): W3CVC {

    // Convert Map<string, Any> to Map<String, JsonElement>
    val credential = W3CVC(data.mapValues { it.value.toJsonElement() })

    println(credential.toPrettyJson())
    return credential
}

private fun signVC(assertion : Map<String, Any>, vc: W3CVC): String {

    // Update issue date and time
    val issuanceDate = Clock.System.now()
    val expirationDate = issuanceDate + (6*30).days

    val dataOverwrites = mapOf(
            "issuedOn" to issuanceDate.toString(),
            "expires" to expirationDate.toString(),
    ).toJsonObject()
            //.mapValues { it.value.toJsonElement() }

//    val jwt = runBlocking {
//        vc.baseIssue(
//                key = IssuerKey.instance,
//                did = issuerDid,
//                subject = holderDid,
//                dataOverwrites = emptyMap(),
//                dataUpdates = emptyMap(),
//                additionalJwtHeader = emptyMap(),
//                additionalJwtOptions = emptyMap())
//    }

    val jwt = runBlocking {
        vc.mergingJwtIssue(
                issuerKey = IssuerKey.instance,
                issuerDid = issuerDid,
                subjectDid = holderDid,
                mappings = dataOverwrites,
                additionalJwtHeader = emptyMap(),
                additionalJwtOptions = emptyMap(),
        )
    }

    return jwt
}

private fun presentSDJwt(jwt : String) {
    // parse previously created SD-JWT
    val sdJwt = SDJwt.parse(jwt)

    // present without disclosing SD fields
    val presentedUndisclosedJwt = sdJwt.present(discloseAll = false)
    println("------------------------")
    println("Undisclosed")
    println(presentedUndisclosedJwt)

    // present disclosing all SD fields
    val presentedDisclosedJwt = sdJwt.present(discloseAll = true)
    println("------------------------")
    println("Disclosed")
    println(presentedDisclosedJwt)

    // present disclosing selective fields, using SDMap
    val presentedSelectiveJwt = sdJwt.present(mapOf(
            "context" to SDField(true)
    ).toSDMap())
    println("------------------------")
    println("Selective")
    println(presentedSelectiveJwt)

    // present disclosing fields, using JSON paths
    val presentedSelectiveJwt2 = sdJwt.present(
            SDMap.generateSDMap(listOf("id"))
    )
    println("------------------------")
    println("Selective with JSON")
    println(presentedSelectiveJwt2)

}

private fun verifyVC(jwt : String) {

    val sharedSecret = "ef23f749-7238-481a-815c-f0c2157dfa8e"

    // Create SimpleJWTCryptoProvider with MACSigner and MACVerifier
    val cryptoProvider = SimpleJWTCryptoProvider(JWSAlgorithm.HS256, jwsSigner = null, jwsVerifier = MACVerifier(sharedSecret))

//    val undisclosedJwt = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI0NTYiLCJfc2QiOlsiaGx6ZmpmMDRvNVpzTFIyNWhhNGMtWS05SFcyRFVseGNnaU1ZZDMyNE5nWSJdfQ.2fsLqzujWt0hS0peLS8JLHyyo3D5KCDkNnHcBYqQwVo~"

    // verify and parse presented SD-JWT with all fields undisclosed, throws Exception if verification fails!
    val parsedVerifiedUndisclosedJwt = SDJwt.verifyAndParse(jwt, cryptoProvider)

    // print full payload with disclosed fields only
    println("Undisclosed JWT payload:")
    println(parsedVerifiedUndisclosedJwt.sdJwt.fullPayload)
//    println(parsedVerifiedUndisclosedJwt.sdPayload.fullPayload.toString())

    // alternatively parse and verify in 2 steps:
    val parsedUndisclosedJwt = SDJwt.parse(jwt)
    val isValid = parsedUndisclosedJwt.verify(cryptoProvider)
    println("Undisclosed SD-JWT verified: $isValid")

    val parsedVerifiedDisclosedJwt = SDJwt.verifyAndParse(
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI0NTYiLCJfc2QiOlsiaGx6ZmpmMDRvNVpzTFIyNWhhNGMtWS05SFcyRFVseGNnaU1ZZDMyNE5nWSJdfQ.2fsLqzujWt0hS0peLS8JLHyyo3D5KCDkNnHcBYqQwVo~WyJ4RFk5VjBtOG43am82ZURIUGtNZ1J3Iiwic3ViIiwiMTIzIl0~",
            cryptoProvider
    )
    // print full payload with disclosed fields
    println("Disclosed JWT payload:")
//    println(parsedVerifiedDisclosedJwt.sdPayload.fullPayload.toString())
    println(parsedVerifiedDisclosedJwt.sdJwt.fullPayload.toString())
}
