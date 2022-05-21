package lsposed.orange.ui.main

import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable
import lsposed.orange.model.Orientation

data class AppListItem(
    val packageName: String,
    val name: String,
    val icon: Drawable,
    val info: ApplicationInfo,
    val orientation: Orientation = Orientation.UNSPECIFIED,
)