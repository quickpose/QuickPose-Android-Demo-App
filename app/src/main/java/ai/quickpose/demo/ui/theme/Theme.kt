package ai.quickpose.demo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ColorScheme = lightColorScheme(
    primary = Accent,
    onPrimary = Color.White,
    secondary = AccentDark,
    background = ListBackground,
    surface = Color.White,
)

@Composable
fun QuickPoseAndroidDemoAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ColorScheme,
        typography = Typography,
        content = content
    )
}
