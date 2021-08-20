package com.ordolabs.domain.usecase

import kotlinx.coroutines.flow.Flow

/**
 * Basic use case. Takes one parameter of [Parameter] type and
 * returns a [Flow] of [Result] type.
 */
interface BaseUseCase<in Parameter, out Result> {
    suspend operator fun invoke(param: Parameter): Flow<Result>
}