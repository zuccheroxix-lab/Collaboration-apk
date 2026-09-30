package com.example.data

import kotlinx.coroutines.flow.Flow

class GameRepository(private val dao: GameProfileDao) {

    fun getAllProfiles(): Flow<List<GameProfileEntity>> = dao.getAllProfiles()

    fun getProfile(packageName: String): Flow<GameProfileEntity?> = dao.getProfile(packageName)

    suspend fun getProfileSync(packageName: String): GameProfileEntity? = dao.getProfileSync(packageName)

    suspend fun saveProfile(profile: GameProfileEntity) {
        dao.upsertProfile(profile.copy(lastUpdated = System.currentTimeMillis()))
    }

    suspend fun deleteProfile(packageName: String) {
        dao.deleteProfile(packageName)
    }
}
