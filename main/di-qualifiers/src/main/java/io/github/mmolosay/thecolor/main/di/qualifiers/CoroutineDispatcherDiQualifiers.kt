package io.github.mmolosay.thecolor.main.di.qualifiers

import javax.inject.Qualifier

object CoroutineDispatcherDiQualifiers {

    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class DefaultDispatcher

    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class IoDispatcher
}