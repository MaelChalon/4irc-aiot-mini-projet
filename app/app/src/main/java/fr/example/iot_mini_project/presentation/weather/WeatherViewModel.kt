package fr.example.iot_mini_project.presentation.weather

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.example.iot_mini_project.domain.model.Microbit
import fr.example.iot_mini_project.domain.model.MicrobitData
import fr.example.iot_mini_project.network.NetworkData
import fr.example.iot_mini_project.repository.UDPRepository
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {

    private val repository = UDPRepository.getInstance()

    data class WeatherState(
        val microbitData: MicrobitData = MicrobitData(
            id = 0,
            temp = 0f,
            hum = 0f,
            pressure = 0,
            uv = 0,
            light = 0,
            order = "THPUL"
        ),
    )

    private val _state = mutableStateOf(WeatherState())

    val state: WeatherState
        get() = _state.value

    fun launch(microbit: Microbit): Boolean {
        var result = false
        viewModelScope.launch {
            _state.value = state.copy(
                microbitData = state.microbitData.copy(
                    id = microbit.id,
                ),
            )

            repository.sendMessage("getValues ${microbit.id}")
            val data = repository.getMessage() ?: return@launch

            val microbitData = MicrobitData.fromNetworkData(
                NetworkData.fromJsonString(data)
            )

            result = true

            _state.value = state.copy(
                microbitData = microbitData,
            )

        }
        return result
    }

    fun getData(context: Context) {
        viewModelScope.launch {
            repository.sendMessage("getValues ${state.microbitData.id}")
            val data = repository.getMessage() ?: return@launch

            val microbitData = MicrobitData.fromNetworkData(
                NetworkData.fromJsonString(data)
            )


            println("Received data: $microbitData")

            _state.value = state.copy(
                microbitData = microbitData,
            )

            Toast.makeText(
                context,
                "Les données sont à jour.",
                Toast.LENGTH_SHORT
            ).show()

        }
    }

    fun updateOrder(index: Int) {
        viewModelScope.launch {
            val order = state.microbitData.order.toMutableList()
            val char = order[index]
            order[index] = order[index - 1]
            order[index - 1] = char

            _state.value = state.copy(
                microbitData = state.microbitData.copy(
                    order = order.joinToString("")
                )
            )

            repository.sendMessage("putOrder ${state.microbitData.id} ${state.microbitData.order}")
        }

    }


}