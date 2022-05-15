package lsposed.orange.ui

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import lsposed.orange.BuildConfig
import lsposed.orange.R

class SettingFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {

    companion object {
        val TAG = SettingFragment::class.simpleName
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        findPreference<SwitchPreferenceCompat>(getString(R.string.key_setting_hide_icon))
            ?.onPreferenceChangeListener = this
        findPreference<Preference>(getString(R.string.key_about_version))
            ?.summary = BuildConfig.VERSION_NAME
        findPreference<Preference>(getString(R.string.key_about_github))?.let {
            it.intent = Intent(Intent.ACTION_VIEW, Uri.parse(it.summary.toString()))
        }
    }

    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
        return activity?.let {
            val status =
                if (newValue as Boolean)
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                else
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            it.packageManager.setComponentEnabledSetting(
                ComponentName(it, it.packageName + ".Launcher"),
                status,
                PackageManager.DONT_KILL_APP
            )
            true
        } ?: false
    }
}