package io.github.mmolosay.thecolor.demo.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.mmolosay.thecolor.demo.domain.Amount
import io.github.mmolosay.thecolor.demo.domain.RecipeRepository
import io.github.mmolosay.thecolor.main.di.qualifiers.CoroutineDispatcherDiQualifiers.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

// keeps the last amount between launches; reading it takes a moment,
// so that the screen's wait for its startup data can be seen on a device
@Singleton
class SharedPreferencesRecipeRepository @Inject constructor(
    @ApplicationContext context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : RecipeRepository {

    private val preferences = context.getSharedPreferences(FileName, Context.MODE_PRIVATE)

    override suspend fun lastAmount(): Amount? {
        delay(ReadTime)
        val string = withContext(ioDispatcher) {
            preferences.getString(LastAmountKey, null)
        }
        return string?.toAmount()
    }

    override suspend fun saveLastAmount(amount: Amount) {
        val string = amount.toPreferenceString()
        withContext(ioDispatcher) {
            preferences.edit()
                .putString(LastAmountKey, string)
                .commit()
        }
    }

    // "g:120", "oz:1.50", "cups:1:2:4"
    private fun Amount.toPreferenceString(): String =
        when (this) {
            is Amount.Grams -> "g:${this.value}"
            is Amount.Ounces -> "oz:${this.value.toPlainString()}"
            is Amount.Cups -> "cups:${this.whole}:${this.numerator}:${this.denominator}"
        }

    // null for a string it can't read
    private fun String.toAmount(): Amount? {
        val parts = this.split(":")
        val numbers = parts.drop(1)
        return when (parts.first()) {
            "g" -> {
                val grams = numbers.singleOrNull()?.toIntOrNull() ?: return null
                Amount.Grams(value = grams)
            }
            "oz" -> {
                val ounces = numbers.singleOrNull()?.toBigDecimalOrNull() ?: return null
                Amount.Ounces(value = ounces)
            }
            "cups" -> {
                val ints = numbers.map { it.toIntOrNull() ?: return null }
                if (ints.size != 3) return null
                Amount.Cups(whole = ints[0], numerator = ints[1], denominator = ints[2])
            }
            else -> null
        }
    }

    private companion object {
        const val FileName = "recipe"
        const val LastAmountKey = "last_amount"
        val ReadTime = 300.milliseconds
    }
}
