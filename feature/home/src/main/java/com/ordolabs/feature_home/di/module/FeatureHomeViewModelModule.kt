package com.ordolabs.feature_home.di.module

import androidx.lifecycle.ViewModel
import com.ordolabs.feature_home.viewmodel.colordata.details.ColorDetailsObtainViewModel
import com.ordolabs.feature_home.viewmodel.colordata.details.ColorDetailsViewModel
import com.ordolabs.feature_home.viewmodel.colordata.scheme.ColorSchemeConfigViewModel
import com.ordolabs.feature_home.viewmodel.colordata.scheme.ColorSchemeEditorViewModel
import com.ordolabs.feature_home.viewmodel.colordata.scheme.ColorSchemeObtainViewModel
import com.ordolabs.thecolor.di.mapkey.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface FeatureHomeViewModelModule {

    // region Color data


    // endregion

    // region Color details

    @[Binds IntoMap ViewModelKey(ColorDetailsViewModel::class)]
    fun bindColorDetailsiewModel(vm: ColorDetailsViewModel): ViewModel

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