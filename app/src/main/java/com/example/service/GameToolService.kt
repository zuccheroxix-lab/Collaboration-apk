package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.GameProfileEntity
import com.example.engine.SensitivityEngine
import com.example.model.ActiveSessionState
import com.example.model.LaunchMode
import com.example.model.ShizukuStatus
import com.example.model.SystemSettingsBackup
import com.example.shizuku.ShizukuManager
import com.example.util.DeviceUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class GameToolService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var overlayManager: FloatingOverlayManager? = null
    private var currentProfile: GameProfileEntity? = null
    private var settingsBackup: SystemSettingsBackup = SystemSettingsBackup()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        overlayManager = FloatingOverlayManager(this)
        _isServiceRunning.value = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START

        if (action == ACTION_STOP) {
            val autoRestore = intent?.getBooleanExtra(EXTRA_AUTO_RESTORE, true) ?: true
            stopToolService(autoRestore)
            return START_NOT_STICKY
        }

        val sensX = intent?.getFloatExtra(EXTRA_SENS_X, 1.0f) ?: 1.0f
        val sensY = intent?.getFloatExtra(EXTRA_SENS_Y, 1.0f) ?: 1.0f
        val pkg = intent?.getStringExtra(EXTRA_PACKAGE) ?: "global"
        val gameName = intent?.getStringExtra(EXTRA_GAME_NAME) ?: "Sensiv Engine"
        val targetDpi = intent?.getIntExtra(EXTRA_TARGET_DPI, 0) ?: 0
        val pointerSpeed = intent?.getIntExtra(EXTRA_POINTER_SPEED, 0) ?: 0
        val useOverlay = intent?.getBooleanExtra(EXTRA_USE_OVERLAY, false) ?: false
        val modeStr = intent?.getStringExtra(EXTRA_LAUNCH_MODE) ?: LaunchMode.OPTIMIZE.name
        val launchMode = try { LaunchMode.valueOf(modeStr) } catch (_: Exception) { LaunchMode.OPTIMIZE }

        val profile = GameProfileEntity(
            packageName = pkg,
            gameName = gameName,
            sensX = sensX,
            sensY = sensY,
            targetDpi = targetDpi,
            pointerSpeed = pointerSpeed,
            useOverlay = useOverlay,
            preferredMode = launchMode.name
        )
        currentProfile = profile
        _activeProfile.value = profile

        // 1. Start foreground with notification
        val notification = buildNotification(profile, launchMode)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        // 2. Backup current settings before applying any modifications
        serviceScope.launch(Dispatchers.IO) {
            settingsBackup = ShizukuManager.backupCurrentSettings(this@GameToolService)
            _activeBackup.value = settingsBackup

            val appliedOptimizations = mutableListOf<String>()

            // 3. Apply hardware pointer speed
            val mappedSpeed = SensitivityEngine.mapSensitivityToPointerSpeed((sensX + sensY) / 2f)
            val speedOk = DeviceUtil.applySystemPointerSpeed(this@GameToolService, mappedSpeed)
            if (speedOk) {
                appliedOptimizations.add("Pointer speed level $mappedSpeed")
            }

            // 4. Apply Shizuku Privileged settings based on LaunchMode
            if (ShizukuManager.getStatus() == ShizukuStatus.PERMISSION_GRANTED) {
                when (launchMode) {
                    LaunchMode.OPTIMIZE -> {
                        ShizukuManager.writeAndVerifySetting("global", "window_animation_scale", "0.75")
                        ShizukuManager.writeAndVerifySetting("global", "transition_animation_scale", "0.75")
                        ShizukuManager.writeAndVerifySetting("global", "animator_duration_scale", "0.75")
                        appliedOptimizations.add("Animation latency scale: 0.75x")
                    }
                    LaunchMode.STABLE -> {
                        ShizukuManager.writeAndVerifySetting("global", "window_animation_scale", "1.00")
                        ShizukuManager.writeAndVerifySetting("global", "transition_animation_scale", "1.00")
                        ShizukuManager.writeAndVerifySetting("global", "animator_duration_scale", "1.00")
                        appliedOptimizations.add("Standard animation consistency: 1.00x")
                    }
                    LaunchMode.PERFORMANCE -> {
                        ShizukuManager.writeAndVerifySetting("global", "window_animation_scale", "0.50")
                        ShizukuManager.writeAndVerifySetting("global", "transition_animation_scale", "0.50")
                        ShizukuManager.writeAndVerifySetting("global", "animator_duration_scale", "0.50")
                        appliedOptimizations.add("Ultra-low UI latency scale: 0.50x")

                        // High refresh rate lock if supported
                        val deviceCap = DeviceUtil.getDeviceCapability(this@GameToolService)
                        val maxRate = deviceCap.supportedRefreshRates.maxOrNull() ?: 60f
                        if (maxRate > 60f) {
                            ShizukuManager.writeAndVerifySetting("system", "peak_refresh_rate", String.format(Locale.US, "%.1f", maxRate))
                            ShizukuManager.writeAndVerifySetting("system", "min_refresh_rate", String.format(Locale.US, "%.1f", maxRate))
                            appliedOptimizations.add("Peak refresh rate locked: ${maxRate.toInt()}Hz")
                        }
                    }
                }

                if (targetDpi > 0) {
                    ShizukuManager.setWmDensity(targetDpi)
                    appliedOptimizations.add("DPI touch travel scaled: ${targetDpi} DPI")
                }
                ShizukuManager.setSystemPointerSpeed(mappedSpeed)
            }

            _activeSession.value = ActiveSessionState(
                isRunning = true,
                gamePackage = pkg,
                gameName = gameName,
                mode = launchMode,
                appliedSensX = sensX,
                appliedSensY = sensY,
                startedAt = System.currentTimeMillis(),
                appliedOptimizations = appliedOptimizations
            )
        }

        // 5. Show overlay if enabled
        if (useOverlay) {
            overlayManager?.show(profile)
        } else {
            overlayManager?.hide()
        }

        return START_STICKY
    }

    private fun buildNotification(profile: GameProfileEntity, mode: LaunchMode): Notification {
        val stopIntent = Intent(this, GameToolService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            101,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val mainIntent = Intent(this, MainActivity::class.java)
        val mainPendingIntent = PendingIntent.getActivity(
            this,
            102,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val content = "[${mode.name}] Sens X: ${String.format(Locale.US, "%.2f", profile.sensX)}x | Y: ${String.format(Locale.US, "%.2f", profile.sensY)}x • ${profile.gameName}"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("⚡ Sensiv Game Engine Active")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(mainPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop Session", stopPendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Sensiv Game Tools Engine",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Active game sensitivity and optimization session"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun stopToolService(autoRestore: Boolean = true) {
        overlayManager?.hide()

        // AUTO RESTORE: Restore previous backed up system settings if enabled
        if (autoRestore) {
            serviceScope.launch(Dispatchers.IO) {
                if (settingsBackup.isBackedUp) {
                    ShizukuManager.restoreSettings(settingsBackup)
                    DeviceUtil.applySystemPointerSpeed(this@GameToolService, settingsBackup.pointerSpeed)
                } else {
                    DeviceUtil.applySystemPointerSpeed(this@GameToolService, 0)
                    if (ShizukuManager.getStatus() == ShizukuStatus.PERMISSION_GRANTED) {
                        ShizukuManager.resetWmDensity()
                        ShizukuManager.setSystemPointerSpeed(0)
                    }
                }
            }
        }

        _isServiceRunning.value = false
        _activeProfile.value = null
        _activeSession.value = ActiveSessionState(isRunning = false)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        stopToolService()
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "sensiv_game_engine_channel"
        const val NOTIFICATION_ID = 2001

        const val ACTION_START = "com.example.ACTION_START"
        const val ACTION_STOP = "com.example.ACTION_STOP"

        const val EXTRA_SENS_X = "extra_sens_x"
        const val EXTRA_SENS_Y = "extra_sens_y"
        const val EXTRA_PACKAGE = "extra_package"
        const val EXTRA_GAME_NAME = "extra_game_name"
        const val EXTRA_TARGET_DPI = "extra_target_dpi"
        const val EXTRA_POINTER_SPEED = "extra_pointer_speed"
        const val EXTRA_USE_OVERLAY = "extra_use_overlay"
        const val EXTRA_LAUNCH_MODE = "extra_launch_mode"
        const val EXTRA_AUTO_RESTORE = "extra_auto_restore"

        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

        private val _activeProfile = MutableStateFlow<GameProfileEntity?>(null)
        val activeProfile: StateFlow<GameProfileEntity?> = _activeProfile.asStateFlow()

        private val _activeSession = MutableStateFlow(ActiveSessionState())
        val activeSession: StateFlow<ActiveSessionState> = _activeSession.asStateFlow()

        private val _activeBackup = MutableStateFlow<SystemSettingsBackup?>(null)
        val activeBackup: StateFlow<SystemSettingsBackup?> = _activeBackup.asStateFlow()

        fun start(context: Context, profile: GameProfileEntity, mode: LaunchMode) {
            val intent = Intent(context, GameToolService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_SENS_X, profile.sensX)
                putExtra(EXTRA_SENS_Y, profile.sensY)
                putExtra(EXTRA_PACKAGE, profile.packageName)
                putExtra(EXTRA_GAME_NAME, profile.gameName)
                putExtra(EXTRA_TARGET_DPI, profile.targetDpi)
                putExtra(EXTRA_POINTER_SPEED, profile.pointerSpeed)
                putExtra(EXTRA_USE_OVERLAY, profile.useOverlay)
                putExtra(EXTRA_LAUNCH_MODE, mode.name)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context, autoRestore: Boolean = true) {
            val intent = Intent(context, GameToolService::class.java).apply {
                action = ACTION_STOP
                putExtra(EXTRA_AUTO_RESTORE, autoRestore)
            }
            context.startService(intent)
        }
    }
}
