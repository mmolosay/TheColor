package io.github.mmolosay.thecolor.testing

import io.mockk.clearMocks

fun clearMocksOnlyRecordedCalls(
    firstMock: Any,
    vararg mocks: Any,
) =
    clearMocks(
        firstMock = firstMock,
        mocks = mocks,
        answers = false,
        recordedCalls = true,
        childMocks = false,
        verificationMarks = false,
        exclusionRules = false,
    )