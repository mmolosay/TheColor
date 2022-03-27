package com.ordolabs.feature_settings.ui.fragment.category.content

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.RadioButton
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ordolabs.feature_settings.R
import com.ordolabs.feature_settings.databinding.CategoryAppearanceThemeDialogBinding
import com.ordolabs.thecolor.model.settings.ApplicationSettings
import com.ordolabs.thecolor.ui.dialog.BaseBottomSheetDialogFragment

class AppearanceCategoryThemeDialog : BaseBottomSheetDialogFragment() {

    var listener: Listener? = null

    private val binding: CategoryAppearanceThemeDialogBinding by viewBinding()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.category_appearance_theme_dialog, container, false)
    }

    // region Set views

    override fun setViews() {
        setThemeRadioButtons()
        setCurrentThemeRadioButton()
    }

    private fun setThemeRadioButtons() {
        binding.themeSystem.setOnCheckedChangeListener(::onThemeRadioButtonChecked)
        binding.themeLight.setOnCheckedChangeListener(::onThemeRadioButtonChecked)
        binding.themeDark.setOnCheckedChangeListener(::onThemeRadioButtonChecked)
    }

    private fun setCurrentThemeRadioButton() {
        val theme = listener?.getCurrentTheme() ?: return
        val button = getRadioButtonForTheme(theme)
        button.isChecked = true
        button.jumpDrawablesToCurrentState() // check without animation
    }

    // endregion

    // region View actions

    private fun onThemeRadioButtonChecked(button: CompoundButton, isChecked: Boolean) {
        val theme = getThemeForRadioButton(button)
        listener?.onThemeRadioButtonChecked(button, theme, isChecked)
    }

    // endregion

    // region View utils

    private fun getRadioButtonForTheme(theme: ApplicationSettings.Appearance.Theme): RadioButton =
        when (theme) {
            ApplicationSettings.Appearance.Theme.SYSTEM -> binding.themeSystem
            ApplicationSettings.Appearance.Theme.LIGHT -> binding.themeLight
            ApplicationSettings.Appearance.Theme.DARK -> binding.themeDark
        }

    private fun getThemeForRadioButton(radiobutton: View): ApplicationSettings.Appearance.Theme =
        when (radiobutton) {
            binding.themeLight -> ApplicationSettings.Appearance.Theme.LIGHT
            binding.themeDark -> ApplicationSettings.Appearance.Theme.DARK
            else -> ApplicationSettings.Appearance.Theme.SYSTEM
        }

    // endregion

    interface Listener {

        fun getCurrentTheme(): ApplicationSettings.Appearance.Theme?

        fun onThemeRadioButtonChecked(
            button: CompoundButton,
            theme: ApplicationSettings.Appearance.Theme,
            isChecked: Boolean
        )
    }

    companion object {
        fun newInstance() =
            AppearanceCategoryThemeDialog()
    }
}