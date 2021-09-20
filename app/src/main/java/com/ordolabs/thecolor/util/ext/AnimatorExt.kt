package com.ordolabs.thecolor.util.ext

import android.animation.Animator
import android.animation.AnimatorSet

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

inline fun AnimatorSet.play(
    animator: Animator,
    builder: AnimatorSet.Builder.() -> Unit
): AnimatorSet {
    this.play(animator).apply(builder)
    return this
}