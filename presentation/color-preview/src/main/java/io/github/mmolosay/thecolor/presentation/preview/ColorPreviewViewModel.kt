package io.github.mmolosay.thecolor.presentation.preview

import dagger.Module
import dagger.Provides
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.presentation.common.colorint.ColorToColorIntUseCase
import io.github.mmolosay.thecolor.presentation.common.viewmodel.SimpleViewModel
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewViewModelDiModule.GateForDataFlow
import io.github.mmolosay.thecolor.utils.OpenSuspendGate
import io.github.mmolosay.thecolor.utils.SuspendGate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Qualifier

/**
 * Handles presentation logic of the 'Color Preview' feature.
 *
 * Unlike typical `ViewModel`s, it doesn't derive from Google's [ViewModel][androidx.lifecycle.ViewModel],
 * thus cannot be instantiated using [ViewModelProvider][androidx.lifecycle.ViewModelProvider].
 *
 * Instead, it can be created within "simple" `ViewModel` or Google's `ViewModel`.
 */
class ColorPreviewViewModel @AssistedInject constructor(
    @Assisted coroutineScope: CoroutineScope,
    @GateForDataFlow private val gateForDataFlow: SuspendGate,
    private val colorToColorInt: ColorToColorIntUseCase,
) : SimpleViewModel(coroutineScope) {

    private val _dataFlow = MutableStateFlow<ColorPreviewData?>(null)
    val dataFlow: StateFlow<ColorPreviewData?> = _dataFlow.asStateFlow()

    /**
     * Sets the new [color]. It will be transformed to the [ColorPreviewData] and exposed via [dataFlow].
     * This method suspends until the processing of the new [color] is finished and the [dataFlow] has
     * emitted a new data.
     */
    suspend fun setColor(color: Color?) {
        gateForDataFlow.awaitOpen()
        val data = ColorPreviewData(
            color = with(colorToColorInt) { color?.toColorInt() },
        )
        _dataFlow.emit(data)
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            coroutineScope: CoroutineScope,
        ): ColorPreviewViewModel
    }
}

@Module
@InstallIn(ViewModelComponent::class)
internal object ColorPreviewViewModelDiModule {

    @Provides
    @GateForDataFlow
    fun provideGateForDataFlow(): SuspendGate =
        OpenSuspendGate

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class GateForDataFlow
}