package com.ordolabs.domain.usecase

interface BaseUseCase<in Parameter, out Result> {
    suspend operator fun invoke(param: Parameter): Result
}