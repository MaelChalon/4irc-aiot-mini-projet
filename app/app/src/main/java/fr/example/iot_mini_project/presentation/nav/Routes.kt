@file:Suppress("PLUGIN_IS_NOT_ENABLED")

package fr.example.iot_mini_project.presentation.nav

import kotlinx.serialization.Serializable

@Serializable
sealed class Route {

    @Serializable
    data object NavGraph : Route()

    @Serializable
    data class Weather(val microbitId: Int) : Route()

    @Serializable
    data object Settings : Route()

    @Serializable
    data object SelectMicroBit : Route()

}