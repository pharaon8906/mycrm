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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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

private data class BottomTab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val bottomTabs = listOf(
    BottomTab("Головна", Icons.Default.Home),
    BottomTab("Прогрес", Icons.Default.ShowChart),
    BottomTab("Поради", Icons.Default.FitnessCenter),
    BottomTab("Профіль", Icons.Default.PersonOutline)
)

@Composable
private fun CalorieCamRoot() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pendingPhotoPath by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingCameraFile by remember { mutableStateOf<File?>(null) }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
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
    if (photoPath != null) {
        CaptureScreen(
            photoFile = File(photoPath),
            onDone = { pendingPhotoPath = null },
            onDiscard = {
                PhotoStore.delete(File(photoPath))
                pendingPhotoPath = null
            }
        )
        return
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomTabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> HomeScreen(
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
                    }
                )
                1 -> StatsScreen()
                2 -> TipsScreen(profile = savedProfile)
                else -> ProfileScreen(
                    initial = savedProfile,
                    onDone = { selectedTab = 0 }
                )
            }
        }
    }
}
