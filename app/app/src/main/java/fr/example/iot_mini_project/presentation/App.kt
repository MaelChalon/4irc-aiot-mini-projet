package fr.example.iot_mini_project.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import fr.example.iot_mini_project.domain.model.Microbit
import fr.example.iot_mini_project.presentation.nav.Route
import fr.example.iot_mini_project.presentation.selectMicroBit.SelectMicroBitView
import fr.example.iot_mini_project.presentation.settings.SettingsView
import fr.example.iot_mini_project.presentation.weather.WeatherView

@Composable
fun App(

) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val startDestination = Route.NavGraph

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            navController = navController,
            startDestination = startDestination,
        ) {
            navigation<Route.NavGraph>(
                startDestination = Route.Settings
            ) {
                composable<Route.Settings> { backstackEntry ->
                    SettingsView(
                        navigateToServer = {
                            navController.navigate(
                                Route.SelectMicroBit
                            )
                        }
                    )
                }

                composable<Route.SelectMicroBit> { backstackEntry ->
                    SelectMicroBitView(
                        onMicrobitSelected = { microbit ->
                            navController.navigate(
                                Route.Weather(
                                    microbitId = microbit.id,
                                )
                            )
                        },
                        onSettingsClick = {
                            navController.navigate(Route.Settings)
                        }
                    )
                }

                composable<Route.Weather> { backstackEntry ->
                    val route = backstackEntry.toRoute<Route.Weather>()
                    WeatherView(
                        microbit = Microbit(
                            id = route.microbitId,
                        ),
                        goBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }

}