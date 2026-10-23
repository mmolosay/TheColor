package io.github.mmolosay.thecolor.composition

import io.github.mmolosay.thecolor.utils.mapState
import kotlinx.coroutines.flow.StateFlow

// What a Feature Instance gives its View: its view data, computed from its store. Its value is
// worked out when it's read, and it emits only when the view data changes, so a commit elsewhere on
// the screen reaches none of its collectors
fun <S, V> Store<S, *>.viewData(toViewData: (S) -> V): StateFlow<V> =
    this.mapState(toViewData)
