package io.github.mmolosay.thecolor.data.remote.mapper

import io.github.mmolosay.thecolor.data.remote.api.TheColorApiService
import io.github.mmolosay.thecolor.domain.model.ColorScheme
import io.github.mmolosay.thecolor.domain.model.ColorScheme as DomainColorScheme

fun DomainColorScheme.Mode.toDto(): TheColorApiService.SchemeMode =
    when (this) {
        ColorScheme.Mode.Monochrome -> TheColorApiService.SchemeMode.MONOCHROME
        ColorScheme.Mode.MonochromeDark -> TheColorApiService.SchemeMode.MONOCHROME_DARK
        ColorScheme.Mode.MonochromeLight -> TheColorApiService.SchemeMode.MONOCHROME_LIGHT
        ColorScheme.Mode.Analogic -> TheColorApiService.SchemeMode.ANALOGIC
        ColorScheme.Mode.Complement -> TheColorApiService.SchemeMode.COMPLEMENT
        ColorScheme.Mode.AnalogicComplement -> TheColorApiService.SchemeMode.ANALOGIC_COMPLEMENT
        ColorScheme.Mode.Triad -> TheColorApiService.SchemeMode.TRIAD
        ColorScheme.Mode.Quad -> TheColorApiService.SchemeMode.QUAD
    }