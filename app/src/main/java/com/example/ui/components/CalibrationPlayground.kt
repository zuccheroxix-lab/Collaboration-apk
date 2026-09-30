package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CalibrationStats
import com.example.model.DeviceCapability
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun CalibrationPlayground(
    calibrationStats: CalibrationStats,
    deviceCapability: DeviceCapability,
    sensX: Float,
    sensY: Float,
    onTouchDelta: (Float, Float, Float, Float) -> Unit,
    onResetStats: () -> Unit,
    modifier: Modifier = Modifier
) {
    var touchStartPos by remember { mutableStateOf<Offset?>(null) }
    var currentTouchPos by remember { mutableStateOf<Offset?>(null) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("calibration_card"),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SENSITIVITY CALIBRATION",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Real input delta & vector transformation test",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }

                IconButton(
                    onClick = onResetStats,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Calibration Stats",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Calibration Details Card (Original X, Applied X, Original Y, Applied Y, Method used, Compatibility)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkBackground)
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Calibration Status:", color = TextSecondary, fontSize = 11.sp)
                    Text(
                        if (calibrationStats.isVerified) "VERIFIED (${calibrationStats.sampleCount} samples @ ${calibrationStats.eventFrequencyHz}Hz)" else "AWAITING USER INPUT",
                        color = if (calibrationStats.isVerified) NeonGreen else NeonAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Original X → Applied X:", color = TextSecondary, fontSize = 11.sp)
                    Text(
                        "1.00x → ${String.format(Locale.US, "%.2f", sensX)}x (${String.format(Locale.US, "%.1f", calibrationStats.rawDeltaX)}px → ${String.format(Locale.US, "%.1f", calibrationStats.transformedDeltaX)}px)",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Original Y → Applied Y:", color = TextSecondary, fontSize = 11.sp)
                    Text(
                        "1.00x → ${String.format(Locale.US, "%.2f", sensY)}x (${String.format(Locale.US, "%.1f", calibrationStats.rawDeltaY)}px → ${String.format(Locale.US, "%.1f", calibrationStats.transformedDeltaY)}px)",
                        color = NeonPurple,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Method Used:", color = TextSecondary, fontSize = 11.sp)
                    Text(
                        calibrationStats.methodUsed,
                        color = TextPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Device Compatibility:", color = TextSecondary, fontSize = 11.sp)
                    Text(
                        "${deviceCapability.compatibility.displayName} (${deviceCapability.androidVersion})",
                        color = if (deviceCapability.compatibility == com.example.model.CompatibilityLevel.SUPPORTED) NeonGreen else NeonAmber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Real-time Canvas Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkBackground)
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                    .testTag("calibration_pad")
                    .pointerInput(sensX, sensY) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                touchStartPos = offset
                                currentTouchPos = offset
                            },
                            onDragEnd = {
                                touchStartPos = null
                                currentTouchPos = null
                            },
                            onDragCancel = {
                                touchStartPos = null
                                currentTouchPos = null
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                currentTouchPos = change.position
                                onTouchDelta(
                                    change.position.x,
                                    change.position.y,
                                    dragAmount.x,
                                    dragAmount.y
                                )
                            }
                        )
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val cx = w / 2f
                    val cy = h / 2f

                    // Grid lines
                    drawLine(
                        color = Color(0x1A00E5FF),
                        start = Offset(0f, cy),
                        end = Offset(w, cy),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = Color(0x1A00E5FF),
                        start = Offset(cx, 0f),
                        end = Offset(cx, h),
                        strokeWidth = 1.dp.toPx()
                    )

                    drawCircle(
                        color = Color(0x0D00E5FF),
                        radius = 40.dp.toPx(),
                        center = Offset(cx, cy)
                    )
                    drawCircle(
                        color = Color(0x0D00E5FF),
                        radius = 80.dp.toPx(),
                        center = Offset(cx, cy)
                    )

                    touchStartPos?.let { start ->
                        currentTouchPos?.let { curr ->
                            val rawDx = curr.x - start.x
                            val rawDy = curr.y - start.y

                            // 1. Raw touch vector (translucent white)
                            drawLine(
                                color = Color.White.copy(alpha = 0.4f),
                                start = start,
                                end = curr,
                                strokeWidth = 2.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                            drawCircle(
                                color = Color.White.copy(alpha = 0.5f),
                                radius = 4.dp.toPx(),
                                center = curr
                            )

                            // 2. Transformed sensitivity vector (Neon Cyan)
                            val txTarget = Offset(start.x + rawDx * sensX, start.y + rawDy * sensY)
                            drawLine(
                                color = NeonCyan,
                                start = start,
                                end = txTarget,
                                strokeWidth = 3.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                            drawCircle(
                                color = NeonCyan,
                                radius = 5.dp.toPx(),
                                center = txTarget
                            )
                        }
                    }
                }

                if (touchStartPos == null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "SWIPE HERE TO CALIBRATE",
                            color = NeonCyan.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "White = Original Vector • Cyan = Applied Sensitivity Vector",
                            color = TextTertiary,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }
}
