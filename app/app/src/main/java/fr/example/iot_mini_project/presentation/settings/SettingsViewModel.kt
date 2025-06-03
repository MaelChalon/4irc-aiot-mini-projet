package fr.example.iot_mini_project.presentation.settings

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import fr.example.iot_mini_project.domain.model.Server
import fr.example.iot_mini_project.repository.UDPRepository
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {

    private val repository = UDPRepository.getInstance()
    private val gson = Gson()


    data class SettingsState(
        val ip: String = "",
        val port: String = "10000",
        val serverList: List<Server> = emptyList(),
    )


    private val _state = mutableStateOf(SettingsState())

    val state: SettingsState
        get() = _state.value

    companion object {
        private const val PREFS_NAME = "settings"
        private const val KEY_SERVERS = "servers"
    }

    fun addServer(ip: String, port: String, context: Context) {
        viewModelScope.launch {

            // check if ip respect ipv4 format
            val ipRegex = Regex(
                "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
            )
            if (!ipRegex.matches(ip)) {
                Toast.makeText(
                    context,
                    "L'adresse IP n'est pas valide.",
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }
            // check if port is a number
            val portRegex = Regex("^[0-9]{1,5}$")
            if (!portRegex.matches(port)) {
                Toast.makeText(
                    context,
                    "Le port n'est pas valide.",
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }

            var server = Server(ip, port, state = false, bending = true)

            if (state.serverList.any { it.ipAddress == ip && it.port == port }) {
                Toast.makeText(
                    context,
                    "Le serveur existe déjà dans la liste.",
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }
            val newList = state.serverList.toMutableList()
            newList.add(server)
            _state.value = state.copy(
                serverList = newList
            )


            setServer(server)

            repository.sendMessage("isAvailable")

            val response = repository.getMessage()

            _state.value = state.copy(
                serverList = state.serverList.filter { it != server }
            )

            if (response != null)
                server = server.copy(state = true, bending = false)
            else
                server = server.copy(state = false, bending = false)

            _state.value = state.copy(
                serverList = state.serverList + server
            )

            saveServerListToPreferences(context, newList)
        }

    }

    fun removeServer(server: Server, context: Context) {
        if (server.bending) {
            Toast.makeText(
                context,
                "Le serveur est en cours de connexion.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        val newList = state.serverList.toMutableList()
        newList.remove(server)
        _state.value = state.copy(
            serverList = newList
        )
        saveServerListToPreferences(context, newList)
    }

    fun setServer(server: Server): Boolean {
        return repository.setup(server.ipAddress, server.port.toInt())
    }

    private fun saveServerListToPreferences(context: Context, servers: List<Server>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val serversJson = gson.toJson(servers)
        prefs.edit().apply {
            putString(KEY_SERVERS, serversJson)
            apply()
        }
    }

    fun loadServerListFromPreferences(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val serversJson = prefs.getString(KEY_SERVERS, "[]")
        val type = object : TypeToken<List<Server>>() {}.type
        val servers = gson.fromJson<List<Server>>(serversJson, type)
        _state.value = state.copy(serverList = servers)
        updateServerListState()
    }

    fun updateServerListState() {
        val serverList = state.serverList.map {
            it.copy(bending = true)
        }

        _state.value = state.copy(serverList = serverList)

        serverList.forEachIndexed { index, server ->
            viewModelScope.launch {
                setServer(server)
                repository.sendMessage("isAvailable")
                val response = repository.getMessage()

                // Utilise _state.value.serverList au lieu de serverList pour avoir l'état le plus récent
                val currentList = _state.value.serverList.toMutableList()
                currentList[index] = server.copy(
                    state = response != null,
                    bending = false
                )

                _state.value = state.copy(serverList = currentList)
            }
        }
    }

}