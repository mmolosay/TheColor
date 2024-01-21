package io.github.mmolosay.thecolor.main

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object DiDispatchersModule {

    @Provides
    @Named("uiDataUpdateDispatcher")
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Main.immediate
}