package fr.example.iot_mini_project.domain.model

import android.os.Handler
import fr.example.iot_mini_project.cipher.VigenereCipher
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.BlockingQueue


class UDPThread(
    private val ipAddress: String,
    private val port: Int = 53,
    private val queue: BlockingQueue<String>,
    private val handler: Handler
) : Thread() {

    private lateinit var UDPSocket: DatagramSocket
    private lateinit var address: InetAddress
    private val cipher = VigenereCipher("HERMES", "APOLLON")


    init {
        try {
            UDPSocket = DatagramSocket()
            UDPSocket.soTimeout = 2000
            address = InetAddress.getByName(ipAddress)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun sendMessage(message: String) {
        try {
            val encryptedMessage = cipher.encrypt(message)
            val buffer = encryptedMessage.toByteArray()
            val packet = DatagramPacket(buffer, buffer.size, address, port)
            UDPSocket.send(packet)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun getMessage(): String? {
        return try {
            val buffer = ByteArray(1024)
            val packet = DatagramPacket(buffer, buffer.size)
            UDPSocket.receive(packet) // blocking
            val encryptedMessage = String(packet.data, 0, packet.length)
            cipher.decrypt(encryptedMessage)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    override fun run() {
        while (true) {
            val message: String = queue.take()
            println("Sending message: $message")
            sendMessage(message)
            val receivedMessage = getMessage()
            receivedMessage?.let {
                handler.post {
                    handler.obtainMessage(0, it).sendToTarget()
                }
            }
        }
    }
}
