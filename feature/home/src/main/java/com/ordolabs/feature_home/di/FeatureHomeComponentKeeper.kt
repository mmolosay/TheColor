package com.ordolabs.feature_home.di

import com.ordolabs.thecolor.di.ScopedComponentKeeper

/**
 * Interface of object, keeping instance of [FeatureHomeComponent].
 *
 * @see ScopedComponentKeeper
 */
interface FeatureHomeComponentKeeper : ScopedComponentKeeper {

    val featureHomeComponent: FeatureHomeComponent
}