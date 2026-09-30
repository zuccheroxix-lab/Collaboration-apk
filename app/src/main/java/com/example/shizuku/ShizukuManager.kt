package com.example.shizuku

import android.content.Context
import android.content.pm.PackageManager
import com.example.model.ShizukuStatus
import com.example.model.SystemSettingsBackup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.reflect.Method
import java.util.Locale

data class CommandResult(
    val exitCode: Int,
    val output: String,
    val error: String,
    val isSuccess: Boolean
)

object ShizukuManager {

    const val SHIZUKU_REQUEST_CODE = 4001

    private var isListenersRegistered = false
    private val newProcessMethod: Method? by lazy {
        try {
            Shizuku::class.java.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            ).apply { isAccessible = true }
        } catch (_: Throwable) {
            null
        }
    }

    fun init(onStatusChanged: () -> Unit) {
        if (isListenersRegistered) return
        try {
            Shizuku.addBinderReceivedListenerSticky {
                onStatusChanged()
            }
            Shizuku.addBinderDeadListener {
                onStatusChanged()
            }
            Shizuku.addRequestPermissionResultListener { _, _ ->
                onStatusChanged()
            }
            isListenersRegistered = true
        } catch (_: Throwable) {
            // Shizuku provider not yet initialized or binder unavailable
        }
    }

    fun isShizukuInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo("moe.shizuku.privileged.api", 0)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun getStatus(): ShizukuStatus {
        return try {
            val ping = Shizuku.pingBinder()
            if (!ping) {
                return ShizukuStatus.NOT_RUNNING
            }
            if (Shizuku.isPreV11()) {
                return ShizukuStatus.RUNNING
            }
            val hasPermission = Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
            if (hasPermission) {
                ShizukuStatus.PERMISSION_GRANTED
            } else {
                ShizukuStatus.PERMISSION_DENIED
            }
        } catch (_: Throwable) {
            ShizukuStatus.NOT_RUNNING
        }
    }

    fun requestPermission() {
        try {
            if (Shizuku.pingBinder()) {
                if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                    Shizuku.requestPermission(SHIZUKU_REQUEST_CODE)
                }
            }
        } catch (_: Throwable) {
            // Failed to request permission
        }
    }

    suspend fun executePrivileged(command: String): CommandResult = withContext(Dispatchers.IO) {
        try {
            if (getStatus() != ShizukuStatus.PERMISSION_GRANTED) {
                return@withContext CommandResult(
                    exitCode = -1,
                    output = "",
                    error = "FAILED — Shizuku permission not granted or service not running",
                    isSuccess = false
                )
            }

            val method = newProcessMethod
                ?: return@withContext CommandResult(
                    exitCode = -1,
                    output = "",
                    error = "FAILED — Shizuku process bridge unavailable",
                    isSuccess = false
                )

            val process = method.invoke(
                null,
                arrayOf("sh", "-c", command),
                null,
                null
            ) as Process

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val errReader = BufferedReader(InputStreamReader(process.errorStream))
            val output = StringBuilder()
            val error = StringBuilder()

            var line: String?
            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }
            reader.close()

            while (errReader.readLine().also { line = it } != null) {
                error.append(line).append("\n")
            }
            errReader.close()

            val exitCode = process.waitFor()
            CommandResult(
                exitCode = exitCode,
                output = output.toString().trim(),
                error = error.toString().trim(),
                isSuccess = exitCode == 0
            )
        } catch (e: Throwable) {
            CommandResult(
                exitCode = -1,
                output = "",
                error = "FAILED — COMMAND NOT SUPPORTED (${e.localizedMessage ?: "execution error"})",
                isSuccess = false
            )
        }
    }

    suspend fun readSetting(namespace: String, key: String): String? {
        val result = executePrivileged("settings get $namespace $key")
        return if (result.isSuccess && result.output.isNotBlank() && result.output != "null") {
            result.output.trim()
        } else {
            null
        }
    }

    suspend fun writeAndVerifySetting(
        namespace: String,
        key: String,
        value: String
    ): Boolean {
        val writeResult = executePrivileged("settings put $namespace $key $value")
        if (!writeResult.isSuccess) return false
        val verifyResult = executePrivileged("settings get $namespace $key")
        return verifyResult.isSuccess && verifyResult.output.trim() == value.trim()
    }

    suspend fun backupCurrentSettings(context: Context): SystemSettingsBackup = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        var pointerSpeed = 0
        try {
            pointerSpeed = android.provider.Settings.System.getInt(resolver, "pointer_speed", 0)
        } catch (_: Exception) {}

        var winScale = 1.0f
        var transScale = 1.0f
        var animScale = 1.0f
        var peakRefresh = 0f
        var minRefresh = 0f

        if (getStatus() == ShizukuStatus.PERMISSION_GRANTED) {
            readSetting("global", "window_animation_scale")?.toFloatOrNull()?.let { winScale = it }
            readSetting("global", "transition_animation_scale")?.toFloatOrNull()?.let { transScale = it }
            readSetting("global", "animator_duration_scale")?.toFloatOrNull()?.let { animScale = it }
            readSetting("system", "peak_refresh_rate")?.toFloatOrNull()?.let { peakRefresh = it }
            readSetting("system", "min_refresh_rate")?.toFloatOrNull()?.let { minRefresh = it }
        }

        SystemSettingsBackup(
            pointerSpeed = pointerSpeed,
            windowAnimationScale = winScale,
            transitionAnimationScale = transScale,
            animatorDurationScale = animScale,
            peakRefreshRate = peakRefresh,
            minRefreshRate = minRefresh,
            isBackedUp = true
        )
    }

    suspend fun restoreSettings(backup: SystemSettingsBackup): Boolean = withContext(Dispatchers.IO) {
        if (!backup.isBackedUp) return@withContext false
        var allRestored = true

        if (getStatus() == ShizukuStatus.PERMISSION_GRANTED) {
            val winOk = writeAndVerifySetting("global", "window_animation_scale", String.format(Locale.US, "%.2f", backup.windowAnimationScale))
            val transOk = writeAndVerifySetting("global", "transition_animation_scale", String.format(Locale.US, "%.2f", backup.transitionAnimationScale))
            val animOk = writeAndVerifySetting("global", "animator_duration_scale", String.format(Locale.US, "%.2f", backup.animatorDurationScale))

            if (backup.peakRefreshRate > 0f) {
                writeAndVerifySetting("system", "peak_refresh_rate", String.format(Locale.US, "%.1f", backup.peakRefreshRate))
            } else {
                executePrivileged("settings delete system peak_refresh_rate")
            }

            if (backup.minRefreshRate > 0f) {
                writeAndVerifySetting("system", "min_refresh_rate", String.format(Locale.US, "%.1f", backup.minRefreshRate))
            } else {
                executePrivileged("settings delete system min_refresh_rate")
            }

            writeAndVerifySetting("system", "pointer_speed", backup.pointerSpeed.toString())
            allRestored = winOk && transOk && animOk
        }

        allRestored
    }

    suspend fun setWmDensity(dpi: Int): CommandResult {
        return executePrivileged("wm density $dpi")
    }

    suspend fun resetWmDensity(): CommandResult {
        return executePrivileged("wm density reset")
    }

    suspend fun setSystemPointerSpeed(speed: Int): CommandResult {
        val clamped = speed.coerceIn(-7, 7)
        return executePrivileged("settings put system pointer_speed $clamped")
    }
}
