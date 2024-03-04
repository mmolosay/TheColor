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
    // TODO: BasicTextField2 migration
    fun provideDefaultDispatcher(): CoroutineDispatcher =
        Dispatchers.Main.immediate // https://medium.com/androiddevelopers/effective-state-management-for-textfield-in-compose-d6e5b070fbe5

    // TODO: use me
//    @Provides
//    @Named("defaultDispatcher")
//    fun provideDefaultDispatcher(): CoroutineDispatcher =
//        Dispatchers.Default

    @Provides
    @Named("ioDispatcher")
    fun provideIoDispatcher(): CoroutineDispatcher =
        Dispatchers.IO
}