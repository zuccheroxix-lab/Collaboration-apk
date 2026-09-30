package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.GameToolsApplication
import com.example.data.GameProfileEntity
import com.example.data.GameRepository
import com.example.engine.SensitivityEngine
import com.example.model.ActiveSessionState
import com.example.model.AppDestination
import com.example.model.CalibrationStats
import com.example.model.DeviceCapability
import com.example.model.DiagnosticLogEntry
import com.example.model.InstalledGame
import com.example.model.LaunchMode
import com.example.model.PresetType
import com.example.model.ShizukuStatus
import com.example.model.ToolsSubTab
import com.example.service.GameToolService
import com.example.shizuku.ShizukuManager
import com.example.util.DeviceUtil
import com.example.util.GameScanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GameToolsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GameRepository = (application as GameToolsApplication).repository
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    // Navigation State
    private val _currentDestination = MutableStateFlow(AppDestination.HOME)
    val currentDestination: StateFlow<AppDestination> = _currentDestination.asStateFlow()

    private val _currentToolsSubTab = MutableStateFlow(ToolsSubTab.SENSITIVITY)
    val currentToolsSubTab: StateFlow<ToolsSubTab> = _currentToolsSubTab.asStateFlow()

    private val _gameSearchQuery = MutableStateFlow("")
    val gameSearchQuery: StateFlow<String> = _gameSearchQuery.asStateFlow()

    // UI Tab Index (backward compatibility)
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // Sensitivity State
    private val _sensX = MutableStateFlow(1.00f)
    val sensX: StateFlow<Float> = _sensX.asStateFlow()

    private val _sensY = MutableStateFlow(1.00f)
    val sensY: StateFlow<Float> = _sensY.asStateFlow()

    private val _isLinked = MutableStateFlow(true)
    val isLinked: StateFlow<Boolean> = _isLinked.asStateFlow()

    private val _linkRatio = MutableStateFlow(1.00f)
    val linkRatio: StateFlow<Float> = _linkRatio.asStateFlow()

    private val _preset = MutableStateFlow(PresetType.MEDIUM)
    val preset: StateFlow<PresetType> = _preset.asStateFlow()

    // Games and Profiles
    private val _allInstalledApps = MutableStateFlow<List<InstalledGame>>(emptyList())
    val allInstalledApps: StateFlow<List<InstalledGame>> = _allInstalledApps.asStateFlow()

    private val _launcherGames = MutableStateFlow<List<InstalledGame>>(emptyList())
    val launcherGames: StateFlow<List<InstalledGame>> = _launcherGames.asStateFlow()

    private val _savedProfiles = MutableStateFlow<List<GameProfileEntity>>(emptyList())
    val savedProfiles: StateFlow<List<GameProfileEntity>> = _savedProfiles.asStateFlow()

    private val _selectedGame = MutableStateFlow<InstalledGame?>(null)
    val selectedGame: StateFlow<InstalledGame?> = _selectedGame.asStateFlow()

    // Launch Mode Selection Modal
    private val _gameForModeSelection = MutableStateFlow<InstalledGame?>(null)
    val gameForModeSelection: StateFlow<InstalledGame?> = _gameForModeSelection.asStateFlow()

    // Device and Permissions
    private val _deviceCapability = MutableStateFlow(DeviceUtil.getDeviceCapability(application))
    val deviceCapability: StateFlow<DeviceCapability> = _deviceCapability.asStateFlow()

    private val _shizukuStatus = MutableStateFlow(ShizukuManager.getStatus())
    val shizukuStatus: StateFlow<ShizukuStatus> = _shizukuStatus.asStateFlow()

    // Features config
    private val _privilegedDpiEnabled = MutableStateFlow(false)
    val privilegedDpiEnabled: StateFlow<Boolean> = _privilegedDpiEnabled.asStateFlow()

    private val _targetDpi = MutableStateFlow(0)
    val targetDpi: StateFlow<Int> = _targetDpi.asStateFlow()

    private val _useOverlayHud = MutableStateFlow(false)
    val useOverlayHud: StateFlow<Boolean> = _useOverlayHud.asStateFlow()

    // Calibration
    private val _calibrationStats = MutableStateFlow(CalibrationStats())
    val calibrationStats: StateFlow<CalibrationStats> = _calibrationStats.asStateFlow()

    // Diagnostics Log
    private val _diagnosticLogs = MutableStateFlow<List<DiagnosticLogEntry>>(emptyList())
    val diagnosticLogs: StateFlow<List<DiagnosticLogEntry>> = _diagnosticLogs.asStateFlow()

    // Active session
    val activeSession: StateFlow<ActiveSessionState> = GameToolService.activeSession
    val isToolRunning: StateFlow<Boolean> = GameToolService.isServiceRunning

    init {
        refreshHardwareInfo()
        loadInstalledApps()
        observeProfiles()
        addLog("SYSTEM", "Sensiv Game Tools initialized. Compatibility: ${_deviceCapability.value.compatibility.displayName}", true)
        logInputMethodCapabilities()
    }

    fun navigateTo(destination: AppDestination) {
        _currentDestination.value = destination
    }

    fun setToolsSubTab(subTab: ToolsSubTab) {
        _currentToolsSubTab.value = subTab
    }

    fun setGameSearchQuery(query: String) {
        _gameSearchQuery.value = query
    }

    fun applySensitivity(context: Context): Boolean {
        val speed = SensitivityEngine.mapSensitivityToPointerSpeed((_sensX.value + _sensY.value) / 2f)
        val pointerSuccess = DeviceUtil.applySystemPointerSpeed(context, speed)
        
        viewModelScope.launch {
            if (_privilegedDpiEnabled.value && _shizukuStatus.value == ShizukuStatus.PERMISSION_GRANTED) {
                if (_targetDpi.value > 0) {
                    ShizukuManager.setWmDensity(_targetDpi.value)
                }
                ShizukuManager.setSystemPointerSpeed(speed)
            }
        }
        
        addLog("APPLY", "Sensitivity applied: X=${_sensX.value}x, Y=${_sensY.value}x -> System Pointer Speed: $speed", pointerSuccess)
        return pointerSuccess
    }

    fun handleBackPress(): Boolean {
        if (_gameForModeSelection.value != null) {
            _gameForModeSelection.value = null
            return true
        }
        if (_currentDestination.value != AppDestination.HOME) {
            _currentDestination.value = AppDestination.HOME
            return true
        }
        return false
    }

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    private fun logInputMethodCapabilities() {
        val cap = _deviceCapability.value
        addLog("DEVICE", "${cap.androidVersion} • ABI: ${cap.abi} (${if (cap.is64Bit) "64-bit" else "32-bit"})", true)
        addLog("BATTERY", "Battery: ${cap.batteryLevel}% • Thermal: ${cap.thermalStatus}", true)
        addLog("DISPLAY", "Screen: ${cap.displayWidth}x${cap.displayHeight} @ ${cap.defaultDpi} DPI • Refresh Rate: ${cap.currentRefreshRate.toInt()}Hz", true)
        addLog("SHIZUKU", "Privileged ADB Bridge: ${cap.shizukuStatus.displayName}", cap.shizukuStatus == ShizukuStatus.PERMISSION_GRANTED)
        addLog("SECURITY", "Direct Kernel Touch Hooking: UNSUPPORTED on non-root Android (Linux /dev/input sandbox). Supported methods: Pointer Speed, Shizuku DPI, Assistive Overlay.", true)
    }

    fun refreshHardwareInfo() {
        val app = getApplication<Application>()
        _deviceCapability.value = DeviceUtil.getDeviceCapability(app)
        _shizukuStatus.value = ShizukuManager.getStatus()
    }

    private fun loadInstalledApps() {
        viewModelScope.launch {
            val apps = GameScanner.getInstalledGames(getApplication())
            _allInstalledApps.value = apps
            // Games added to launcher by default
            _launcherGames.value = apps.filter { it.isDetectedAsGame }
            if (_selectedGame.value == null && _launcherGames.value.isNotEmpty()) {
                _selectedGame.value = _launcherGames.value.first()
            }
        }
    }

    private fun observeProfiles() {
        viewModelScope.launch {
            repository.getAllProfiles().collect { list ->
                _savedProfiles.value = list
            }
        }
    }

    fun addGameToLauncher(game: InstalledGame) {
        val current = _launcherGames.value.toMutableList()
        if (current.none { it.packageName == game.packageName }) {
            current.add(game.copy(isAddedToLauncher = true))
            _launcherGames.value = current
            addLog("LAUNCHER", "Added ${game.appName} to Game Launcher", true)
        }
    }

    fun removeGameFromLauncher(packageName: String) {
        val current = _launcherGames.value.toMutableList()
        current.removeAll { it.packageName == packageName }
        _launcherGames.value = current
        addLog("LAUNCHER", "Removed $packageName from Game Launcher", true)
    }

    fun openModeSelectionForGame(game: InstalledGame) {
        _gameForModeSelection.value = game
        selectGame(game)
    }

    fun closeModeSelection() {
        _gameForModeSelection.value = null
    }

    fun launchGameWithMode(context: Context, game: InstalledGame, mode: LaunchMode) {
        _gameForModeSelection.value = null
        addLog("MODE", "[${mode.displayName}] Validating requirements for ${game.appName}...", true)

        val cap = _deviceCapability.value

        // Check device condition
        addLog("DEVICE_CHECK", "Battery: ${cap.batteryLevel}% | Thermal: ${cap.thermalStatus} | Android: ${cap.androidVersion}", true)

        // Validate Shizuku
        val isShizukuOk = _shizukuStatus.value == ShizukuStatus.PERMISSION_GRANTED
        if (!isShizukuOk) {
            addLog("SHIZUKU", "Privileged ADB not granted. Applying Native System Pointer and Overlay methods only. (SKIP Shizuku commands)", false)
        }

        // Start session and apply configurations
        val profile = GameProfileEntity(
            packageName = game.packageName,
            gameName = game.appName,
            sensX = _sensX.value,
            sensY = _sensY.value,
            isLinked = _isLinked.value,
            linkRatio = _linkRatio.value,
            preset = _preset.value.name,
            pointerSpeed = SensitivityEngine.mapSensitivityToPointerSpeed((_sensX.value + _sensY.value) / 2f),
            targetDpi = if (_privilegedDpiEnabled.value) _targetDpi.value else 0,
            useOverlay = _useOverlayHud.value,
            preferredMode = mode.name
        )

        viewModelScope.launch {
            repository.saveProfile(profile)
        }

        GameToolService.start(context, profile, mode)
        addLog("CONFIG_APPLIED", "Mode [${mode.displayName}] applied. Sens X: ${_sensX.value}x, Y: ${_sensY.value}x", true)

        // Launch Game Intent
        val launched = GameScanner.launchApp(context, game.packageName)
        if (launched) {
            addLog("LAUNCH", "SUCCESS: Launched ${game.appName}. Session RUNNING.", true)
        } else {
            addLog("LAUNCH", "FAILED: Could not open package ${game.packageName}", false)
        }
    }

    fun stopSession(context: Context) {
        addLog("SESSION", "Stopping session. Initiating AUTO RESTORE...", true)
        GameToolService.stop(context)
        addLog("RESTORE", "Settings restored to previous baseline. Session STOPPED.", true)
    }

    fun setSensX(newX: Float) {
        val clamped = (newX * 100).toInt() / 100f
        _sensX.value = clamped
        if (_isLinked.value) {
            val newY = SensitivityEngine.calculateLinkedY(clamped, _linkRatio.value)
            _sensY.value = newY
        }
        _preset.value = SensitivityEngine.detectPreset(_sensX.value, _sensY.value)
        updateComputedTargetDpi()
    }

    fun setSensY(newY: Float) {
        val clamped = (newY * 100).toInt() / 100f
        _sensY.value = clamped
        if (_isLinked.value) {
            val newX = SensitivityEngine.calculateLinkedX(clamped, _linkRatio.value)
            _sensX.value = newX
        }
        _preset.value = SensitivityEngine.detectPreset(_sensX.value, _sensY.value)
        updateComputedTargetDpi()
    }

    fun setLinked(linked: Boolean) {
        _isLinked.value = linked
        if (linked && _sensX.value > 0f) {
            _linkRatio.value = _sensY.value / _sensX.value
            addLog("ENGINE", "X/Y Linked at ratio ${String.format(Locale.US, "%.2f", _linkRatio.value)}", true)
        } else {
            addLog("ENGINE", "X/Y Unlinked (independent axes)", true)
        }
    }

    fun setLinkRatio(ratio: Float) {
        _linkRatio.value = ratio
        if (_isLinked.value) {
            _sensY.value = SensitivityEngine.calculateLinkedY(_sensX.value, ratio)
            updateComputedTargetDpi()
        }
    }

    fun applyPreset(presetType: PresetType) {
        val (px, py) = SensitivityEngine.getPresetValues(presetType)
        _preset.value = presetType
        _sensX.value = px
        _sensY.value = py
        if (_isLinked.value) {
            _linkRatio.value = if (px > 0) py / px else 1.0f
        }
        updateComputedTargetDpi()
        addLog("PRESET", "Preset applied: $presetType (X=${px}x, Y=${py}x)", true)
    }

    fun resetToDefault() {
        _sensX.value = 1.00f
        _sensY.value = 1.00f
        _isLinked.value = true
        _linkRatio.value = 1.00f
        _preset.value = PresetType.MEDIUM
        _privilegedDpiEnabled.value = false
        _useOverlayHud.value = false
        _targetDpi.value = _deviceCapability.value.defaultDpi
        addLog("RESET", "Parameters reverted to system defaults (1.00x)", true)
    }

    private fun updateComputedTargetDpi() {
        if (_privilegedDpiEnabled.value) {
            _targetDpi.value = SensitivityEngine.calculateTargetDpi(
                _deviceCapability.value.defaultDpi,
                _sensX.value,
                _sensY.value
            )
        }
    }

    fun setPrivilegedDpiEnabled(enabled: Boolean) {
        _privilegedDpiEnabled.value = enabled
        if (enabled) {
            updateComputedTargetDpi()
            addLog("SHIZUKU_DPI", "Privileged DPI scaling activated (Target: ${_targetDpi.value} DPI)", true)
        } else {
            addLog("SHIZUKU_DPI", "Privileged DPI scaling disabled", true)
            viewModelScope.launch {
                ShizukuManager.resetWmDensity()
            }
        }
    }

    fun setUseOverlayHud(enabled: Boolean) {
        _useOverlayHud.value = enabled
        addLog("OVERLAY", "Floating game calibration HUD: ${if (enabled) "ENABLED" else "DISABLED"}", true)
    }

    fun selectGame(game: InstalledGame) {
        _selectedGame.value = game
        viewModelScope.launch {
            val existing = repository.getProfileSync(game.packageName)
            if (existing != null) {
                _sensX.value = existing.sensX
                _sensY.value = existing.sensY
                _isLinked.value = existing.isLinked
                _linkRatio.value = existing.linkRatio
                _preset.value = try { PresetType.valueOf(existing.preset) } catch (_: Exception) { PresetType.CUSTOM }
                _useOverlayHud.value = existing.useOverlay
                _privilegedDpiEnabled.value = existing.targetDpi > 0
                _targetDpi.value = if (existing.targetDpi > 0) existing.targetDpi else _deviceCapability.value.defaultDpi
                addLog("PROFILE", "Loaded profile for ${game.appName} (X=${existing.sensX}x, Y=${existing.sensY}x, Mode=${existing.preferredMode})", true)
            } else {
                addLog("PROFILE", "Selected ${game.appName} (Using active sensitivity)", true)
            }
        }
    }

    fun saveCurrentProfile() {
        val game = _selectedGame.value ?: return
        viewModelScope.launch {
            val profile = GameProfileEntity(
                packageName = game.packageName,
                gameName = game.appName,
                sensX = _sensX.value,
                sensY = _sensY.value,
                isLinked = _isLinked.value,
                linkRatio = _linkRatio.value,
                preset = _preset.value.name,
                pointerSpeed = SensitivityEngine.mapSensitivityToPointerSpeed((_sensX.value + _sensY.value) / 2f),
                targetDpi = if (_privilegedDpiEnabled.value) _targetDpi.value else 0,
                useOverlay = _useOverlayHud.value
            )
            repository.saveProfile(profile)
            addLog("PROFILE_SAVE", "Profile saved permanently for ${game.appName}", true)
        }
    }

    fun deleteProfile(packageName: String) {
        viewModelScope.launch {
            repository.deleteProfile(packageName)
            addLog("PROFILE_DELETE", "Profile removed for $packageName", true)
        }
    }

    fun requestShizukuPermission() {
        ShizukuManager.requestPermission()
        addLog("SHIZUKU", "Requesting ADB permission from Shizuku manager...", true)
        _shizukuStatus.value = ShizukuManager.getStatus()
    }

    fun processCalibrationTouch(rawX: Float, rawY: Float, dx: Float, dy: Float) {
        _calibrationStats.value = SensitivityEngine.processCalibrationInput(
            currentStats = _calibrationStats.value,
            rawX = rawX,
            rawY = rawY,
            rawDx = dx,
            rawDy = dy,
            sensX = _sensX.value,
            sensY = _sensY.value
        )
    }

    fun resetCalibration() {
        _calibrationStats.value = CalibrationStats()
    }

    fun addLog(tag: String, message: String, isSuccess: Boolean = true) {
        val newEntry = DiagnosticLogEntry(
            tag = tag,
            message = message,
            isSuccess = isSuccess
        )
        val current = _diagnosticLogs.value.toMutableList()
        current.add(0, newEntry) // Most recent first
        if (current.size > 80) {
            _diagnosticLogs.value = current.take(80)
        } else {
            _diagnosticLogs.value = current
        }
    }
}
