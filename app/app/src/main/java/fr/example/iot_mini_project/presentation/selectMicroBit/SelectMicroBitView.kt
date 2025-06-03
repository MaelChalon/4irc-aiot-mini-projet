package fr.example.iot_mini_project.presentation.selectMicroBit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.example.iot_mini_project.domain.model.Microbit
import fr.example.iot_mini_project.domain.model.MicrobitList

@Composable
fun SelectMicroBitView(
    viewModel: SelectMicroBitViewModel = SelectMicroBitViewModel(),
    modifier: Modifier = Modifier,
    onMicrobitSelected: (Microbit) -> Unit = {},
    onSettingsClick: () -> Unit = {},
) {
    val state = viewModel.state

    LaunchedEffect(Unit) {
        viewModel.getDevices()
    }

    SelectMicroBitViewContent(
        modifier = modifier,
        microbitList = state.microbitList,
        onMicrobitSelected = { microbit ->
            onMicrobitSelected(microbit)
        },
        onSettingsClick = onSettingsClick
    )
}

@Composable
fun SelectMicroBitViewContent(
    modifier: Modifier = Modifier,
    microbitList: MicrobitList,
    onMicrobitSelected: (Microbit) -> Unit,
    onSettingsClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Select a sensor center",
                modifier = Modifier,
                style = MaterialTheme.typography.headlineLarge
            )
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier,
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(microbitList.list) { microbit ->
                Button(
                    onClick = { onMicrobitSelected(microbit) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors().copy(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(16.dp),
                ) {
                    Text(
                        text = "Microbit ${microbit.id}",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}