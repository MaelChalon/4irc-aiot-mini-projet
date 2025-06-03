package fr.example.iot_mini_project.presentation.weather

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.example.iot_mini_project.domain.model.Microbit
import fr.example.iot_mini_project.domain.model.MicrobitData

@Composable
fun WeatherView(
    modifier: Modifier = Modifier,
    microbit: Microbit,
    goBack: () -> Unit = {},
) {
    val viewModel = viewModel<WeatherViewModel>()

    val state = viewModel.state

    LaunchedEffect(microbit.id) {
        viewModel.launch(
            microbit = microbit
        )
    }

    val weatherValue = remember(
        state.microbitData.id,
        state.microbitData.temp,
        state.microbitData.hum,
        state.microbitData.pressure,
        state.microbitData.uv,
        state.microbitData.light,
        state.microbitData.order
    ) {
        state.microbitData
    }

    val context = LocalContext.current

    WeatherViewContent(
        modifier = modifier,
        microbit = microbit,
        weatherValue = weatherValue,
        goBack = goBack,
        updateOrder = { index ->
            viewModel.updateOrder(index)
        },
        getData = {
            viewModel.getData(context = context)
        },
    )
}

@Composable
fun WeatherViewContent(
    modifier: Modifier = Modifier,
    microbit: Microbit,
    weatherValue: MicrobitData,
    goBack: () -> Unit = {},
    updateOrder: (Int) -> Unit,
    getData: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)

    ) {

        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = goBack,
                ) {
                    Icon(
                        modifier = Modifier.size(32.dp),
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "GoBack"
                    )
                }
                IconButton(
                    onClick = getData,
                ) {
                    Icon(
                        modifier = Modifier.size(32.dp),
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "GoBack"
                    )
                }
            }

            Text(
                text = "Microbit ${microbit.id}",
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                text = "Date : ${
                    java.text.SimpleDateFormat("dd/MM/yyyy").format(java.util.Date())
                }",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        WeatherDataList(
            weatherValue = weatherValue,
            modifier = Modifier.fillMaxWidth(),
            updateItemOrder = { index ->
                updateOrder(index)
            },
        )
    }
}


@Composable
fun WeatherDataList(
    weatherValue: MicrobitData,
    updateItemOrder: (Int) -> Unit,
    modifier: Modifier = Modifier
) {


    val weatherItems = remember(weatherValue) {
        val order = weatherValue.order
        val list = mutableListOf<Triple<String, Float, String>>()
        for (c in order) {
            when (c) {
                'T' -> list.add(Triple("Temperature", weatherValue.temp, "°C"))
                'H' -> list.add(Triple("Humidity", weatherValue.hum, "%"))
                'P' -> list.add(Triple("Pression", weatherValue.pressure.toFloat(), "hPa"))
                'U' -> list.add(Triple("UV", weatherValue.uv.toFloat(), "nm"))
                'L' -> list.add(Triple("Light", weatherValue.light.toFloat(), "lum"))
            }
        }
        return@remember list
    }

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        itemsIndexed(weatherItems) { index, item ->
            WeatherDataItem(
                label = item.first,
                value = item.second,
                unit = item.third,
                onPressUp = {
                    updateItemOrder(index)
                },
                isUpable = index > 0,
            )
        }
    }
}

@Composable
fun WeatherDataItem(
    label: String,
    value: Float,
    unit: String,
    onPressUp: () -> Unit,
    isUpable: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "$label - $value$unit",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        if (isUpable) {
            IconButton(
                onClick = onPressUp,
                modifier = Modifier
                    .size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "UpItem"
                )
            }
        }
    }
}