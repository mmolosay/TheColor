package io.github.mmolosay.thecolor.presentation.input

import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.composition.Store
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.SelectAllTextOnTextFieldFocus
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferencesRepository
import io.github.mmolosay.thecolor.domain.utils.PrefState
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldCore
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldFeature
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldState
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFormatter
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TextFieldFeatureTest {

    val userPreferencesRepository: UserPreferencesRepository = mockk {
        every { flowOfSelectAllTextOnTextFieldFocus } returns run {
            val value = PrefState.BeingInitialized
            MutableStateFlow<PrefState<SelectAllTextOnTextFieldFocus>>(value)
        }
    }

    val core = TextFieldCore(
        formatter = TextFormatter { text -> text },
    )

    lateinit var sut: TextFieldFeature

    @Test
    fun `given the collector has '4', when the text goes to '42' and back before it resumes, then it gets '4' again, so that the View's buffer catches up`() =
        runTest {
            val store = Store(initialState = core.initialState(text = "4"))
            createSut(store = store)
            val delivered = mutableListOf<String>()
            // resumes only when the test runs it, as a View's collector does after the current main-thread task
            backgroundScope.launch(StandardTestDispatcher(testScheduler)) {
                sut.viewData.collect { delivered += it.text }
            }
            runCurrent()

            sut.enterText(text = "42", expected = "4")
            // this State change comes from outside the feature
            store.update { state ->
                val withText = with(core) { state.withText("4") } // back to '4', as a parent would
                Change(withText)
            }
            runCurrent()

            delivered shouldBe listOf("4", "4")
        }

    @Test
    fun `given the 'select all text on focus' setting is 'BeingInitialized', when it becomes enabled, then 'selectAllTextOnFocus' is 'true'`() {
        val flowOfSelectAllTextOnTextFieldFocus = run {
            val value = PrefState.BeingInitialized
            MutableStateFlow<PrefState<SelectAllTextOnTextFieldFocus>>(value)
        }
        every {
            userPreferencesRepository.flowOfSelectAllTextOnTextFieldFocus
        } returns run {
            flowOfSelectAllTextOnTextFieldFocus
        }
        val store = Store(initialState = core.initialState(text = ""))
        createSut(store = store)

        // enabled isn't the default value, so the test can't pass by falling back to it
        flowOfSelectAllTextOnTextFieldFocus.value = run {
            val value = SelectAllTextOnTextFieldFocus(enabled = true)
            val result = PrefState.Result.HasValue(value)
            PrefState.Ready(result)
        }

        sut.viewData.value.selectAllTextOnFocus shouldBe true
    }

    fun createSut(
        store: Store<TextFieldState, Nothing>,
    ) =
        TextFieldFeature(
            core = core,
            store = store,
            scope = CoroutineScope(Job() + Dispatchers.Unconfined),
            userPreferencesRepository = userPreferencesRepository,
        ).also {
            sut = it
        }
}
