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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lsposed.orange.R
import lsposed.orange.model.ConfigApp
import lsposed.orange.model.ConfigAppRepo
import lsposed.orange.model.ModuleDB
import lsposed.orange.model.Orientation

class MainViewModel(app: Application) : AndroidViewModel(app),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val prefs = PreferenceManager.getDefaultSharedPreferences(app)
    private val keyshowSystemApps =
        getApplication<Application>().getString(R.string.key_setting_show_system_apps)
    private val showSystemApps get() = prefs.getBoolean(keyshowSystemApps, false)
    private val configAppRepo = ConfigAppRepo(ModuleDB.getInstance(app).configAppDao())
    private val appList = mutableListOf<AppListItem>()
    private val appListLiveDataInternal = MutableLiveData<List<AppListItem>>()
    private val isLoadingLiveDataInternal = MutableLiveData<Boolean>()
    private var fetchConfigAppsJob: Job? = null
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
                    AppListItem(
                        packageName = appInfo.packageName,
                        name = packageManager.getApplicationLabel(appInfo).toString(),
                        icon = packageManager.getApplicationIcon(appInfo),
                        info = appInfo,
                    )
                }
                .sortBy { it.packageName }
            fetchConfigAppsJob?.cancel()
            fetchConfigAppsJob = launch {
                configAppRepo.configApps.collectLatest { configApps ->
                    appList.replaceAll { appListItem ->
                        val configApp =
                            configApps.find { it.packageName == appListItem.packageName }
                        appListItem.copy(
                            orientation = if (configApp != null) {
                                Orientation.values()[configApp.orientation]
                            } else {
                                Orientation.UNSPECIFIED
                            }
                        )
                    }
                    if (isLoadingLiveDataInternal.value != false) {
                        appList.sortBy { it.orientation == Orientation.UNSPECIFIED }
                        filterApps()
                        isLoadingLiveDataInternal.postValue(false)
                        hasLoadedAppList = true
                    } else {
                        filterApps()
                    }
                }
            }
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

    fun updateConfigApp(packageName: String, orientation: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val configApp = ConfigApp(packageName, orientation)
            if (orientation != 0) {
                configAppRepo.insert(configApp)
            } else {
                configAppRepo.delete(configApp)
            }
        }
    }
}