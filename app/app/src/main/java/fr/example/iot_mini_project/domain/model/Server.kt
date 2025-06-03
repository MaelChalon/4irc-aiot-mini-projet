package fr.example.iot_mini_project.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Server(
    val ipAddress: String,
    val port: String,
    val state: Boolean,
    val bending: Boolean,
)
