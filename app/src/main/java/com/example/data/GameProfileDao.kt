package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameProfileDao {

    @Query("SELECT * FROM game_profiles ORDER BY lastUpdated DESC")
    fun getAllProfiles(): Flow<List<GameProfileEntity>>

    @Query("SELECT * FROM game_profiles WHERE packageName = :packageName LIMIT 1")
    fun getProfile(packageName: String): Flow<GameProfileEntity?>

    @Query("SELECT * FROM game_profiles WHERE packageName = :packageName LIMIT 1")
    suspend fun getProfileSync(packageName: String): GameProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: GameProfileEntity): Long

    @Query("DELETE FROM game_profiles WHERE packageName = :packageName")
    suspend fun deleteProfile(packageName: String)
}
