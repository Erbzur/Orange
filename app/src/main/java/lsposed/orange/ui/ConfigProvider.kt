package lsposed.orange.ui

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import lsposed.orange.model.ModuleDB

class ConfigProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = "lsposed.orange.configprovider"
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? = selectionArgs?.get(0)?.let { packageName ->
        context?.let { ModuleDB.getInstance(it).configAppDao().findRaw(packageName) }
    }

    override fun onCreate() = true
    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, vals: ContentValues?): Uri? = null
    override fun delete(uri: Uri, sel: String?, selArgs: Array<String>?) = 0
    override fun update(uri: Uri, v: ContentValues?, s: String?, sa: Array<String>?) = 0
}