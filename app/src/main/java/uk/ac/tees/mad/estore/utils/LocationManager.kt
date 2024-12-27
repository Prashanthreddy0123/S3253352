package uk.ac.tees.mad.estore.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LocationManager @Inject constructor(
    private val application: Context
) {
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(application)
    }

    suspend fun getCurrentLocation(): Location {
        return suspendCancellableCoroutine { continuation ->
            try {
                if (checkLocationPermission()) {
                    val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                        .setMinUpdateDistanceMeters(10f)
                        .build()

                    val locationCallback = object : LocationCallback() {
                        override fun onLocationResult(result: LocationResult) {
                            result.lastLocation?.let { location ->
                                continuation.resume(location)
                            } ?: continuation.resumeWithException(
                                Exception("Unable to get location")
                            )
                            fusedLocationClient.removeLocationUpdates(this)
                        }
                    }

                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.getMainLooper()
                    ).addOnFailureListener { exception ->
                        continuation.resumeWithException(exception)
                    }

                    // Set up cancellation
                    continuation.invokeOnCancellation {
                        fusedLocationClient.removeLocationUpdates(locationCallback)
                    }
                } else {
                    continuation.resumeWithException(
                        SecurityException("Location permission not granted")
                    )
                }
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }
    }

    suspend fun getAddressFromLocation(location: Location): Address {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(application, Locale.getDefault())

                @Suppress("DEPRECATION")
                val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine { continuation ->
                        geocoder.getFromLocation(
                            location.latitude,
                            location.longitude,
                            1
                        ) { addresses ->
                            if (addresses.isNotEmpty()) {
                                continuation.resume(addresses)
                            } else {
                                continuation.resumeWithException(
                                    Exception("No address found")
                                )
                            }
                        }
                    }
                } else {
                    geocoder.getFromLocation(location.latitude, location.longitude, 1)
                }

                addresses?.firstOrNull() ?: throw Exception("No address found")
            } catch (e: Exception) {
                throw Exception("Error getting address: ${e.message}")
            }
        }
    }

    fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            application,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    application,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    suspend fun formatAddress(address: Address): String {
        return buildString {
            address.getAddressLine(0)?.let { append(it) }
        }
    }
}
