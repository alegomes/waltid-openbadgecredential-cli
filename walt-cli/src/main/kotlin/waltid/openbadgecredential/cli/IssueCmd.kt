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
import id.walt.credentials.vc.vcs.W3CVC

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

        val entityIdentificationNumber = "1234"
        val issuingAuthorityId = "abcd"
        val proofType = "document"
        val proofLocation = "Brasilia/BR"

        lateinit var key: Key

        runBlocking {
            key = LocalKey.generate(KeyType.Ed25519)
        }

        // generate an asymmetric key of type EdDSA ED25519
        // val keyId = keyService.generate(KeyAlgorithm.EdDSA_Ed25519)

        val issuerKey = key
        val issuerDid = "did:key:issr"    // DidService.create(DidMethod.ebsi)
        val subjectDid = "did:key:sbj"    // DidService.create(DidMethod.key)
        val holderDid = "did:key:hodlr"

        val w3cCredential = createVC(entityIdentificationNumber, issuingAuthorityId, proofType, proofLocation)

        // Issue VC
        val issuedVC = issueVC(entityIdentificationNumber, issuingAuthorityId, w3cCredential, issuerKey, issuerDid, subjectDid, holderDid)

        println("Issued VC: $issuedVC")

    }

    private fun issueVC(entityIdentificationNumber: String,
                        issuingAuthorityId: String,
                        w3cCredential: W3CVC,
                        issuerKey : Key,
                        issuerDid : String,
                        subjectDid : String,
                        holderDid : String): String {

        // addContext("https://www.w3.org/ns/credentials/examples/v2") // [W3CV2 VC context, custom context]
        // addType("MyCustomCredential") // [VerifiableCredential, MyCustomCredential]
        // randomCredentialSubjectUUID()
        // validFromNow()
        // validFor(90.days)
        // useStatusList2021Revocation("https://university.example/credentials/status/3", 94567)

        val dataOverwrites = mapOf("entityIdentification" to entityIdentificationNumber.toJsonElement())
        val dataUpdates = mapOf("issuingAuthority" to mapOf("issuingAuthority" to issuingAuthorityId.toJsonElement()))

        var jwt : String = ""
        runBlocking {
            jwt = w3cCredential.baseIssue(
                    key = issuerKey,
                    did = issuerDid,
                    subject = holderDid,
                    dataOverwrites = emptyMap(),
                    dataUpdates = emptyMap(),
                    additionalJwtHeader = emptyMap(),
                    additionalJwtOptions = emptyMap(),
            )
        }

        return jwt
    }

    private fun createVC(entityIdentificationNumber : String,
                         issuingAuthorityId : String,
                         proofType : String,
                         proofLocation : String): W3CVC {
        // Create VC document

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

        return w3cCredential
    }
}
