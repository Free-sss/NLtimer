package com.nltimer.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.nltimer.core.data.SettingsPrefs
import com.nltimer.core.data.SettingsPrefsImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okio.Path.Companion.toPath
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> = PreferenceDataStoreFactory.createWithPath {
        context.filesDir.resolve("settings.preferences_pb").absolutePath.toPath()
    }

    @Provides
    @Singleton
    fun provideSettingsPrefs(
        dataStore: DataStore<Preferences>
    ): SettingsPrefs = SettingsPrefsImpl(dataStore)
}
