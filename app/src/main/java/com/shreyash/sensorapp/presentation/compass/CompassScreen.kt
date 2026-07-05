package com.shreyash.sensorapp.presentation.compass

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shreyash.sensorapp.presentation.theme.SensorAppTheme
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompassScreen(
    onBack: () -> Unit,
    viewModel: CompassViewModel = hiltViewModel()
) {
    val heading by viewModel.heading.collectAsStateWithLifecycle()
    val isAvailable by viewModel.isAvailable.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> viewModel.startObserving()
                Lifecycle.Event.ON_STOP -> viewModel.stopObserving()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Compass") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        CompassScreenContent(
            heading = heading,
            isAvailable = isAvailable,
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
private fun CompassScreenContent(
    heading: Float,
    isAvailable: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isAvailable) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Compass is not available.\nRequires accelerometer and magnetometer.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(32.dp)
            )
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CompassView(
                heading = heading,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "${heading.roundToInt()}°",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = directionFromHeading(heading),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Magnetic North",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CompassView(
    heading: Float,
    modifier: Modifier = Modifier
) {
    val animatedHeading = remember { Animatable(0f) }

    LaunchedEffect(heading) {
        val target = heading
        val current = animatedHeading.value
        val diff = (target - current + 540f) % 360f - 180f
        val adjustedTarget = current + diff
        animatedHeading.animateTo(adjustedTarget, tween(durationMillis = 80))
    }

    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
    val onSurface = MaterialTheme.colorScheme.onSurface
    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface

    Canvas(modifier = modifier.padding(8.dp)) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val r = minOf(cx, cy)

            val canvas = drawContext.canvas.nativeCanvas
            canvas.save()
            canvas.rotate(-animatedHeading.value, cx, cy)

            drawCircle(color = outlineVariant, radius = r, center = Offset(cx, cy), style = Stroke(2.dp.toPx()))
            drawCircle(color = outlineVariant.copy(alpha = 0.2f), radius = r * 0.96f, center = Offset(cx, cy), style = Stroke(1.dp.toPx()))

            for (i in 0 until 72) {
                val angle = Math.toRadians((i * 5).toDouble()).toFloat()
                val isMajor = i % 6 == 0
                val tickLen = if (isMajor) r * 0.1f else r * 0.05f
                val outerR = if (isMajor) r * 0.88f else r * 0.9f
                val x1 = cx + outerR * sin(angle)
                val y1 = cy - outerR * cos(angle)
                val x2 = cx + (outerR - tickLen) * sin(angle)
                val y2 = cy - (outerR - tickLen) * cos(angle)
                drawLine(
                    color = onSurface.copy(alpha = if (isMajor) 0.8f else 0.4f),
                    start = Offset(x1, y1),
                    end = Offset(x2, y2),
                    strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx()
                )
            }

            val paint = android.graphics.Paint().apply {
                textSize = 34f
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
                isFakeBoldText = true
            }

            val labelR = r * 0.72f
            val compassLabels = listOf(
                "N" to 0f, "NE" to 45f, "E" to 90f, "SE" to 135f,
                "S" to 180f, "SW" to 225f, "W" to 270f, "NW" to 315f
            )
            for ((label, angleDeg) in compassLabels) {
                val a = Math.toRadians(angleDeg.toDouble()).toFloat()
                val lx = cx + labelR * sin(a)
                val ly = cy - labelR * cos(a)
                paint.color = if (label == "N") android.graphics.Color.rgb(255, 80, 80)
                else android.graphics.Color.WHITE
                canvas.drawText(label, lx, ly + 12f, paint)
            }

            val degreeLabels = listOf(30, 60, 120, 150, 210, 240, 300, 330)
            paint.textSize = 20f
            paint.alpha = 120
            val degreeLabelR = r * 0.76f
            for (deg in degreeLabels) {
                val a = Math.toRadians(deg.toDouble()).toFloat()
                val lx = cx + degreeLabelR * sin(a)
                val ly = cy - degreeLabelR * cos(a)
                canvas.drawText("$deg", lx, ly + 7f, paint)
            }

            canvas.restore()

            val topIndicator = Path().apply {
                moveTo(cx, cy - r * 0.9f)
                lineTo(cx - r * 0.08f, cy - r * 0.78f)
                lineTo(cx + r * 0.08f, cy - r * 0.78f)
                close()
            }
            drawPath(topIndicator, color = Color(0xFFFF4444))

            val bottomIndicator = Path().apply {
                moveTo(cx, cy + r * 0.9f)
                lineTo(cx - r * 0.06f, cy + r * 0.78f)
                lineTo(cx + r * 0.06f, cy + r * 0.78f)
                close()
            }
            drawPath(bottomIndicator, color = Color(0xFF444444))

            drawCircle(
                color = surface,
                radius = 6.dp.toPx(),
                center = Offset(cx, cy)
            )
            drawCircle(
                color = primary,
                radius = 3.dp.toPx(),
                center = Offset(cx, cy)
            )
        }
    }
}

private fun directionFromHeading(heading: Float): String {
    val directions = arrayOf(
        "N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
        "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW"
    )
    val index = ((heading + 11.25f) / 22.5f).toInt() % 16
    return directions[index]
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, backgroundColor = 0xFF1A1C1E)
@Composable
private fun PreviewCompassScreen() {
    SensorAppTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Compass") },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { padding ->
            CompassScreenContent(
                heading = 45f,
                isAvailable = true,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, backgroundColor = 0xFF1A1C1E)
@Composable
private fun PreviewCompassScreenUnavailable() {
    SensorAppTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Compass") },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { padding ->
            CompassScreenContent(
                heading = 0f,
                isAvailable = false,
                modifier = Modifier.padding(padding)
            )
        }
    }
}
