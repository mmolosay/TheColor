package io.github.mmolosay.thecolor.composition

import io.github.mmolosay.thecolor.utils.mapState
import kotlinx.coroutines.flow.StateFlow

// What a Feature Instance gives its View: its view data, computed from its store when read. It isn't
// a 'StateFlow', so that a View takes it only the way meant for it, by listening to the store's
// commits rather than collecting
interface ViewDataSource<V> {
    val value: V

    // experiment: the view data as a flow, for a View that collects it instead of listening
    val flow: StateFlow<V>

    // 'listener' is called on the main thread after every commit that changes the screen's state
    fun listen(listener: () -> Unit): AutoCloseable
}

fun <S, V> Store<S, *>.viewData(toViewData: (S) -> V): ViewDataSource<V> =
    StoreViewDataSource(store = this, toViewData = toViewData)

private class StoreViewDataSource<S, V>(
    private val store: Store<S, *>,
    private val toViewData: (S) -> V,
) : ViewDataSource<V> {

    // the last state the view data was computed from, and that view data
    @Volatile
    private var last: Pair<S, V>? = null

    // The newest. The same state instance gives the same view data, so only a new one is computed,
    // and a commit elsewhere on the screen costs a View one comparison
    override val value: V
        get() {
            val state = store.value
            val last = this.last
            if (last != null && last.first === state) return last.second
            return toViewData(state).also { viewData -> this.last = state to viewData }
        }

    override val flow: StateFlow<V> =
        store.mapState { _ -> this.value }

    override fun listen(listener: () -> Unit): AutoCloseable =
        store.listen(listener)
}
