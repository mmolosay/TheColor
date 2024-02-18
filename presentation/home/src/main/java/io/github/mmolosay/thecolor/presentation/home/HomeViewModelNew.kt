package io.github.mmolosay.thecolor.presentation.home

import androidx.lifecycle.ViewModel
import io.github.mmolosay.thecolor.presentation.CurrentColorStore
import javax.inject.Inject

// TODO: remove "New" suffix once the old ViewModel is gone
class HomeViewModelNew @Inject constructor(
    private val currentColorStore: CurrentColorStore,
) : ViewModel() {


}