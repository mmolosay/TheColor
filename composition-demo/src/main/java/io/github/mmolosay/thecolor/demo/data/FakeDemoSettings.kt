package io.github.mmolosay.thecolor.demo.data

import io.github.mmolosay.thecolor.demo.domain.AmountUnit
import io.github.mmolosay.thecolor.demo.domain.DemoSettings
import io.github.mmolosay.thecolor.main.di.qualifiers.AppCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

@Singleton
class FakeDemoSettings @Inject constructor(
    @AppCoroutineScope appScope: CoroutineScope,
) : DemoSettings {

    // read a moment after start, so that the screen's wait for its startup data can be seen
    // on a device; not the tabs' default, so that its arrival can be seen too
    override val preferredUnit: StateFlow<AmountUnit?> =
        flow {
            delay(ReadTime)
            emit(AmountUnit.Ounces)
        }
            .stateIn(appScope, SharingStarted.Eagerly, initialValue = null)

    override val ouncesDecimalPlaces: StateFlow<Int?> =
        MutableStateFlow(2)

    private companion object {
        val ReadTime = 300.milliseconds
    }
}
