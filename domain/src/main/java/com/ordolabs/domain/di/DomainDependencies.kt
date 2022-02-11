package com.ordolabs.domain.di

import com.ordolabs.domain.repository.ColorRemoteRepository
import com.ordolabs.domain.repository.ColorValidatorRepository

/**
 * DIP in order to make ':domain' module absolutely stable.
 */
interface DomainDependencies {

    val colorValidatorRepository: ColorValidatorRepository
    val colorRemoteRepository: ColorRemoteRepository
}