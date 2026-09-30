package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_profiles")
data class GameProfileEntity(
    @PrimaryKey val packageName: String,
    val gameName: String,
    val sensX: Float = 1.0f,
    val sensY: Float = 1.0f,
    val isLinked: Boolean = true,
    val linkRatio: Float = 1.0f,
    val preset: String = "MEDIUM",
    val pointerSpeed: Int = 0, // -7 to +7
    val targetDpi: Int = 0,    // 0 = default
    val useOverlay: Boolean = false,
    val preferredMode: String = "OPTIMIZE", // OPTIMIZE, STABLE, PERFORMANCE
    val isPinnedToLauncher: Boolean = true,
    val lastUpdated: Long = System.currentTimeMillis()
)
