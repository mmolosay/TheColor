package io.github.mmolosay.thecolor.data.mapper

import io.github.mmolosay.thecolor.data.remote.model.SchemeModeDto
import io.github.mmolosay.thecolor.domain.model.ColorScheme
import io.github.mmolosay.thecolor.domain.model.ColorScheme as DomainColorScheme

fun DomainColorScheme.Mode.toDto(): SchemeModeDto =
    when (this) {
        ColorScheme.Mode.Monochrome -> SchemeModeDto.Monochrome
        ColorScheme.Mode.MonochromeDark -> SchemeModeDto.MonochromeDark
        ColorScheme.Mode.MonochromeLight -> SchemeModeDto.MonochromeLight
        ColorScheme.Mode.Analogic -> SchemeModeDto.Analogic
        ColorScheme.Mode.Complement -> SchemeModeDto.Complement
        ColorScheme.Mode.AnalogicComplement -> SchemeModeDto.AnalogicComplement
        ColorScheme.Mode.Triad -> SchemeModeDto.Triad
        ColorScheme.Mode.Quad -> SchemeModeDto.Quad
    }