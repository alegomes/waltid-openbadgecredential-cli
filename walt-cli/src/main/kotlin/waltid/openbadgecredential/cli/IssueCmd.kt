package waltid.openbadgecredential.cli

import picocli.CommandLine.*
import picocli.CommandLine.Model.CommandSpec

import id.walt.credentials.CredentialBuilderType
import id.walt.credentials.CredentialBuilder
import id.walt.credentials.CredentialBuilder.*
import id.walt.crypto.utils.JsonUtils.toJsonObject
import id.walt.crypto.utils.JsonUtils.toJsonElement
import id.walt.crypto.keys.LocalKey
import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.Key

import id.walt.credentials.issuance.Issuer.baseIssue

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.coroutines.runBlocking

// import kotlinx.serialization.json.jsonObject

@Command(
        name = "issue",
        mixinStandardHelpOptions = true,
        description = ["Issues a verifiable credential"]
)
class IssueCmd : Runnable {

    @Spec var spec: CommandSpec? = null

    override fun run() {

        // Create VC document
        val entityIdentificationNumber = "1234"
        val issuingAuthorityId = "abcd"
        val proofType = "document"
        val proofLocation = "Brasilia/BR"

        val credentialBuilder = CredentialBuilderType.W3CV2CredentialBuilder
        val credentialSubject = mapOf(
            "entityIdentification" to entityIdentificationNumber,
            "issuingAuthority" to issuingAuthorityId,
            "issuingCircumstances" to
                    mapOf("proofType" to proofType,
                          "locationType" to "physicalLocation",
                          "location" to proofLocation)).toJsonObject()
        val w3cCredential = CredentialBuilder(credentialBuilder)
                            .apply { useCredentialSubject(credentialSubject) }
                            .buildW3C()

//        println("VC created: "+w3cCredential)

        // Issue VC
        // addContext("https://www.w3.org/ns/credentials/examples/v2") // [W3CV2 VC context, custom context]
        // addType("MyCustomCredential") // [VerifiableCredential, MyCustomCredential]
                               
        // randomCredentialSubjectUUID() 

        lateinit var key : Key

        runBlocking {
            key = LocalKey.generate(KeyType.Ed25519)
        }

        // generate an asymmetric key of type EdDSA ED25519
        // val keyId = keyService.generate(KeyAlgorithm.EdDSA_Ed25519)

        val issuerKey = key
        val issuerDid = "did:key:issr"    // DidService.create(DidMethod.ebsi)
        val subjectDid = "did:key:sbj"    // DidService.create(DidMethod.key)
        val holderDid = "did:key:hodlr"

        // validFromNow()
        // validFor(90.days)

        // useStatusList2021Revocation("https://university.example/credentials/status/3", 94567)

        val dataOverwrites = mapOf("entityIdentification" to entityIdentificationNumber.toJsonElement())
        val dataUpdates = mapOf("issuingAuthority" to mapOf("issuingAuthority" to issuingAuthorityId.toJsonElement()))

        runBlocking {
            val jwt = w3cCredential.baseIssue(
                key = issuerKey,
                did = issuerDid,
                subject = holderDid,
                dataOverwrites = emptyMap(),
                dataUpdates = emptyMap(),
                additionalJwtHeader = emptyMap(),
                additionalJwtOptions = emptyMap(),
        )

            println("VC issued: " + jwt)
        }


    }
}
