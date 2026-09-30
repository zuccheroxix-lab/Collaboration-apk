package com.example

import android.app.Application
import com.example.data.GameDatabase
import com.example.data.GameRepository
import com.example.shizuku.ShizukuManager

class GameToolsApplication : Application() {

    lateinit var repository: GameRepository
        private set

    lateinit var settingsManager: com.example.util.AppSettingsManager
        private set

    override fun onCreate() {
        super.onCreate()
        val database = GameDatabase.getInstance(this)
        repository = GameRepository(database.gameProfileDao())
        settingsManager = com.example.util.AppSettingsManager(this)

        // Register Shizuku binder and permission callbacks
        ShizukuManager.init {
            // Callback when Shizuku binder connects or dies
        }
    }
}
