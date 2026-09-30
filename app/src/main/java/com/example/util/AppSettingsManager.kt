package com.example.util

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AppSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppSettingsManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun loadSettings(): AppSettings {
        return AppSettings(
            isDarkUi = prefs.getBoolean(KEY_DARK_UI, true),
            isCompactMode = prefs.getBoolean(KEY_COMPACT_MODE, false),
            confirmBeforeSession = prefs.getBoolean(KEY_CONFIRM_BEFORE_SESSION, true),
            autoRestore = prefs.getBoolean(KEY_AUTO_RESTORE, true),
            rememberLastProfile = prefs.getBoolean(KEY_REMEMBER_LAST_PROFILE, true),
            lastSelectedPackage = prefs.getString(KEY_LAST_SELECTED_PACKAGE, null)
        )
    }

    fun setCompactMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_COMPACT_MODE, enabled).apply()
        _settings.value = _settings.value.copy(isCompactMode = enabled)
    }

    fun setConfirmBeforeSession(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_CONFIRM_BEFORE_SESSION, enabled).apply()
        _settings.value = _settings.value.copy(confirmBeforeSession = enabled)
    }

    fun setAutoRestore(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_RESTORE, enabled).apply()
        _settings.value = _settings.value.copy(autoRestore = enabled)
    }

    fun setRememberLastProfile(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REMEMBER_LAST_PROFILE, enabled).apply()
        _settings.value = _settings.value.copy(rememberLastProfile = enabled)
    }

    fun setLastSelectedPackage(packageName: String?) {
        prefs.edit().putString(KEY_LAST_SELECTED_PACKAGE, packageName).apply()
        _settings.value = _settings.value.copy(lastSelectedPackage = packageName)
    }

    fun resetToDefaults() {
        prefs.edit()
            .putBoolean(KEY_DARK_UI, true)
            .putBoolean(KEY_COMPACT_MODE, false)
            .putBoolean(KEY_CONFIRM_BEFORE_SESSION, true)
            .putBoolean(KEY_AUTO_RESTORE, true)
            .putBoolean(KEY_REMEMBER_LAST_PROFILE, true)
            .remove(KEY_LAST_SELECTED_PACKAGE)
            .apply()
        _settings.value = AppSettings()
    }

    companion object {
        private const val PREFS_NAME = "sensiv_app_settings"
        private const val KEY_DARK_UI = "dark_ui"
        private const val KEY_COMPACT_MODE = "compact_mode"
        private const val KEY_CONFIRM_BEFORE_SESSION = "confirm_before_session"
        private const val KEY_AUTO_RESTORE = "auto_restore"
        private const val KEY_REMEMBER_LAST_PROFILE = "remember_last_profile"
        private const val KEY_LAST_SELECTED_PACKAGE = "last_selected_package"
    }
}
