package io.github.mmolosay.thecolor.presentation.util.ext

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator

fun Animator.doOnEndOnce(
    action: (animator: Animator) -> Unit
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

fun ObjectAnimator.startOrReverse() {
    if (this.isRunning) {
        this.reverse()
    } else {
        this.start()
    }
}

fun ValueAnimator.startOrReverse() {
    if (this.isRunning) {
        this.reverse()
    } else {
        this.start()
    }
}