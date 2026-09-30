package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.AppDestination
import com.example.model.ShizukuStatus
import com.example.model.ToolsSubTab
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameToolsScreen(
    viewModel: GameToolsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val currentDestination by viewModel.currentDestination.collectAsStateWithLifecycle()
    val currentToolsSubTab by viewModel.currentToolsSubTab.collectAsStateWithLifecycle()

    val sensX by viewModel.sensX.collectAsStateWithLifecycle()
    val sensY by viewModel.sensY.collectAsStateWithLifecycle()
    val isLinked by viewModel.isLinked.collectAsStateWithLifecycle()
    val linkRatio by viewModel.linkRatio.collectAsStateWithLifecycle()
    val preset by viewModel.preset.collectAsStateWithLifecycle()

    val launcherGames by viewModel.launcherGames.collectAsStateWithLifecycle()
    val allInstalledApps by viewModel.allInstalledApps.collectAsStateWithLifecycle()
    val savedProfiles by viewModel.savedProfiles.collectAsStateWithLifecycle()
    val gameForModeSelection by viewModel.gameForModeSelection.collectAsStateWithLifecycle()

    val deviceCapability by viewModel.deviceCapability.collectAsStateWithLifecycle()
    val shizukuStatus by viewModel.shizukuStatus.collectAsStateWithLifecycle()
    val activeSession by viewModel.activeSession.collectAsStateWithLifecycle()

    val privilegedDpiEnabled by viewModel.privilegedDpiEnabled.collectAsStateWithLifecycle()
    val targetDpi by viewModel.targetDpi.collectAsStateWithLifecycle()

    val calibrationStats by viewModel.calibrationStats.collectAsStateWithLifecycle()
    val diagnosticLogs by viewModel.diagnosticLogs.collectAsStateWithLifecycle()

    // Proper Android Back handling: never traps the user, returns to HOME cleanly
    BackHandler(enabled = currentDestination != AppDestination.HOME || gameForModeSelection != null) {
        viewModel.handleBackPress()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        activeSession.isRunning -> NeonGreen
                                        shizukuStatus == ShizukuStatus.PERMISSION_GRANTED -> NeonCyan
                                        else -> TextTertiary
                                    }
                                )
                        )
                        Column {
                            Text(
                                text = "SENSIV",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = if (activeSession.isRunning) {
                                    "SESSION: ${activeSession.gameName ?: "RUNNING"}"
                                } else {
                                    "System Sensitivity Tools"
                                },
                                color = if (activeSession.isRunning) NeonGreen else TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.refreshHardwareInfo() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh System Status",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = 0.dp,
                modifier = Modifier.testTag("main_bottom_nav")
            ) {
                AppDestination.entries.forEach { dest ->
                    val isSelected = currentDestination == dest
                    val icon = when (dest) {
                        AppDestination.HOME -> Icons.Default.Home
                        AppDestination.TOOLS -> Icons.Default.Tune
                        AppDestination.LAUNCHER -> Icons.Default.RocketLaunch
                        AppDestination.DEVELOPER -> Icons.Default.Badge
                    }

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.navigateTo(dest) },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = dest.title,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = dest.title,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonCyan,
                            selectedTextColor = NeonCyan,
                            indicatorColor = NeonCyan.copy(alpha = 0.15f),
                            unselectedIconColor = TextTertiary,
                            unselectedTextColor = TextTertiary
                        ),
                        modifier = Modifier.testTag("nav_${dest.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
        ) {
            when (currentDestination) {
                AppDestination.HOME -> {
                    item {
                        HomeScreen(
                            deviceCapability = deviceCapability,
                            shizukuStatus = shizukuStatus,
                            activeSession = activeSession,
                            sensX = sensX,
                            sensY = sensY,
                            onStopSession = { viewModel.stopSession(context) },
                            onNavigate = { dest, subTab ->
                                viewModel.navigateTo(dest)
                                if (subTab != null) {
                                    viewModel.setToolsSubTab(subTab)
                                }
                            }
                        )
                    }
                }

                AppDestination.TOOLS -> {
                    item {
                        ToolsScreen(
                            currentSubTab = currentToolsSubTab,
                            onSubTabSelected = { viewModel.setToolsSubTab(it) },
                            sensX = sensX,
                            sensY = sensY,
                            isLinked = isLinked,
                            linkRatio = linkRatio,
                            preset = preset,
                            onSensXChange = { viewModel.setSensX(it) },
                            onSensYChange = { viewModel.setSensY(it) },
                            onLinkToggle = { viewModel.setLinked(it) },
                            onLinkRatioChange = { viewModel.setLinkRatio(it) },
                            onPresetSelect = { viewModel.applyPreset(it) },
                            onReset = { viewModel.resetToDefault() },
                            onApplySensitivity = {
                                val success = viewModel.applySensitivity(context)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = if (success) "Sensitivity applied to Android system" else "Apply completed (Check Write Settings permission)",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            },
                            calibrationStats = calibrationStats,
                            onTouchDelta = { rx, ry, dx, dy ->
                                viewModel.processCalibrationTouch(rx, ry, dx, dy)
                            },
                            onResetCalibrationStats = { viewModel.resetCalibration() },
                            shizukuStatus = shizukuStatus,
                            deviceCapability = deviceCapability,
                            targetDpi = targetDpi,
                            isPrivilegedDpiEnabled = privilegedDpiEnabled,
                            onRequestShizukuPermission = { viewModel.requestShizukuPermission() },
                            onTogglePrivilegedDpi = { viewModel.setPrivilegedDpiEnabled(it) },
                            diagnosticLogs = diagnosticLogs,
                            savedProfiles = savedProfiles,
                            onDeleteProfile = { viewModel.deleteProfile(it) },
                            onLoadProfile = { profile ->
                                viewModel.selectGame(com.example.model.InstalledGame(profile.packageName, profile.gameName))
                            }
                        )
                    }
                }

                AppDestination.LAUNCHER -> {
                    if (activeSession.isRunning) {
                        item {
                            SessionCard(
                                activeSession = activeSession,
                                shizukuStatus = shizukuStatus,
                                onStopSession = { viewModel.stopSession(context) }
                            )
                        }
                    }

                    item {
                        GameLauncherSection(
                            launcherGames = launcherGames,
                            allInstalledApps = allInstalledApps,
                            savedProfiles = savedProfiles,
                            onLaunchGameClicked = { game ->
                                viewModel.openModeSelectionForGame(game)
                            },
                            onAddGameToLauncher = { game ->
                                viewModel.addGameToLauncher(game)
                            },
                            onRemoveGameFromLauncher = { pkg ->
                                viewModel.removeGameFromLauncher(pkg)
                            }
                        )
                    }
                }

                AppDestination.DEVELOPER -> {
                    item {
                        DeveloperScreen()
                    }
                }
            }
        }
    }

    // Modal Sheet for Launch Mode Selection
    gameForModeSelection?.let { game ->
        val profile = savedProfiles.firstOrNull { it.packageName == game.packageName }
        val initialMode = profile?.preferredMode?.let {
            try { com.example.model.LaunchMode.valueOf(it) } catch (_: Exception) { com.example.model.LaunchMode.OPTIMIZE }
        } ?: com.example.model.LaunchMode.OPTIMIZE

        LaunchModeDialog(
            game = game,
            deviceCapability = deviceCapability,
            shizukuStatus = shizukuStatus,
            sensX = sensX,
            sensY = sensY,
            initialMode = initialMode,
            onDismiss = { viewModel.closeModeSelection() },
            onConfirmLaunch = { selectedMode ->
                viewModel.launchGameWithMode(context, game, selectedMode)
            }
        )
    }
}
