package ai.quickpose.demo

import ai.quickpose.demo.ui.theme.Accent
import ai.quickpose.demo.ui.theme.ListBackground
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureListScreen(category: FeatureCategory, onBack: () -> Unit, onItemSelected: (DemoItem) -> Unit) {
    val context = LocalContext.current
    val sections = remember(category) { category.sections(context) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = ListBackground,
            topBar = {
                LargeTopAppBar(
                        title = { Text(category.title, fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = ListBackground,
                                scrolledContainerColor = ListBackground
                        ),
                        scrollBehavior = scrollBehavior
                )
            }
    ) { innerPadding ->
        LazyColumn(contentPadding = innerPadding, modifier = Modifier.padding(horizontal = 16.dp)) {
            sections.forEach { section ->
                if (section.name.isNotEmpty()) {
                    item(key = "header-${section.name}") {
                        Text(
                                section.name,
                                color = Color.Gray,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 8.dp)
                        )
                    }
                } else {
                    item { Spacer(Modifier.size(8.dp)) }
                }
                itemsIndexed(section.items) { index, item ->
                    val shape = when {
                        section.items.size == 1 -> RoundedCornerShape(16.dp)
                        index == 0 -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                        index == section.items.lastIndex -> RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                        else -> RoundedCornerShape(0.dp)
                    }
                    Row(
                            modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(shape)
                                    .background(Color.White)
                                    .clickable { onItemSelected(item) }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(category.icon, contentDescription = null, tint = Accent, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(16.dp))
                        Text(item.title, fontSize = 17.sp, modifier = Modifier.weight(1f))
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.LightGray)
                    }
                    if (index != section.items.lastIndex) {
                        HorizontalDivider(
                                color = Color(0xFFE5E5EA),
                                modifier = Modifier
                                        .background(Color.White)
                                        .padding(start = 54.dp)
                        )
                    }
                }
            }
            item { Spacer(Modifier.size(24.dp)) }
        }
    }
}
