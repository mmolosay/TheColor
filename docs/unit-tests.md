# Unit tests

Conventions for unit tests. Which tests are worth writing, and how they are written.

## Table of Contents

* [Valuable unit tests](#valuable-unit-tests)
  * [When to write a test](#when-to-write-a-test)
  * [What is not worth a test](#what-is-not-worth-a-test)
  * [How tests are written](#how-tests-are-written)
* [Tooling](#tooling)
  * [Libraries](#libraries)
  * [Shared test helpers](#shared-test-helpers)
* [Files and classes](#files-and-classes)
  * [Naming and location](#naming-and-location)
  * [Class layout](#class-layout)
  * [Visibility modifiers](#visibility-modifiers)
* [The SUT](#the-sut)
* [Test names](#test-names)
* [Test body](#test-body)
  * [Given-When-Then structure](#given-when-then-structure)
  * [Assertions per test](#assertions-per-test)
  * [Comments and named steps](#comments-and-named-steps)
  * [Cross-referencing tests](#cross-referencing-tests)
  * [Documenting complex tests](#documenting-complex-tests)
* [Mocking](#mocking)
* [Parameterized tests](#parameterized-tests)
* [Concurrency tests](#concurrency-tests)
* [Kotest assertions](#kotest-assertions)

-----

## Valuable unit tests

Most of the principles come from this presentation:
[Write awesome tests by Jeroen Mols](https://youtu.be/F8Gc8Nwf0yk?si=M6Z_-75ueUsn4iO_).

Unit tests describe the software contract of a component. To recall the expected behaviour of a
component, it should be enough to open its unit tests instead of going through its code.
Chasing code coverage is not a goal, and neither is testing that obvious code indeed *does* work.

### When to write a test

A unit test is written (or added) **only** if:

- a piece of code was caught containing a bug;
- a piece of code may be executed in a multitude of ways and yield different results / produce various side-effects;
- a piece of code is vital for the application;
- a piece of code employs an API whose behaviour is not entirely clear.

### What is not worth a test

- Code that obviously works. *"When an item is added, then it is present in the items"* only restates the implementation.
- Implementation details in place of the contract. Asserting that a returned list is not an `ArrayList`
  tests how `toList()` happens to be implemented. The contract is that the list is a snapshot,
  so the test mutates the source afterwards and asserts that the list has not changed.

### How tests are written

- A new test goes next to the other tests that target the same feature of the component.
- Most tests have only one assertion — see [Assertions per test](#assertions-per-test).
- Tests with a complex *given*, *when* or *then* part get verbose documentation — see [Documenting complex tests](#documenting-complex-tests).
- Mocked behaviour is kept in the body of each test. Dependencies are rarely mocked on the level of the test class — see [Mocking](#mocking).
- Visibility modifiers are almost never used in test classes — see [Visibility modifiers](#visibility-modifiers).

-----

## Tooling

### Libraries

| Library | Used for |
|---|---|
| JUnit Jupiter | Running tests |
| Kotest | Assertions |
| MockK | Mocking |
| kotlinx-coroutines-test | Testing coroutines |
| Turbine | Testing flows |
| Robolectric | Code that uses Android framework classes |

### Shared test helpers

- A helper used within one module stays in the test source set of that module —
  e.g. a test implementation of a factory, which creates real objects with test dependencies.
- A helper used by several modules goes to a dedicated `testing` Gradle module with a regular `main` source set
  (not Gradle `testFixtures`). The modules that need it depend on it in tests only.

-----

## Files and classes

### Naming and location

- One test class per subject, named `<Subject>Test`.
- Tests are in the module's `src/test/java`, in the module's root package,
  even if the subject is in a sub-package: a test of `<root>.viewmodel.ArticleViewModel` is in `<root>`.
- A type and its extensions are tested separately:
  `PlaylistTest` covers what is declared in `Playlist`, `PlaylistExtTest` covers the extension functions from `PlaylistExt.kt`.
- Unrelated extension functions get separate test classes, named `<Receiver>Ext<Function>Test`:
  `StringExtCapitalizeTest`, `StringExtTruncateTest`.
- When the subject is not obvious from the name of the test class, a class KDoc says what is tested:
  ```kotlin
  /**
   * Tests [String.truncate] extension.
   */
  class StringExtTruncateTest
  ```
- When a test class covers several functions, their tests may be grouped with `// region '<function>'` and `// endregion`.

### Class layout

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class ArticleViewModelTest {

    val testDispatcher = UnconfinedTestDispatcher()

    @RegisterExtension
    @Suppress("unused")
    val mainDispatcherExtension = ... // replaces 'Dispatchers.Main' with 'testDispatcher'

    val articleRepository: ArticleRepository = mockk()
    val analytics: Analytics = mockk(relaxed = true)

    lateinit var sut: ArticleViewModel

    @Test
    fun `when 'refresh' is invoked, then SUT emits 'Ready' state`() = ...

    fun createSut(...) = ...

    val ArticleViewModel.data: ArticleData
        get() = ...

    companion object {
        // data for parameterized tests
    }
}
```

Members go in this order:

1. The test dispatcher, and the extension that replaces `Dispatchers.Main` if the subject uses it.
2. Dependencies of the subject — mocks and real instances.
3. `sut`.
4. Tests.
5. `createSut()`, then accessors and helpers used by the tests.
6. `companion object` with data for parameterized tests.

### Visibility modifiers

Members of test classes have no visibility modifiers. The exceptions:

- A test class is `internal` when it has to expose an `internal` type. When the subject is `internal`,
  so is its test class, because `sut` is a public property.
- File-level helpers used only in their own file may be `private`.

-----

## The SUT

- The subject is always named `sut`, and test names call it "SUT": `given SUT is created, ...`.
- A subject with no dependencies to vary between tests is created once, as a property:
  ```kotlin
  val sut = PriceFormatter()
  ```
- Otherwise, it's a `lateinit var sut` assigned by `createSut()`:
  ```kotlin
  fun createSut() =
      ArticleViewModel(
          articleRepository = articleRepository,
      ).also {
          sut = it
      }
  ```
  Each test calls `createSut()` **after** stubbing the dependencies: a subject may read its dependencies
  as soon as it is created — e.g. a ViewModel that starts collecting a flow on initialization.
- `createSut()` has parameters with default values for dependencies that vary between tests:
  ```kotlin
  fun createSut(
      createData: CreateArticleDataUseCase = createDataMock,
  ) =
      ArticleViewModel(
          createData = createData,
      ).also {
          sut = it
      }
  ```
  When some tests use a real implementation of a dependency and others use a mock, the class KDoc says why —
  e.g. a mock when a test doesn't check the contents of the data, the real implementation when it does.
- When the arguments of the constructor are what the test is about, the subject is created inline:
  ```kotlin
  sut = Animator(initialState = State.Hidden)
  ```
- State that tests read often gets an accessor. When the state must be of a certain type, the accessor
  asserts it, so that a test fails with a clear message rather than a `ClassCastException`:
  ```kotlin
  val data: ArticleData
      get() = sut.dataStateFlow.value.shouldBeInstanceOf<DataState.Ready>().data
  ```

-----

## Test names

- A name is a sentence in backticks, in the form `given …, when …, then …`:
  ```
  given an article is saved, when an unknown article is deleted, then 'false' is returned and the 'articles' are unchanged
  ```
- `given` is omitted when there's no precondition beyond the subject existing:
  ```
  when 'refresh' is invoked, then SUT emits 'Ready' state
  ```
- Names of code elements — functions, properties, types, states, literal values — are wrapped in single quotes.
  So are the names of features and actions. Ordinary nouns are not, even when they match a parameter name:
  ```
  when the cache is modified outside of the 'transaction' block, then an exception is thrown
  when 'null' title is set, then 'titleFlow' is updated with 'null' title
  given that 'auto save' feature is enabled, when the text is changed, then the draft is saved
  ```
- A consequence that explains why the outcome matters follows `, so that` or `, thus`:
  ```
  when 'refresh' is invoked, then the ongoing 'refresh' job is canceled, so that the repository is only accessed once
  ```
- `NOT` in capitals emphasises a negative outcome that is easy to misread:
  ```
  when the upload has finished and the sync is still running, then the article is NOT reported as saved
  ```
- Variants of one scenario are numbered with a `#N` prefix:
  ```
  #1 clearing a stack also clears its nested stacks
  #2 clearing a stack also clears its nested stacks
  ```
  When scenarios are too intricate to fit a sentence, the name is only `#N`, and the KDoc describes
  the given/when/then in full.
- A parameterized test is named after what all of its rows have in common:
  ```
  email is validated as expected
  ```

-----

## Test body

### Given-When-Then structure

A test body consists of three sections — *given*, *when* and *then* — separated from each other with
one empty line. There are no empty lines within a section.

```kotlin
@Test
fun `given an article is in the cart, when it is removed, then the cart is empty`() {
    val article = Article(id = 1)
    createSut()
    sut.add(article)

    sut.remove(article)

    sut.items.shouldBeEmpty()
}
```

- Creating the subject is part of *given*.
- When *when* and *then* are one expression, like `shouldThrow { … }` or `shouldNotThrowAny { … }`,
  the test has two sections.
- In a complex test, a section may need empty lines within it. Then each section is titled with a comment line:
  ```kotlin
  // GIVEN
  val firstUpload = CompletableDeferred<Unit>()
  coEvery { uploader.upload(firstArticle) } coAnswers { firstUpload.await() }
  createSut()
  sut.publish(firstArticle)

  val secondUpload = CompletableDeferred<Unit>()
  coEvery { uploader.upload(secondArticle) } coAnswers { secondUpload.await() }
  sut.publish(secondArticle)

  // WHEN
  secondUpload.complete(Unit)
  firstUpload.complete(Unit)

  // THEN
  sut.data.lastPublished shouldBe secondArticle
  ```
- Variations of the titles:
  - `// WHEN #2`, `// WHEN #3` mark the numbered steps described in the test's KDoc.
  - `// WHEN-THEN #1`, `// WHEN-THEN #2` mark repeated rounds of acting and asserting.
  - `// "THEN"` with an explanation marks a *then* with no assertion, e.g. when the expectation is that nothing throws:
    ```kotlin
    // "THEN"
    // if any exception inside SUT is thrown, then 'runTest()' will re-throw it and the test will fail
    ```
- Inside a Turbine `.test { }` block, *when* and *then* are in the lambda, titled `// WHEN` and `// THEN`.

### Assertions per test

- Tests should strive to have one assertion.
- Several assertions are fine when together they describe one outcome — e.g. several properties of one result.
- A test may assert the assumptions it relies on, so that a broken setup can't make it pass by accident.

### Comments and named steps

- A step whose purpose isn't obvious gets a trailing comment:
  ```kotlin
  skipItems(1) // replayed value of 'StateFlow'
  onFinished.captured() // report that the upload has finished
  suspendCancellableCoroutine {} // suspends indefinitely
  ```
- A step where nothing happens on purpose is still written down, as a no-op call with a comment,
  so that it isn't mistaken for a forgotten one:
  ```kotlin
  onStarted.captured() // report that the upload has started
  doNothing() // don't report that it has finished yet
  ```
- Statements that form one step may be wrapped in a `run` block with a label that names the step:
  ```kotlin
  run emitArticleFromServer@{
      val article = Article(id = 1, title = "Hello")
      articleFlow.emit(article)
  }
  ```
- A `run` block without a label scopes the temporary values needed to build one value:
  ```kotlin
  every { settingsRepository.flowOfTheme } returns run {
      val theme = Theme(isDark = true)
      val preference = Preference.Ready(theme)
      MutableStateFlow(preference)
  }
  ```

### Cross-referencing tests

When a test relies on a fact that another test in the same file proves, the two are linked with labels:

- the test that proves the fact is marked with `@Test // ANCHOR:Label=N`;
- the line that relies on the fact is marked with `// REFERENCE:Label=N`.

```kotlin
@Test // ANCHOR:Label=1
fun `given the list is empty, when an article arrives, then the insertion animation starts`() { ... }

@Test
fun `when the insertion animation has finished, then the article is shown`() {
    ...
    // REFERENCE:Label=1
    verify(exactly = 1) {
        view.animateInsertion(...)
    }
    ...
}
```

Labels are numbered within a file.

### Documenting complex tests

A test with a complex *given*, *when* or *then* part gets a KDoc, in one of two forms.

- Prose, starting with "Tests that":
  ```kotlin
  /**
   * Tests that [ArticleCache] keeps articles in the order they were added,
   * so that the last added article is always the last one in the list.
   */
  ```
- Sections, when the scenario takes several steps. The steps are numbered, and links point to the code involved:
  ```kotlin
  /**
   * GIVEN
   * 1. [sut] is created
   * 2. there's a draft in [DraftStore]
   *
   * WHEN
   * [publishAction] is invoked with [ValidationResult.Valid]
   *
   * THEN
   * [ArticleViewModel.publish] is invoked.
   */
  ```

A decision that applies to many tests of a class, like using a real dependency instead of a mock,
is documented once, in the KDoc of the class.

-----

## Mocking

- Dependencies are declared as properties of the test class, with an explicit type,
  and are stubbed **in the body of each test**:
  ```kotlin
  val articleRepository: ArticleRepository = mockk()
  ```
  ```kotlin
  coEvery { articleRepository.getArticle(id = 1) } returns article
  createSut()
  ```
  Stubbing on the level of the class is limited to defaults that every test needs; tests override them when needed:
  ```kotlin
  val settingsRepository: SettingsRepository = mockk {
      every { flowOfTheme } returns MutableStateFlow(Theme(isDark = false))
  }
  ```
- A pure, cheap dependency is a real implementation rather than a mock, with a comment saying so:
  ```kotlin
  // real implementation, there's no need to have mock for formatting dates
  val dateFormatter = DateFormatter()
  ```
- A value whose content doesn't matter is a mock: `mockk<Article>()`.
  A real instance is used where identity matters:
  ```kotlin
  val emptyState = State.Empty // it's crucial to use real instance here so that the reference is the same
  ```

-----

## Parameterized tests

- Rows come from a `@MethodSource` function in the `companion object`.
- Each row is built with an infix function named after the expectation, so that the data reads as a table.
  Rows are numbered with aligned `/* #N */` comments, to find the row of a failing index at a glance:
  ```kotlin
  @JvmStatic
  fun data() = listOf(
      /* #0  */ "" shouldBeValid false,
      /* #1  */ "name" shouldBeValid false,
      /* #2  */ "name@domain" shouldBeValid false,
      /* #3  */ "name@domain.com" shouldBeValid true,
  )

  infix fun String.shouldBeValid(expectedValid: Boolean): Array<Any> =
      arrayOf(this, expectedValid)
  ```
  Other examples of names: `convertsTo`, `resultsIn`.
- A row of more than two values is a `TestCase` data class with named arguments, converted with `asArrayOfAnys()`:
  ```kotlin
  @JvmStatic
  fun data(): List<Array<Any>> = listOf(
      TestCase(
          text = "Hello world",
          maxLength = 5,
          expectedResult = "Hello",
      ),
  )
      .map { it.asArrayOfAnys() }

  data class TestCase(
      val text: String,
      val maxLength: Int,
      val expectedResult: String,
  )

  fun TestCase.asArrayOfAnys(): Array<Any> =
      arrayOf(text, maxLength, expectedResult)
  ```
- All rows share one test name, so the assertion is wrapped in `withClue` that names the input:
  ```kotlin
  withClue("Email \"$email\" should be valid=$expectedValid, but was valid=$isValid") {
      isValid shouldBe expectedValid
  }
  ```
- A case that doesn't fit the table gets its own test, with a comment saying why:
  ```kotlin
  @Test // special case not included in @ParameterizedTest to avoid complicating 'TestCase'
  ```

-----

## Concurrency tests

- A concurrency test is a `@RepeatedTest(10)`, because a race doesn't show up on every run.
- The shape of the test:
  1. a fixed thread pool, with a thread per participant;
  2. a `CyclicBarrier`, so that all threads enter the code under test at once;
  3. `AtomicInteger` counters of active and maximum concurrent executions — atomic, so that the numbers
     are accurate when the test fails;
  4. a pause within the critical section, to widen the window for a race;
  5. an assertion that the maximum is `1`.
  ```kotlin
  sut.withLock {
      val active = activeConcurrentExecutions.incrementAndGet()
      maxConcurrentExecutions.updateAndGet { max(it, active) }
      // widen the time window of the critical section to give other threads a bigger chance to enter it if it's not synchronized
      Thread.sleep(10)
      activeConcurrentExecutions.decrementAndGet()
  }
  ```
- The pause matches the lock: `delay()` under a `Mutex`, `Thread.sleep()` under a thread-owned lock like `ReentrantLock`.
- A counter that is decremented after a suspension point is decremented in `finally`,
  because the coroutine may be cancelled at that point.
- The executor or dispatcher is always closed, in `finally` or with `use`.
- A test that may hang on a blocked thread gets a JUnit timeout in `SEPARATE_THREAD` mode:
  ```kotlin
  @Timeout(value = 30, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
  class ArticleCacheTest
  ```
  Neither the timeout of `runTest` nor `@Timeout` in its default mode can stop a blocked thread.

-----

## Kotest assertions

- The infix style is preferred: `actual shouldBe expected`.
- Use `withClue("…") { }` to add context to the message of a failure when dealing with non-trivial cases.
