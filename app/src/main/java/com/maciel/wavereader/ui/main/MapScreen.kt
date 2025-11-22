package com.maciel.wavereader.ui.main

import com.maciel.wavereader.BuildConfig
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.maciel.wavereader.R
import com.maciel.wavereader.utils.RequestLocationPermission
import com.maciel.wavereader.viewmodels.LocationViewModel

/*
* Map Screen for user location and getting locations through map interaction.
* Technically part of the search screen tab.
*
* Resources: https://medium.com/@karollismarmokas/integrating-google-maps-in-android-with-jetpack-compose-user-location-and-search-bar-a432c9074349
 */
@Composable
fun MapScreen(
    locationViewModel: LocationViewModel,
    fusedLocationClient: FusedLocationProviderClient
) {
    val context = LocalContext.current
    val coordinates by locationViewModel.coordinatesState.observeAsState()
    val apiKey = BuildConfig.MAPS_API_KEY

    if (!Places.isInitialized()) {
        Places.initialize(context.applicationContext, apiKey)
    }

    val cameraPositionState = rememberCameraPositionState()
    val markerState = rememberMarkerState(position = LatLng(0.0, 0.0))

    RequestLocationPermission(
        onDenied = {
            //TODO
        }
    ) {
        LaunchedEffect(Unit) {
            locationViewModel.fetchUserLocation(context, fusedLocationClient)
        }

        LaunchedEffect(coordinates) {
            coordinates?.let { (lat, lon) ->
                val latLng = LatLng(lat, lon)
                markerState.position = latLng
                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(latLng, 12f))
            }
        }

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = true),
            uiSettings = MapUiSettings(zoomControlsEnabled = true),
            onMapClick = { latLng ->
                locationViewModel.updateCoordinates(latLng.latitude, latLng.longitude)
                locationViewModel.setLocationText(latLng.latitude, latLng.longitude)
            }
        ) {
            coordinates?.let { (lat, lon) ->
                Marker(
                    state = markerState,
                    title = "Selected Location"
                )
            }
        }
    }
}



