package ru.aasmc.petfinder.common.data.preferences

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import ru.aasmc.petfinder.common.data.preferences.PreferencesConstants.KEY_IV
import ru.aasmc.petfinder.common.data.preferences.PreferencesConstants.KEY_LAST_LOGIN
import ru.aasmc.petfinder.common.data.preferences.PreferencesConstants.KEY_MAX_DISTANCE
import ru.aasmc.petfinder.common.data.preferences.PreferencesConstants.KEY_POSTCODE
import ru.aasmc.petfinder.common.data.preferences.PreferencesConstants.KEY_TOKEN
import ru.aasmc.petfinder.common.data.preferences.PreferencesConstants.KEY_TOKEN_EXPIRATION_TIME
import ru.aasmc.petfinder.common.data.preferences.PreferencesConstants.KEY_TOKEN_TYPE
import java.text.DateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PetSavePreferences @Inject constructor(
    @ApplicationContext context: Context
) : Preferences {
    companion object {
        const val PREFERENCES_NAME = "PET_FINDER_PREFERENCES"
    }

    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    override fun putToken(token: String) {
        edit { putString(KEY_TOKEN, token) }
    }

    override fun putTokenExpirationTime(time: Long) {
        edit { putLong(KEY_TOKEN_EXPIRATION_TIME, time) }
    }

    override fun putTokenType(tokenType: String) {
        edit { putString(KEY_TOKEN_TYPE, tokenType) }
    }

    override fun getToken(): String {
        return preferences.getString(KEY_TOKEN, "").orEmpty()
    }

    override fun getTokenExpirationTime(): Long {
        return preferences.getLong(KEY_TOKEN_EXPIRATION_TIME, -1)
    }

    override fun getTokenType(): String {
        return preferences.getString(KEY_TOKEN_TYPE, "").orEmpty()
    }

    override fun deleteTokenInfo() {
        edit {
            remove(KEY_TOKEN)
            remove(KEY_TOKEN_EXPIRATION_TIME)
            remove(KEY_TOKEN_TYPE)
        }
    }

    override fun getPostcode(): String {
        return preferences.getString(KEY_POSTCODE, "").orEmpty()
    }

    override fun putPostcode(postcode: String) {
        edit { putString(KEY_POSTCODE, postcode) }
    }

    override fun getMaxDistanceAllowedToGetAnimals(): Int {
        return preferences.getInt(KEY_MAX_DISTANCE, 0)
    }

    override fun putMaxDistanceAllowedToGetAnimals(distance: Int) {
        edit { putInt(KEY_MAX_DISTANCE, distance) }
    }

    override fun putLastLoggedInTime() {
        val currentDateTimeString = DateFormat.getDateTimeInstance().format(Date())
        edit { putString(KEY_LAST_LOGIN, currentDateTimeString) }
    }

    override fun getLastLoggedIn(): String? {
        return preferences.getString(KEY_LAST_LOGIN, null)
    }

    override fun iv(): ByteArray {
        val base64Iv = preferences.getString(KEY_IV, "")
        return Base64.decode(base64Iv, Base64.NO_WRAP)
    }

    override fun saveIV(iv: ByteArray) {
        val ivString = Base64.encodeToString(iv, Base64.NO_WRAP)
        edit {
            putString(KEY_IV, ivString)
        }
    }

    private inline fun edit(block: SharedPreferences.Editor.() -> Unit) {
        with(preferences.edit()) {
            block()
            commit()
        }
    }
}


















