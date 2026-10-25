package io.github.mmolosay.thecolor.demo.domain

interface RecipeRepository {

    suspend fun lastAmount(): Amount?

    suspend fun saveLastAmount(amount: Amount)
}
