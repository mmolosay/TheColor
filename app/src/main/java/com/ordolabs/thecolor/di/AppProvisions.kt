package com.ordolabs.thecolor.di

import androidx.lifecycle.ViewModelProvider
import com.ordolabs.domain.di.DomainProvisions

interface AppProvisions :
    DomainProvisions {

    //    val viewModelMultibinding: Map<Class<out ViewModel>, @JvmSuppressWildcards ViewModel>
    val viewModelFactory: ViewModelProvider.Factory
}