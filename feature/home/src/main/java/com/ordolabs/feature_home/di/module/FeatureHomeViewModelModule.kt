package com.ordolabs.feature_home.di.module

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ordolabs.feature_home.viewmodel.colordata.ColorDataViewModel
import com.ordolabs.feature_home.viewmodel.colordata.details.ColorDetailsObtainViewModel
import com.ordolabs.feature_home.viewmodel.colordata.scheme.ColorSchemeObtainViewModel
import com.ordolabs.feature_home.viewmodel.colorinput.ColorInputViewModel
import com.ordolabs.feature_home.viewmodel.colorinput.ColorValidatorViewModel
import com.ordolabs.thecolor.di.mapkey.ViewModelKey
import com.ordolabs.thecolor.viewmodel.factory.ViewModelFactory
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface FeatureHomeViewModelModule {

    @Binds
    fun bindViewModelFactory(instance: ViewModelFactory): ViewModelProvider.Factory

    // region Home

    // TODO: HomeViewModel

    // endregion

    // region Color input

    @[Binds IntoMap ViewModelKey(ColorInputViewModel::class)]
    fun bindColorInputViewModel(vm: ColorInputViewModel): ViewModel

    @[Binds IntoMap ViewModelKey(ColorValidatorViewModel::class)]
    fun bindColorValidatorViewModel(vm: ColorValidatorViewModel): ViewModel

    // endregion

    // region Color data

    @[Binds IntoMap ViewModelKey(ColorDataViewModel::class)]
    fun bindColorDataViewModel(vm: ColorDataViewModel): ViewModel

    // endregion

    // region Color details

    @[Binds IntoMap ViewModelKey(ColorDetailsObtainViewModel::class)]
    fun bindColorDetailsObtainViewModel(vm: ColorDetailsObtainViewModel): ViewModel

    // endregion

    // region Color scheme

    @[Binds IntoMap ViewModelKey(ColorSchemeObtainViewModel::class)]
    fun bindColorSchemeObtainViewModel(vm: ColorSchemeObtainViewModel): ViewModel

    // endregion
}