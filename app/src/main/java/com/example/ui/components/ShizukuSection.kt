package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
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
import com.example.model.ShizukuStatus
import com.example.ui.theme.*

@Composable
fun ShizukuSection(
    shizukuStatus: ShizukuStatus,
    defaultDpi: Int,
    targetDpi: Int,
    isPrivilegedDpiEnabled: Boolean,
    onRequestPermission: () -> Unit,
    onTogglePrivilegedDpi: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSetupDialog by remember { mutableStateOf(false) }

    val isGranted = shizukuStatus == ShizukuStatus.PERMISSION_GRANTED

    val statusColor = when (shizukuStatus) {
        ShizukuStatus.PERMISSION_GRANTED -> NeonGreen
        ShizukuStatus.RUNNING -> NeonAmber
        ShizukuStatus.PERMISSION_DENIED -> NeonRed
        ShizukuStatus.NOT_RUNNING -> TextTertiary
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("shizuku_card"),
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Shizuku Privileged Bridge",
                        tint = statusColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "SHIZUKU PRIVILEGED TOOLS",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                IconButton(
                    onClick = { showSetupDialog = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                        contentDescription = "Shizuku Setup Help",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Real status badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(statusColor.copy(alpha = 0.12f))
                    .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "STATUS: ${shizukuStatus.name.replace("_", " ")}",
                        color = statusColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = when (shizukuStatus) {
                            ShizukuStatus.PERMISSION_GRANTED -> "Privileged ADB binder ready for DPI & Pointer tuning"
                            ShizukuStatus.RUNNING -> "Shizuku running. Permission request required."
                            ShizukuStatus.PERMISSION_DENIED -> "Permission denied by user in Shizuku app"
                            ShizukuStatus.NOT_RUNNING -> "Shizuku service is not running on device"
                        },
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }

                if (shizukuStatus == ShizukuStatus.RUNNING || shizukuStatus == ShizukuStatus.PERMISSION_DENIED) {
                    Button(
                        onClick = onRequestPermission,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonAmber, contentColor = DarkBackground),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Grant", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Privileged Display DPI Scaling
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkBackground)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Hardware DPI Sensitivity Scaling",
                        color = if (isGranted) TextPrimary else TextTertiary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (isGranted) {
                            "Modifies display metric ratio (Default: ${defaultDpi} DPI → Scaled: ${if (targetDpi > 0) targetDpi else defaultDpi} DPI)"
                        } else {
                            "Disabled (Requires Shizuku Permission Granted)"
                        },
                        color = TextTertiary,
                        fontSize = 10.sp
                    )
                }

                Switch(
                    checked = isPrivilegedDpiEnabled && isGranted,
                    onCheckedChange = { onTogglePrivilegedDpi(it) },
                    enabled = isGranted,
                    modifier = Modifier.testTag("shizuku_dpi_switch"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NeonCyan,
                        checkedTrackColor = NeonCyan.copy(alpha = 0.3f),
                        uncheckedThumbColor = TextTertiary,
                        uncheckedTrackColor = DarkSurfaceVariant
                    )
                )
            }
        }
    }

    if (showSetupDialog) {
        AlertDialog(
            onDismissRequest = { showSetupDialog = false },
            title = {
                Text(
                    text = "Shizuku ADB Integration Guide",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Shizuku allows Game Tools to use ADB-level commands without root, enabling true hardware DPI scaling and privileged settings.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    HorizontalDivider(color = DarkBorder)
                    Text(
                        text = "Setup steps:\n1. Install Shizuku from GitHub or Play Store.\n2. Enable Developer Options & Wireless Debugging in Android Settings.\n3. Pair and Start Shizuku via Wireless Debugging.\n4. Return here and tap 'Grant' to activate privileged features.",
                        color = TextPrimary,
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showSetupDialog = false }) {
                    Text("Understood", color = NeonCyan)
                }
            },
            containerColor = DarkSurface
        )
    }
}
