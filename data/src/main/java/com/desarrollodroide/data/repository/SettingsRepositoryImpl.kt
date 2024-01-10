package com.desarrollodroide.data.repository

import com.desarrollodroide.data.local.preferences.SettingsPreferenceDataSource
import com.desarrollodroide.model.Account
import com.desarrollodroide.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class SettingsRepositoryImpl(
    private val settingsPreferenceDataSource: SettingsPreferenceDataSource
): SettingsRepository {
    override suspend fun getUser() = settingsPreferenceDataSource.userDataStream.map {
        User(
            session = it.session,
            account = Account(
                id = it.account.id,
                userName = it.account.userName,
                password = it.account.password,
                owner = it.account.owner,
                serverUrl = it.account.serverUrl,
            )
        )
    }.first()

    override suspend fun getUserName() = settingsPreferenceDataSource.userDataStream.map { it.account.userName }

    override val userDataStream: Flow<User> =
        settingsPreferenceDataSource.userDataStream

    override suspend fun setTheme(isDark: Boolean) {
       settingsPreferenceDataSource.setTheme(isDark)
    }

    override fun isDarkTheme() = settingsPreferenceDataSource.isDarkTheme()

}