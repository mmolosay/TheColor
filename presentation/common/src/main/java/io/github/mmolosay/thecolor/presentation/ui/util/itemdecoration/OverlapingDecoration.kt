package io.github.mmolosay.thecolor.presentation.ui.util.itemdecoration

import android.content.res.Resources
import android.graphics.Rect
import android.view.View
import androidx.annotation.DimenRes
import androidx.annotation.Px
import androidx.recyclerview.widget.RecyclerView
import io.github.mmolosay.thecolor.presentation.ui.util.itemdecoration.OverlapingDecoration.Horizontal
import io.github.mmolosay.thecolor.presentation.ui.util.itemdecoration.OverlapingDecoration.Vertical

/**
 * Adds [horizontal] and [vertical] offset to each item's `left` and `top` accordingly,
 * so items overlap and stack one upon another.
 *
 * Either one of derived classes [Vertical], [Horizontal] or [Combined] should be used.
 */
abstract class OverlapingDecoration(
    @Px private val horizontal: Int,
    @Px private val vertical: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) return
        if (position == 0) return
        outRect.left -= horizontal
        outRect.top -= vertical
    }

    class Vertical(
        @Px offset: Int
    ) : OverlapingDecoration(
        horizontal = 0,
        vertical = offset
    ) {
        constructor(
            resources: Resources,
            @DimenRes offsetRes: Int
        ) : this(
            resources.getDimensionPixelOffset(offsetRes)
        )
    }

    class Horizontal(
        @Px offset: Int
    ) : OverlapingDecoration(
        horizontal = offset,
        vertical = 0
    ) {
        constructor(
            resources: Resources,
            @DimenRes offsetRes: Int
        ) : this(
            resources.getDimensionPixelOffset(offsetRes)
        )
    }
}