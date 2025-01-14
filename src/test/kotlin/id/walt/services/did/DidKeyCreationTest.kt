package id.walt.services.did

import id.walt.crypto.KeyAlgorithm
import id.walt.crypto.KeyId
import id.walt.model.DidMethod
import id.walt.servicematrix.ServiceMatrix
import id.walt.services.crypto.CryptoService
import io.kotest.core.spec.style.StringSpec

class DidKeyCreationTest : StringSpec({
    ServiceMatrix("service-matrix.properties")

    val cryptoService = CryptoService.getService()

    fun createAndLoadDid(key: KeyId) {
        val did = DidService.create(DidMethod.key, key.id)
        println("Created: $did")

        val loaded = DidService.load(did)
        println("Loaded: $loaded")
    }

    "Create default did:key" {
        val did = DidService.create(DidMethod.key)
        println("Created: $did")

        val loaded = DidService.load(did)
        println("Loaded: $loaded")
    }

    "Create EdDSA_Ed25519 did:key" {
        val key = cryptoService.generateKey(KeyAlgorithm.EdDSA_Ed25519)
        createAndLoadDid(key)
    }

    "Create ECDSA_Secp256r1 did:key" {
        val key = cryptoService.generateKey(KeyAlgorithm.ECDSA_Secp256r1)
        createAndLoadDid(key)
    }
    "Create ECDSA_Secp256k1 did:key" {
        val key = cryptoService.generateKey(KeyAlgorithm.ECDSA_Secp256k1)
        createAndLoadDid(key)
    }

    "Create RSA did:key" {
        val key = cryptoService.generateKey(KeyAlgorithm.RSA)
        createAndLoadDid(key)
    }


})

