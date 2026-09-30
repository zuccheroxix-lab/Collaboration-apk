package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GameProfileEntity
import com.example.model.CalibrationStats
import com.example.model.DeviceCapability
import com.example.model.DiagnosticLogEntry
import com.example.model.PresetType
import com.example.model.ShizukuStatus
import com.example.model.ToolsSubTab
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun ToolsScreen(
    currentSubTab: ToolsSubTab,
    onSubTabSelected: (ToolsSubTab) -> Unit,
    sensX: Float,
    sensY: Float,
    isLinked: Boolean,
    linkRatio: Float,
    preset: PresetType,
    onSensXChange: (Float) -> Unit,
    onSensYChange: (Float) -> Unit,
    onLinkToggle: (Boolean) -> Unit,
    onLinkRatioChange: (Float) -> Unit,
    onPresetSelect: (PresetType) -> Unit,
    onReset: () -> Unit,
    onApplySensitivity: () -> Unit,
    calibrationStats: CalibrationStats,
    onTouchDelta: (Float, Float, Float, Float) -> Unit,
    onResetCalibrationStats: () -> Unit,
    shizukuStatus: ShizukuStatus,
    deviceCapability: DeviceCapability,
    targetDpi: Int,
    isPrivilegedDpiEnabled: Boolean,
    onRequestShizukuPermission: () -> Unit,
    onTogglePrivilegedDpi: (Boolean) -> Unit,
    diagnosticLogs: List<DiagnosticLogEntry>,
    savedProfiles: List<GameProfileEntity> = emptyList(),
    onDeleteProfile: (String) -> Unit = {},
    onLoadProfile: (GameProfileEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("tools_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sub-tabs navigation pill row
        ScrollableTabRow(
            selectedTabIndex = currentSubTab.ordinal,
            containerColor = DarkSurface,
            contentColor = NeonCyan,
            edgePadding = 0.dp,
            indicator = { tabPositions ->
                if (currentSubTab.ordinal < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[currentSubTab.ordinal]),
                        color = NeonCyan,
                        height = 3.dp
                    )
                }
            },
            divider = { HorizontalDivider(color = DarkBorder) },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
        ) {
            ToolsSubTab.entries.forEach { subTab ->
                val isSelected = currentSubTab == subTab
                Tab(
                    selected = isSelected,
                    onClick = { onSubTabSelected(subTab) },
                    text = {
                        Text(
                            text = subTab.title,
                            color = if (isSelected) NeonCyan else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            letterSpacing = 0.5.sp
                        )
                    },
                    modifier = Modifier.testTag("tab_${subTab.name.lowercase()}")
                )
            }
        }

        // Sub-tab content
        when (currentSubTab) {
            ToolsSubTab.SENSITIVITY -> {
                SensitivityControls(
                    sensX = sensX,
                    sensY = sensY,
                    isLinked = isLinked,
                    linkRatio = linkRatio,
                    preset = preset,
                    onSensXChange = onSensXChange,
                    onSensYChange = onSensYChange,
                    onLinkToggle = onLinkToggle,
                    onLinkRatioChange = onLinkRatioChange,
                    onPresetSelect = onPresetSelect,
                    onReset = onReset,
                    onCalibrateClick = { onSubTabSelected(ToolsSubTab.CALIBRATION) },
                    onApplyClick = onApplySensitivity
                )
            }

            ToolsSubTab.CALIBRATION -> {
                CalibrationPlayground(
                    calibrationStats = calibrationStats,
                    deviceCapability = deviceCapability,
                    sensX = sensX,
                    sensY = sensY,
                    onTouchDelta = onTouchDelta,
                    onResetStats = onResetCalibrationStats
                )
            }

            ToolsSubTab.SHIZUKU -> {
                ShizukuSection(
                    shizukuStatus = shizukuStatus,
                    defaultDpi = deviceCapability.defaultDpi,
                    targetDpi = targetDpi,
                    isPrivilegedDpiEnabled = isPrivilegedDpiEnabled,
                    onRequestPermission = onRequestShizukuPermission,
                    onTogglePrivilegedDpi = onTogglePrivilegedDpi
                )
            }

            ToolsSubTab.DIAGNOSTICS -> {
                DiagnosticsSheet(
                    deviceCapability = deviceCapability,
                    diagnosticLogs = diagnosticLogs
                )
            }

            ToolsSubTab.PROFILES -> {
                GameProfilesSection(
                    savedProfiles = savedProfiles,
                    onLoadProfile = onLoadProfile,
                    onDeleteProfile = onDeleteProfile
                )
            }
        }
    }
}

@Composable
private fun GameProfilesSection(
    savedProfiles: List<GameProfileEntity>,
    onLoadProfile: (GameProfileEntity) -> Unit,
    onDeleteProfile: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("game_profiles_card"),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder))
    ) {
        Column(
            modifier = Modifier
                .padding(18.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FolderShared,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = "GAME PROFILES",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Locally persisted profile configurations per package",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            if (savedProfiles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkBackground)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No saved profiles yet. Profiles are saved automatically when launching games.",
                        color = TextTertiary,
                        fontSize = 11.sp
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    savedProfiles.forEach { profile ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = DarkBackground),
                            shape = RoundedCornerShape(10.dp),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = profile.gameName,
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = profile.packageName,
                                        color = TextTertiary,
                                        fontSize = 9.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "X: ${String.format(Locale.US, "%.2f", profile.sensX)}",
                                            color = NeonCyan,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "Y: ${String.format(Locale.US, "%.2f", profile.sensY)}",
                                            color = NeonPurple,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "[${profile.preferredMode}]",
                                            color = NeonGreen,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = { onLoadProfile(profile) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FileDownload,
                                            contentDescription = "Load Profile",
                                            tint = NeonCyan,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDeleteProfile(profile.packageName) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Profile",
                                            tint = TextTertiary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
