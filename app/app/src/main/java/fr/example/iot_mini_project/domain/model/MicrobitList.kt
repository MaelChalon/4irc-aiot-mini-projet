package fr.example.iot_mini_project.domain.model

import fr.example.iot_mini_project.network.NetworkMicrobitList

data class MicrobitList(
    val list: List<Microbit>,
) {
    companion object {
        fun fromNetworkMicrobitList(networkMicrobitList: NetworkMicrobitList): MicrobitList {
            return MicrobitList(
                list = networkMicrobitList.list.map { Microbit(id = it) }
            )
        }
    }
}

