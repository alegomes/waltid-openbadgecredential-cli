package waltid.openbadgecredential.cli.model

import id.walt.crypto.keys.*
import id.walt.did.dids.DidService
import kotlinx.coroutines.runBlocking

class Profile {

    val key : Key by lazy {
        runBlocking {
            LocalKey.generate(KeyType.Ed25519) // Ed25519 is the EdDSA signature scheme using SHA-512 (SHA-2) and Curve25519
//            LocalKey.generate(KeyType.RSA)
//            LocalKey.generate(KeyType.secp256k1)

//            val tseMetadata = TSEKeyMetadata("http://127.0.0.1:8200/v1/transit", "dev-only-token")
//            TSEKey.generate(KeyType.Ed25519, tseMetadata)
        }
    }

    val did by lazy {
        runBlocking {
            DidService.registerByKey("key", key).did
        }
    }
}