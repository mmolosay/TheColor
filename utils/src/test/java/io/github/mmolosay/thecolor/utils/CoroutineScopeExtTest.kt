package io.github.mmolosay.thecolor.utils

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import org.junit.Test
import kotlin.coroutines.EmptyCoroutineContext

class CoroutineScopeExtTest {

    @Test
    fun `CoroutineScope(parent) returns a scope that is canceled when the parent is`() {
        val parent = CoroutineScope(context = EmptyCoroutineContext)
        val child = CoroutineScope(parent)

        parent.cancel()

        parent.isActive shouldBe false
        child.isActive shouldBe false
    }

    @Test
    fun `CoroutineScope(parent) returns a scope that when canceled doesn't cancel the parent`() {
        val parent = CoroutineScope(context = EmptyCoroutineContext)
        val child = CoroutineScope(parent)

        child.cancel()

        parent.isActive shouldBe true
        child.isActive shouldBe false
    }
}