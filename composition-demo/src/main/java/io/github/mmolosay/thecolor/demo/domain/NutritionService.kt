package io.github.mmolosay.thecolor.demo.domain

interface NutritionService {

    suspend fun nutritionFacts(ingredient: Ingredient, amount: Amount): Result<NutritionFacts>

    // the best substitutes for flour by that sort, each for the same amount
    suspend fun substitutes(amount: Amount, sort: Sort): Result<List<NutritionFacts>>
}
