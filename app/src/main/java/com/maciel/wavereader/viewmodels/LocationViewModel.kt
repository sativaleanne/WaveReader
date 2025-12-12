package com.maciel.wavereader.viewmodels

import android.content.Context
import android.location.Address
import android.location.Geocoder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import java.util.Locale


class LocationViewModel : ViewModel() {

    private val _coordinatesState = MutableLiveData<Pair<Double, Double>>()
    val coordinatesState: LiveData<Pair<Double, Double>> = _coordinatesState

    private val _locationError = mutableStateOf(false)
    val locationError: Boolean
        get() = _locationError.value

    var displayLocationText by mutableStateOf("No location selected")
        private set

    private var geocoder: Geocoder? = null

    private fun getGeocoder(context: Context): Geocoder {
        if (geocoder == null) {
            geocoder = Geocoder(context, Locale.getDefault())
        }
        return geocoder!!
    }

    fun resetLocationState() {
        _locationError.value = false
        displayLocationText = "No location selected"
    }

    private fun setLocationError(hasError: Boolean) {
        _locationError.value = hasError
    }

    // Map interaction
    fun setLocationText(lat: Double, lon: Double) {
        updateCoordinates(lat, lon)
        displayLocationText = formatLatLong(lat, lon)
    }

    // User Location
    fun fetchUserLocation(context: Context, fusedLocationClient: FusedLocationProviderClient) {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val lat = it.latitude
                    val lon = it.longitude
                    updateCoordinates(lat, lon)
                    reverseGeocode(context, lat, lon)
                }
            }
        } catch (e: SecurityException) {
            setLocationError(true)
            e.printStackTrace()
        }
    }

    fun selectLocation(placeName: String, context: Context) {
        val geocoder = getGeocoder(context)
        geocoder.getFromLocationName(
            placeName,
            1,
            object : Geocoder.GeocodeListener {
                override fun onGeocode(addresses: MutableList<Address>) {
                    handleGeocodeResult(addresses, fallback = {
                        setLocationError(true)
                    })
                }

                override fun onError(errorMessage: String?) {
                    setLocationError(true)
                }
            }
        )
    }

    fun fetchLocationAndSave(
        context: Context,
        fusedLocationClient: FusedLocationProviderClient,
        viewModel: SensorViewModel,
        onSavingStarted: () -> Unit = {},
        onSavingFinished: () -> Unit = {},
        onSaveSuccess: () -> Unit = {}
    ) {
        onSavingStarted()

        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val lat = location.latitude
                    val lon = location.longitude
                    updateCoordinates(lat, lon)
                    viewModel.setCurrentLocation(formatLatLong(lat, lon), Pair(lat, lon))
                } else {
                    viewModel.setCurrentLocation("Unknown Location", null)
                }

                viewModel.saveToFirestore()
                onSavingFinished()
                onSaveSuccess()
            }
        } catch (e: SecurityException) {
            onSavingFinished()
            e.printStackTrace()
        }
    }

    private fun reverseGeocode(context: Context, lat: Double, lon: Double) {
        val geocoder = getGeocoder(context)
        geocoder.getFromLocation(
            lat,
            lon,
            1,
            object : Geocoder.GeocodeListener {
                override fun onGeocode(addresses: MutableList<Address>) {
                    handleGeocodeResult(addresses, fallback = {
                        displayLocationText = formatLatLong(lat, lon)
                    })
                }

                override fun onError(errorMessage: String?) {
                    displayLocationText = formatLatLong(lat, lon)
                }
            }
        )
    }

    private fun handleGeocodeResult(
        addresses: MutableList<Address>,
        fallback: () -> Unit
    ) {
        val address = addresses.firstOrNull()
        if (address != null) {
            val lat = address.latitude
            val lon = address.longitude
            updateCoordinates(lat, lon)
            displayLocationText = formatAddressOrCoordinates(address, lat, lon)
            setLocationError(false)
        } else {
            fallback()
        }
    }

    fun updateCoordinates(lat: Double, lon: Double) {
        _coordinatesState.postValue(Pair(lat, lon))
        setLocationError(false)
    }

    private fun formatAddressOrCoordinates(address: Address?, lat: Double, lon: Double): String {
        return address?.let {
            listOfNotNull(it.locality, it.adminArea)
                .takeIf { it.isNotEmpty() }
                ?.joinToString(", ")
        } ?: formatLatLong(lat, lon)
    }

    fun formatLatLong(lat: Double, lon: Double): String {
        val latDir = if (lat >= 0) "N" else "S"
        val lonDir = if (lon >= 0) "E" else "W"
        return "%.4f°%s, %.4f°%s".format(
            kotlin.math.abs(lat), latDir,
            kotlin.math.abs(lon), lonDir
        )
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                LocationViewModel()
            }
        }
    }
}

