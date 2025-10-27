package io.github.mmolosay.thecolor.presentation.preview

import dagger.Module
import dagger.Provides
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.presentation.impl.colorint.ColorToColorIntUseCase
import io.github.mmolosay.thecolor.presentation.impl.viewmodel.SimpleViewModel
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewViewModelDiModule.GateForDataFlow
import io.github.mmolosay.thecolor.utils.OpenSuspendGate
import io.github.mmolosay.thecolor.utils.SuspendGate
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import javax.inject.Named
import javax.inject.Qualifier

/**
 * Handles presentation logic of the 'Color Preview' feature.
 *
 * Unlike typical `ViewModel`s, it doesn't derive from Google's [ViewModel][androidx.lifecycle.ViewModel],
 * thus cannot be instantiated using [ViewModelProvider][androidx.lifecycle.ViewModelProvider].
 *
 * Instead, it can be created within "simple" `ViewModel` or Google's `ViewModel`.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ColorPreviewViewModel @AssistedInject constructor(
    @Assisted coroutineScope: CoroutineScope,
    @Assisted colorFlow: Flow<Color?>,
    @Assisted colorProcessedConfirmationChannel: SendChannel<Color?>?,
    @GateForDataFlow gateForDataFlow: SuspendGate,
    private val colorToColorInt: ColorToColorIntUseCase,
    @Named("defaultDispatcher") private val defaultDispatcher: CoroutineDispatcher,
) : SimpleViewModel(coroutineScope) {

    val dataFlow: StateFlow<ColorPreviewData?> = kotlin.run {
        fun data(color: Color?) =
            ColorPreviewData(
                color = with(colorToColorInt) { color?.toColorInt() },
            )
        var lastConsumedColor: Color? = null
        colorFlow
            .transformLatest { color ->
                gateForDataFlow.awaitOpen()
                lastConsumedColor = color
                emit(data(color))
            }
            .flowOn(defaultDispatcher)
            .stateIn(coroutineScope, SharingStarted.Eagerly, initialValue = null)
            .also { flow ->
                coroutineScope.launch(defaultDispatcher) {
                    flow.collectLatest {
                        colorProcessedConfirmationChannel?.send(lastConsumedColor)
                    }
                }
            }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            coroutineScope: CoroutineScope,
            colorFlow: Flow<Color?>,
            colorProcessedConfirmationChannel: SendChannel<Color?>?,
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