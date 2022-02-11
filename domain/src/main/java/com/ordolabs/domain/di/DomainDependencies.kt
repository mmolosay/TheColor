package com.ordolabs.domain.di

import com.ordolabs.domain.repository.IColorRemoteRepository
import com.ordolabs.domain.repository.IColorValidatorRepository

/**
 * DIP in order to make ':domain' module absolutely stable.
 */
interface DomainDependencies {

    val colorValidatorRepository: IColorValidatorRepository
    val colorRemoteRepository: IColorRemoteRepository
}