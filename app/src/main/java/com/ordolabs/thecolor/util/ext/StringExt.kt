package com.ordolabs.thecolor.util.ext

import androidx.fragment.app.Fragment

// TODO: abolish; should be applied to SharedPreferences keys, and not required in Bundles
inline fun <reified F : Fragment> String.makeArgumentsKey(): String =
    "${F::class.java.canonicalName}.$this"