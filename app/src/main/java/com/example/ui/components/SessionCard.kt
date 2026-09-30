package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
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
import com.example.model.ShizukuStatus
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun SessionCard(
    activeSession: ActiveSessionState,
    shizukuStatus: ShizukuStatus,
    onStopSession: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!activeSession.isRunning) return

    val modeColor = when (activeSession.mode?.name?.uppercase()) {
        "PERFORMANCE" -> NeonPurple
        "STABLE" -> NeonGreen
        else -> NeonCyan
    }

    val isShizukuConnected = shizukuStatus == ShizukuStatus.PERMISSION_GRANTED || shizukuStatus == ShizukuStatus.RUNNING

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("session_active_card"),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(NeonCyan.copy(alpha = 0.5f)))
    ) {
        Column(
            modifier = Modifier
                .padding(18.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
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
                            .background(NeonGreen)
                    )
                    Text(
                        text = "GAME SESSION",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Button(
                    onClick = onStopSession,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonRed,
                        contentColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("stop_session_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop Session",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "STOP SESSION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Session metrics grid matching prompt requirements
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkBackground)
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SessionRow(
                    label = "Game:",
                    value = activeSession.gameName ?: "Unknown Application",
                    valueColor = TextPrimary
                )
                SessionRow(
                    label = "Mode:",
                    value = activeSession.mode?.name ?: "OPTIMIZE",
                    valueColor = modeColor
                )
                SessionRow(
                    label = "Shizuku:",
                    value = if (isShizukuConnected) "CONNECTED" else "DISCONNECTED",
                    valueColor = if (isShizukuConnected) NeonGreen else NeonRed
                )
                SessionRow(
                    label = "Sensitivity:",
                    value = "X ${String.format(Locale.US, "%.2f", activeSession.appliedSensX)}   Y ${String.format(Locale.US, "%.2f", activeSession.appliedSensY)}",
                    valueColor = NeonCyan
                )
                SessionRow(
                    label = "Status:",
                    value = activeSession.status,
                    valueColor = NeonGreen
                )
            }
        }
    }
}

@Composable
private fun SessionRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
