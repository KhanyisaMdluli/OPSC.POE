package com.opsc.solowork_1.utils

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.*

class LanguageManager(private val context: Context) {

    companion object {
        const val PREF_LANGUAGE = "app_language"
        const val DEFAULT_LANGUAGE = "en"
    }

    private val preferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun setAppLanguage(languageCode: String) {
        preferences.edit().putString(PREF_LANGUAGE, languageCode).apply()
        updateLanguage(languageCode)
    }

    fun getCurrentLanguage(): String {
        return preferences.getString(PREF_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
    }

    fun updateLanguage(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val resources = context.resources
        val configuration = Configuration(resources.configuration)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale)
        } else {
            configuration.locale = locale
        }

        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

    fun applySavedLanguage(activity: Activity) {
        val savedLanguage = getCurrentLanguage()
        updateLanguage(savedLanguage)
    }
}
