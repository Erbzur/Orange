package lsposed.orange.model

import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged

class ConfigAppRepo(private val configAppDao: ConfigAppDao) {

    val configApps = configAppDao.getAll().distinctUntilChanged().conflate()

    suspend fun insert(vararg configApps: ConfigApp) = configAppDao.insert(*configApps)
    suspend fun delete(vararg configApps: ConfigApp) = configAppDao.delete(*configApps)
}