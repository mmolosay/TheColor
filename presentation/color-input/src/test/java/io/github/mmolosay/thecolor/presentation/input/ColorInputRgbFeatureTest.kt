package io.github.mmolosay.thecolor.presentation.input

import io.github.mmolosay.thecolor.domain.color.ColorConverter
import io.github.mmolosay.thecolor.domain.color.ColorFactory
import io.github.mmolosay.thecolor.domain.color.prototype.ColorPrototypeValidator
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.SelectAllTextOnTextFieldFocus
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.SmartBackspace
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferencesRepository
import io.github.mmolosay.thecolor.domain.utils.PrefState
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbCore
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbFeature
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbOutcome
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbState
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldCore
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldFeature
import io.github.mmolosay.thecolor.utils.Lens
import io.github.mmolosay.thecolor.utils.composition.Change
import io.github.mmolosay.thecolor.utils.composition.Store
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.jupiter.api.Test

class ColorInputRgbFeatureTest {

    // the fields collect the select-all setting, which no test changes
    val userPreferencesRepository: UserPreferencesRepository = mockk {
        every { flowOfSelectAllTextOnTextFieldFocus } returns run {
            val value = PrefState.BeingInitialized
            MutableStateFlow<PrefState<SelectAllTextOnTextFieldFocus>>(value)
        }
    }

    // real implementation, operations on the State are pure
    val core = run {
        val colorInputMapper = ColorInputMapper()
        ColorInputRgbCore(
            textFieldFactory = TextFieldCore.Factory { formatter ->
                TextFieldCore(formatter)
            },
            colorConverter = ColorConverter(),
            colorInputMapper = colorInputMapper,
            colorInputValidator = ColorInputValidator(
                colorInputMapper = colorInputMapper,
                colorFactory = ColorFactory(
                    colorPrototypeValidator = ColorPrototypeValidator(),
                ),
            ),
        )
    }

    val textFieldFactory = TextFieldFeature.Factory { core, store, scope ->
        TextFieldFeature(core, store, scope, userPreferencesRepository)
    }

    lateinit var sut: ColorInputRgbFeature

    @Test
    fun `given the smart backspace setting is 'BeingInitialized', when it becomes off, then 'isSmartBackspaceEnabled' is 'false'`() {
        val flowOfSmartBackspace = run {
            val value = PrefState.BeingInitialized
            MutableStateFlow<PrefState<SmartBackspace>>(value)
        }
        every { userPreferencesRepository.flowOfSmartBackspace } returns flowOfSmartBackspace
        createSut()

        // off isn't the default value, so the test can't pass by falling back to it
        flowOfSmartBackspace.value = run {
            val value = SmartBackspace(enabled = false)
            val result = PrefState.Result.HasValue(value)
            PrefState.Ready(result)
        }

        sut.viewData.value.isSmartBackspaceEnabled shouldBe false
    }

    fun createSut() =
        ColorInputRgbFeature(
            core = core,
            store = run {
                val store = Store(initialState = core.initialState(color = null))
                // RGB hands up Outcomes, so its Store is a parent's slice; this parent ignores them
                val lens = Lens<ColorInputRgbState, ColorInputRgbState>(
                    get = { state -> state },
                    set = { _, state -> state },
                )
                store.slice(lens) { state, _: ColorInputRgbOutcome? -> Change(state) }
            },
            scope = CoroutineScope(Job() + Dispatchers.Unconfined),
            textFieldFactory = textFieldFactory,
            userPreferencesRepository = userPreferencesRepository,
        ).also {
            sut = it
        }
}
