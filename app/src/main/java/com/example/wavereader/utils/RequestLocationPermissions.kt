package com.example.wavereader.utils

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.PermissionStatus


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestLocationPermission(
    onDenied: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val permissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    when (permissionState.status) {
        is PermissionStatus.Granted -> {
            content()
        }
        is PermissionStatus.Denied -> {
            LaunchedEffect(Unit) {
                permissionState.launchPermissionRequest()
            }
            onDenied?.invoke()
        }
    }
}