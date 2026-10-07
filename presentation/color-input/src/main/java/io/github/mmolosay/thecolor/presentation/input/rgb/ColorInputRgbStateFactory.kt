package io.github.mmolosay.thecolor.presentation.input.rgb

import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorConverter
import io.github.mmolosay.thecolor.domain.user.preferences.DefaultUserPreferences
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferencesRepository
import io.github.mmolosay.thecolor.domain.utils.getOrElse
import io.github.mmolosay.thecolor.presentation.input.ColorInputMapper
import io.github.mmolosay.thecolor.presentation.input.model.causedByUser
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldDataFactory
import javax.inject.Inject

class ColorInputRgbStateFactory @Inject constructor(
    private val textFieldDataFactory: TextFieldDataFactory,
    private val colorConverter: ColorConverter,
    private val colorInputMapper: ColorInputMapper,
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    fun create(color: Color?): ColorInputRgbState {
        val colorInput = run {
            val rgb = with(colorConverter) { color?.toRgb() }
            with(colorInputMapper) { rgb?.toColorInput() }
        }
        fun createTextFieldData(value: String?): TextFieldData =
            textFieldDataFactory.create(
                text = TextFieldData.Text(value.orEmpty()) causedByUser false,
                isClearTextFeatureEnabled = false,
            )
        val isSmartBackspaceEnabled = userPreferencesRepository.flowOfSmartBackspace
            .value.getOrElse { DefaultUserPreferences.SmartBackspace }
            .enabled
        return ColorInputRgbState(
            rTextField = createTextFieldData(colorInput?.r),
            gTextField = createTextFieldData(colorInput?.g),
            bTextField = createTextFieldData(colorInput?.b),
            inputSubmissionResult = null,
            isSmartBackspaceEnabled = isSmartBackspaceEnabled,
            color = color,
        )
    }
}