package com.ordolabs.feature_home.util

import androidx.fragment.app.Fragment
import com.ordolabs.feature_home.di.FeatureHomeComponent
import com.ordolabs.feature_home.di.FeatureHomeComponentKeeper

object FeatureHomeUtil {

    val Fragment.featureHomeComponent: FeatureHomeComponent
        get() = when (this) {
            is FeatureHomeComponentKeeper -> this.featureHomeComponent
            else -> requireNotNull(this.parentFragment?.featureHomeComponent)
        }
}