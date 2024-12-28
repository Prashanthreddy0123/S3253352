package uk.ac.tees.mad.estore.ui.screens.profile

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {

                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }

                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ProfileImageSection(
                    imageUri = uiState.profilePictureUri,
                    isEditing = uiState.isEditing,
                    onCameraClick = {

                    },
                    onGalleryClick = {

                    }
                )

                ProfileDetailsSection(
                    uiState = uiState,
                    onValueChange = viewModel::updateField
                )

                if (uiState.isEditing) {
                    Button(
                        onClick = { },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text("Save Changes")
                    }
                }

                Button(
                    onClick = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Sign Out")
                }
            }
        }
    }
}

@Composable
fun ProfileImageSection(
    imageUri: Uri?,
    isEditing: Boolean,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(16.dp)
            .size(120.dp)
    ) {
        // Profile Image
        imageUri?.let { img ->
            AsyncImage(
                model = img,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } ?: Image(
            imageVector = Icons.Outlined.Person,
            contentDescription = "Profile Picture",
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        // Edit
        if (isEditing) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(4.dp)
            ) {
                IconButton(
                    onClick = onCameraClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.PhotoCamera,
                        contentDescription = "Take Photo",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = onGalleryClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.PhotoLibrary,
                        contentDescription = "Choose from Gallery",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileDetailsSection(
    uiState: ProfileUiState,
    onValueChange: (ProfileUiState) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = uiState.name,
            onValueChange = { onValueChange(uiState.copy(name = it)) },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.isEditing
        )

        OutlinedTextField(
            value = uiState.email,
            onValueChange = { onValueChange(uiState.copy(email = it)) },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            enabled = false // Email cannot be edited
        )

        OutlinedTextField(
            value = uiState.phone,
            onValueChange = { onValueChange(uiState.copy(phone = it)) },
            label = { Text("Phone") },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.isEditing,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )

        OutlinedTextField(
            value = uiState.address,
            onValueChange = { onValueChange(uiState.copy(address = it)) },
            label = { Text("Address") },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.isEditing,
            maxLines = 3
        )
    }
}