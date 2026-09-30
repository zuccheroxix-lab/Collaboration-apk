package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActiveSessionState
import com.example.model.AppDestination
import com.example.model.CompatibilityLevel
import com.example.model.DeviceCapability
import com.example.model.ShizukuStatus
import com.example.model.ToolsSubTab
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun HomeScreen(
    deviceCapability: DeviceCapability,
    shizukuStatus: ShizukuStatus,
    activeSession: ActiveSessionState,
    sensX: Float,
    sensY: Float,
    onStopSession: () -> Unit,
    onNavigate: (AppDestination, ToolsSubTab?) -> Unit,
    modifier: Modifier = Modifier
) {
    val isShizukuConnected = shizukuStatus == ShizukuStatus.PERMISSION_GRANTED || shizukuStatus == ShizukuStatus.RUNNING

    // Real Sensitivity Status based on actual conditions
    val sensitivityStatus = when {
        activeSession.isRunning -> "APPLIED"
        deviceCapability.compatibility == CompatibilityLevel.UNSUPPORTED -> "UNSUPPORTED"
        deviceCapability.hasWriteSettings || isShizukuConnected -> "READY"
        else -> "UNSUPPORTED"
    }

    val sensitivityStatusColor = when (sensitivityStatus) {
        "APPLIED" -> NeonGreen
        "READY" -> NeonCyan
        "FAILED" -> NeonRed
        else -> TextTertiary
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("home_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ================= 1. HEADER =================
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "SENSIV",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            Text(
                text = "System Sensitivity Tools",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // ================= 2. SYSTEM STATUS =================
        Text(
            text = "SYSTEM STATUS",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        // 4 Compact Status Cards (2x2 Grid)
        // 4 Compact Status Cards (2x2 Grid)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Shizuku Status
                CompactStatusCard(
                    title = "SHIZUKU",
                    value = if (isShizukuConnected) "CONNECTED" else "DISCONNECTED",
                    dotColor = if (isShizukuConnected) NeonGreen else NeonRed,
                    valueColor = if (isShizukuConnected) NeonGreen else NeonRed,
                    modifier = Modifier.weight(1f),
                    testTag = "status_shizuku"
                )

                // 2. Device Status
                val isDeviceReady = deviceCapability.compatibility != CompatibilityLevel.UNSUPPORTED
                CompactStatusCard(
                    title = "DEVICE",
                    value = deviceCapability.deviceName.ifBlank { "Android Device" },
                    dotColor = if (isDeviceReady) NeonGreen else NeonRed,
                    valueColor = TextPrimary,
                    modifier = Modifier.weight(1f),
                    testTag = "status_device"
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 3. Android Status
                val isSdkSupported = deviceCapability.sdkInt >= 24
                CompactStatusCard(
                    title = "ANDROID",
                    value = "API ${deviceCapability.sdkInt}",
                    dotColor = if (isSdkSupported) NeonGreen else NeonRed,
                    valueColor = TextPrimary,
                    modifier = Modifier.weight(1f),
                    testTag = "status_android"
                )

                // 4. Session Status
                CompactStatusCard(
                    title = "SESSION",
                    value = if (activeSession.isRunning) "RUNNING" else "IDLE",
                    dotColor = if (activeSession.isRunning) NeonGreen else NeonRed.copy(alpha = 0.5f),
                    valueColor = if (activeSession.isRunning) NeonGreen else TextSecondary,
                    modifier = Modifier.weight(1f),
                    testTag = "status_session"
                )
            }
        }

        // ================= 3. GLOBAL SENSITIVITY CARD =================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("global_sensitivity_card"),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(NeonCyan.copy(alpha = 0.35f)))
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "GLOBAL SENSITIVITY",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(sensitivityStatusColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(sensitivityStatusColor)
                            )
                        }
                        Text(
                            text = "STATUS",
                            color = TextTertiary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = sensitivityStatus,
                            color = sensitivityStatusColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // X Value Box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkBackground)
                            .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "X",
                                color = NeonCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = String.format(Locale.US, "%.2f", sensX),
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    // Y Value Box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkBackground)
                            .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Y",
                                color = NeonPurple,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = String.format(Locale.US, "%.2f", sensY),
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                OutlinedButton(
                    onClick = { onNavigate(AppDestination.TOOLS, ToolsSubTab.SENSITIVITY) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan)
                ) {
                    Text(
                        text = "TUNE SENSITIVITY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        // Active Session Card if running
        if (activeSession.isRunning) {
            SessionCard(
                activeSession = activeSession,
                shizukuStatus = shizukuStatus,
                onStopSession = onStopSession
            )
        }

        // ================= 4. QUICK ACTIONS =================
        Text(
            text = "QUICK ACTIONS",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(top = 4.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            QuickActionRow(
                title = "SENSITIVITY",
                subtitle = "Global X / Y precision scaling",
                icon = Icons.Default.Tune,
                iconColor = NeonCyan,
                onClick = { onNavigate(AppDestination.TOOLS, ToolsSubTab.SENSITIVITY) },
                testTag = "quick_action_sensitivity"
            )

            QuickActionRow(
                title = "GAME LAUNCHER",
                subtitle = "Select game & launch mode",
                icon = Icons.Default.RocketLaunch,
                iconColor = NeonGreen,
                onClick = { onNavigate(AppDestination.LAUNCHER, null) },
                testTag = "quick_action_launcher"
            )

            QuickActionRow(
                title = "CALIBRATION",
                subtitle = "Test & calibrate real touch vectors",
                icon = Icons.Default.Speed,
                iconColor = NeonAmber,
                onClick = { onNavigate(AppDestination.TOOLS, ToolsSubTab.CALIBRATION) },
                testTag = "quick_action_calibration"
            )

            QuickActionRow(
                title = "SHIZUKU",
                subtitle = "Privileged ADB permission & density",
                icon = Icons.Default.Security,
                iconColor = if (isShizukuConnected) NeonGreen else NeonRed,
                onClick = { onNavigate(AppDestination.TOOLS, ToolsSubTab.SHIZUKU) },
                testTag = "quick_action_shizuku"
            )

            QuickActionRow(
                title = "DIAGNOSTICS",
                subtitle = "System capability & audit logs",
                icon = Icons.Default.Assessment,
                iconColor = NeonPurple,
                onClick = { onNavigate(AppDestination.TOOLS, ToolsSubTab.DIAGNOSTICS) },
                testTag = "quick_action_diagnostics"
            )
        }
    }
}

@Composable
private fun CompactStatusCard(
    title: String,
    value: String,
    dotColor: Color,
    valueColor: Color,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Card(
        modifier = modifier.testTag(testTag),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder))
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                // Subtle status indicator icon (green/red dot with soft halo)
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(dotColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                }
            }

            Text(
                text = value,
                color = valueColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun QuickActionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Text(
                        text = title,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = subtitle,
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open",
                tint = TextTertiary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
