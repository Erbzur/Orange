package lsposed.orange.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "config_apps")
data class ConfigApp(
    @PrimaryKey
    @ColumnInfo(name = "package_name")
    val packageName: String,
    val orientation: Int = Orientation.UNSPECIFIED.ordinal,
)