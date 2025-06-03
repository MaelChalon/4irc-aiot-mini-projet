package fr.example.iot_mini_project.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ServerList(
    val list: List<Server> = emptyList()
)
