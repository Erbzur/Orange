package lsposed.orange.model

import kotlinx.serialization.Serializable

@Serializable
data class ConfigApp(
    val packageName: String,
    val orientation: Int = Orientation.UNSPECIFIED.ordinal,
)