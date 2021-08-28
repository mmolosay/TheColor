package com.ordolabs.thecolor.di

import com.ordolabs.thecolor.viewmodel.HomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    viewModel {
        HomeViewModel(
            // nothing is here
        )
    }

}