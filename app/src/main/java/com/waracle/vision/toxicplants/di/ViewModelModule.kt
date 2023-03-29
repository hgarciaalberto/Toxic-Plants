package com.waracle.vision.toxicplants.di

import android.content.Context
import com.waracle.vision.toxicplants.camera.video.FileManager
import com.waracle.vision.toxicplants.camera.video.PermissionsHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {

    @ViewModelScoped
    @Provides
    fun provideFileManager(@ApplicationContext appContext: Context) =
        FileManager(appContext)

    @ViewModelScoped
    @Provides
    fun providePermissionHandler() =
        PermissionsHandler()
}
