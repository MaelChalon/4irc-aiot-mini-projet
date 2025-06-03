package fr.example.iot_mini_project.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class NetworkMicrobitList(
    @SerialName("LIST")
    val list: List<Int>,
) {
    companion object {
        fun fromJsonString(jsonString: String): NetworkMicrobitList {
            return Json.decodeFromString(NetworkMicrobitList.serializer(), jsonString)
        }
    }
}

