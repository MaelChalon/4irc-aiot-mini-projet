package fr.example.iot_mini_project.presentation.selectMicroBit

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.example.iot_mini_project.domain.model.MicrobitList
import fr.example.iot_mini_project.network.NetworkMicrobitList
import fr.example.iot_mini_project.repository.UDPRepository
import kotlinx.coroutines.launch

class SelectMicroBitViewModel : ViewModel() {
    val repository = UDPRepository.getInstance()

    data class SelectMicroBitState(
        val microbitList: MicrobitList = MicrobitList(emptyList())
    )

    private val _state = mutableStateOf(SelectMicroBitState())

    val state: SelectMicroBitState
        get() = _state.value

    fun getDevices(): Boolean {
        var result = false
        viewModelScope.launch {
            repository.sendMessage("getDevices")
            var list = repository.getMessage()


            
            if (list == null) {
                return@launch
            }

            val microbitList = MicrobitList.fromNetworkMicrobitList(
                NetworkMicrobitList.fromJsonString(list)
            )

            result = true

            _state.value = state.copy(
                microbitList = microbitList
            )

        }
        return result
    }

}