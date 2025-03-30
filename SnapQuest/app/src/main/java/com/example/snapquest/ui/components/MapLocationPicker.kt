package com.example.snapquest.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import androidx.compose.runtime.*
import com.example.snapquest.manages.LocationManager
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch


@Composable
fun MapLocationPicker(
    modifier: Modifier = Modifier,
    initialLocation: LatLng? = null,
    onLocationChanged: (LatLng) -> Unit = {}
) {
    var selectedLocation by remember { mutableStateOf(initialLocation ?: LatLng(0.0, 0.0)) }
    var mapLoaded by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(selectedLocation, 15f)
    }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val currentLocation = LocationManager.getLastLocation()
                currentLocation?.let {
                    selectedLocation = LatLng(it.latitude, it.longitude)
                    // Força o mapa a centralizar na nova localização
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(selectedLocation, 15f)
                }
            } catch (e: Exception) {
                initialLocation?.let {
                    selectedLocation = it
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(selectedLocation, 15f)
                }
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) {
            selectedLocation = cameraPositionState.position.target
            onLocationChanged(selectedLocation)
        }
    }

    Column(modifier = modifier) {
        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Text("Getting your location...", modifier = Modifier.padding(8.dp))
        } else {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = true,
                    mapType = MapType.NORMAL
                ),
                uiSettings = MapUiSettings(
                    myLocationButtonEnabled = true,
                    scrollGesturesEnabled = true,
                    zoomGesturesEnabled = true
                ),
                onMapLoaded = { mapLoaded = true }
            ) {
                Marker(
                    state = MarkerState(position = selectedLocation),
                    title = "Selected Location",
                    snippet = "Lat: ${"%.6f".format(selectedLocation.latitude)}, Lng: ${"%.6f".format(selectedLocation.longitude)}",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
                    zIndex = 1f
                )
            }
        }
    }
}