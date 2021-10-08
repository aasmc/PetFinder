package ru.aasmc.petfinder.common.data.preferences

import ru.aasmc.petfinder.common.data.preferences.PreferencesConstants.KEY_MAX_DISTANCE
import ru.aasmc.petfinder.common.data.preferences.PreferencesConstants.KEY_POSTCODE

class FakePreferences: Preferences {

    private val preferences = mutableMapOf<String, Any>()

    override fun putToken(token: String) {
        preferences[PreferencesConstants.KEY_TOKEN] = token
    }

    override fun putTokenExpirationTime(time: Long) {
        preferences[PreferencesConstants.KEY_TOKEN_EXPIRATION_TIME] = time
    }

    override fun putTokenType(tokenType: String) {
        preferences[PreferencesConstants.KEY_TOKEN_TYPE] = tokenType
    }

    override fun getToken(): String {
        return preferences[PreferencesConstants.KEY_TOKEN] as String
    }

    override fun getTokenExpirationTime(): Long {
        return preferences[PreferencesConstants.KEY_TOKEN_EXPIRATION_TIME] as Long
    }

    override fun getTokenType(): String {
        return preferences[PreferencesConstants.KEY_TOKEN_TYPE] as String
    }

    override fun deleteTokenInfo() {
        preferences.clear()
    }


    override fun getPostcode(): String {
        return preferences[KEY_POSTCODE] as String
    }

    override fun putPostcode(postcode: String) {
        preferences[KEY_POSTCODE] = postcode
    }

    override fun getMaxDistanceAllowedToGetAnimals(): Int {
        return preferences[KEY_MAX_DISTANCE] as Int
    }

    override fun putMaxDistanceAllowedToGetAnimals(distance: Int) {
        preferences[KEY_MAX_DISTANCE] = distance
    }

    override fun putLastLoggedInTime() {

    }

    override fun getLastLoggedIn(): String? {
        return null
    }

    override fun iv(): ByteArray {
        return ByteArray(0)
    }

    override fun saveIV(iv: ByteArray) {
        // not implemented
    }

}