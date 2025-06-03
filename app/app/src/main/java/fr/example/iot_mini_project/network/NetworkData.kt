package fr.example.iot_mini_project.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class NetworkData(
    @SerialName("ID")
    val id: Int,
    @SerialName("TEMPERATURE")
    val temperature: Float,
    @SerialName("HUMIDITY")
    val humidity: Float,
    @SerialName("PRESSURE")
    val pressure: Int,
    @SerialName("UV")
    val uv: Int,
    @SerialName("LIGHT")
    val light: Int,
    @SerialName("ORDER")
    val order: String,
) {
    companion object {
        fun fromJsonString(jsonString: String): NetworkData {
            return Json.decodeFromString(NetworkData.serializer(), jsonString)
        }

    }
}



