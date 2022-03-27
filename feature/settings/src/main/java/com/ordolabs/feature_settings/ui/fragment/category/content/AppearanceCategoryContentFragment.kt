package com.ordolabs.feature_settings.ui.fragment.category.content

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatDelegate
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ordolabs.feature_settings.R
import com.ordolabs.feature_settings.databinding.CategoryContentAppearanceFragmentBinding
import com.ordolabs.thecolor.model.settings.ApplicationSettings
import com.ordolabs.thecolor.model.settings.ApplicationSettings.Appearance
import com.ordolabs.thecolor.model.settings.ApplicationSettingsUtil.nightMode

class AppearanceCategoryContentFragment : BaseCategoryContentFragment() {

    private val binding: CategoryContentAppearanceFragmentBinding by viewBinding()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.category_content_appearance_fragment, container, false)
    }

    // region Set views

    override fun setViews() {
        setThemeCategory()
    }

    private fun setThemeCategory() {
        binding.themeTitle.setOnClickListener {
            onThemeCategoryClick()
        }
    }

    // endregion

    // region Click listeners

    private fun onThemeCategoryClick() {
        AppearanceCategoryThemeDialog.newInstance().apply {
            listener = ThemeDialogListener()
        }.show(childFragmentManager)
    }

    // endregion

    // region BaseCategoryContentFragment

    override fun populateViews(settings: ApplicationSettings) {
        populateTheme(settings.appearance.theme)
    }

    private fun populateTheme(current: Appearance.Theme) {
//        val radiobutton = getRadioButtonForTheme(current)
//        radiobutton.isChecked = true
//        radiobutton.jumpDrawablesToCurrentState() // skips animation
    }

    // endregion

    inner class ThemeDialogListener : AppearanceCategoryThemeDialog.Listener {

        override fun getCurrentTheme(): Appearance.Theme? =
            settings?.appearance?.theme

        override fun onThemeRadioButtonChecked(
            button: CompoundButton,
            theme: Appearance.Theme,
            isChecked: Boolean
        ) {
            if (!isChecked) return // ignore unchecking
            val settings = settings ?: return // ignore if settings are not obtained yet
            if (theme == settings.appearance.theme) return // already set

            AppCompatDelegate.setDefaultNightMode(theme.nightMode)
            val updated = settings.appearance.copy(theme = theme)
            settingsVM.editAppearance(updated)
        }
    }

    companion object {

        fun newInstance() =
            AppearanceCategoryContentFragment()
    }
}