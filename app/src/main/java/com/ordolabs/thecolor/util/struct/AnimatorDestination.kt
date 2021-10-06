package com.ordolabs.thecolor.util.struct

class AnimatorDestination {

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

    fun set(toEnd: Boolean) {
        this.current = Type.END.takeIf { toEnd } ?: Type.START
    }

    fun clear() {
        this.current = null
    }

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