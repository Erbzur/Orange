package lsposed.orange.hook

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import lsposed.orange.model.ConfigApp
import lsposed.orange.ui.ConfigProvider

class ModuleConfig(private val context: Context) {

    companion object {
        private val configUri = Uri.parse("content://${ConfigProvider.AUTHORITY}")
    }

    @SuppressLint("Range")
    fun findConfigApp(packageName: String) =
        context.contentResolver.query(configUri, null, null, arrayOf(packageName), null)
            ?.use {
                if (it.moveToFirst()) {
                    ConfigApp(
                        it.getString(it.getColumnIndex("package_name")),
                        it.getInt(it.getColumnIndex("orientation"))
                    )
                } else {
                    null
                }
            }
}