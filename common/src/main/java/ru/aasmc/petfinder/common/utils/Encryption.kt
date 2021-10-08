package ru.aasmc.petfinder.common.utils

import android.annotation.TargetApi
import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import androidx.security.crypto.MasterKeys
import ru.aasmc.petfinder.common.data.preferences.Preferences
import java.io.File
import java.security.KeyStore
import java.security.SecureRandom
import java.util.HashMap
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class Encryption {
    companion object {

        private const val KEYSTORE_ALIAS = "PetSaveLoginKey"
        private const val PROVIDER = "AndroidKeyStore"

        private fun getSecretKey(): SecretKey {
            val keyStore = KeyStore.getInstance(PROVIDER)

            // before the keystore can be accessed it must be loaded
            keyStore.load(null)
            return keyStore.getKey(KEYSTORE_ALIAS, null) as SecretKey
        }

        @TargetApi(23)
        private fun getCipher(): Cipher {
            return Cipher.getInstance(
                KeyProperties.KEY_ALGORITHM_AES + "/"
                        + KeyProperties.BLOCK_MODE_GCM + "/"
                        + KeyProperties.ENCRYPTION_PADDING_NONE
            )
        }

        @TargetApi(23)
        fun generateSecretKey() {
            val keyGeneratorParameterSpec = KeyGenParameterSpec.Builder(
                KEYSTORE_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setUserAuthenticationRequired(true)
                .setUserAuthenticationValidityDurationSeconds(120)
                .build()

            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, PROVIDER
            )

            keyGenerator.init(keyGeneratorParameterSpec)
            keyGenerator.generateKey()
        }

        fun createLoginPassword(preferences: Preferences): ByteArray {
            val cipher = getCipher()
            val secretKey = getSecretKey()
            val random = SecureRandom()
            val passwordBytes = ByteArray(256)
            // create a random password using SecureRandom
            random.nextBytes(passwordBytes)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val ivParameters = cipher.parameters.getParameterSpec(GCMParameterSpec::class.java)
            // randomized initialization vector, needed to decrypt the data, and save it to the shared preferences
            val iv = ivParameters.iv
            preferences.saveIV(iv)
            return cipher.doFinal(passwordBytes)
        }

        fun decryptPassword(password: ByteArray, preferences: Preferences): ByteArray {
            val cipher = getCipher()
            val secretKey = getSecretKey()
            val iv = preferences.iv()
            val ivParams = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParams)
            return cipher.doFinal(password)
        }

        @TargetApi(23)
        fun encryptFile(context: Context, file: File): EncryptedFile {
            val masterKey = MasterKey.Builder(context.applicationContext)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            return EncryptedFile.Builder(
                context,
                file,
                masterKey,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build()
        }

        fun encrypt(
            dataToEncrypt: ByteArray,
            password: CharArray
        ): HashMap<String, ByteArray> {
            val map = HashMap<String, ByteArray>()

            //TODO: Add custom encrypt code here

            return map
        }

        fun decrypt(map: HashMap<String, ByteArray>, password: CharArray): ByteArray? {

            var decrypted: ByteArray? = null

            //TODO: Add custom decrypt code here

            return decrypted
        }

        //NOTE: Here's a keystore version of the encryption for your reference :]
        private fun keystoreEncrypt(dataToEncrypt: ByteArray): HashMap<String, ByteArray> {
            val map = HashMap<String, ByteArray>()
            try {

                //Get the key
                val keyStore = KeyStore.getInstance("AndroidKeyStore")
                keyStore.load(null)

                val secretKeyEntry =
                    keyStore.getEntry("MyKeyAlias", null) as KeyStore.SecretKeyEntry
                val secretKey = secretKeyEntry.secretKey

                //Encrypt data
                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                cipher.init(Cipher.ENCRYPT_MODE, secretKey)
                val ivBytes = cipher.iv
                val encryptedBytes = cipher.doFinal(dataToEncrypt)

                map["iv"] = ivBytes
                map["encrypted"] = encryptedBytes
            } catch (e: Throwable) {
                e.printStackTrace()
            }

            return map
        }

        private fun keystoreDecrypt(map: HashMap<String, ByteArray>): ByteArray? {
            var decrypted: ByteArray? = null
            try {

                //Get the key
                val keyStore = KeyStore.getInstance("AndroidKeyStore")
                keyStore.load(null)

                val secretKeyEntry =
                    keyStore.getEntry("MyKeyAlias", null) as KeyStore.SecretKeyEntry
                val secretKey = secretKeyEntry.secretKey

                //Extract info from map
                val encryptedBytes = map["encrypted"]
                val ivBytes = map["iv"]

                //Decrypt data
                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                val spec = GCMParameterSpec(128, ivBytes)
                cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
                decrypted = cipher.doFinal(encryptedBytes)
            } catch (e: Throwable) {
                e.printStackTrace()
            }

            return decrypted
        }

        @TargetApi(23)
        fun keystoreTest() {

            val keyGenerator =
                KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                "MyKeyAlias",
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                //.setUserAuthenticationRequired(true) // requires lock screen, invalidated if lock screen is disabled
                //.setUserAuthenticationValidityDurationSeconds(120) // only available x seconds from password authentication. -1 requires finger print - every time
                .setRandomizedEncryptionRequired(true) // different ciphertext for same plaintext on each call
                .build()
            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()

            val map = keystoreEncrypt("My very sensitive string!".toByteArray(Charsets.UTF_8))
            val decryptedBytes = keystoreDecrypt(map)
            decryptedBytes?.let {
                val decryptedString = String(it, Charsets.UTF_8)
                Log.e("MyApp", "The decrypted string is: $decryptedString")
            }
        }

    }
}