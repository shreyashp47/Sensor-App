package com.shreyash.sensorapp.presentation.detail

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shreyash.sensorapp.domain.model.SensorReading
import com.shreyash.sensorapp.domain.model.SensorType
import com.shreyash.sensorapp.presentation.theme.SensorAppTheme
import com.shreyash.sensorapp.presentation.theme.SensorGreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.OutputStreamWriter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorDetailScreen(
    sensorType: SensorType,
    onBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val currentReading by viewModel.currentReading.collectAsStateWithLifecycle()
    val isLogging by viewModel.isLogging.collectAsStateWithLifecycle()
    val chartReadings by viewModel.chartReadings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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
                title = { Text(sensorType.displayName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.toggleLogging() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLogging) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = if (isLogging) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(if (isLogging) "Stop Logging" else "Start Logging")
                }

                Button(
                    onClick = {
                        scope.launch {
                            val uri = exportToCsv(context, chartReadings, sensorType)
                            if (uri != null) {
                                snackbarHostState.showSnackbar("CSV exported to Downloads")
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = chartReadings.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Export CSV")
                }
            }
        }
    ) { padding ->
        SensorDetailScreenContent(
            sensorType = sensorType,
            currentReading = currentReading,
            isLogging = isLogging,
            chartReadings = chartReadings,
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
private fun SensorDetailScreenContent(
    sensorType: SensorType,
    currentReading: SensorReading?,
    isLogging: Boolean,
    chartReadings: List<SensorReading>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LiveIndicator()

        Spacer(Modifier.height(16.dp))

        LiveValueDisplay(
            reading = currentReading,
            sensorType = sensorType
        )

        Spacer(Modifier.height(24.dp))

        LiveLineChart(
            readings = chartReadings,
            sensorType = sensorType,
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "X: ${formatDetailValue(currentReading?.values?.getOrNull(0))} ${sensorType.unitX}" +
                    if (sensorType.axisCount >= 2) "  Y: ${formatDetailValue(currentReading?.values?.getOrNull(1))} ${sensorType.unitY}" else "" +
                    if (sensorType.axisCount >= 3) "  Z: ${formatDetailValue(currentReading?.values?.getOrNull(2))} ${sensorType.unitZ}" else "",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        SensorUsageHint(sensorType = sensorType)

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SensorUsageHint(sensorType: SensorType) {
    val hint = when (sensorType) {
        SensorType.ACCELEROMETER -> "Tilt or shake the device to see acceleration forces change across X, Y, and Z axes."
        SensorType.GYROSCOPE -> "Rotate the device to measure the rate of rotation around each axis."
        SensorType.LINEAR_ACCELERATION -> "Move the device quickly to measure acceleration excluding gravity."
        SensorType.MAGNETOMETER -> "Move the device near a metal object or wave it in a figure-8 pattern to test."
        SensorType.GRAVITY -> "Tilt the device to see how gravity distributes across each axis."
        SensorType.ROTATION_VECTOR -> "Rotate the device to observe orientation changes relative to the world."
        SensorType.LIGHT -> "Cover and uncover the light sensor (usually near the front camera) to see lux level changes."
        SensorType.PROXIMITY -> "Cover the top of the device to trigger the sensor. Used to detect when the phone is held to the ear."
        SensorType.PRESSURE -> "Ambient air pressure changes with altitude. Try moving to a different floor or elevation."
        SensorType.STEP_COUNTER -> "Walk or simulate steps to see the step count increment in real time."
    }

    Text(
        text = hint,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun LiveIndicator() {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        )
    )
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = SensorGreen.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(SensorGreen.copy(alpha = alpha))
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "LIVE",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = SensorGreen
            )
        }
    }
}

@Composable
private fun LiveValueDisplay(
    reading: SensorReading?,
    sensorType: SensorType
) {
    if (reading == null) {
        Text(
            text = "Waiting for sensor data...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    if (sensorType == SensorType.PRESSURE) {
        val value = reading.values.firstOrNull() ?: 0f
        val condition = when {
            value < 980f -> "STORM"
            value < 1010f -> "RAIN"
            value < 1025f -> "NORMAL"
            else -> "HIGH"
        }
        val conditionColor = when (condition) {
            "STORM" -> Color(0xFFE53935)
            "RAIN" -> Color(0xFF42A5F5)
            "NORMAL" -> SensorGreen
            else -> Color(0xFFFFB300)
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = condition,
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = conditionColor
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = when (condition) {
                        "STORM" -> "Very low pressure — stormy weather likely"
                        "RAIN" -> "Low pressure — rain or unsettled weather"
                        "NORMAL" -> "Standard atmospheric pressure"
                        else -> "High pressure — fair and stable weather"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "${formatLargeValue(value)} hPa",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Sea level: 1013.25 hPa",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    } else if (sensorType == SensorType.LIGHT) {
        val value = reading.values.firstOrNull() ?: 0f
        val level = when {
            value <= 1f -> "DARK"
            value <= 50f -> "DIM"
            value <= 500f -> "INDOOR"
            value <= 10000f -> "OUTDOOR"
            else -> "SUNLIGHT"
        }
        val levelColor = when (level) {
            "DARK" -> Color(0xFF6B6B6B)
            "DIM" -> Color(0xFF9E9E9E)
            "INDOOR" -> Color(0xFFFFD54F)
            "OUTDOOR" -> Color(0xFFFFB300)
            else -> Color(0xFFFF6F00)
        }
        val fraction = (value / 50000f).coerceIn(0f, 1f)

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = level,
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = levelColor
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = when (level) {
                        "DARK" -> "Very little or no light detected"
                        "DIM" -> "Low light conditions, like a dimly lit room"
                        "INDOOR" -> "Typical indoor lighting level"
                        "OUTDOOR" -> "Outdoor conditions, cloudy or shaded"
                        else -> "Bright direct sunlight"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                Brush.horizontalGradient(listOf(
                                    Color(0xFF6B6B6B),
                                    Color(0xFFFFD54F),
                                    Color(0xFFFF6F00)
                                ))
                            )
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "${formatLargeValue(value)} lx",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    } else if (sensorType == SensorType.PROXIMITY) {
        val value = reading.values.firstOrNull() ?: 0f
        val isObstructed = value < 1f

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isObstructed) "OBSTRUCTED" else "CLEAR",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isObstructed) Color(0xFFFF6B6B) else SensorGreen
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = if (isObstructed) "An object is covering the sensor"
                    else "No object detected near the sensor",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else if (sensorType.axisCount == 1) {
        val value = reading.values.firstOrNull() ?: 0f
        Text(
            text = formatLargeValue(value),
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        if (sensorType.unitSingle != null && sensorType.unitSingle.isNotEmpty()) {
            Text(
                text = sensorType.unitSingle,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            AxisValue(
                label = "X",
                value = reading.values.getOrNull(0) ?: 0f,
                unit = sensorType.unitX
            )
            if (sensorType.axisCount >= 2) {
                AxisValue(
                    label = "Y",
                    value = reading.values.getOrNull(1) ?: 0f,
                    unit = sensorType.unitY
                )
            }
            if (sensorType.axisCount >= 3) {
                AxisValue(
                    label = "Z",
                    value = reading.values.getOrNull(2) ?: 0f,
                    unit = sensorType.unitZ
                )
            }
        }
    }
}

@Composable
private fun AxisValue(
    label: String,
    value: Float,
    unit: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.widthIn(min = 96.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = formatLargeValue(value),
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        if (unit.isNotEmpty()) {
            Text(
                text = unit,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun LiveLineChart(
    readings: List<SensorReading>,
    sensorType: SensorType,
    modifier: Modifier = Modifier
) {
    if (readings.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(
                text = "No data yet",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val axisIndex = 0
    val values = readings.map { it.values.getOrElse(axisIndex) { 0f } }
    val lineColor = MaterialTheme.colorScheme.primary
    val fillColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val crosshairColor = MaterialTheme.colorScheme.tertiary

    var touchIndex by remember { mutableStateOf(-1) }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 8.dp, top = 24.dp, end = 8.dp, bottom = 24.dp)
                .pointerInput(values) {
                    detectTapGestures { offset ->
                        if (values.size < 2) return@detectTapGestures
                        val stepX = size.width.toFloat() / (values.size - 1)
                        val index = ((offset.x / stepX) + 0.5f).toInt()
                            .coerceIn(0, values.size - 1)
                        touchIndex = if (touchIndex == index) -1 else index
                    }
                }
        ) {
            if (values.size < 2) return@Canvas

            val minVal = values.min()
            val maxVal = values.max()
            val range = if (maxVal - minVal == 0f) 1f else maxVal - minVal
            val paddingV = 0.1f
            val adjustedMin = minVal - range * paddingV
            val adjustedMax = maxVal + range * paddingV
            val adjustedRange = adjustedMax - adjustedMin

            val stepX = size.width / (values.size - 1).coerceAtLeast(1)

            fun yForValue(value: Float): Float {
                val normY = ((value - adjustedMin) / adjustedRange).coerceIn(0f, 1f)
                return size.height - normY * size.height
            }

            for (i in 0 until values.size) {
                val x = i * stepX
                drawLine(
                    color = gridColor,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 0.5f
                )
            }

            val linePath = Path()
            val fillPath = Path()
            values.forEachIndexed { index, value ->
                val x = index * stepX
                val y = yForValue(value)
                if (index == 0) {
                    linePath.moveTo(x, y)
                    fillPath.moveTo(x, size.height)
                    fillPath.lineTo(x, y)
                } else {
                    linePath.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }
            }
            fillPath.lineTo((values.size - 1) * stepX, size.height)
            fillPath.close()

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(fillColor.copy(alpha = 0.2f), fillColor.copy(alpha = 0.02f)),
                    endY = size.height
                )
            )

            drawPath(
                path = linePath,
                color = lineColor,
                style = Stroke(
                    width = 2.5.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            val labelPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.GRAY
                textSize = 22f
                textAlign = android.graphics.Paint.Align.RIGHT
                isAntiAlias = true
            }

            drawContext.canvas.nativeCanvas.drawText(
                formatLargeValue(adjustedMax),
                -4.dp.toPx(),
                0f,
                labelPaint
            )

            drawContext.canvas.nativeCanvas.drawText(
                formatLargeValue(adjustedMin),
                -4.dp.toPx(),
                size.height,
                labelPaint
            )

            if (touchIndex in values.indices && values.size >= 2) {
                val x = touchIndex * stepX
                val value = values[touchIndex]
                val y = yForValue(value)
                val point = Offset(x, y)

                drawLine(
                    color = crosshairColor,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1.5f
                )

                drawCircle(
                    color = crosshairColor,
                    radius = 5.dp.toPx(),
                    center = point
                )
                drawCircle(
                    color = crosshairColor.copy(alpha = 0.3f),
                    radius = 10.dp.toPx(),
                    center = point
                )

                drawCrosshairTooltip(
                    value = value,
                    x = x,
                    y = y
                )
            }
        }
    }
}

private fun DrawScope.drawCrosshairTooltip(value: Float, x: Float, y: Float) {
    val text = formatLargeValue(value)
    val paint = android.graphics.Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = 28f
        textAlign = android.graphics.Paint.Align.CENTER
        isAntiAlias = true
        isFakeBoldText = true
    }

    val textWidth = paint.measureText(text)
    val padding = 16f
    val tooltipHeight = paint.textSize + padding * 2
    val rectWidth = textWidth + padding * 2
    val rectLeft = (x - rectWidth / 2).coerceIn(0f, size.width - rectWidth)
    val rectTop = (y - tooltipHeight - 16.dp.toPx()).coerceAtLeast(0f)
    val rectBottom = rectTop + tooltipHeight
    val rectRight = rectLeft + rectWidth

    drawRoundRect(
        color = Color(50, 50, 55, 230),
        topLeft = Offset(rectLeft, rectTop),
        size = Size(rectWidth, tooltipHeight),
        cornerRadius = CornerRadius(10f, 10f)
    )

    drawContext.canvas.nativeCanvas.drawText(
        text,
        x,
        rectTop + padding + paint.textSize / 2 + 2f,
        paint
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1C1E)
@Composable
private fun PreviewLiveIndicator() {
    SensorAppTheme {
        LiveIndicator()
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1C1E)
@Composable
private fun PreviewLiveValueDisplay() {
    SensorAppTheme {
        LiveValueDisplay(
            reading = SensorReading(
                sensorType = SensorType.ACCELEROMETER,
                values = listOf(9.8f, 0.0f, 0.0f),
                accuracy = 3,
                timestampMs = System.currentTimeMillis()
            ),
            sensorType = SensorType.ACCELEROMETER
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1C1E)
@Composable
private fun PreviewAxisValue() {
    SensorAppTheme {
        AxisValue(label = "X", value = 9.8f, unit = "m/s²")
    }
}

private val mockChartReadings = List(60) { i ->
    SensorReading(
        sensorType = SensorType.ACCELEROMETER,
        values = listOf(9.8f + kotlin.math.sin(i * 0.5f) * 2f, 0.5f, -0.3f),
        accuracy = 3,
        timestampMs = System.currentTimeMillis() - (60 - i) * 100L
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, backgroundColor = 0xFF1A1C1E)
@Composable
private fun PreviewSensorDetailScreen() {
    SensorAppTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Accelerometer") },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {},
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Start Logging")
                    }

                    Button(
                        onClick = {},
                        modifier = Modifier.weight(1f),
                        enabled = true
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Export CSV")
                    }
                }
            }
        ) { padding ->
            SensorDetailScreenContent(
                sensorType = SensorType.ACCELEROMETER,
                currentReading = SensorReading(
                    sensorType = SensorType.ACCELEROMETER,
                    values = listOf(9.8f, 0.0f, 0.0f),
                    accuracy = 3,
                    timestampMs = System.currentTimeMillis()
                ),
                isLogging = false,
                chartReadings = mockChartReadings,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

private fun formatLargeValue(value: Float): String {
    return String.format("%.1f", value)
}

private fun formatDetailValue(value: Float?): String {
    if (value == null) return "--"
    return formatLargeValue(value)
}

private suspend fun exportToCsv(
    context: Context,
    readings: List<SensorReading>,
    sensorType: SensorType
): android.net.Uri? = withContext(Dispatchers.IO) {
    try {
        val fileName = "${sensorType.name}_${System.currentTimeMillis()}.csv"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "text/csv")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }

            val uri = context.contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues
            ) ?: return@withContext null

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                writeCsv(outputStream, readings)
            }

            contentValues.clear()
            contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
            context.contentResolver.update(uri, contentValues, null, null)
            uri
        } else {
            val dir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            )
            dir.mkdirs()
            val file = java.io.File(dir, fileName)
            file.outputStream().use { outputStream ->
                writeCsv(outputStream, readings)
            }
            android.net.Uri.fromFile(file)
        }
    } catch (e: Exception) {
        null
    }
}

private fun writeCsv(outputStream: java.io.OutputStream, readings: List<SensorReading>) {
    val writer = BufferedWriter(OutputStreamWriter(outputStream))
    writer.write("Timestamp_ms,Value1,Value2,Value3\n")
    readings.forEach { reading ->
        writer.write("${reading.timestampMs},${reading.values.joinToString(",")}\n")
    }
    writer.flush()
}
