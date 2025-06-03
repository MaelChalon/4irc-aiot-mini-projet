package fr.example.iot_mini_project.repository

import android.os.Handler
import android.os.Looper
import android.os.Message
import fr.example.iot_mini_project.domain.model.UDPThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.LinkedBlockingQueue

class UDPRepository private constructor() {
    private val messageQueue = LinkedBlockingQueue<String>()
    private lateinit var udp: UDPThread
    private val scope = CoroutineScope(Dispatchers.IO)

    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            // Handle the message received from UDPThread
            val receivedMessage = msg.obj as String
            // Update the UI with the received message
            println(receivedMessage)
        }
    }

    companion object {
        @Volatile
        private var instance: UDPRepository? = null

        // Obtenir l'instance unique du repository
        fun getInstance(): UDPRepository {
            return instance ?: synchronized(this) {
                instance ?: UDPRepository().also { instance = it }
            }
        }
    }


    fun setup(
        ipAddress: String,
        port: Int
    ): Boolean {
        return try {
            udp = UDPThread(
                ipAddress = ipAddress,
                port = port,
                queue = messageQueue,
                handler = handler
            )
            udp.start()
            true
        } catch (e: Exception) {
            println("Error setting up UDP: ${e.message}")
            false
        }
    }

    suspend fun sendMessage(message: String) {
        withContext(Dispatchers.IO) {
            try {
                udp.sendMessage(message)
            } catch (e: InterruptedException) {
                println("Error sending message: ${e.message}")
            }
        }
    }

    suspend fun getMessage(): String? {
        return withContext(Dispatchers.IO) {
            try {
                udp.getMessage()
            } catch (e: Exception) {
                println("Error getting message: ${e.message}")
                null
            }
        }
    }

}