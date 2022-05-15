package lsposed.orange.model

enum class Orientation {

    UNSPECIFIED,
    PORTRAIT,
    LANDSCAPE,
    REVERSE_LANDSCAPE,
    SENSOR_LANDSCAPE,
    SENSOR;

    fun toLabel() = when (this) {
        PORTRAIT -> "P"
        LANDSCAPE -> "L"
        REVERSE_LANDSCAPE -> "RL"
        SENSOR_LANDSCAPE -> "SL"
        SENSOR -> "S"
        else -> ""
    }
}