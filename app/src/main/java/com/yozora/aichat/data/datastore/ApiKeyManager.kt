package com.yozora.aichat.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class ApiKeyManager(
    private val dataStore: DataStore<Preferences>
) {
    private val keysKey = stringSetPreferencesKey("gemini_api_keys")
    private val activeIndexKey = intPreferencesKey("gemini_active_key_index")
    private val tavilyApiKey = stringPreferencesKey("tavily_api_key")

    val keys: Flow<List<String>> = dataStore.data.map { preferences ->
        preferences[keysKey]?.toList().orEmpty()
    }

    suspend fun addKey(key: String) {
        val trimmed = key.trim()
        if (trimmed.isEmpty()) return
        dataStore.edit { preferences ->
            val updated = preferences[keysKey].orEmpty().toMutableSet()
            updated += trimmed
            preferences[keysKey] = updated
            preferences[activeIndexKey] = preferences[activeIndexKey] ?: 0
        }
    }

    suspend fun replaceWithSingleKey(key: String) {
        val trimmed = key.trim()
        dataStore.edit { preferences ->
            if (trimmed.isEmpty()) {
                preferences.remove(keysKey)
                preferences.remove(activeIndexKey)
            } else {
                preferences[keysKey] = setOf(trimmed)
                preferences[activeIndexKey] = 0
            }
        }
    }

    suspend fun clearKeys() {
        dataStore.edit { preferences ->
            preferences.remove(keysKey)
            preferences.remove(activeIndexKey)
        }
    }

    suspend fun removeKey(key: String) {
        dataStore.edit { preferences ->
            val updated = preferences[keysKey].orEmpty().toMutableSet()
            updated -= key
            preferences[keysKey] = updated
            val lastIndex = (updated.size - 1).coerceAtLeast(0)
            preferences[activeIndexKey] = (preferences[activeIndexKey] ?: 0).coerceAtMost(lastIndex)
        }
    }

    suspend fun currentKey(): String? {
        val preferences = dataStore.data.first()
        val allKeys = preferences[keysKey]?.toList().orEmpty()
        if (allKeys.isEmpty()) return null
        val index = (preferences[activeIndexKey] ?: 0).floorMod(allKeys.size)
        return allKeys[index]
    }

    suspend fun keyForProvider(providerId: String): String? {
        val preferences = dataStore.data.first()
        return preferences[providerKey(providerId)] ?: currentKey()
    }

    suspend fun replaceProviderKey(providerId: String, key: String) {
        val trimmed = key.trim()
        dataStore.edit { preferences ->
            if (trimmed.isEmpty()) {
                preferences.remove(providerKey(providerId))
            } else {
                preferences[providerKey(providerId)] = trimmed
            }
        }
    }

    suspend fun clearProviderKey(providerId: String) {
        dataStore.edit { preferences ->
            preferences.remove(providerKey(providerId))
        }
    }

    val tavilyKey: Flow<String?> = dataStore.data.map { preferences ->
        preferences[tavilyApiKey]
    }

    suspend fun keyForTavily(): String? {
        return dataStore.data.first()[tavilyApiKey]
    }

    suspend fun replaceTavilyKey(key: String) {
        val trimmed = key.trim()
        dataStore.edit { preferences ->
            if (trimmed.isEmpty()) {
                preferences.remove(tavilyApiKey)
            } else {
                preferences[tavilyApiKey] = trimmed
            }
        }
    }

    suspend fun clearTavilyKey() {
        dataStore.edit { preferences ->
            preferences.remove(tavilyApiKey)
        }
    }

    fun providerKeys(providerIds: List<String>): Flow<Map<String, String>> {
        return dataStore.data.map { preferences ->
            providerIds.mapNotNull { id ->
                preferences[providerKey(id)]?.let { id to it }
            }.toMap()
        }
    }

    suspend fun rotateKey(): String? {
        var nextKey: String? = null
        dataStore.edit { preferences ->
            val allKeys = preferences[keysKey]?.toList().orEmpty()
            if (allKeys.isEmpty()) return@edit
            val nextIndex = ((preferences[activeIndexKey] ?: 0) + 1).floorMod(allKeys.size)
            preferences[activeIndexKey] = nextIndex
            nextKey = allKeys[nextIndex]
        }
        return nextKey
    }

    fun mask(key: String): String {
        if (key.length <= 10) return "*".repeat(key.length)
        return key.take(4) + "..." + key.takeLast(6)
    }

    private fun providerKey(providerId: String) = stringPreferencesKey("api_key_$providerId")

    private fun Int.floorMod(divisor: Int): Int = ((this % divisor) + divisor) % divisor
}
