package com.example.data.local

import kotlinx.coroutines.flow.Flow

class FoodSafetyRepository(
    private val userProfileDao: UserProfileDao,
    private val scanHistoryDao: ScanHistoryDao
) {
    val userProfile: Flow<UserProfile?> = userProfileDao.getUserProfile()
    val scanHistory: Flow<List<ScanHistory>> = scanHistoryDao.getAllHistory()

    suspend fun saveUserProfile(profile: UserProfile) {
        userProfileDao.insertOrUpdateProfile(profile)
    }

    suspend fun addScanHistory(scan: ScanHistory) {
        scanHistoryDao.insertScan(scan)
    }

    suspend fun clearHistory() {
        scanHistoryDao.clearAllHistory()
    }
}
