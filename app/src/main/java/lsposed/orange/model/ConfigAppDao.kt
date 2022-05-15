package lsposed.orange.model

import android.database.Cursor
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfigAppDao {

    @Query("SELECT * FROM config_apps WHERE package_name = :packageName")
    fun findRaw(packageName: String): Cursor

    @Query("SELECT * FROM config_apps")
    fun getAll(): Flow<List<ConfigApp>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg configApps: ConfigApp)

    @Delete
    suspend fun delete(vararg configApps: ConfigApp)
}