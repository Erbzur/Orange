package lsposed.orange.model

import android.content.pm.ActivityInfo

enum class Orientation {

    UNSPECIFIED,
    PORTRAIT,
    LANDSCAPE,
    REVERSE_LANDSCAPE,
    SENSOR_LANDSCAPE,
    SENSOR;

    fun toLabel() = when (this) {
        UNSPECIFIED -> ""
        PORTRAIT -> "P"
        LANDSCAPE -> "L"
        REVERSE_LANDSCAPE -> "RL"
        SENSOR_LANDSCAPE -> "SL"
        SENSOR -> "S"
    }
}

fun mapActivityOrientation(orientation: Int) = when (Orientation.values()[orientation]) {
    Orientation.UNSPECIFIED -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    Orientation.PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    Orientation.LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    Orientation.REVERSE_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
    Orientation.SENSOR_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
    Orientation.SENSOR -> ActivityInfo.SCREEN_ORIENTATION_SENSOR
}