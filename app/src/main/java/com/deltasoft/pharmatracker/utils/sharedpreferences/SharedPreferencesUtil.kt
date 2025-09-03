package com.deltasoft.pharmatracker.utils.sharedpreferences

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesUtil(context: Context) {

    // Define a constant for the SharedPreferences file name.
    companion object {
        private const val PREFS_NAME = "my_app_prefs"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)



    /**
     * Saves a string value to SharedPreferences.
     * @param key The key to store the value under.
     * @param value The string value to save.
     */
    fun saveString(key: PrefsKey, value: String) {
        prefs.edit().putString(key.name, value).apply()
    }

    /**
     * Retrieves a string value from SharedPreferences.
     * @param key The key of the value to retrieve.
     * @param defaultValue The default value to return if the key is not found.
     * @return The retrieved string value or the default value.
     */
    fun getString(key: PrefsKey, defaultValue: String = ""): String {
        return prefs.getString(key.name, defaultValue) ?: defaultValue
    }

    /**
     * Saves an integer value to SharedPreferences.
     * @param key The key to store the value under.
     * @param value The integer value to save.
     */
    fun saveInt(key: PrefsKey, value: Int) {
        prefs.edit().putInt(key.name, value).apply()
    }

    /**
     * Retrieves an integer value from SharedPreferences.
     * @param key The key of the value to retrieve.
     * @param defaultValue The default value to return if the key is not found.
     * @return The retrieved integer value or the default value.
     */
    fun getInt(key: PrefsKey, defaultValue: Int = 0): Int {
        return prefs.getInt(key.name, defaultValue)
    }

    /**
     * Saves a boolean value to SharedPreferences.
     * @param key The key to store the value under.
     * @param value The boolean value to save.
     */
    fun saveBoolean(key: PrefsKey, value: Boolean) {
        prefs.edit().putBoolean(key.name, value).apply()
    }

    /**
     * Retrieves a boolean value from SharedPreferences.
     * @param key The key of the value to retrieve.
     * @param defaultValue The default value to return if the key is not found.
     * @return The retrieved boolean value or the default value.
     */
    fun getBoolean(key: PrefsKey, defaultValue: Boolean = false): Boolean {
        return prefs.getBoolean(key.name, defaultValue)
    }

    /**
     * Saves a long value to SharedPreferences.
     * @param key The key to store the value under.
     * @param value The long value to save.
     */
    fun saveLong(key: PrefsKey, value: Long) {
        prefs.edit().putLong(key.name, value).apply()
    }

    /**
     * Retrieves a long value from SharedPreferences.
     * @param key The key of the value to retrieve.
     * @param defaultValue The default value to return if the key is not found.
     * @return The retrieved long value or the default value.
     */
    fun getLong(key: PrefsKey, defaultValue: Long = 0L): Long {
        return prefs.getLong(key.name, defaultValue)
    }

    /**
     * Clears all data from SharedPreferences.
     */
    fun clearAll() {
        prefs.edit().clear().apply()
    }
}