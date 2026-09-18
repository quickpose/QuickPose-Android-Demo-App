package ai.quickpose.demo

import ai.quickpose.camera.QuickPoseCameraSwitchView
import ai.quickpose.core.Feature
import ai.quickpose.core.FeatureResult
import ai.quickpose.core.QuickPose
import ai.quickpose.core.QuickPoseThresholdCounter
import ai.quickpose.core.QuickPoseThresholdTimer
import ai.quickpose.core.Status
import ai.quickpose.demo.ui.theme.Accent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

/** What the bottom panel shows for the running feature. */
private sealed interface Readout {
    data class Count(val value: Int, val label: String, val progress: Float?) : Readout
    data class Hold(val seconds: Double, val progress: Float) : Readout
}

@Composable
fun DemoScreen(item: DemoItem, cameraGranted: Boolean, onRequestCamera: () -> Unit, onBack: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        if (cameraGranted) {
            CameraDemo(item, onBack)
        } else {
            Column(
                    modifier = Modifier.align(Alignment.Center).padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                        "Camera access is required to run the QuickPose demos.",
                        color = Color.White,
                        fontSize = 17.sp,
                        textAlign = TextAlign.Center
                )
                Button(onClick = onRequestCamera, colors = ButtonDefaults.buttonColors(containerColor = Accent)) {
                    Text("Allow camera")
                }
            }
            Box(Modifier.statusBarsPadding().padding(16.dp)) {
                CircleButton(Icons.AutoMirrored.Filled.ArrowBack, "Back", onBack)
            }
        }
    }
}

@Composable
private fun CameraDemo(item: DemoItem, onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    val quickPose = remember { QuickPose(context, QuickPoseConfig.SDK_KEY) } // register for your free key at https://dev.quickpose.ai
    val cameraView = remember { QuickPoseCameraSwitchView(context, quickPose) }
    val features = remember(item) { item.features.toTypedArray() }
    val primary = features.first()
    val counter = remember { QuickPoseThresholdCounter() }
    val timer = remember { QuickPoseThresholdTimer() }

    var useFrontCamera by remember { mutableStateOf(true) }
    var switchingCamera by remember { mutableStateOf(false) }
    var readout by remember { mutableStateOf<Readout?>(null) }
    var feedbackText by remember { mutableStateOf<String?>(null) }
    var statusText by remember { mutableStateOf("Powered by QuickPose.ai") }

    DisposableEffect(lifecycleOwner, quickPose) {
        // Bumped on every stop so a camera that finishes opening after the user has
        // left (or paused) is shut down instead of starting a QuickPose session nobody stops.
        var generation = 0
        var running = false

        fun start() {
            if (running) return
            running = true
            val startedGeneration = ++generation
            scope.launch {
                cameraView.start(useFrontCamera)
                if (startedGeneration != generation) {
                    cameraView.stop()
                    return@launch
                }
                quickPose.start(features, onFrame = { status, _, results, feedback, _ ->
                    when (status) {
                        is Status.Success -> {
                            val newReadout = readoutFor(primary, results[primary], counter, timer)
                            val newFeedback = feedback[primary]?.displayString
                            mainHandler.post {
                                statusText = "Powered by QuickPose.ai v${quickPose.quickPoseVersion()}\n${status.fps} fps"
                                if (newReadout != null) readout = newReadout
                                feedbackText = newFeedback
                            }
                        }
                        is Status.SdkValidationError -> mainHandler.post {
                            statusText = "SDK key missing or invalid.\nGet a free key at dev.quickpose.ai"
                        }
                        else -> Unit
                    }
                })
            }
        }

        fun stop() {
            if (!running) return
            running = false
            generation++
            quickPose.stop()
            cameraView.stop()
            timer.stop()
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> start()
                Lifecycle.Event.ON_PAUSE -> stop()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            stop()
            mainHandler.removeCallbacksAndMessages(null)
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    Box(Modifier.fillMaxSize()) {
        AndroidView(factory = { cameraView }, modifier = Modifier.fillMaxSize())

        Row(
                modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CircleButton(Icons.AutoMirrored.Filled.ArrowBack, "Back", onBack)
            Text(
                    item.title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                            .clip(RoundedCornerShape(22.dp))
                            .background(Accent.copy(alpha = 0.8f))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
            )
            Spacer(Modifier.weight(1f))
            CircleButton(Icons.Filled.Share, "Share screenshot") { shareScreenshot(context, cameraView, mainHandler) }
            CircleButton(Icons.Filled.Cameraswitch, "Switch camera") {
                if (switchingCamera) return@CircleButton
                switchingCamera = true
                scope.launch {
                    useFrontCamera = !useFrontCamera
                    quickPose.stop()
                    cameraView.start(useFrontCamera)
                    quickPose.resume()
                    switchingCamera = false
                }
            }
        }

        feedbackText?.let {
            Text(
                    it,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 24.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Accent.copy(alpha = 0.8f))
                            .padding(16.dp)
            )
        }

        Column(
                modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            readout?.let { ReadoutPanel(it) }
            Text(
                    statusText,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
            )
        }
    }
}

/** Runs on the QuickPose frame thread: advances the rep counter / hold timer for this frame. */
private fun readoutFor(
        feature: Feature,
        result: FeatureResult?,
        counter: QuickPoseThresholdCounter,
        timer: QuickPoseThresholdTimer
): Readout? {
    if (result == null) return null
    return when (feature) {
        is Feature.Fitness -> {
            val progress = result.value.coerceIn(0f, 1f)
            if (result.stringValue.contains("plank", ignoreCase = true)) {
                timer.time(result.value)
                Readout.Hold(timer.getState().time, progress)
            } else {
                Readout.Count(counter.count(result.value).count, "REPS", progress)
            }
        }
        is Feature.RaisedFingers -> Readout.Count(result.value.toInt(), "Raised Fingers", null)
        is Feature.ThumbsUp -> Readout.Count(if (result.value > 0.7f) 1 else 0, "Thumbs Up", null)
        is Feature.ThumbsUpOrDown -> {
            val confident = result.value > 0.7f
            val value = when {
                confident && result.stringValue.contains("up", ignoreCase = true) -> 1
                confident && result.stringValue.contains("down", ignoreCase = true) -> -1
                else -> 0
            }
            Readout.Count(value, "Thumbs Up or Down", null)
        }
        else -> null
    }
}

@Composable
private fun ReadoutPanel(readout: Readout) {
    val (value, label, progress) = when (readout) {
        is Readout.Count -> Triple("${readout.value}", readout.label, readout.progress)
        is Readout.Hold -> Triple("%.1fs".format(readout.seconds), "HOLD", readout.progress)
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Column(
                modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Accent.copy(alpha = 0.6f))
                        .padding(horizontal = 28.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, color = Color.White, fontSize = 72.sp, fontWeight = FontWeight.Bold, lineHeight = 76.sp)
            Text(label.uppercase(), color = Color.White.copy(alpha = 0.9f), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
        if (progress != null) {
            Spacer(Modifier.size(12.dp))
            Box(
                    Modifier
                            .fillMaxWidth(0.8f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.25f))
            ) {
                Box(
                        Modifier
                                .fillMaxWidth(progress)
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White)
                )
            }
        }
    }
}

@Composable
private fun CircleButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    Box(
            modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Accent.copy(alpha = 0.8f))
                    .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = description, tint = Color.White)
    }
}

private fun shareScreenshot(context: Context, cameraView: QuickPoseCameraSwitchView, mainHandler: Handler) {
    cameraView.captureFrame { bitmap ->
        if (bitmap == null) {
            mainHandler.post { Toast.makeText(context, "Capture failed", Toast.LENGTH_SHORT).show() }
            return@captureFrame
        }
        val dir = File(context.cacheDir, "shared").apply { mkdirs() }
        val file = File(dir, "quickpose_screenshot.jpg")
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it) }
        bitmap.recycle()
        mainHandler.post {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Screenshot"))
        }
    }
}
