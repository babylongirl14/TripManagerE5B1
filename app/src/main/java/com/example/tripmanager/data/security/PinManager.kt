package com.example.tripmanager.data.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class PinManager(context: Context) {
    private val sharedPreferences: SharedPreferences
    
    companion object {
        private const val PREF_FILE_NAME = "pin_prefs"
        private const val PIN_KEY = "secure_pin"
        private const val PIN_ATTEMPTS_KEY = "pin_attempts"
        private const val MAX_ATTEMPTS = 3
    }

    init {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        sharedPreferences = EncryptedSharedPreferences.create(
            PREF_FILE_NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun savePin(pin: String) {
        sharedPreferences.edit().putString(PIN_KEY, pin).apply()
        resetAttempts()
    }

    fun getPin(): String? {
        return sharedPreferences.getString(PIN_KEY, null)
    }

    fun hasPin(): Boolean {
        return getPin() != null
    }

    fun clearPin() {
        sharedPreferences.edit().remove(PIN_KEY).apply()
        resetAttempts()
    }

    fun recordAttempt() {
        val attempts = sharedPreferences.getInt(PIN_ATTEMPTS_KEY, 0)
        sharedPreferences.edit().putInt(PIN_ATTEMPTS_KEY, attempts + 1).apply()
    }

    fun getAttempts(): Int {
        return sharedPreferences.getInt(PIN_ATTEMPTS_KEY, 0)
    }

    fun resetAttempts() {
        sharedPreferences.edit().putInt(PIN_ATTEMPTS_KEY, 0).apply()
    }

    fun isLocked(): Boolean {
        return getAttempts() >= MAX_ATTEMPTS
    }
}