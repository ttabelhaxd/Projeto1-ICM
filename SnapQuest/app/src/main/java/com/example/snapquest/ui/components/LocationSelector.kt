package com.example.snapquest.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LocationSelector(
    latitude: String,
    longitude: String,
    onSelectLocation: () -> Unit,
    onUseCurrentLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = latitude,
                onValueChange = {},
                label = { Text("Latitude") },
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                readOnly = true
            )

            OutlinedTextField(
                value = longitude,
                onValueChange = {},
                label = { Text("Longitude") },
                modifier = Modifier.weight(1f),
                readOnly = true
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = onSelectLocation,
                modifier = Modifier.weight(1f).padding(end = 4.dp)
            ) {
                Text("Select on Map")
            }

            Button(
                onClick = onUseCurrentLocation,
                modifier = Modifier.weight(1f).padding(start = 4.dp)
            ) {
                Text("Use Current")
            }
        }
    }
}