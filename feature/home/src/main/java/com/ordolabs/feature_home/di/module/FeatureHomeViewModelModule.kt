package com.ordolabs.feature_home.di.module

import androidx.lifecycle.ViewModel
import com.ordolabs.feature_home.viewmodel.colordata.ColorDataViewModel
import com.ordolabs.feature_home.viewmodel.colordata.details.ColorDetailsObtainViewModel
import com.ordolabs.feature_home.viewmodel.colordata.details.ColorDetailsViewModel
import com.ordolabs.feature_home.viewmodel.colordata.scheme.ColorSchemeConfigViewModel
import com.ordolabs.feature_home.viewmodel.colordata.scheme.ColorSchemeEditorViewModel
import com.ordolabs.feature_home.viewmodel.colordata.scheme.ColorSchemeObtainViewModel
import com.ordolabs.feature_home.viewmodel.colorinput.ColorInputViewModel
import com.ordolabs.feature_home.viewmodel.colorinput.ColorValidatorViewModel
import com.ordolabs.thecolor.di.mapkey.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import dagger.multibindings.Multibinds

@Module
interface FeatureHomeViewModelModule {

    @Multibinds
    fun multibindViewModels(): Map<Class<out ViewModel>, @JvmSuppressWildcards ViewModel>

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

    @[Binds IntoMap ViewModelKey(ColorDetailsViewModel::class)]
    fun bindColorDetailsViewModel(vm: ColorDetailsViewModel): ViewModel

    @[Binds IntoMap ViewModelKey(ColorDetailsObtainViewModel::class)]
    fun bindColorDetailsObtainViewModel(vm: ColorDetailsObtainViewModel): ViewModel

    // endregion

    // region Color scheme

    @[Binds IntoMap ViewModelKey(ColorSchemeConfigViewModel::class)]
    fun bindColorSchemeConfigViewModel(vm: ColorSchemeConfigViewModel): ViewModel

    @[Binds IntoMap ViewModelKey(ColorSchemeEditorViewModel::class)]
    fun bindColorSchemeEditorViewModel(vm: ColorSchemeEditorViewModel): ViewModel

    @[Binds IntoMap ViewModelKey(ColorSchemeObtainViewModel::class)]
    fun bindColorSchemeObtainViewModel(vm: ColorSchemeObtainViewModel): ViewModel

    // endregion
}