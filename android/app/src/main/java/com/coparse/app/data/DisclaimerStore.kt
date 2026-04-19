package com.coparse.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("coparse_prefs")

class DisclaimerStore(private val context: Context) {
    private val keyDisclaimer = booleanPreferencesKey("disclaimer_accepted")

    val disclaimerAccepted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[keyDisclaimer] ?: false
    }

    suspend fun setDisclaimerAccepted(value: Boolean) {
        context.dataStore.edit { it[keyDisclaimer] = value }
    }
}
