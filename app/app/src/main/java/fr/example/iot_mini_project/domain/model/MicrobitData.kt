package fr.example.iot_mini_project.domain.model

import fr.example.iot_mini_project.network.NetworkData

data class MicrobitData(
    val id: Int,
    val temp: Float,
    val hum: Float,
    val pressure: Int,
    val uv: Int,
    val light: Int,
    val order: String,
) {
    companion object {
        fun fromNetworkData(
            networkData: NetworkData,
        ): MicrobitData {
            return MicrobitData(
                id = networkData.id,
                temp = networkData.temperature,
                hum = networkData.humidity,
                pressure = networkData.pressure,
                uv = networkData.uv,
                light = networkData.light,
                order = networkData.order,
            )
        }

    }
}
