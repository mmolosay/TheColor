package io.github.mmolosay.thecolor.demo

import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.composition.Store
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.TextFieldCore
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.TextFieldFeature
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.TextFieldState
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.TextFormatter
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TextFieldFeatureTest {

    // keeps any text, so that only the expected value decides whether an edit applies
    val core = TextFieldCore(formatter = TextFormatter { text -> text })

    lateinit var store: Store<TextFieldState, Nothing>

    lateinit var sut: TextFieldFeature

    @Test
    fun `given the collector has '4', when the text goes to '42' and back before it resumes, then it gets '4' again, so that the View's buffer catches up`() =
        runTest {
            createSut(text = "4")
            val delivered = mutableListOf<String>()
            // resumes only when the test runs it, as a View's collector does after the current main-thread task
            backgroundScope.launch(StandardTestDispatcher(testScheduler)) {
                sut.viewData.collect { delivered += it.text }
            }
            runCurrent()

            sut.enterText(text = "42", expected = "4")
            store.update { state -> with(core) { Change(state.withText("4")) } } // as a parent would
            runCurrent()

            delivered shouldBe listOf("4", "4")
        }

    fun createSut(text: String) {
        store = Store(initialState = core.initialState(text))
        sut = TextFieldFeature(
            core = core,
            store = store,
            scope = CoroutineScope(Job()),
        )
    }
}
