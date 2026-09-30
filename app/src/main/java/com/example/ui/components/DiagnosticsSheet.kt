package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DeviceCapability
import com.example.model.DiagnosticLogEntry
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DiagnosticsSheet(
    deviceCapability: DeviceCapability,
    diagnosticLogs: List<DiagnosticLogEntry>,
    modifier: Modifier = Modifier
) {
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("diagnostics_card"),
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.BugReport,
                    contentDescription = "Diagnostics Log",
                    tint = NeonCyan,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "HARDWARE & DIAGNOSTIC LOGS",
                    color = NeonCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Device Technical Metrics Grid
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
                    Text("OS & Architecture:", color = TextSecondary, fontSize = 11.sp)
                    Text(
                        "${deviceCapability.androidVersion} • ${deviceCapability.abi} (${if (deviceCapability.is64Bit) "64-bit" else "32-bit"})",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Display Metrics:", color = TextSecondary, fontSize = 11.sp)
                    Text(
                        "${deviceCapability.displayWidth}x${deviceCapability.displayHeight} @ ${deviceCapability.defaultDpi} DPI",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Hardware Pointer API:", color = TextSecondary, fontSize = 11.sp)
                    Text(
                        if (deviceCapability.hasWriteSettings) "Supported (Granted)" else "Requires Permission",
                        color = if (deviceCapability.hasWriteSettings) NeonGreen else NeonAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Transparent Kernel Hooking:", color = TextSecondary, fontSize = 11.sp)
                    Text(
                        "UNSUPPORTED ON NON-ROOT",
                        color = NeonRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Technical Integrity Note
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceVariant)
                    .padding(8.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Android sandboxing prevents third-party apps from intercepting raw touchscreen events across other apps without root kernel drivers. Game Tools operates via genuine supported methods: Android System Pointer Speed, Shizuku DPI scaling, and Assistive Floating calibration surface.",
                    color = TextTertiary,
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Diagnostic Log Stream
            Text(
                text = "EXECUTION AUDIT LOG (${diagnosticLogs.size} events):",
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkBackground)
                    .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(diagnosticLogs) { log ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = timeFormat.format(Date(log.timestamp)),
                                color = TextTertiary,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "[${log.tag}]",
                                color = if (log.isSuccess) NeonCyan else NeonRed,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = log.message,
                                color = if (log.isSuccess) TextPrimary else NeonAmber,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}
