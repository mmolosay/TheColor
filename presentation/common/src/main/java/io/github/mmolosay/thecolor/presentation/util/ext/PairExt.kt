package io.github.mmolosay.thecolor.presentation.util.ext

/**
 * Returns either [Pair.first] or [Pair.second] based on specified [selector].
 *
 * @return [Pair.first] if [selector] is `true`, otherwise [Pair.second].
 */
infix fun <T> Pair<T, T>.by(selector: Boolean): T {
    return if (selector) this.first else this.second
}

infix fun Pair<Float, Float>.factor(fraction: Float): Float {
    return first + (second - first) * fraction
}