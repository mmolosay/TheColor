package io.github.mmolosay.thecolor.presentation.input.group

import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorInputType
import io.github.mmolosay.thecolor.domain.user.preferences.DefaultUserPreferences
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferencesRepository
import io.github.mmolosay.thecolor.domain.utils.filterReady
import io.github.mmolosay.thecolor.domain.utils.getOrElse
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexStateFactory
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvStateFactory
import io.github.mmolosay.thecolor.presentation.input.model.ColorState
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbStateFactory
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ColorInputGroupStateFactory @Inject constructor(
    private val hexFactory: ColorInputHexStateFactory,
    private val rgbFactory: ColorInputRgbStateFactory,
    private val hsvFactory: ColorInputHsvStateFactory,
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    suspend fun create(color: Color?): ColorInputGroupState {
        val preferredInputType = userPreferencesRepository.flowOfColorInputType
            .filterReady().first()
            .getOrElse { DefaultUserPreferences.PreferredColorInputType }
        // make list of all input types with the preferred one being first
        val orderedInputTypes = run {
            val allInputTypes = ColorInputType.entries
            val allInputTypesWithoutPreferredOne = allInputTypes.filter { it != preferredInputType }
            listOf(preferredInputType) + allInputTypesWithoutPreferredOne
        }
        return ColorInputGroupState(
            selectedInputType = preferredInputType,
            orderedInputTypes = orderedInputTypes,
            colorState = ColorState(
                color = color,
                source = null,
                revision = ColorState.Revision(0),
            ),
            hex = hexFactory.create(color),
            rgb = rgbFactory.create(color),
            hsv = hsvFactory.create(color),
        )
    }
}