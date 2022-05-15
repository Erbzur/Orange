package lsposed.orange.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import lsposed.orange.R

@Database(entities = [ConfigApp::class], version = 1)
abstract class ModuleDB : RoomDatabase() {

    abstract fun configAppDao(): ConfigAppDao

    companion object {
        @Volatile
        private var instance: ModuleDB? = null

        fun getInstance(context: Context) = instance ?: synchronized(this) {
            Room.databaseBuilder(
                context.applicationContext,
                ModuleDB::class.java,
                context.getString(R.string.app_name)
            ).build().also { instance = it }
        }
    }
}