package io.github.mmolosay.thecolor.demo.domain

data class Ingredient(
    val name: String,
) {
    companion object {
        val Flour = Ingredient(name = "Flour")
    }
}
