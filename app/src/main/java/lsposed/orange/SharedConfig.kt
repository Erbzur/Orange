package lsposed.orange

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import de.robv.android.xposed.XSharedPreferences
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import lsposed.orange.model.ConfigApp
import java.io.File
import java.util.Properties

private const val KEY_CONFIG_APPS = "config_apps"
private const val DEFAULT_JSON_EMPTY_ARRAY = "[]"

sealed class SharedConfig {

    protected val configApps by lazy {
        Json.decodeFromString<List<ConfigApp>>(getConfigAppsJSON())
            .associateBy { it.packageName }
            .toMutableMap()
    }

    protected abstract fun getConfigAppsJSON(): String

    fun findConfigApp(packageName: String) = configApps[packageName]

    class Fetcher : SharedConfig() {
        private val xprefs = XSharedPreferences(BuildConfig.APPLICATION_ID)

        override fun getConfigAppsJSON() =
            xprefs.getString(KEY_CONFIG_APPS, DEFAULT_JSON_EMPTY_ARRAY)!!
    }

    class Provider(context: Context) : SharedConfig() {
        private val configFile = File(context.dataDir, "configs.properties")
        private val properties = Properties()
        private val prefs: SharedPreferences
        private val keyHideIcon = context.getString(R.string.key_setting_hide_icon)
        private val keyShowSystemApps = context.getString(R.string.key_setting_show_system_apps)

        init {
            configFile.takeIf { it.exists() }?.inputStream()?.let { properties.load(it) }
            val prefName = "${context.packageName}_preferences"
            prefs = runCatching {
                context.getSharedPreferences(prefName, Context.MODE_WORLD_READABLE)
            }.getOrElse {
                context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
            }
            prefs.edit {
                putBoolean(keyHideIcon, properties.getProperty(keyHideIcon, "false").toBoolean())
                putBoolean(keyShowSystemApps,
                    properties.getProperty(keyShowSystemApps, "false").toBoolean())
                putString(KEY_CONFIG_APPS, getConfigAppsJSON())
            }
        }

        override fun getConfigAppsJSON() =
            properties.getProperty(KEY_CONFIG_APPS, DEFAULT_JSON_EMPTY_ARRAY)!!

        fun savePref(key: String) {
            when (key) {
                keyHideIcon,
                keyShowSystemApps -> {
                    properties.setProperty(key, prefs.getBoolean(key, false).toString())
                    properties.store(configFile.outputStream(), null)
                }
            }
        }

        fun addConfigApp(packageName: String, orientation: Int) {
            configApps[packageName] = ConfigApp(packageName, orientation)
            saveConfigApps()
        }

        fun removeConfigApp(packageName: String) {
            configApps.remove(packageName)
            saveConfigApps()
        }

        private fun saveConfigApps() {
            Json.encodeToString(configApps.values.toList()).let {
                properties.setProperty(KEY_CONFIG_APPS, it)
                properties.store(configFile.outputStream(), null)
                prefs.edit { putString(KEY_CONFIG_APPS, it) }
            }
        }
    }
}