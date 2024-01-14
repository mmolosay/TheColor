package io.github.mmolosay.presentation.util.struct

/**
 * Class that holds data of `Animator` animating destination.
 * You should call class methods manually.
 */
class AnimatorDestination {

    /**
     * Current destination of `Animator`.
     * Contains [AnimatorDestination.Type] if animation is running or `null`,
     * if has never started or already ended.
     */
    var current: Type? = null
        private set

    val isStart: Boolean?
        get() = current?.let { it == Type.START }

    val isEnd: Boolean?
        get() = current?.let { it == Type.END }

    fun setStart() {
        this.current = Type.START
    }

    fun setEnd() {
        this.current = Type.END
    }

    /**
     * Sets specified animation destination.
     * You should call it when you __start__ your `Animator`.
     */
    fun set(toEnd: Boolean) {
        this.current = Type.END.takeIf { toEnd } ?: Type.START
    }

    /**
     * Clears animation destination.
     * You should call it when animation __ended__.
     */
    fun clear() {
        this.current = null
    }

    /**
     * Reverses animation destination.
     * You should call it when you __reverse__ your `ObjectAnimator` or `ValueAnimator`.
     */
    fun reverse() {
        current?.let {
            this.current = !it
        }
    }

    enum class Type {
        START,
        END;

        operator fun not(): Type =
            when (this) {
                START -> END
                END -> START
            }
    }
}