package io.github.mmolosay.thecolor.presentation.ui.adapter.base

interface OnRecyclerItemClicksListener {

    fun onRecyclerItemClick(position: Int) {
        // default empty implementation
    }

    fun onRecyclerItemLongClick(position: Int) {
        // default empty implementation
    }
}