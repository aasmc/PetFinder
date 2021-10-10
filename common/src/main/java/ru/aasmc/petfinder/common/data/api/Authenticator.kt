package ru.aasmc.petfinder.common.data.api

import android.util.Base64
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec

class Authenticator {

    private val publicKey: PublicKey
    private val privateKey: PrivateKey

    init {
        // generates public and private keys with Elliptic Curve cryptography algorithms.
        val keyPairGenerator = KeyPairGenerator.getInstance("EC")
        // 256 bytes is the recommended size
        keyPairGenerator.initialize(256)
        val keyPair = keyPairGenerator.genKeyPair()

        publicKey = keyPair.public
        privateKey = keyPair.private
    }

    fun sign(data: ByteArray): ByteArray {
        // use elliptic curve digital signature algorithm (ECDSA)
        val signature = Signature.getInstance("SHA512withECDSA")
        signature.initSign(privateKey)
        signature.update(data)
        return signature.sign()
    }


    fun verify(signature: ByteArray, data: ByteArray): Boolean {
        val verifySignature = Signature.getInstance("SHA512withECDSA")
        verifySignature.initVerify(publicKey)
        verifySignature.update(data)
        return verifySignature.verify(signature)
    }


    fun verify(signature: ByteArray, data: ByteArray, publicKeyString: String): Boolean {
        val verifySignature = Signature.getInstance("SHA512withECDSA")
        // convert a Base64 public key string into a PublicKey object
        val bytes = Base64.decode(publicKeyString, Base64.NO_WRAP)
        val publicKey = KeyFactory.getInstance("EC").generatePublic(X509EncodedKeySpec(bytes))
        // initialize the signature with the public key for verification
        verifySignature.initVerify(publicKey)
        verifySignature.update(data)
        // performs the verification. returns true if the verification succeeds
        return verifySignature.verify(signature)
    }

    fun publicKey(): String {
        return Base64.encodeToString(publicKey.encoded, Base64.NO_WRAP)
    }
}


























