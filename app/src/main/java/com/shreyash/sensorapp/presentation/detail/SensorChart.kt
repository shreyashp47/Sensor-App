package com.shreyash.sensorapp.presentation.detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.unit.dp
import com.shreyash.sensorapp.domain.model.SensorReading
import com.shreyash.sensorapp.domain.model.SensorType

@Composable
fun LiveLineChart(
    readings: List<SensorReading>,
    @Suppress("UNUSED_PARAMETER") sensorType: SensorType,
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

@Composable
fun MultiAxisLineChart(
    readings: List<SensorReading>,
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

    val axisColors = listOf(
        Color(0xFFE24B4A),
        Color(0xFF639922),
        Color(0xFF378ADD)
    )
    val axisLabels = listOf("X", "Y", "Z")

    val axisValues = (0..2).map { axis ->
        readings.map { it.values.getOrElse(axis) { 0f } }
    }

    val allValues = axisValues.flatten()
    val minVal = allValues.min()
    val maxVal = allValues.max()
    val range = if (maxVal - minVal == 0f) 1f else maxVal - minVal
    val paddingV = 0.1f
    val adjustedMin = minVal - range * paddingV
    val adjustedMax = maxVal + range * paddingV
    val adjustedRange = adjustedMax - adjustedMin

    val gridColor = MaterialTheme.colorScheme.outlineVariant

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
                .pointerInput(axisValues) {
                    detectTapGestures { offset ->
                        if (axisValues[0].size < 2) return@detectTapGestures
                        val stepX = size.width.toFloat() / (axisValues[0].size - 1)
                        val index = ((offset.x / stepX) + 0.5f).toInt()
                            .coerceIn(0, axisValues[0].size - 1)
                        touchIndex = if (touchIndex == index) -1 else index
                    }
                }
        ) {
            if (axisValues[0].size < 2) return@Canvas

            val stepX = size.width / (axisValues[0].size - 1).coerceAtLeast(1)

            fun yForValue(value: Float): Float {
                val normY = ((value - adjustedMin) / adjustedRange).coerceIn(0f, 1f)
                return size.height - normY * size.height
            }

            for (i in 0 until axisValues[0].size) {
                val x = i * stepX
                drawLine(
                    color = gridColor,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 0.5f
                )
            }

            axisValues.forEachIndexed { axisIdx, values ->
                val linePath = Path()
                values.forEachIndexed { index, value ->
                    val x = index * stepX
                    val y = yForValue(value)
                    if (index == 0) {
                        linePath.moveTo(x, y)
                    } else {
                        linePath.lineTo(x, y)
                    }
                }
                drawPath(
                    path = linePath,
                    color = axisColors[axisIdx],
                    style = Stroke(
                        width = 2.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

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

            if (touchIndex in axisValues[0].indices && axisValues[0].size >= 2) {
                val x = touchIndex * stepX
                drawLine(
                    color = Color.White.copy(alpha = 0.5f),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1f
                )

                val tooltipPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 24f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                    isFakeBoldText = true
                }

                val lines = axisValues.mapIndexed { i, values ->
                    "${axisLabels[i]}: ${formatLargeValue(values[touchIndex])}"
                }
                val tooltipText = lines.joinToString("  ")
                val textWidth = tooltipPaint.measureText(tooltipText)
                val padding = 12f
                val tooltipHeight = tooltipPaint.textSize + padding * 2
                val rectWidth = textWidth + padding * 2
                val rectLeft = (x - rectWidth / 2).coerceIn(0f, size.width - rectWidth)
                val rectTop = 4.dp.toPx()

                drawRoundRect(
                    color = Color(40, 40, 45, 230),
                    topLeft = Offset(rectLeft, rectTop),
                    size = Size(rectWidth, tooltipHeight),
                    cornerRadius = CornerRadius(8f, 8f)
                )

                drawContext.canvas.nativeCanvas.drawText(
                    tooltipText,
                    x,
                    rectTop + padding + tooltipPaint.textSize / 2 + 2f,
                    tooltipPaint
                )

                axisValues.forEachIndexed { axisIdx, values ->
                    val value = values[touchIndex]
                    val y = yForValue(value)
                    drawCircle(
                        color = axisColors[axisIdx],
                        radius = 4.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 14.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            axisColors.forEachIndexed { i, color ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(color)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = axisLabels[i],
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
