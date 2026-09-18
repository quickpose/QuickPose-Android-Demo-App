package ai.quickpose.demo

import ai.quickpose.core.Feature
import ai.quickpose.core.FitnessFeature
import ai.quickpose.core.Landmarks
import ai.quickpose.core.RangeOfMotion
import ai.quickpose.core.Side
import ai.quickpose.core.Style
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.ui.graphics.vector.ImageVector

/** One launchable demo: a title shown in the menu and the QuickPose features it runs. */
data class DemoItem(val title: String, val features: List<Feature>)

data class DemoSection(val name: String, val items: List<DemoItem>)

enum class FeatureCategory(val title: String, val icon: ImageVector, val subtitle: String) {
    FITNESS("Fitness", Icons.Filled.FitnessCenter, "Squats, push-ups, planks and more, with rep counting"),
    HEALTH("Health", Icons.Filled.Favorite, "Range of motion for shoulder, hip, knee, neck and back"),
    SPORTS("Sports", Icons.AutoMirrored.Filled.DirectionsBike, "Cycling and rowing joint angles"),
    INPUT("Input", Icons.Filled.PanTool, "Raised fingers, thumbs up and thumbs down"),
    OVERLAY_COLOURS("Overlay Colours", Icons.Filled.Brush, "Styled skeleton overlays: colours, glows, outlines and image fills"),
    CONDITIONAL("Conditional", Icons.Filled.Palette, "Overlays that change colour on thresholds"),
    GENERAL("General", Icons.Filled.AccessibilityNew, "Skeleton overlays and body landmarks");

    fun sections(context: Context): List<DemoSection> = when (this) {
        FITNESS -> DemoCatalog.fitnessSections()
        OVERLAY_COLOURS -> listOf(DemoSection("", DemoCatalog.overlayStylePresets(context)))
        HEALTH -> listOf(DemoSection("", DemoCatalog.health()))
        SPORTS -> listOf(DemoSection("", DemoCatalog.sports()))
        INPUT -> listOf(DemoSection("", DemoCatalog.input()))
        CONDITIONAL -> listOf(DemoSection("", DemoCatalog.conditional()))
        GENERAL -> listOf(DemoSection("", DemoCatalog.general()))
    }
}

object DemoCatalog {

    private fun Feature.asItem(title: String = displayString()) =
            DemoItem(title.replaceFirstChar { it.uppercase() }, listOf(this))

    private fun fitness(exercise: FitnessFeature) = Feature.Fitness(exercise).asItem()

    fun fitnessSections(): List<DemoSection> = listOf(
            DemoSection("Legs & Glutes", listOf(
                    fitness(FitnessFeature.Squats),
                    fitness(FitnessFeature.SumoSquats),
                    fitness(FitnessFeature.Lunges(Side.LEFT)),
                    fitness(FitnessFeature.Lunges(Side.RIGHT)),
                    fitness(FitnessFeature.SideLunges(Side.LEFT)),
                    fitness(FitnessFeature.SideLunges(Side.RIGHT)),
                    fitness(FitnessFeature.GluteBridge),
                    fitness(FitnessFeature.HipAbductionStanding(Side.LEFT)),
                    fitness(FitnessFeature.HipAbductionStanding(Side.RIGHT)),
            )),
            DemoSection("Core", listOf(
                    fitness(FitnessFeature.SitUps),
                    fitness(FitnessFeature.VUps),
                    fitness(FitnessFeature.LegRaises),
                    fitness(FitnessFeature.CobraWings),
                    fitness(FitnessFeature.Plank),
                    fitness(FitnessFeature.PlankStraightArm),
            )),
            DemoSection("Upper Body", listOf(
                    fitness(FitnessFeature.PushUps),
                    fitness(FitnessFeature.FrontPushUps),
                    fitness(FitnessFeature.BicepCurls),
                    fitness(FitnessFeature.BicepCurlsSingleArm(Side.LEFT)),
                    fitness(FitnessFeature.BicepCurlsSingleArm(Side.RIGHT)),
                    fitness(FitnessFeature.OverheadDumbbellPress),
                    fitness(FitnessFeature.LateralRaises),
                    fitness(FitnessFeature.FrontRaises),
            )),
            DemoSection("Cardio", listOf(
                    fitness(FitnessFeature.JumpingJacks),
                    fitness(FitnessFeature.Skipping),
                    fitness(FitnessFeature.HighKneeTaps),
                    fitness(FitnessFeature.ShoulderTaps),
                    fitness(FitnessFeature.MountainClimbers),
                    fitness(FitnessFeature.Boxing),
                    fitness(FitnessFeature.Burpees),
            )),
    )

    fun health(): List<DemoItem> = listOf(
            Feature.RangeOfMotion(RangeOfMotion.Shoulder(Side.LEFT, false)).asItem(),
            Feature.RangeOfMotion(RangeOfMotion.Shoulder(Side.RIGHT, true)).asItem(),
            Feature.RangeOfMotion(RangeOfMotion.Hip(Side.RIGHT, true)).asItem(),
            Feature.RangeOfMotion(RangeOfMotion.Knee(Side.RIGHT, true)).asItem(),
            Feature.RangeOfMotion(RangeOfMotion.Neck(false)).asItem(),
            Feature.RangeOfMotion(RangeOfMotion.Back(false)).asItem(),
    )

    fun sports(): List<DemoItem> {
        val bikeStyle = Style(relativeFontSize = 0.33f, relativeArcSize = 0.4f, relativeLineWidth = 0.3f)
        return listOf(
                DemoItem("Cycling/Rowing", listOf(
                        Feature.RangeOfMotion(RangeOfMotion.Shoulder(Side.RIGHT, false), bikeStyle),
                        Feature.RangeOfMotion(RangeOfMotion.Elbow(Side.RIGHT, false), bikeStyle),
                        Feature.RangeOfMotion(RangeOfMotion.Hip(Side.RIGHT, false), bikeStyle),
                        Feature.RangeOfMotion(RangeOfMotion.Knee(Side.RIGHT, true), bikeStyle),
                ))
        )
    }

    fun input(): List<DemoItem> = listOf(
            Feature.RaisedFingers().asItem(),
            Feature.ThumbsUp().asItem(),
            Feature.ThumbsUpOrDown().asItem(),
    )

    fun conditional(): List<DemoItem> {
        val greenStyle = Style(conditionalColors = listOf(Style.ConditionalColor(min = 40f, max = null, color = Color.valueOf(Color.GREEN))))
        val redStyle = Style(conditionalColors = listOf(Style.ConditionalColor(min = 180f, max = null, color = Color.valueOf(Color.RED))))
        return listOf(
                Feature.RangeOfMotion(RangeOfMotion.Shoulder(Side.LEFT, false), greenStyle).asItem(),
                Feature.RangeOfMotion(RangeOfMotion.Knee(Side.RIGHT, true), redStyle).asItem(),
        )
    }

    fun general(): List<DemoItem> =
            Landmarks.Group.commonLimbs().map { Feature.Overlay(it).asItem() } +
                    Feature.ShowPoints().asItem()

    fun overlayStylePresets(context: Context): List<DemoItem> {
        val white = Color.valueOf(Color.WHITE)
        val green = Color.valueOf(Color.GREEN)
        val black = Color.valueOf(Color.BLACK)
        val presets = listOf(
                "Classic White" to Style(),
                "Green" to Style(color = green),
                "Red" to Style(color = Color.valueOf(Color.RED)),
                "Thick Lines" to Style(relativeLineWidth = 2f),
                "Dashed" to Style(linePattern = Style.LinePattern.DASHED),
                "Dotted" to Style(linePattern = Style.LinePattern.DOTTED),
                "Glow" to Style(color = green, shadow = Style.Shadow(color = green, radius = 32f, offsetX = 0f, offsetY = 0f)),
                "Shadow" to Style(color = white, shadow = Style.Shadow(color = black, radius = 14f, offsetX = 0f, offsetY = 10f)),
                "Outlined" to Style(outline = Style.Outline(color = black, relativeWidth = 0.6f)),
                "Orange Glow Fill" to Style(relativeLineWidth = 2f, imageFill = StyleTextures.orangeGlow),
                "Galaxy Fill" to Style(relativeLineWidth = 2f, imageFill = StyleTextures.galaxy(context)),
        )
        return presets.map { (title, style) -> DemoItem(title, listOf(Feature.Overlay(Landmarks.Group.WholeBody(), style))) }
    }
}

/**
 * Image fills for the overlay style presets: a bundled photo (NASA Hubble Ultra
 * Deep Field, public domain) and one procedural orange radial gradient.
 */
object StyleTextures {
    private var galaxyBitmap: Bitmap? = null

    fun galaxy(context: Context): Bitmap =
            galaxyBitmap ?: BitmapFactory.decodeResource(context.resources, R.drawable.galaxy).also { galaxyBitmap = it }

    val orangeGlow: Bitmap by lazy {
        val width = 360
        val height = 640
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val paint = Paint().apply {
            shader = RadialGradient(
                    width / 2f, height / 2f, height * 0.7f,
                    intArrayOf(Color.rgb(255, 217, 77), Color.rgb(255, 115, 0), Color.rgb(140, 13, 0)),
                    floatArrayOf(0f, 0.5f, 1f),
                    Shader.TileMode.CLAMP
            )
        }
        Canvas(bitmap).drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        bitmap
    }
}
