package com.example.tamanbacaan.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.provider.Settings
import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await

// Correct coordinates for Universitas Multimedia Nusantara, Gading Serpong
private const val UMN_LAT = -6.2576
private const val UMN_LON = 106.6186
private const val RADIUS_METERS = 500f // within 500 meters counts as "di perpustakaan"

// Set to true untuk testing/development agar bisa pinjam buku tanpa harus di perpustakaan
const val DEBUG_SKIP_LOCATION_CHECK = true

@SuppressLint("MissingPermission")
suspend fun isUserAtUmn(context: Context): Boolean {
    // Skip location check in debug mode
    if (DEBUG_SKIP_LOCATION_CHECK) {
        Log.d("LocationCheck", "DEBUG MODE: Skipping location check")
        return true
    }

    val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    if (!hasPermission) {
        Log.w("LocationCheck", "Location permission not granted.")
        return false
    }

    return try {
        val fused = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
        val loc: Location? = fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
        if (loc == null) {
            Log.w("LocationCheck", "Fused location returned null. Maybe location is turned off?")
            return false
        }

        val results = FloatArray(1)
        Location.distanceBetween(loc.latitude, loc.longitude, UMN_LAT, UMN_LON, results)
        val distance = results[0]
        Log.d("LocationCheck", "Distance to UMN center: $distance meters")

        distance <= RADIUS_METERS
    } catch (e: Exception) {
        Log.e("LocationCheck", "Error getting location", e)
        false
    }
}

@Composable
fun LibraryLocationIndicator(
    modifier: Modifier = Modifier,
    onStatusChanged: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    var permissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    var showSettingsPrompt by remember { mutableStateOf(false) }
    var hasRequestedPermission by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms: Map<String, Boolean> ->
        val fine = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarse = perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        val granted = fine || coarse
        Log.d("LocationIndicator", "Permission callback: fine=$fine coarse=$coarse")
        permissionGranted = granted
        if (granted) {
            Toast.makeText(context, "Izin lokasi diberikan", Toast.LENGTH_SHORT).show()
            showSettingsPrompt = false
        } else {
            var activity: Activity? = null
            var ctx = context
            while (activity == null && ctx is android.content.ContextWrapper) {
                if (ctx is Activity) activity = ctx
                else ctx = ctx.baseContext
            }

            val shouldShowRationale = activity?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.ACCESS_FINE_LOCATION)
            } ?: false

            if (!shouldShowRationale && hasRequestedPermission) {
                showSettingsPrompt = true
                Toast.makeText(context, "Izin lokasi diblokir. Buka pengaturan untuk mengizinkan.", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Izin lokasi ditolak", Toast.LENGTH_SHORT).show()
            }
        }
    }

    var isAtLibrary by remember { mutableStateOf<Boolean?>(null) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val newPermissionStatus = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                if (newPermissionStatus != permissionGranted) {
                    permissionGranted = newPermissionStatus
                    if(newPermissionStatus) showSettingsPrompt = false
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }


    LaunchedEffect(permissionGranted) {
        if (permissionGranted) {
            val at = isUserAtUmn(context)
            isAtLibrary = at
            onStatusChanged(at)
        } else {
            isAtLibrary = null
            onStatusChanged(false)
        }
    }

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        when {
            !permissionGranted && !showSettingsPrompt -> {
                OutlinedButton(onClick = {
                    hasRequestedPermission = true
                    launcher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                }) {
                    Text("Izinkan Lokasi")
                }
            }
            !permissionGranted && showSettingsPrompt -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Izin lokasi diblokir", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = Uri.fromParts("package", context.packageName, null)
                        context.startActivity(intent)
                     }) {
                         Text("Buka Pengaturan")
                     }
                 }
             }
            isAtLibrary == true -> {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(4.dp)) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF4CAF50)))
                    Spacer(Modifier.width(6.dp))
                    Text("Di Perpustakaan", style = MaterialTheme.typography.labelMedium)
                }
            }
            isAtLibrary == false -> {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(4.dp)) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFF44336)))
                    Spacer(Modifier.width(6.dp))
                    Text("Di Luar Perpustakaan", style = MaterialTheme.typography.labelMedium)
                }
            }
            else -> {
                Text("Memeriksa lokasi...", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
