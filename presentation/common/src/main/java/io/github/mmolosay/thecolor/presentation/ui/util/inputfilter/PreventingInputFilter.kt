package io.github.mmolosay.thecolor.presentation.ui.util.inputfilter

import android.text.InputFilter
import android.text.Spanned

/**
 * Prevents typing in after [preceding] [what].
 */
class PreventingInputFilter(
    private val preceding: String,
    private val what: String
) : InputFilter {

    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        source ?: return null
        dest ?: return null
        if (dest.toString() == preceding && dstart == preceding.length && source.toString() == what) {
            return ""
        }
        return null
    }
}