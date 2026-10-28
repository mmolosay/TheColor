package io.github.mmolosay.thecolor.demo

import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.composition.Store
import io.github.mmolosay.thecolor.demo.domain.Amount
import io.github.mmolosay.thecolor.demo.domain.AmountConverter
import io.github.mmolosay.thecolor.demo.domain.CupSampler
import io.github.mmolosay.thecolor.demo.domain.DemoSettings
import io.github.mmolosay.thecolor.demo.domain.isSameAs
import io.github.mmolosay.thecolor.demo.presentation.feature.cups.CupDialCore
import io.github.mmolosay.thecolor.demo.presentation.feature.cups.CupDialFeature
import io.github.mmolosay.thecolor.demo.presentation.feature.editor.AmountEditorCore
import io.github.mmolosay.thecolor.demo.presentation.feature.editor.AmountEditorFeature
import io.github.mmolosay.thecolor.demo.presentation.feature.editor.AmountEditorOutcome
import io.github.mmolosay.thecolor.demo.presentation.feature.editor.AmountEditorState
import io.github.mmolosay.thecolor.demo.presentation.feature.grams.GramsInputCore
import io.github.mmolosay.thecolor.demo.presentation.feature.grams.GramsInputFeature
import io.github.mmolosay.thecolor.demo.presentation.feature.ounces.OuncesInputCore
import io.github.mmolosay.thecolor.demo.presentation.feature.ounces.OuncesInputFeature
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.TextFieldCore
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.TextFieldFeature
import io.github.mmolosay.thecolor.demo.presentation.format.LocaleAmountFormatter
import io.github.mmolosay.thecolor.utils.Lens
import io.kotest.inspectors.forAll
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

/**
 * Tests [AmountEditorFeature] with its real inputs, on a store of its own, as its parent gives it.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AmountEditorFeatureTest {

    // real implementations: the conversion between grams and ounces is what the inputs agree on
    val converter = AmountConverter()

    val core = run {
        val textFieldCoreFactory = TextFieldCore.Factory(::TextFieldCore)
        AmountEditorCore(
            amountConverter = converter,
            gramsFactory = { GramsInputCore(converter, textFieldCoreFactory) },
            ouncesFactory = { OuncesInputCore(converter, textFieldCoreFactory) },
            cupsFactory = { CupDialCore(converter, LocaleAmountFormatter()) },
        )
    }

    // never read, so the ounces keep their default separator
    val settings: DemoSettings = mockk {
        every { decimalSeparator } returns MutableStateFlow(null)
    }

    // never called: no test drags the dial
    val cupSampler: CupSampler = mockk()

    lateinit var store: Store<AmountEditorState, Nothing>

    lateinit var sut: AmountEditorFeature

    @Test
    fun `when the user types in grams, then every state committed has the ounces standing for the same amount`() =
        runTest {
            createSut(amount = Amount.Grams(value = 4))
            val committed = mutableListOf<AmountEditorState>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                store.drop(1).collect { committed += it } // unconfined, so it runs inside each commit
            }

            sut.grams.textField.enterText(text = "42", expected = "4")

            committed.shouldNotBeEmpty()
            committed.forAll { it.inputsAgree() shouldBe true }
        }

    @Test
    fun `given the parent has just set an amount, when a keystroke made on the text before it arrives, then the keystroke is dropped and both inputs show the parent's amount`() {
        createSut(amount = Amount.Grams(value = 4))
        store.update { state ->
            with(core) { Change(state.withAmount(Amount.Grams(value = 100))) }
        }

        sut.grams.textField.enterText(text = "42", expected = "4")

        texts shouldBe ("100" to "3.53")
    }

    fun createSut(amount: Amount?) {
        store = Store(initialState = core.initialState(amount))
        // the parent's part: it answers no submission and hands nothing up
        val slice = run {
            val lens = Lens<AmountEditorState, AmountEditorState>(
                get = { state -> state },
                set = { _, state -> state },
            )
            store.slice(lens) { state, _: AmountEditorOutcome? -> Change(state) }
        }
        sut = AmountEditorFeature(
            core = core,
            store = slice,
            scope = CoroutineScope(Job()),
            gramsFactory = gramsFactory,
            ouncesFactory = ouncesFactory,
            cupsFactory = cupsFactory,
        )
    }

    val textFieldFactory = TextFieldFeature.Factory(::TextFieldFeature)

    val gramsFactory = GramsInputFeature.Factory { core, store, scope ->
        GramsInputFeature(core, store, scope, textFieldFactory)
    }

    val ouncesFactory = OuncesInputFeature.Factory { core, store, scope ->
        OuncesInputFeature(core, store, scope, textFieldFactory, settings)
    }

    val cupsFactory = CupDialFeature.Factory { core, store, scope ->
        CupDialFeature(core, store, scope, cupSampler, Dispatchers.Unconfined)
    }

    // the grams' text and the ounces' text, as their Views show them
    val texts: Pair<String, String>
        get() {
            val grams = sut.grams.textField.viewData.value.text
            val ounces = sut.ounces.textField.viewData.value.text
            return grams to ounces
        }

    fun AmountEditorState.inputsAgree(): Boolean {
        val gramsAmount = with(core.gramsCore) { grams.amount() }
        val ouncesAmount = with(core.ouncesCore) { ounces.amount() }
        return context(converter) { gramsAmount isSameAs ouncesAmount }
    }
}
