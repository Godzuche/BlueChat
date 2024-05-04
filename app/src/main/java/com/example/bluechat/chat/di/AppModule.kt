package com.example.bluechat.chat.di

import android.content.Context
import com.example.bluechat.chat.data.bluetooth.AndroidBluetoothController
import com.example.bluechat.chat.domain.BluetoothController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideBluetoothController(@ApplicationContext context: Context): BluetoothController =
        AndroidBluetoothController(context)
}