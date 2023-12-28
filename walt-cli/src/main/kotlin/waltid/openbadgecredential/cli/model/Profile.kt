package waltid.openbadgecredential.cli.model

import id.walt.crypto.keys.Key
import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.LocalKey
import id.walt.did.dids.DidService
import kotlinx.coroutines.runBlocking

class Profile {

    val key : Key by lazy {
        runBlocking {
            LocalKey.generate(KeyType.Ed25519)
        }
    }

    val did by lazy {
        runBlocking {
            DidService.registerByKey("key", key).did
        }
    }
}