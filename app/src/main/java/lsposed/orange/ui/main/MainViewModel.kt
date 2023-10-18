package lsposed.orange.ui.main

import android.app.Application
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lsposed.orange.R
import lsposed.orange.SharedConfig
import lsposed.orange.model.Orientation

class MainViewModel(app: Application) : AndroidViewModel(app),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val sharedConfig = SharedConfig.Provider(app)
    private val prefs = PreferenceManager.getDefaultSharedPreferences(app)
    private val keyshowSystemApps = app.getString(R.string.key_setting_show_system_apps)
    private val showSystemApps get() = prefs.getBoolean(keyshowSystemApps, false)
    private val appList = mutableListOf<AppListItem>()
    private val appListLiveDataInternal = MutableLiveData<List<AppListItem>>()
    private val isLoadingLiveDataInternal = MutableLiveData<Boolean>()
    private var hasLoadedAppList = false
    val appListLiveData: LiveData<List<AppListItem>> = appListLiveDataInternal
    val isLoadingLiveData: LiveData<Boolean> = isLoadingLiveDataInternal

    @Volatile
    var queryText = ""
        set(value) {
            field = value
            viewModelScope.launch { filterApps() }
        }

    init {
        prefs.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onCleared() {
        super.onCleared()
        prefs.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        sharedConfig.savePref(key)
        if (key == keyshowSystemApps) {
            hasLoadedAppList = false
        }
    }

    fun loadAppList(forceReload: Boolean = false) {
        if (isLoadingLiveDataInternal.value == true || hasLoadedAppList && !forceReload) {
            return
        }
        isLoadingLiveDataInternal.value = true
        viewModelScope.launch(Dispatchers.Default) {
            appList.clear()
            val packageManager = getApplication<Application>().packageManager
            packageManager.getInstalledApplications(0)
                .filter {
                    showSystemApps || (it.flags and ApplicationInfo.FLAG_SYSTEM == 0 &&
                            it.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP == 0)
                }
                .mapTo(appList) { appInfo ->
                    val configApp = sharedConfig.findConfigApp(appInfo.packageName)
                    AppListItem(
                        packageName = appInfo.packageName,
                        name = packageManager.getApplicationLabel(appInfo).toString(),
                        icon = packageManager.getApplicationIcon(appInfo),
                        info = appInfo,
                        orientation = if (configApp != null) {
                            Orientation.values()[configApp.orientation]
                        } else {
                            Orientation.UNSPECIFIED
                        }
                    )
                }
                .sortBy { it.packageName }
            appList.sortBy { it.orientation == Orientation.UNSPECIFIED }
            filterApps()
            isLoadingLiveDataInternal.postValue(false)
            hasLoadedAppList = true
        }
    }

    fun updateConfigApp(packageName: String, orientation: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            if (orientation != Orientation.UNSPECIFIED.ordinal) {
                sharedConfig.addConfigApp(packageName, orientation)
            } else {
                sharedConfig.removeConfigApp(packageName)
            }
            appList.replaceAll {
                if (it.packageName == packageName)
                    it.copy(orientation = Orientation.values()[orientation])
                else
                    it
            }
            filterApps()
        }
    }

    private suspend fun filterApps() = withContext(Dispatchers.Default) {
        appListLiveDataInternal.postValue(if (queryText.isNotEmpty()) {
            appList.filter {
                it.name.contains(queryText, true) ||
                        it.packageName.contains(queryText, true)
            }
        } else {
            appList.toList()
        })
    }
}