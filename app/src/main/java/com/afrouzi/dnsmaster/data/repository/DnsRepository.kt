package com.afrouzi.dnsmaster.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.afrouzi.dnsmaster.data.DefaultDnsServers
import com.afrouzi.dnsmaster.model.DnsItem
import com.afrouzi.dnsmaster.model.VpnConnectionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dns_master_prefs")

class DnsRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    companion object {
        private val KEY_SELECTED_DNS_ID = stringPreferencesKey("selected_dns_id")
        private val KEY_CUSTOM_SERVERS = stringPreferencesKey("custom_dns_servers_json")
        private val KEY_FAVORITES = stringSetPreferencesKey("favorite_dns_ids")
        private val KEY_LANGUAGE = stringPreferencesKey("app_language") // "fa" or "en"
        private val KEY_THEME = stringPreferencesKey("app_theme") // "system", "dark", "light"
        private val KEY_AUTO_CONNECT_BOOT = booleanPreferencesKey("auto_connect_boot")

        fun getDefaultSystemLanguage(): String {
            val lang = java.util.Locale.getDefault().language.lowercase()
            return if (lang.startsWith("fa") || lang.startsWith("prs") || lang.startsWith("pes")) "fa" else "en"
        }

        @Volatile
        private var INSTANCE: DnsRepository? = null

        fun getInstance(context: Context): DnsRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DnsRepository(context.applicationContext).also { INSTANCE = it }
            }
        }

        // Global live runtime state for VPN
        val connectionState = MutableStateFlow(VpnConnectionState.DISCONNECTED)
        val connectedDns = MutableStateFlow<DnsItem?>(null)
        val connectedStartTime = MutableStateFlow(0L)
        val activePingMs = MutableStateFlow<Long?>(null)
        val currentLanguage = MutableStateFlow(getDefaultSystemLanguage())
    }

    val selectedDnsIdFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_SELECTED_DNS_ID] ?: DefaultDnsServers.list.first().id
    }

    val customDnsListFlow: Flow<List<DnsItem>> = context.dataStore.data.map { prefs ->
        val rawJson = prefs[KEY_CUSTOM_SERVERS] ?: "[]"
        try {
            json.decodeFromString<List<DnsItem>>(rawJson)
        } catch (e: Exception) {
            emptyList()
        }
    }

    val favoritesFlow: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[KEY_FAVORITES] ?: setOf("cloudflare", "google", "shecan", "403online", "adguard_default", "radar_game")
    }

    val languageFlow: Flow<String> = context.dataStore.data.map { prefs ->
        val lang = prefs[KEY_LANGUAGE] ?: getDefaultSystemLanguage()
        currentLanguage.value = lang
        lang
    }

    val themeFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_THEME] ?: "dark"
    }

    val autoConnectBootFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_AUTO_CONNECT_BOOT] ?: false
    }

    suspend fun setSelectedDnsId(id: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SELECTED_DNS_ID] = id
        }
    }

    suspend fun addCustomDns(item: DnsItem) {
        context.dataStore.edit { prefs ->
            val currentJson = prefs[KEY_CUSTOM_SERVERS] ?: "[]"
            val currentList = try {
                json.decodeFromString<List<DnsItem>>(currentJson).toMutableList()
            } catch (e: Exception) {
                mutableListOf()
            }
            currentList.removeAll { it.id == item.id }
            currentList.add(0, item)
            prefs[KEY_CUSTOM_SERVERS] = json.encodeToString(currentList)
        }
    }

    suspend fun deleteCustomDns(id: String) {
        context.dataStore.edit { prefs ->
            val currentJson = prefs[KEY_CUSTOM_SERVERS] ?: "[]"
            val currentList = try {
                json.decodeFromString<List<DnsItem>>(currentJson).toMutableList()
            } catch (e: Exception) {
                mutableListOf()
            }
            currentList.removeAll { it.id == id }
            prefs[KEY_CUSTOM_SERVERS] = json.encodeToString(currentList)
        }
    }

    suspend fun toggleFavorite(id: String) {
        context.dataStore.edit { prefs ->
            val current = (prefs[KEY_FAVORITES] ?: emptySet()).toMutableSet()
            if (current.contains(id)) {
                current.remove(id)
            } else {
                current.add(id)
            }
            prefs[KEY_FAVORITES] = current
        }
    }

    suspend fun setLanguage(lang: String) {
        currentLanguage.value = lang
        context.dataStore.edit { prefs ->
            prefs[KEY_LANGUAGE] = lang
        }
    }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME] = theme
        }
    }

    suspend fun setAutoConnectBoot(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_AUTO_CONNECT_BOOT] = enabled
        }
    }

    suspend fun getDnsById(id: String, customList: List<DnsItem>): DnsItem? {
        return customList.find { it.id == id } ?: DefaultDnsServers.list.find { it.id == id }
    }
}
