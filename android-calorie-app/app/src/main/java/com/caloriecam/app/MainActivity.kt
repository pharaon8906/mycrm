package com.caloriecam.app

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.caloriecam.app.data.UserProfileRepository
import com.caloriecam.app.ui.CaptureScreen
import com.caloriecam.app.ui.HomeScreen
import com.caloriecam.app.ui.ProfileScreen
import com.caloriecam.app.ui.StatsScreen
import com.caloriecam.app.ui.TipsScreen
import com.caloriecam.app.ui.theme.CalorieCamTheme
import com.caloriecam.app.util.PhotoStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalorieCamTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CalorieCamRoot()
                }
            }
        }
    }
}

@Composable
private fun CalorieCamRoot() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pendingPhotoPath by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingCameraFile by remember { mutableStateOf<File?>(null) }
    var showProfile by rememberSaveable { mutableStateOf(false) }
    var showTips by rememberSaveable { mutableStateOf(false) }
    var showStats by rememberSaveable { mutableStateOf(false) }
    val savedProfile by UserProfileRepository.profileFlow(context).collectAsState(initial = null)

    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingPhotoPath = pendingCameraFile?.absolutePath
        } else {
            pendingCameraFile?.let { PhotoStore.delete(it) }
        }
        pendingCameraFile = null
    }

    fun launchCamera() {
        val file = PhotoStore.newCameraFile(context)
        val uri: Uri = PhotoStore.contentUriFor(context, file)
        pendingCameraFile = file
        takePictureLauncher.launch(uri)
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) launchCamera() }

    val pickMediaLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val file = withContext(Dispatchers.IO) { PhotoStore.copyToInternal(context, uri) }
                pendingPhotoPath = file.absolutePath
            }
        }
    }

    val photoPath = pendingPhotoPath
    when {
        showProfile -> {
            ProfileScreen(
                initial = savedProfile,
                onDone = { showProfile = false },
                onCancel = { showProfile = false }
            )
        }
        showTips -> {
            TipsScreen(
                profile = savedProfile,
                onBack = { showTips = false }
            )
        }
        showStats -> {
            StatsScreen(onBack = { showStats = false })
        }
        photoPath != null -> {
            CaptureScreen(
                photoFile = File(photoPath),
                onDone = { pendingPhotoPath = null },
                onDiscard = {
                    PhotoStore.delete(File(photoPath))
                    pendingPhotoPath = null
                }
            )
        }
        else -> {
            HomeScreen(
                onCameraClick = {
                    val granted = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                    if (granted) launchCamera() else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                },
                onGalleryClick = {
                    pickMediaLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onProfileClick = { showProfile = true },
                onTipsClick = { showTips = true },
                onStatsClick = { showStats = true }
            )
        }
    }
}
