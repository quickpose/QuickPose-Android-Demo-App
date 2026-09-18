package ai.quickpose.demo

import ai.quickpose.demo.ui.theme.QuickPoseAndroidDemoAppTheme
import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat

sealed interface Screen {
    data object Landing : Screen
    data class Category(val category: FeatureCategory) : Screen
    data class Demo(val item: DemoItem) : Screen
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuickPoseAndroidDemoAppTheme {
                DemoApp()
            }
        }
    }
}

@Composable
fun DemoApp() {
    val context = LocalContext.current
    val backStack = remember { mutableStateListOf<Screen>(Screen.Landing) }
    var cameraGranted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        cameraGranted = granted
    }
    LaunchedEffect(Unit) {
        if (!cameraGranted) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    val current = backStack.last()
    BackHandler(enabled = backStack.size > 1) { backStack.removeAt(backStack.lastIndex) }
    val goBack: () -> Unit = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) }

    when (current) {
        Screen.Landing -> {
            LightStatusBarIcons(light = false)
            LandingScreen(onCategorySelected = { backStack.add(Screen.Category(it)) })
        }
        is Screen.Category -> {
            LightStatusBarIcons(light = true)
            FeatureListScreen(
                    category = current.category,
                    onBack = goBack,
                    onItemSelected = { backStack.add(Screen.Demo(it)) }
            )
        }
        is Screen.Demo -> {
            LightStatusBarIcons(light = false)
            DemoScreen(
                    item = current.item,
                    cameraGranted = cameraGranted,
                    onRequestCamera = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    onBack = goBack
            )
        }
    }
}

/** Dark status bar icons on light screens, white icons on the accent and camera screens. */
@Composable
private fun LightStatusBarIcons(light: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return
    SideEffect {
        val window = (view.context as Activity).window
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = light
    }
}
