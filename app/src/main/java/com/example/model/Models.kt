package com.example.model

enum class ShizukuStatus(val displayName: String) {
    NOT_RUNNING("NOT RUNNING"),
    RUNNING("RUNNING"),
    PERMISSION_GRANTED("PERMISSION GRANTED"),
    PERMISSION_DENIED("PERMISSION DENIED")
}

enum class LaunchMode(val displayName: String, val description: String) {
    OPTIMIZE(
        "OPTIMIZE",
        "Balanced mode. Safe background workload trimming, balanced touch response, optimal thermals."
    ),
    STABLE(
        "STABLE",
        "Endurance mode. Consistent framerate, stable touch sampling, no aggressive clock or thermal spikes."
    ),
    PERFORMANCE(
        "PERFORMANCE",
        "Performance mode. Maximum supported refresh rate, reduced animation latency, prioritized pointer speed."
    )
}

enum class CompatibilityLevel(val displayName: String) {
    SUPPORTED("SUPPORTED"),
    PARTIALLY_SUPPORTED("PARTIALLY SUPPORTED"),
    UNSUPPORTED("UNSUPPORTED")
}

enum class PresetType {
    LOW,
    MEDIUM,
    HIGH,
    CUSTOM
}

data class SystemSettingsBackup(
    val pointerSpeed: Int = 0,
    val windowAnimationScale: Float = 1.0f,
    val transitionAnimationScale: Float = 1.0f,
    val animatorDurationScale: Float = 1.0f,
    val peakRefreshRate: Float = 0f,
    val minRefreshRate: Float = 0f,
    val isBackedUp: Boolean = false
)

data class DeviceCapability(
    val androidVersion: String,
    val sdkInt: Int,
    val abi: String,
    val is64Bit: Boolean,
    val displayWidth: Int,
    val displayHeight: Int,
    val defaultDpi: Int,
    val currentDpi: Int,
    val currentRefreshRate: Float,
    val supportedRefreshRates: List<Float>,
    val batteryLevel: Int,
    val thermalStatus: String,
    val deviceName: String = "",
    val systemPointerSpeed: Int, // -7 to +7
    val hasWriteSettings: Boolean,
    val hasOverlayPermission: Boolean,
    val hasNotificationPermission: Boolean,
    val shizukuStatus: ShizukuStatus,
    val compatibility: CompatibilityLevel
)

enum class AppDestination(val title: String) {
    HOME("Home"),
    TOOLS("Tools"),
    LAUNCHER("Launcher"),
    DEVELOPER("Developer")
}

enum class ToolsSubTab(val title: String) {
    SENSITIVITY("Sensitivity"),
    CALIBRATION("Calibration"),
    SHIZUKU("Shizuku"),
    DIAGNOSTICS("Diagnostics"),
    PROFILES("Game Profiles"),
    SETTINGS("Settings")
}

data class AppSettings(
    val isDarkUi: Boolean = true,
    val isCompactMode: Boolean = false,
    val confirmBeforeSession: Boolean = true,
    val autoRestore: Boolean = true,
    val rememberLastProfile: Boolean = true,
    val lastSelectedPackage: String? = null
)

data class DiagnosticLogEntry(
    val id: Long = System.currentTimeMillis(),
    val timestamp: Long = System.currentTimeMillis(),
    val tag: String,
    val message: String,
    val isSuccess: Boolean = true
)

data class CalibrationStats(
    val rawX: Float = 0f,
    val rawY: Float = 0f,
    val rawDeltaX: Float = 0f,
    val rawDeltaY: Float = 0f,
    val transformedDeltaX: Float = 0f,
    val transformedDeltaY: Float = 0f,
    val cumulativeRawDistance: Float = 0f,
    val cumulativeTransformedDistance: Float = 0f,
    val sampleCount: Int = 0,
    val eventFrequencyHz: Int = 0,
    val lastEventTimestamp: Long = 0L,
    val isVerified: Boolean = false,
    val methodUsed: String = "Native Coordinate Transformation & Hardware Pointer Speed",
    val compatibilityNote: String = "Direct Kernel Hooking: UNSUPPORTED on non-root Android"
)

data class InstalledGame(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean = false,
    val isDetectedAsGame: Boolean = true,
    val isAddedToLauncher: Boolean = true
)

data class ActiveSessionState(
    val isRunning: Boolean = false,
    val gamePackage: String? = null,
    val gameName: String? = null,
    val mode: LaunchMode? = null,
    val appliedSensX: Float = 1.0f,
    val appliedSensY: Float = 1.0f,
    val startedAt: Long = 0L,
    val status: String = "RUNNING",
    val appliedOptimizations: List<String> = emptyList()
)
