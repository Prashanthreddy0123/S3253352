package uk.ac.tees.mad.estore.ui.screens.profile

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import uk.ac.tees.mad.estore.utils.LocationManager
import uk.ac.tees.mad.estore.utils.LocationUtils
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val locationManager: LocationManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    private var tempImageUri: Uri? = null

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                val user = auth.currentUser ?: throw Exception("User not found")

                val userData = firestore.collection("users")
                    .document(user.uid)
                    .get()
                    .await()

                val profileData = userData.data
                    ?: throw Exception("Profile data not found")

                val pp = profileData["profilePicture"] as? String

                _uiState.update {
                    it.copy(
                        name = profileData["name"] as? String ?: "",
                        email = user.email ?: "",
                        phone = profileData["phone"] as? String ?: "",
                        address = profileData["address"] as? String ?: "",
                        profilePictureUri = pp?.let { Uri.parse(it) },
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Failed to load profile",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun startEditing() {
        _uiState.update { it.copy(isEditing = true) }
    }

    fun updateField(value: ProfileUiState) {
        _uiState.update { value }
    }

    fun saveChanges() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                val user = auth.currentUser ?: throw Exception("User not found")

                val userData: HashMap<String, Any> = hashMapOf(
                    "name" to uiState.value.name,
                    "phone" to uiState.value.phone,
                    "address" to uiState.value.address
                )
                tempImageUri?.let { uri ->
                    val task = storage.reference
                        .child("profile_pictures/${user.uid}")
                        .putFile(uri)
                        .await()
                    userData["profilePicture"] = task.storage.downloadUrl.await()
                }


                firestore.collection("users")
                    .document(user.uid)
                    .update(userData)
                    .await()

                _uiState.update {
                    it.copy(
                        isEditing = false,
                        isLoading = false
                    )
                }

                loadUserProfile() // Refreshing profile data
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Failed to save changes",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun updateProfilePicture(uri: Uri) {
        tempImageUri = uri
        _uiState.update { it.copy(profilePictureUri = uri) }
    }

    fun launchCamera(context: Context, launcher: ActivityResultLauncher<Uri>) {
        val imageUri = createImageUri(context)
        tempImageUri = imageUri
        launcher.launch(imageUri)
    }

    fun refreshProfilePicture() {
        _uiState.update { it.copy(profilePictureUri = tempImageUri) }
    }

    private fun createImageUri(context: Context): Uri {
        val imageFile = File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "profile_picture_${System.currentTimeMillis()}.jpg"
        )
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            imageFile
        )
    }

    fun getCurrentLocation(context: Context) {
        if (locationManager.checkLocationPermission()) {
            viewModelScope.launch {
                try {
                    _uiState.update { it.copy(isLoadingLocation = true) }

                    // Get the location
                    val location = locationManager.getCurrentLocation()

                    // Convert location to address
                    val address = locationManager.getAddressFromLocation(location)

                    // Format the address
                    val formattedAddress = locationManager.formatAddress(address)

                    _uiState.update {
                        it.copy(
                            address = formattedAddress,
                            isLoadingLocation = false
                        )
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            error = e.message ?: "Error getting location",
                            isLoadingLocation = false
                        )
                    }
                }
            }
        } else {
            // Request location permission if not granted
            if (context is ComponentActivity) {
                LocationUtils.requestLocationPermission(context)
            }
        }


    }

    fun signOut() {
        auth.signOut()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}


data class ProfileUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val profilePictureUri: Uri? = null,
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val isLoadingLocation: Boolean = false,
    val error: String? = null
)

enum class ProfileField {
    NAME, EMAIL, PHONE, ADDRESS
}

data class UserProfile(
    val name: String = "",
    val phone: String = "",
    val address: String = "",
    val profilePicture: String = ""
)