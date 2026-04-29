package com.moneyvisor.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
    private val CHART_INTERVAL = stringPreferencesKey("chart_interval")
    private val CURRENCY_CODE = stringPreferencesKey("currency_code")
    private val IS_BIOMETRIC_ENABLED = booleanPreferencesKey("is_biometric_enabled")
    private val IS_PRIVACY_MODE_ENABLED = booleanPreferencesKey("is_privacy_mode_enabled")
    private val THEME_MODE = stringPreferencesKey("theme_mode")
    private val IS_FAB_ENABLED = booleanPreferencesKey("is_fab_enabled")

    val isFirstLaunch: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_FIRST_LAUNCH] ?: true
    }

    val chartInterval: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CHART_INTERVAL] ?: "WEEKLY"
    }

    val currencyCode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CURRENCY_CODE] ?: "USD"
    }

    val isBiometricEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_BIOMETRIC_ENABLED] ?: false
    }

    val isPrivacyModeEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_PRIVACY_MODE_ENABLED] ?: false
    }

    val themeMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[THEME_MODE] ?: "SYSTEM"
    }

    val isFabEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_FAB_ENABLED] ?: true
    }

    suspend fun setFirstLaunchCompleted() {
        context.dataStore.edit { preferences ->
            preferences[IS_FIRST_LAUNCH] = false
        }
    }

    suspend fun setChartInterval(interval: String) {
        context.dataStore.edit { preferences ->
            preferences[CHART_INTERVAL] = interval
        }
    }

    suspend fun setCurrencyCode(code: String) {
        context.dataStore.edit { preferences ->
            preferences[CURRENCY_CODE] = code
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_BIOMETRIC_ENABLED] = enabled
        }
    }

    suspend fun setPrivacyModeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_PRIVACY_MODE_ENABLED] = enabled
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode
        }
    }

    suspend fun setFabEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_FAB_ENABLED] = enabled
        }
    }
}
