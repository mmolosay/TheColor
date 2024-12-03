package io.github.mmolosay.thecolor.presentation.api

import io.github.mmolosay.thecolor.presentation.api.nav.bar.NavBarAppearance
import io.github.mmolosay.thecolor.presentation.api.nav.bar.NavBarAppearanceController
import io.github.mmolosay.thecolor.presentation.api.nav.bar.RootNavBarAppearanceController
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.junit.Test
import kotlin.time.measureTime

class NavBarAppearanceControllerTest {

    val sut: NavBarAppearanceController = RootNavBarAppearanceController()

    @Test
    fun `root controller is created with no appearance`() {
        sut.appearanceFlow.value shouldBe null
    }

    @Test
    fun `pushing appearance to root controller emits it from the flow`() {
        val appearance: NavBarAppearance.WithTag = mockk()
        sut.push(appearance)

        sut.appearanceFlow.value shouldBe appearance
    }

    @Test
    fun `peeling single present appearance emits 'null' from the flow`() {
        val appearance: NavBarAppearance.WithTag = mockk()
        sut.push(appearance)

        sut.peel()

        sut.appearanceFlow.value shouldBe null
    }

    @Test
    fun `peeling appearance when there are none doesn't emit anything from the flow (because stack doesn't change)`() {
        sut.peel()

        sut.appearanceFlow.value shouldBe null
    }

    @Test
    fun `peeling appearance emits underlying appearance from the flow`() {
        val appearance1: NavBarAppearance.WithTag = mockk()
        val appearance2: NavBarAppearance.WithTag = mockk()
        sut.push(appearance1)
        sut.push(appearance2)

        sut.peel()

        sut.appearanceFlow.value shouldBe appearance1
    }

    @Test
    fun `clearing controller emits 'null' appearance from the flow`() {
        val appearance1: NavBarAppearance.WithTag = mockk()
        val appearance2: NavBarAppearance.WithTag = mockk()
        sut.push(appearance1)
        sut.push(appearance2)

        sut.clear()

        sut.appearanceFlow.value shouldBe null
    }

    @Test
    fun `child controller hoists pushed appearance to the parent`() {
        val appearance: NavBarAppearance.WithTag = mockk()
        val child = sut.branch("1")

        child.push(appearance)

        sut.appearanceFlow.value shouldBe appearance
    }

    @Test
    fun `#1 when appearance is peeled from the child controller, then parent emits the appearance that was pushed to the controller tree last`() {
        val appearance1: NavBarAppearance.WithTag = mockk()
        val appearance2: NavBarAppearance.WithTag = mockk()
        val appearance3: NavBarAppearance.WithTag = mockk()
        val appearance4: NavBarAppearance.WithTag = mockk()
        val child1 = sut.branch("1")
        val child2 = sut.branch("2")
        val child3 = sut.branch("3")
        sut.push(appearance1)
        child3.push(appearance2)
        child2.push(appearance3)
        child1.push(appearance4)

        child2.peel()

        sut.appearanceFlow.value shouldBe appearance4
    }

    @Test
    fun `#2 when appearance is peeled from the child controller, then parent emits the appearance that was pushed to the controller tree last`() {
        val appearance1: NavBarAppearance.WithTag = mockk()
        val appearance2: NavBarAppearance.WithTag = mockk()
        val appearance3: NavBarAppearance.WithTag = mockk()
        val child1 = sut.branch("1")
        val child1_1 = child1.branch("1_1")
        sut.push(appearance1)
        child1.push(appearance2)
        child1_1.push(appearance3)

        child1.peel()

        sut.appearanceFlow.value shouldBe appearance3
    }

    @Test
    fun `#3 when appearance is peeled from the child controller, then parent emits the appearance that was pushed to the controller tree last`() {
        val appearance1: NavBarAppearance.WithTag = mockk()
        val appearance2: NavBarAppearance.WithTag = mockk()
        val appearance3: NavBarAppearance.WithTag = mockk()
        val child1 = sut.branch("1")
        val child1_1 = child1.branch("1_1")
        sut.push(appearance1)
        child1_1.push(appearance2)
        child1.push(appearance3)

        child1_1.peel()

        sut.appearanceFlow.value shouldBe appearance3
    }

    @Test
    fun `#4 when appearance is peeled from the child controller, then parent emits the appearance that was pushed to the controller tree last`() {
        val appearance1: NavBarAppearance.WithTag = mockk()
        val appearance2: NavBarAppearance.WithTag = mockk()
        val child1 = sut.branch("1")
        val child1_1 = child1.branch("1_1")

        child1.push(appearance1)
        child1_1.push(appearance2)
        child1_1.clear()

        sut.appearanceFlow.value shouldBe appearance1
    }

    @Test
    fun `#1 clearing appearances of the controller also clears the whole controller tree`() {
        val appearance1: NavBarAppearance.WithTag = mockk()
        val appearance2: NavBarAppearance.WithTag = mockk()
        val appearance3: NavBarAppearance.WithTag = mockk()
        val child1 = sut.branch("1")
        val child2 = sut.branch("2")
        sut.push(appearance1)
        child1.push(appearance2)
        child2.push(appearance3)

        sut.clear()

        sut.appearanceFlow.value shouldBe null
        child1.appearanceFlow.value shouldBe null
        child2.appearanceFlow.value shouldBe null
    }

    @Test
    fun `#2 clearing appearances of the controller also clears the whole controller tree`() {
        val appearance1: NavBarAppearance.WithTag = mockk()
        val appearance2: NavBarAppearance.WithTag = mockk()
        val appearance3: NavBarAppearance.WithTag = mockk()
        val child1 = sut.branch("1")
        val child1_1 = child1.branch("1_1")
        sut.push(appearance1)
        child1.push(appearance2)
        child1_1.push(appearance3)

        child1.clear()

        sut.appearanceFlow.value shouldBe appearance1
        child1.appearanceFlow.value shouldBe null
        child1_1.appearanceFlow.value shouldBe null
    }

    // not a test but rather a benchmark
    @Test
    fun `log the time to clear a nested controller in a controller tree`() {
        val appearance1: NavBarAppearance.WithTag = mockk()
        val appearance2: NavBarAppearance.WithTag = mockk()
        val child1 = sut.branch("home")
        val child2 = sut.branch("settings")
        val child1_1 = child1.branch("color center")
        val child1_1_1 = child1.branch("selected color scheme swatch details dialog")

        child1.push(appearance1)
        child1_1_1.push(appearance2)
        val duration = measureTime {
            child1_1_1.clear()
        }

        println("Time to clear a nested controller in a controller tree: $duration")
    }
}