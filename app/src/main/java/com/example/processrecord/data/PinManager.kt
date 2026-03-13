package com.example.processrecord.data

import android.content.Context
import androidx.core.content.edit
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class PinManager(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    val isPinEnabled: Boolean
        get() = prefs.getString(KEY_PIN_HASH, null) != null

    fun setPin(pin: String) {
        val salt = generateSalt()
        val hash = deriveKey(pin, salt)
        prefs.edit {
            putString(KEY_SALT, salt.toHex())
            putString(KEY_PIN_HASH, hash)
        }
    }

    fun verifyPin(pin: String): Boolean {
        val storedHash = prefs.getString(KEY_PIN_HASH, null) ?: return false
        val storedSalt = prefs.getString(KEY_SALT, null)?.fromHex() ?: return false
        return deriveKey(pin, storedSalt) == storedHash
    }

    fun removePin() {
        prefs.edit {
            remove(KEY_PIN_HASH)
            remove(KEY_SALT)
        }
    }

    private fun generateSalt(): ByteArray {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        return salt
    }

    private fun deriveKey(pin: String, salt: ByteArray): String {
        val spec = PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return factory.generateSecret(spec).encoded.toHex()
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    private fun String.fromHex(): ByteArray {
        val len = length
        val data = ByteArray(len / 2)
        for (i in 0 until len / 2) {
            data[i] = ((Character.digit(this[i * 2], 16) shl 4) + Character.digit(this[i * 2 + 1], 16)).toByte()
        }
        return data
    }

    companion object {
        private const val PREFS_NAME = "pin_auth_prefs"
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_SALT = "pin_salt"
        private const val ITERATIONS = 10_000
        private const val KEY_LENGTH = 256
    }
}
