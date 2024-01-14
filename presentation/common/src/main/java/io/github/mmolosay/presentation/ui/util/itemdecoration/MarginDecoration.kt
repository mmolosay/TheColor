package io.github.mmolosay.presentation.ui.util.itemdecoration

import android.content.res.Resources
import android.graphics.Rect
import android.view.View
import androidx.annotation.DimenRes
import androidx.annotation.Px
import androidx.recyclerview.widget.RecyclerView
import io.github.mmolosay.presentation.ui.util.itemdecoration.MarginDecoration.Combined
import io.github.mmolosay.presentation.ui.util.itemdecoration.MarginDecoration.Horizontal
import io.github.mmolosay.presentation.ui.util.itemdecoration.MarginDecoration.Vertical

/**
 * Adds [horizontal] and [vertical] margin to each `RecyclerView` item.
 *
 * Either one of derived classes [Vertical], [Horizontal] or [Combined] should be used.
 */
sealed class MarginDecoration(
    @Px private val horizontal: Int,
    @Px private val vertical: Int
) : RecyclerView.ItemDecoration() {

    final override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.offset(horizontal, vertical)
    }

    class Vertical(
        @Px margin: Int
    ) : MarginDecoration(
        horizontal = 0,
        vertical = margin
    ) {
        constructor(
            resources: Resources,
            @DimenRes marginRes: Int
        ) : this(
            resources.getDimensionPixelOffset(marginRes)
        )
    }

    class Horizontal(
        @Px margin: Int
    ) : MarginDecoration(
        horizontal = margin,
        vertical = 0
    ) {
        constructor(
            resources: Resources,
            @DimenRes marginRes: Int
        ) : this(
            resources.getDimensionPixelOffset(marginRes)
        )
    }

    class Combined(
        @Px horizontal: Int,
        @Px vertical: Int
    ) : MarginDecoration(
        horizontal,
        vertical
    ) {
        constructor(
            resources: Resources,
            @DimenRes horizontalRes: Int,
            @DimenRes verticalRes: Int
        ) : this(
            horizontal = resources.getDimensionPixelOffset(horizontalRes),
            vertical = resources.getDimensionPixelOffset(verticalRes)
        )
    }
}