package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActiveSessionState
import com.example.model.DeviceCapability
import com.example.model.ShizukuStatus
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun RealTimeStatusBar(
    deviceCapability: DeviceCapability,
    shizukuStatus: ShizukuStatus,
    activeSession: ActiveSessionState,
    sensX: Float,
    sensY: Float,
    onStopSession: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("real_time_status_card"),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder))
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (activeSession.isRunning) NeonGreen else TextTertiary)
                    )
                    Text(
                        text = "LIVE SYSTEM & SESSION STATUS",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                if (activeSession.isRunning) {
                    Button(
                        onClick = onStopSession,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonRed,
                            contentColor = androidx.compose.ui.graphics.Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("stop_session_button")
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("STOP SESSION", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Grid of 6 Telemetry Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MetricItem(
                    label = "DEVICE",
                    value = "Android ${deviceCapability.sdkInt}",
                    subValue = deviceCapability.abi,
                    modifier = Modifier.weight(1f)
                )
                MetricItem(
                    label = "SHIZUKU",
                    value = shizukuStatus.displayName,
                    subValue = if (shizukuStatus == ShizukuStatus.PERMISSION_GRANTED) "Privileged" else "Standard",
                    valueColor = when (shizukuStatus) {
                        ShizukuStatus.PERMISSION_GRANTED -> NeonGreen
                        ShizukuStatus.RUNNING -> NeonAmber
                        else -> TextTertiary
                    },
                    modifier = Modifier.weight(1f)
                )
                MetricItem(
                    label = "GAME",
                    value = activeSession.gameName ?: "Standby",
                    subValue = if (activeSession.isRunning) "Running" else "None",
                    valueColor = if (activeSession.isRunning) NeonCyan else TextSecondary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MetricItem(
                    label = "MODE",
                    value = activeSession.mode?.displayName ?: "NONE",
                    subValue = if (activeSession.isRunning) "Active" else "Standby",
                    valueColor = if (activeSession.isRunning) NeonPurple else TextTertiary,
                    modifier = Modifier.weight(1f)
                )
                MetricItem(
                    label = "SENSITIVITY",
                    value = "X ${String.format(Locale.US, "%.2f", sensX)}",
                    subValue = "Y ${String.format(Locale.US, "%.2f", sensY)}",
                    valueColor = NeonCyan,
                    modifier = Modifier.weight(1f)
                )
                MetricItem(
                    label = "SESSION",
                    value = if (activeSession.isRunning) "RUNNING" else "STOPPED",
                    subValue = if (activeSession.isRunning) "Auto-Restore Ready" else "Baseline",
                    valueColor = if (activeSession.isRunning) NeonGreen else TextTertiary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MetricItem(
    label: String,
    value: String,
    subValue: String,
    modifier: Modifier = Modifier,
    valueColor: androidx.compose.ui.graphics.Color = TextPrimary
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(DarkBackground)
            .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Column {
            Text(
                text = label,
                color = TextTertiary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = value,
                color = valueColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = subValue,
                color = TextSecondary,
                fontSize = 8.sp,
                maxLines = 1
            )
        }
    }
}
