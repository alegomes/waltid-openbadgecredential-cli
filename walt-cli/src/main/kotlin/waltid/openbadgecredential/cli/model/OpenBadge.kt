package waltid.openbadgecredential.cli.model

import id.walt.crypto.utils.JsonUtils.toJsonObject

import kotlin.time.Duration.Companion.days
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID
import kotlinx.datetime.Clock

class OpenBadge(val issuerProfile : Profile) {


    fun fakeIt() : Map<String, Any> {

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
                "id" to this.issuerProfile.did,
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
}