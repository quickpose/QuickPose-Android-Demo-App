package ai.quickpose.demo

import ai.quickpose.demo.ui.theme.Accent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val SDK_KEY_URL = "https://dev.quickpose.ai"

@Composable
fun LandingScreen(onCategorySelected: (FeatureCategory) -> Unit) {
    val uriHandler = LocalUriHandler.current
    var showSdkKeyAlert by rememberSaveable { mutableStateOf(!QuickPoseConfig.hasSdkKey) }

    Column(
            modifier = Modifier
                    .fillMaxSize()
                    .background(Accent)
                    .verticalScroll(rememberScrollState())
                    .safeDrawingPadding()
                    .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.size(8.dp))
        Text("QuickPose Demos", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold)
        Text(
                "Pick a feature to try it live with your camera",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 15.sp
        )
        Spacer(Modifier.size(16.dp))

        FeatureCategory.entries.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { category ->
                    CategoryCard(category, Modifier.weight(1f)) { onCategorySelected(category) }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.size(12.dp))
        }

        Column(
                modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.15f))
        ) {
            LinkRow(Icons.Filled.Key, "Create a free SDK key", "dev.quickpose.ai") { uriHandler.openUri(SDK_KEY_URL) }
            HorizontalDivider(color = Color.White.copy(alpha = 0.2f), modifier = Modifier.padding(start = 56.dp))
            LinkRow(Icons.AutoMirrored.Filled.MenuBook, "Documentation", "docs.quickpose.ai") { uriHandler.openUri("https://docs.quickpose.ai") }
            HorizontalDivider(color = Color.White.copy(alpha = 0.2f), modifier = Modifier.padding(start = 56.dp))
            LinkRow(Icons.Filled.Code, "GitHub", "github.com/quickpose") { uriHandler.openUri("https://github.com/quickpose") }
        }
        Spacer(Modifier.size(24.dp))
    }

    if (showSdkKeyAlert) {
        AlertDialog(
                onDismissRequest = { showSdkKeyAlert = false },
                title = { Text("SDK Key Required") },
                text = {
                    Text("The demos need a QuickPose SDK key to run. Register for a free key at dev.quickpose.ai, then paste it into QuickPoseConfig.SDK_KEY in QuickPoseConfig.kt.")
                },
                confirmButton = {
                    TextButton(onClick = {
                        showSdkKeyAlert = false
                        uriHandler.openUri(SDK_KEY_URL)
                    }) { Text("Get a free SDK key") }
                },
                dismissButton = {
                    TextButton(onClick = { showSdkKeyAlert = false }) { Text("Later") }
                }
        )
    }
}

@Composable
private fun CategoryCard(category: FeatureCategory, modifier: Modifier, onClick: () -> Unit) {
    Column(
            modifier = modifier
                    .heightIn(min = 150.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .clickable(onClick = onClick)
                    .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(category.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(34.dp))
        Text(category.title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        Text(category.subtitle, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, lineHeight = 16.sp)
    }
}

@Composable
private fun LinkRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
            modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.width(28.dp))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
        }
        Icon(Icons.Filled.NorthEast, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
    }
}
