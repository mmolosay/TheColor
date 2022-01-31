package com.ordolabs.thecolor.util.ext

import androidx.fragment.app.Fragment

inline fun <reified F : Fragment> String.makeArgumentsKey(): String =
    "${F::class.java.canonicalName}.$this"