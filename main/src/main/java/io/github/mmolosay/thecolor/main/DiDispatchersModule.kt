package io.github.mmolosay.thecolor.main

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.mmolosay.thecolor.main.di.qualifiers.CoroutineDispatcherDiQualifiers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(SingletonComponent::class)
object DiDispatchersModule {

    @Provides
    @CoroutineDispatcherDiQualifiers.DefaultDispatcher
    fun provideDefaultDispatcher(): CoroutineDispatcher =
        Dispatchers.Default

    @Provides
    @CoroutineDispatcherDiQualifiers.IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher =
        Dispatchers.IO
}