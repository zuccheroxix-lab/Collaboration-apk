package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DeviceCapability
import com.example.model.InstalledGame
import com.example.model.LaunchMode
import com.example.model.ShizukuStatus
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun LaunchModeDialog(
    game: InstalledGame,
    deviceCapability: DeviceCapability,
    shizukuStatus: ShizukuStatus,
    sensX: Float = 1.00f,
    sensY: Float = 1.00f,
    initialMode: LaunchMode = LaunchMode.OPTIMIZE,
    onDismiss: () -> Unit,
    onConfirmLaunch: (LaunchMode) -> Unit
) {
    var selectedMode by remember { mutableStateOf(initialMode) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "GAME SETTINGS",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = game.appName,
                    color = NeonCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = game.packageName,
                    color = TextTertiary,
                    fontSize = 10.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // SENSITIVITY Preview (X 1.00, Y 1.00)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkBackground)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "SENSITIVITY",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "X ${String.format(Locale.US, "%.2f", sensX)}",
                            color = NeonCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Y ${String.format(Locale.US, "%.2f", sensY)}",
                            color = NeonPurple,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // SELECT LAUNCH MODE label
                Text(
                    text = "SELECT LAUNCH MODE",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                // 3 Distinct Mode Cards
                LaunchModeCard(
                    title = "OPTIMIZE",
                    subtitle = "Balanced system mode",
                    description = "Safe background trimming, balanced touch response, optimal thermals.",
                    mode = LaunchMode.OPTIMIZE,
                    isSelected = selectedMode == LaunchMode.OPTIMIZE,
                    activeColor = NeonCyan,
                    onSelect = { selectedMode = LaunchMode.OPTIMIZE }
                )

                LaunchModeCard(
                    title = "STABLE",
                    subtitle = "Stability focused",
                    description = "Consistent framerate, stable touch sampling, no aggressive clock spikes.",
                    mode = LaunchMode.STABLE,
                    isSelected = selectedMode == LaunchMode.STABLE,
                    activeColor = NeonGreen,
                    onSelect = { selectedMode = LaunchMode.STABLE }
                )

                LaunchModeCard(
                    title = "PERFORMANCE",
                    subtitle = "Performance focused",
                    description = "Maximum supported refresh rate, reduced animation latency, peak response.",
                    mode = LaunchMode.PERFORMANCE,
                    isSelected = selectedMode == LaunchMode.PERFORMANCE,
                    activeColor = NeonPurple,
                    onSelect = { selectedMode = LaunchMode.PERFORMANCE }
                )

                // Hardware condition summary
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkBackground)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.BatteryChargingFull, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(13.dp))
                        Text("${deviceCapability.batteryLevel}%", color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.DeviceThermostat, contentDescription = null, tint = NeonAmber, modifier = Modifier.size(13.dp))
                        Text(deviceCapability.thermalStatus, color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = if (shizukuStatus == ShizukuStatus.PERMISSION_GRANTED) NeonGreen else TextTertiary, modifier = Modifier.size(13.dp))
                        Text(if (shizukuStatus == ShizukuStatus.PERMISSION_GRANTED) "Privileged" else "Standard", color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmLaunch(selectedMode) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonCyan,
                    contentColor = DarkBackground
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("confirm_launch_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("LAUNCH ${selectedMode.name}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = DarkSurface
    )
}

@Composable
private fun LaunchModeCard(
    title: String,
    subtitle: String,
    description: String,
    mode: LaunchMode,
    isSelected: Boolean,
    activeColor: Color,
    onSelect: () -> Unit
) {
    val borderColor = if (isSelected) activeColor else DarkBorder
    val bgColor = if (isSelected) activeColor.copy(alpha = 0.12f) else DarkBackground

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable { onSelect() }
            .padding(10.dp)
            .testTag("mode_${mode.name.lowercase()}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = if (isSelected) activeColor else TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = if (isSelected) TextPrimary else TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    color = TextTertiary,
                    fontSize = 9.sp,
                    lineHeight = 12.sp
                )
            }

            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(
                    selectedColor = activeColor,
                    unselectedColor = TextTertiary
                ),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
