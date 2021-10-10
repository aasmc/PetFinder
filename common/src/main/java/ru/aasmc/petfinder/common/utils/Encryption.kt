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
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

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
            try {
                // a cryptographically strong random number generator
                val random = SecureRandom()
                // random data used for hashing with password to create a key
                val salt = ByteArray(256)
                random.nextBytes(salt)

                // PBKDF2 - derive the key from the password, don't use passwords directly
                // the higher the iteration number (1324 here), the longer it would take to operate on a set
                // of keys during a brute force attack.
                val pbKeSpec = PBEKeySpec(password, salt, 1324, 256)
                val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
                val keyBytes = secretKeyFactory.generateSecret(pbKeSpec).encoded
                val keySpec = SecretKeySpec(keyBytes, "AES")

                // Create an initialization vector to XOR it with the first chunk of data
                // that is going to be ciphered. Here we use a CBC (cipher block chaining) mode,
                // that implies that we split data into chunks and XOR each chunk with the preceding one,
                // but to enable proper cipher we need to XOR the first chunk also, and here comes
                // initialization vector - some random data to be used for XOR with the first chunk.
                val ivRandom = SecureRandom()
                val iv = ByteArray(16)
                ivRandom.nextBytes(iv)
                val ivSpec = IvParameterSpec(iv)

                // finally encrypt
                val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
                cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
                val encrypted = cipher.doFinal(dataToEncrypt)

                // package the encrypted data to the hashMap
                // we need salt and iv to decrypt it
                map["salt"] = salt
                map["iv"] = iv
                map["encrypted"] = encrypted
            } catch (e: Exception) {
                Log.e("PetFinderApp", "encryption exception", e)
            }
            return map
        }

        fun decrypt(map: HashMap<String, ByteArray>, password: CharArray): ByteArray? {

            var decrypted: ByteArray? = null

            try {
                val salt = map["salt"]
                val iv = map["iv"]
                val encrypted = map["encrypted"]

                // regenerate key from password
                val pbKeySpec = PBEKeySpec(password, salt, 1324, 256)
                val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
                val keyBytes = secretKeyFactory.generateSecret(pbKeySpec).encoded
                val keySpec = SecretKeySpec(keyBytes, "AES")

                // decrypt
                val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
                val ivSpec = IvParameterSpec(iv)
                cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
                decrypted = cipher.doFinal(encrypted)
            } catch (e: Exception) {
                Log.e("PetFinderApp", "decryption exception", e)
            }
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