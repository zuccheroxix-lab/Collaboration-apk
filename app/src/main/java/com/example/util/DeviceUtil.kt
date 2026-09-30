package com.example.util

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.os.Process
import android.provider.Settings
import android.view.Display
import androidx.core.content.ContextCompat
import com.example.model.CompatibilityLevel
import com.example.model.DeviceCapability
import com.example.model.ShizukuStatus
import com.example.shizuku.ShizukuManager

object DeviceUtil {

    const val SETTING_POINTER_SPEED = "pointer_speed"

    fun getDeviceCapability(context: Context): DeviceCapability {
        val dm = context.resources.displayMetrics
        val resolver = context.contentResolver

        val pointerSpeed = try {
            Settings.System.getInt(resolver, SETTING_POINTER_SPEED, 0)
        } catch (_: Exception) {
            0
        }

        val hasWriteSettings = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.System.canWrite(context)
        } else {
            true
        }

        val hasOverlay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }

        val hasNotification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        val is64Bit = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Process.is64Bit()
        } else {
            Build.SUPPORTED_64_BIT_ABIS.isNotEmpty()
        }

        val abi = Build.SUPPORTED_ABIS.firstOrNull() ?: "unknown"

        // Real Battery Level
        val batteryFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, batteryFilter)
        val batteryLevel = batteryStatus?.let { intent ->
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            if (level >= 0 && scale > 0) (level * 100 / scale) else 100
        } ?: 100

        // Real Thermal Status (API 29+)
        val thermalStatusStr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            when (powerManager?.currentThermalStatus) {
                PowerManager.THERMAL_STATUS_NONE -> "NORMAL"
                PowerManager.THERMAL_STATUS_LIGHT -> "LIGHT (WARM)"
                PowerManager.THERMAL_STATUS_MODERATE -> "MODERATE"
                PowerManager.THERMAL_STATUS_SEVERE -> "SEVERE (THROTTLED)"
                PowerManager.THERMAL_STATUS_CRITICAL -> "CRITICAL"
                PowerManager.THERMAL_STATUS_EMERGENCY -> "EMERGENCY"
                PowerManager.THERMAL_STATUS_SHUTDOWN -> "SHUTDOWN"
                else -> "NOMINAL"
            }
        } else {
            "NOMINAL (API < 29)"
        }

        // Real Display Refresh Rates
        val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
        val defaultDisplay = displayManager?.getDisplay(Display.DEFAULT_DISPLAY)
        val currentRefreshRate = defaultDisplay?.refreshRate ?: 60f
        val supportedRates = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            defaultDisplay?.supportedModes?.map { it.refreshRate }?.distinct()?.sorted() ?: listOf(60f)
        } else {
            listOf(currentRefreshRate)
        }

        val shizukuStatus = ShizukuManager.getStatus()

        val compatibility = when {
            shizukuStatus == ShizukuStatus.PERMISSION_GRANTED -> CompatibilityLevel.SUPPORTED
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> CompatibilityLevel.PARTIALLY_SUPPORTED
            else -> CompatibilityLevel.UNSUPPORTED
        }

        return DeviceCapability(
            androidVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            sdkInt = Build.VERSION.SDK_INT,
            abi = abi,
            is64Bit = is64Bit,
            displayWidth = dm.widthPixels,
            displayHeight = dm.heightPixels,
            defaultDpi = dm.densityDpi,
            currentDpi = dm.densityDpi,
            currentRefreshRate = currentRefreshRate,
            supportedRefreshRates = supportedRates,
            batteryLevel = batteryLevel,
            thermalStatus = thermalStatusStr,
            deviceName = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}",
            systemPointerSpeed = pointerSpeed,
            hasWriteSettings = hasWriteSettings,
            hasOverlayPermission = hasOverlay,
            hasNotificationPermission = hasNotification,
            shizukuStatus = shizukuStatus,
            compatibility = compatibility
        )
    }

    /**
     * Apply pointer speed directly via Settings.System (requires WRITE_SETTINGS)
     */
    fun applySystemPointerSpeed(context: Context, speed: Int): Boolean {
        val clamped = speed.coerceIn(-7, 7)
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.System.canWrite(context)) {
                    Settings.System.putInt(
                        context.contentResolver,
                        SETTING_POINTER_SPEED,
                        clamped
                    )
                    true
                } else {
                    false
                }
            } else {
                Settings.System.putInt(
                    context.contentResolver,
                    SETTING_POINTER_SPEED,
                    clamped
                )
                true
            }
        } catch (_: Exception) {
            false
        }
    }
}
