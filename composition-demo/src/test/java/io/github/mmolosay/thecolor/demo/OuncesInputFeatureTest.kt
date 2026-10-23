package io.github.mmolosay.thecolor.demo

import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.composition.Store
import io.github.mmolosay.thecolor.demo.domain.AmountConverter
import io.github.mmolosay.thecolor.demo.domain.DecimalSeparator
import io.github.mmolosay.thecolor.demo.domain.DemoSettings
import io.github.mmolosay.thecolor.demo.presentation.feature.ounces.OuncesInputEnvironment
import io.github.mmolosay.thecolor.demo.presentation.feature.ounces.OuncesInputFeature
import io.github.mmolosay.thecolor.demo.presentation.feature.ounces.OuncesInputOutcome
import io.github.mmolosay.thecolor.demo.presentation.feature.ounces.OuncesInputState
import io.github.mmolosay.thecolor.demo.presentation.feature.ounces.OuncesInputStateFactory
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.TextFieldFeature
import io.github.mmolosay.thecolor.utils.Lens
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.jupiter.api.Test

class OuncesInputFeatureTest {

    val environment = OuncesInputEnvironment(amountConverter = AmountConverter())

    val separator = MutableStateFlow<DecimalSeparator?>(DecimalSeparator.Point)

    val settings: DemoSettings = mockk {
        every { decimalSeparator } returns separator
    }

    lateinit var sut: OuncesInputFeature

    @Test
    fun `given the ounces show one and a half with the point, when the setting changes to the comma just before the user types '2', then the keystroke applies, and the ounces show '1,52'`() {
        createSut()
        sut.textField.enterText(text = "1.5", expected = "")

        separator.value = DecimalSeparator.Comma
        sut.textField.enterText(text = "1.52", expected = "1.5") // built on what the View showed

        val shown = run {
            val text = sut.textField.viewData.value.text
            text.replace('.', sut.viewData.value.decimalSeparator)
        }
        shown shouldBe "1,52"
    }

    fun createSut() {
        val state = context(environment) { OuncesInputStateFactory.create(amount = null) }
        // called at once, inside each commit, as on the main thread
        val store = Store(initialState = state, main = Dispatchers.Unconfined)
        // the parent's part: it answers no submission
        val slice = run {
            val lens = Lens<OuncesInputState, OuncesInputState>(
                get = { ounces -> ounces },
                set = { _, ounces -> ounces },
            )
            store.slice<OuncesInputState, OuncesInputOutcome>(lens) { ounces, _ -> Change(ounces) }
        }
        sut = OuncesInputFeature(
            // a setting's value applies at once, inside 'MutableStateFlow.value' assignment
            lifetime = CoroutineScope(Job() + Dispatchers.Unconfined),
            store = slice,
            environment = environment,
            textFieldFactory = textFieldFactory,
            settings = settings,
        )
    }

    val textFieldFactory = TextFieldFeature.Factory { lifetime, store, environment ->
        TextFieldFeature(lifetime, store, environment)
    }
}
