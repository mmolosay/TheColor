package io.github.mmolosay.thecolor.main.di.qualifiers

import javax.inject.Qualifier

object DataStoreDiQualifiers {

    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class UserPreferences

    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class DevOptions

    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class MiscValues
}