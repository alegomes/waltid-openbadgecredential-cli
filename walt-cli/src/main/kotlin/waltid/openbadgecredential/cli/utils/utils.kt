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
    ).mapValues { it.value.toJsonElement() }

    val jwt = runBlocking {
        vc.baseIssue(
                key = IssuerKey.instance,
                did = issuerDid,
                subject = holderDid,
                dataOverwrites = emptyMap(),
                dataUpdates = emptyMap(),
                additionalJwtHeader = emptyMap(),
                additionalJwtOptions = emptyMap())
    }

    return jwt
}

