package com.ordolabs.data_bridge.model.repository

import com.ordolabs.data.repository.ColorsHistoryRepository
import com.ordolabs.domain.repository.IColorRemoteRepository
import com.ordolabs.domain.repository.IColorValidatorRepository

interface DataRepositoryProvisions {

    val colorValidationRepository: IColorValidatorRepository
    val colorsHistoryRepository: ColorsHistoryRepository
    val colorRemoteRepository: IColorRemoteRepository
}