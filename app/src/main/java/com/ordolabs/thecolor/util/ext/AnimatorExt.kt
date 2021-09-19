package com.ordolabs.thecolor.util.ext

import android.animation.Animator

inline fun Animator.doOnEndOnce(
    crossinline action: (animator: Animator) -> Unit
): Animator.AnimatorListener {
    val listener = object : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator) {}
        override fun onAnimationCancel(animation: Animator) {}
        override fun onAnimationRepeat(animation: Animator) {}
        override fun onAnimationEnd(animation: Animator) = action.let {
            it.invoke(animation)
            removeListener(this)
        }
    }
    addListener(listener)
    return listener
}
