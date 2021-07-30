package com.ordolabs.thecolor.util

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction

fun FragmentManager.commit(
    allowStateLoss: Boolean = false,
    allowReordering: Boolean = true,
    body: FragmentTransaction.() -> Unit
): Int {
    val transaction = beginTransaction()
    transaction.body()
    transaction.setReorderingAllowed(allowReordering)
    return if (allowStateLoss) {
        transaction.commitAllowingStateLoss()
    } else {
        transaction.commit()
    }
}