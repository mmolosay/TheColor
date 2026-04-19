package io.github.mmolosay.thecolor.presentation.preview

import dagger.Module
import dagger.Provides
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.main.di.qualifiers.CoroutineDispatcherDiQualifiers.DefaultDispatcher
import io.github.mmolosay.thecolor.presentation.common.colorint.ColorToColorIntUseCase
import io.github.mmolosay.thecolor.presentation.common.viewmodel.SimpleViewModel
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewViewModelDiModule.GateForDataFlow
import io.github.mmolosay.thecolor.utils.OpenSuspendGate
import io.github.mmolosay.thecolor.utils.ProcessingRegistry
import io.github.mmolosay.thecolor.utils.SuspendGate
import io.github.mmolosay.thecolor.utils.removeAndCancelAll
import io.github.mmolosay.thecolor.utils.withRegistry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.job
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
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : SimpleViewModel(coroutineScope) {

    private val _dataFlow = MutableStateFlow<ColorPreviewData?>(null)
    val dataFlow: StateFlow<ColorPreviewData?> = _dataFlow.asStateFlow()

    private val opRegistry = ProcessingRegistry<Operation>()

    /**
     * Sets the new [color].
     * It will be transformed to the [ColorPreviewData] and exposed via [dataFlow].
     *
     * All calls to this method are conflated, meaning that if there is an ongoing job that belong to this method,
     * then when it's invoked again the ongoing job will be canceled.
     */
    fun setColor(color: Color?): Deferred<Unit> =
        coroutineScope.async(defaultDispatcher) {
            opRegistry.removeAndCancelAll { it.value is Operation.SetColor }
            val operation = Operation.SetColor(color)
            opRegistry.withRegistry(operation, coroutineContext.job) {
                gateForDataFlow.awaitOpen()
                val data = ColorPreviewData(
                    color = with(colorToColorInt) { color?.toColorInt() },
                )
                _dataFlow.emit(data)
            }
        }

    @AssistedFactory
    fun interface Factory {
        fun create(
            coroutineScope: CoroutineScope,
        ): ColorPreviewViewModel
    }

    /**
     * Directly maps to the public methods of the [ColorPreviewViewModel].
     * Implements "Command" design pattern.
     */
    private sealed interface Operation {

        /**
         * Corresponds to the [ColorPreviewViewModel.setColor] method.
         */
        data class SetColor(
            val color: Color?,
        ) : Operation
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