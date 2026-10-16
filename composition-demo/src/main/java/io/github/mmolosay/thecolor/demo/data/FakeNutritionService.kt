package io.github.mmolosay.thecolor.demo.data

import io.github.mmolosay.thecolor.demo.domain.Amount
import io.github.mmolosay.thecolor.demo.domain.AmountConverter
import io.github.mmolosay.thecolor.demo.domain.Ingredient
import io.github.mmolosay.thecolor.demo.domain.NutritionFacts
import io.github.mmolosay.thecolor.demo.domain.NutritionService
import io.github.mmolosay.thecolor.demo.domain.Sort
import io.github.mmolosay.thecolor.demo.domain.isSameAs
import kotlinx.coroutines.delay
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

// answers slowly, and fails every other request for 13 g, the first included,
// so that late results and retries can be seen on a device
@Singleton
class FakeNutritionService @Inject constructor(
    private val amountConverter: AmountConverter,
) : NutritionService {

    private val nutritionFactsRequestsFor13Grams = AtomicInteger(0)
    private val substitutesRequestsFor13Grams = AtomicInteger(0)

    override suspend fun nutritionFacts(
        ingredient: Ingredient,
        amount: Amount,
    ): Result<NutritionFacts> {
        delay(ResponseTime)
        if (amount.failsThisTime(nutritionFactsRequestsFor13Grams)) return fakeFailure()
        val perGram = NutrientsPerGram[ingredient]
            ?: return Result.failure(IOException("No nutrition facts for ${ingredient.name}"))
        val facts = factsOf(ingredient, amount, perGram)
        return Result.success(facts)
    }

    override suspend fun substitutes(amount: Amount, sort: Sort): Result<List<NutritionFacts>> {
        delay(ResponseTime)
        if (amount.failsThisTime(substitutesRequestsFor13Grams)) return fakeFailure()
        val substitutes = NutrientsPerGram
            .filterKeys { it != Ingredient.Flour }
            .map { (ingredient, perGram) -> factsOf(ingredient, amount, perGram) }
        val sorted = when (sort) {
            Sort.Calories -> substitutes.sortedBy { it.calories } // the lightest first
            Sort.Protein -> substitutes.sortedByDescending { it.protein } // the most protein first
        }
        return Result.success(sorted)
    }

    private fun factsOf(
        ingredient: Ingredient,
        amount: Amount,
        perGram: Nutrients,
    ): NutritionFacts {
        val grams = with(amountConverter) { amount.toGrams() }.value
        val calories = run {
            val exact = BigDecimal(grams).multiply(perGram.calories)
            exact.setScale(0, RoundingMode.HALF_UP).intValueExact()
        }
        val protein = run {
            val exact = BigDecimal(grams).multiply(perGram.protein)
            exact.setScale(1, RoundingMode.HALF_UP)
        }
        return NutritionFacts(
            ingredient = ingredient,
            amount = amount,
            calories = calories,
            protein = protein,
            suggestedAmount = suggestedAmountFor(grams),
        )
    }

    // the nearest multiple of 60 g, half a cup of flour, and never less than that
    private fun suggestedAmountFor(grams: Int): Amount {
        val halfCups = ((grams + 30) / 60).coerceAtLeast(1)
        return Amount.Grams(value = halfCups * 60)
    }

    private fun Amount.failsThisTime(requestsFor13Grams: AtomicInteger): Boolean {
        val is13Grams = context(amountConverter) { this isSameAs ThirteenGrams }
        if (!is13Grams) return false
        val request = requestsFor13Grams.getAndIncrement()
        return (request % 2 == 0)
    }

    private fun <T> fakeFailure(): Result<T> =
        Result.failure(IOException("Every other request for 13 g fails"))

    private class Nutrients(
        val calories: BigDecimal, // kcal
        val protein: BigDecimal, // grams
    )

    private companion object {
        val ResponseTime = 1500.milliseconds
        val ThirteenGrams = Amount.Grams(value = 13)

        // flour first, then its substitutes
        val NutrientsPerGram: Map<Ingredient, Nutrients> = mapOf(
            Ingredient.Flour to Nutrients(
                calories = BigDecimal("3.64"),
                protein = BigDecimal("0.103"),
            ),
            Ingredient(name = "Almond flour") to Nutrients(
                calories = BigDecimal("5.71"),
                protein = BigDecimal("0.212"),
            ),
            Ingredient(name = "Oat flour") to Nutrients(
                calories = BigDecimal("4.04"),
                protein = BigDecimal("0.147"),
            ),
            Ingredient(name = "Coconut flour") to Nutrients(
                calories = BigDecimal("4.43"),
                protein = BigDecimal("0.172"),
            ),
            Ingredient(name = "Rice flour") to Nutrients(
                calories = BigDecimal("3.66"),
                protein = BigDecimal("0.060"),
            ),
            Ingredient(name = "Chickpea flour") to Nutrients(
                calories = BigDecimal("3.87"),
                protein = BigDecimal("0.224"),
            ),
        )
    }
}
