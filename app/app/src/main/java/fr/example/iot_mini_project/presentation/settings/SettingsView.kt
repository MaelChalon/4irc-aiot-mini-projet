package fr.example.iot_mini_project.presentation.settings

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.outlined.Pending
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.example.iot_mini_project.domain.model.Server

@Composable
fun SettingsView(
    modifier: Modifier = Modifier,
    navigateToServer: () -> Unit = {},
) {

    val viewModel = viewModel<SettingsViewModel>()

    val state = viewModel.state
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadServerListFromPreferences(context = context)
    }

    var ip by remember { mutableStateOf(state.ip) }
    var port by remember { mutableStateOf(state.port) }

    SettingsViewContent(
        modifier = modifier,
        ip = ip,
        port = port,
        onIpChange = { ip = it },
        onPortChange = { port = it },
        onSave = {
            viewModel.addServer(ip, port, context)
        },
        servers = state.serverList,
        onServerClick = { server ->
            viewModel.setServer(server)
            navigateToServer()
        },
        onRemoveServer = { server ->
            viewModel.removeServer(server, context)
        },
        onRefresh = {
            viewModel.updateServerListState()
        },
    )
}

@Composable
fun SettingsViewContent(
    modifier: Modifier = Modifier,
    ip: String,
    port: String,
    onIpChange: (String) -> Unit,
    onPortChange: (String) -> Unit,
    onSave: () -> Unit,
    servers: List<Server> = emptyList(),
    onServerClick: (Server) -> Unit = {},
    onRemoveServer: (Server) -> Unit = {},
    onRefresh: () -> Unit = {},
    context: Context = LocalContext.current,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                IconButton(
                    onClick = onRefresh
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

            }
            OutlinedTextField(
                value = ip,
                onValueChange = onIpChange,
                label = { Text("IP Address") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = port,
                onValueChange = onPortChange,
                label = { Text("Port") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
            ) {
                Text("Save", style = MaterialTheme.typography.titleMedium)
            }
        }
        LazyColumn {
            items(servers) { server ->

                Button(
                    onClick = {
                        if (server.bending) {
                            Toast.makeText(
                                context,
                                "Le serveur est en cours de connexion.",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        } else if (server.state) {
                            onServerClick(server)
                        } else {
                            Toast.makeText(
                                context,
                                "Le serveur n'est pas disponible.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentPadding = PaddingValues(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${server.ipAddress}:${server.port}",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (server.bending) {
                                Icon(
                                    imageVector = Icons.Outlined.Pending,
                                    contentDescription = "Bending",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Icon(
                                    imageVector = if (server.state) Icons.Default.Wifi else Icons.Default.WifiOff,
                                    contentDescription = if (server.state) "Available" else "Unavailable",
                                    tint = if (server.state) Color.Green else Color.Red
                                )
                            }
                            IconButton(
                                onClick = {
                                    onRemoveServer(server)
                                },
                                modifier = Modifier
                                    .size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove Server",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                    }
                }

            }
        }


    }
}