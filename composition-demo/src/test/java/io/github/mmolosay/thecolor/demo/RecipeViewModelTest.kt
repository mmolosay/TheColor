package io.github.mmolosay.thecolor.demo

import io.github.mmolosay.thecolor.demo.domain.Amount
import io.github.mmolosay.thecolor.demo.domain.AmountConverter
import io.github.mmolosay.thecolor.demo.domain.CupSampler
import io.github.mmolosay.thecolor.demo.domain.DemoSettings
import io.github.mmolosay.thecolor.demo.domain.NutritionService
import io.github.mmolosay.thecolor.demo.domain.RecipeRepository
import io.github.mmolosay.thecolor.demo.presentation.feature.cups.CupDialEnvironment
import io.github.mmolosay.thecolor.demo.presentation.feature.cups.CupDialFeature
import io.github.mmolosay.thecolor.demo.presentation.feature.editor.AmountEditorEnvironment
import io.github.mmolosay.thecolor.demo.presentation.feature.editor.AmountEditorFeature
import io.github.mmolosay.thecolor.demo.presentation.feature.grams.GramsInputEnvironment
import io.github.mmolosay.thecolor.demo.presentation.feature.grams.GramsInputFeature
import io.github.mmolosay.thecolor.demo.presentation.feature.nutrition.NutritionEnvironment
import io.github.mmolosay.thecolor.demo.presentation.feature.nutrition.NutritionFeature
import io.github.mmolosay.thecolor.demo.presentation.feature.ounces.OuncesInputEnvironment
import io.github.mmolosay.thecolor.demo.presentation.feature.ounces.OuncesInputFeature
import io.github.mmolosay.thecolor.demo.presentation.feature.recipe.RecipeEnvironment
import io.github.mmolosay.thecolor.demo.presentation.feature.recipe.RecipeViewModel
import io.github.mmolosay.thecolor.demo.presentation.feature.sheet.SubstituteSheetEnvironment
import io.github.mmolosay.thecolor.demo.presentation.feature.sheet.SubstituteSheetFeature
import io.github.mmolosay.thecolor.demo.presentation.feature.substitutes.SubstitutesEnvironment
import io.github.mmolosay.thecolor.demo.presentation.feature.substitutes.SubstitutesFeature
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.TextFieldFeature
import io.github.mmolosay.thecolor.demo.presentation.format.LocaleAmountFormatter
import io.github.mmolosay.thecolor.demo.presentation.format.LocaleNutritionFormatter
import io.github.mmolosay.thecolor.testing.MainDispatcherExtension
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

/**
 * Its work runs on a [StandardTestDispatcher], so that a result can be on its way to the store as
 * the user acts, before the work is stopped: as on a device, when a result and the user's action
 * arrive at once.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RecipeViewModelTest {

    val testDispatcher = StandardTestDispatcher()

    @RegisterExtension
    @Suppress("unused")
    val mainDispatcherExtension = MainDispatcherExtension(testDispatcher)

    // real implementations: the editor's texts are what the tests check
    val environment = run {
        val converter = AmountConverter()
        val formatter = LocaleAmountFormatter()
        RecipeEnvironment(
            editor = AmountEditorEnvironment(
                grams = GramsInputEnvironment(amountConverter = converter),
                ounces = OuncesInputEnvironment(amountConverter = converter),
                cups = CupDialEnvironment(
                    amountConverter = converter,
                    amountFormatter = formatter,
                ),
                amountConverter = converter,
            ),
            nutrition = NutritionEnvironment(
                amountConverter = converter,
                amountFormatter = formatter,
                nutritionFormatter = LocaleNutritionFormatter(),
            ),
            substitutes = SubstitutesEnvironment(
                amountConverter = converter,
                nutritionFormatter = LocaleNutritionFormatter(),
            ),
            sheet = SubstituteSheetEnvironment(
                nutrition = NutritionEnvironment(
                    amountConverter = converter,
                    amountFormatter = formatter,
                    nutritionFormatter = LocaleNutritionFormatter(),
                ),
            ),
            amountFormatter = formatter,
        )
    }

    // never read, so the ounces keep their default separator
    val settings: DemoSettings = mockk {
        every { decimalSeparator } returns MutableStateFlow(null)
    }

    val repository: RecipeRepository = mockk()

    // the cards' facts don't arrive within a test
    val nutritionService: NutritionService = mockk {
        coEvery { nutritionFacts(any(), any()) } coAnswers { awaitCancellation() }
        coEvery { substitutes(any(), any()) } coAnswers { awaitCancellation() }
    }

    // never called: no test drags the dial
    val cupSampler: CupSampler = mockk()

    lateinit var sut: RecipeViewModel

    @Test
    fun `given the saved amount is loading, when it arrives as the user types, then the user's text stays`() =
        runTest(testDispatcher) {
            val savedAmount = CompletableDeferred<Amount?>()
            coEvery { repository.lastAmount() } coAnswers { savedAmount.await() }
            createSut()
            runCurrent() // the load starts

            savedAmount.complete(Amount.Grams(value = 150))
            sut.editor.grams.textField.enterText(text = "7", expected = "")
            runCurrent()

            sut.editor.grams.textField.viewData.value.text shouldBe "7"
        }

    @Test
    fun `given the saved amount is loading, when it arrives after the user has typed and cleared the amount, then the amount stays cleared`() =
        runTest(testDispatcher) {
            val savedAmount = CompletableDeferred<Amount?>()
            coEvery { repository.lastAmount() } coAnswers { savedAmount.await() }
            createSut()
            runCurrent() // the load starts
            sut.editor.grams.textField.enterText(text = "7", expected = "")
            sut.editor.grams.textField.clearText() // empties the other inputs too

            savedAmount.complete(Amount.Grams(value = 150))
            runCurrent()

            sut.editor.grams.textField.viewData.value.text shouldBe ""
        }

    @Test
    fun `given 80 g is being saved, when the save ends as the user edits, then NO message shows, since the edit has withdrawn the submission`() =
        runTest(testDispatcher) {
            coEvery { repository.lastAmount() } returns null
            val save = CompletableDeferred<Unit>()
            coEvery { repository.saveLastAmount(Amount.Grams(value = 80)) } coAnswers {
                save.await()
            }
            createSut()
            sut.editor.grams.textField.enterText(text = "80", expected = "")
            sut.editor.grams.submit()
            runCurrent() // the save starts

            save.complete(Unit)
            sut.editor.grams.textField.enterText(text = "805", expected = "80")
            runCurrent()

            sut.viewData.value.messageToShow.shouldBeNull()
        }

    @Test
    fun `given 80 g is being saved, when the save ends as the user submits 805 g, then the 805 g submission still waits and NO message shows, so that the old save doesn't answer the new submission`() =
        runTest(testDispatcher) {
            coEvery { repository.lastAmount() } returns null
            val firstSave = CompletableDeferred<Unit>()
            coEvery { repository.saveLastAmount(Amount.Grams(value = 80)) } coAnswers {
                firstSave.await()
            }
            coEvery { repository.saveLastAmount(Amount.Grams(value = 805)) } coAnswers {
                awaitCancellation() // doesn't end within the test
            }
            createSut()
            sut.editor.grams.textField.enterText(text = "80", expected = "")
            sut.editor.grams.submit()
            runCurrent() // the save of 80 g starts

            firstSave.complete(Unit)
            sut.editor.grams.textField.enterText(text = "805", expected = "80")
            sut.editor.grams.submit()
            runCurrent()

            sut.editor.grams.viewData.value.isPending shouldBe true
            sut.viewData.value.messageToShow.shouldBeNull()
        }

    fun createSut() {
        sut = RecipeViewModel(
            environment = environment,
            editorFactory = editorFactory,
            nutritionFactory = nutritionFactory,
            substitutesFactory = substitutesFactory,
            sheetFactory = sheetFactory,
            textFieldFactory = textFieldFactory,
            repository = repository,
        )
    }

    val textFieldFactory = TextFieldFeature.Factory { scope, store, environment ->
        TextFieldFeature(scope, store, environment)
    }

    val gramsFactory = GramsInputFeature.Factory { scope, store, environment ->
        GramsInputFeature(scope, store, environment, textFieldFactory)
    }

    val ouncesFactory = OuncesInputFeature.Factory { scope, store, environment ->
        OuncesInputFeature(scope, store, environment, textFieldFactory, settings)
    }

    val cupsFactory = CupDialFeature.Factory { scope, store, environment ->
        CupDialFeature(scope, store, environment, cupSampler, Dispatchers.Unconfined)
    }

    val editorFactory = AmountEditorFeature.Factory { scope, store, environment ->
        AmountEditorFeature(scope, store, environment, gramsFactory, ouncesFactory, cupsFactory)
    }

    val nutritionFactory = NutritionFeature.Factory { scope, store, environment ->
        NutritionFeature(scope, store, environment, nutritionService)
    }

    val substitutesFactory = SubstitutesFeature.Factory { scope, store, environment ->
        SubstitutesFeature(scope, store, environment, nutritionService)
    }

    val sheetFactory = SubstituteSheetFeature.Factory { scope, store, environment ->
        SubstituteSheetFeature(scope, store, environment, nutritionFactory)
    }
}
