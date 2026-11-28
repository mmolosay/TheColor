# Feature composition

This document describes the software design of the feature composition approach. It covers the
design's components, how they work together, and what the design guarantees and costs.

The rules for implementing features with this approach are in
[Feature composition conventions](feature-composition-conventions.md).

## Table of Contents

* [Overview](#overview)
  * [Components](#components)
  * [Three trees](#three-trees)
* [Components in detail](#components-in-detail)
  * [State and Outcome](#state-and-outcome)
  * [Core](#core)
  * [Initial State](#initial-state)
  * [Operations](#operations)
  * [Transitions](#transitions)
  * [Reducers](#reducers)
  * [Feature](#feature)
  * [ViewData](#viewdata)
  * [Screen ViewModel](#screen-viewmodel)
* [Example: a submission](#example-a-submission)
* [Trade-offs](#trade-offs)
  * [Advantages](#advantages)
  * [Disadvantages](#disadvantages)

-----

## Overview

Feature composition is an approach to building a screen out of individual, reusable features.
Each feature is a piece of functionality, with its own logic.
A feature can be built out of other features, which it creates, reacts to and changes.
Its purpose is to keep several features in strict agreement on their data, in every committed
State, while each feature stays reusable under any parent. 
For example, ColorEditor's HEX, RGB and HSV inputs must always hold one color.

Features compose into a tree.
If feature A employs feature B in it, then B is considered a child of A, and A is considered
a parent of B. The parent is either another feature or a screen ViewModel. The same feature
works under any parent. Home's tree:

```
HomeViewModel
├── colorEditor: ColorEditor
│   ├── hex: ColorInputHex
│   │   └── textField: TextField
│   ├── rgb: ColorInputRgb
│   │   └── r, g, b: TextField
│   └── hsv: ColorInputHsv
├── colorInputGroup: ColorInputGroup
├── colorCenter: ColorCenter
│   ├── details: ColorDetails
│   └── scheme: ColorScheme
└── selectedSwatch: ColorDetails
```

Features whose States must agree with each other form a hierarchy. All States of a hierarchy live in
one root Store, which is created at the top of the hierarchy. Every feature below the top works with
its part of that State through a view onto this Store. One screen can have several hierarchies.

### Components

A feature `X` is made of these components:

* **State** (`XState`). The feature's data. Stored in Store. Immutable.
* **Outcome** (`XOutcome`). A description of a State change. Propagated up for the parent to react
  to the child's State change.
* **Change** (`Change<XState, XOutcome>`). A new State and at most one Outcome describing it.
* **Core** (`XCore`). The feature's rules, pure functions that operate on its State. It holds the
  dependencies they use, the parent's choices for that place, and the children's Cores.
* **Operation** (function of `XCore` on `XState`). Reads a fact or returns a new State. Used by the
  feature itself and by its parent.
* **Transition** (function of `XCore` on `XState`). Returns the Change for one Action, or for one
  result of the feature's work. Used only by `XFeature`.
* **Reducer** (`on<Child>(…)`, function of the parent's Core). Runs inside a child's update and
  returns the parent's Change.
* **ViewData** (`XViewData`, built by `XCore.toViewData()`). Immutable model of what the View
  presents.
* **Feature** (`XFeature`). The feature running in one place. It holds its Store and Core,
  runs the Transition behind each Action, runs its work, and creates its children's Features.
* **View** (`@Composable fun X(feature: XFeature, …)`). Presents the ViewData and calls the
  Feature's Actions.

### Three trees

Each component of a parent uses only the same component of its child. A Core creates its children's
Cores, Operations call the children's Operations, and a Feature creates its children's Features. So
the States, the Cores and the Features each form a tree that mirrors the tree of features.

* **State.** Within a hierarchy, a child's State is a field of the parent's State, so a child's
  Change and the parent's reaction to it commit together.
* **Core.** The parent's pure functions read and change the child's State through the child's Core.
  They run where no Feature is at hand, before the root Store exists and inside updates.
* **Feature.** Only the parent's Feature has what a child's Feature is created with. It has the
  child's Core, its own scope to pass on, and the Store to give the child, which is either a Store
  view that carries the parent's Reducer or the root Store of the child's own hierarchy.

Only the parent knows where its child's State sits, how it reacts to the child, and what it chooses
for the child's place. Flattening any of the trees would move that knowledge up to the screen
ViewModel.

-----

## Components in detail

The examples are TheColor's features. Excerpts omit imports and unrelated members.

### State and Outcome

```kotlin
data class ColorInputHexState(
    val textField: TextFieldState,
    val submission: ColorInputSubmission?,
)

object ColorInputHexStateLenses {
    val textField = Lens<ColorInputHexState, TextFieldState>(
        get = { s -> s.textField },
        set = { s, v -> s.copy(textField = v) },
    )
}

sealed interface ColorInputHexOutcome {
    data object Submitted : ColorInputHexOutcome
}
```

### Core

```kotlin
class ColorInputHexCore @AssistedInject constructor(
    textFieldFactory: TextFieldCore.Factory,
    private val colorConverter: ColorConverter,
    private val colorInputMapper: ColorInputMapper,
    private val colorInputValidator: ColorInputValidator,
) {
    val textFieldCore = textFieldFactory.create(
        formatter = HexFormatter,
    )
    …
    @AssistedFactory
    fun interface Factory {
        fun create(): ColorInputHexCore
    }
}
```

* The parent's Core creates one Core per place through the child's `Factory`. The parent's Feature
  gives that same object to the child's Feature (`core.textFieldCore`), so the parent's calls into
  the child and the child's own Transitions use the same dependencies and choices.
* Dependency injection supplies each Core's dependencies, its children's factories included, as its
  factory runs. A parent supplies only its choices, so it never names what its descendants need.

### Initial State

A feature's initial State is built by its Core's factory function `initialState(…)`, from what the
parent gives and from values that the Core's dependencies provide, or it is created with the State's
constructor, by the parent or where the root Store gets its initial value. The constructor suits a
State that needs nothing from the Core's dependencies.

```kotlin
// in ColorInputHexCore
fun initialState(color: Color?): ColorInputHexState {
    val text = color?.toColorInput()?.string.orEmpty()
    val textField = textFieldCore.initialState(text = text)
    return ColorInputHexState(textField = textField, submission = null)
}
```

### Operations

```kotlin
// in ColorInputHexCore
fun ColorInputHexState.color(): Color? {
    val input = this.colorInput()
    val result = with(colorInputValidator) { input.validate() }
    return result.getColorOrNull()
}

fun ColorInputHexState.withColor(color: Color?): ColorInputHexState {
    val text = color?.toColorInput()?.string.orEmpty()
    val textField = with(textFieldCore) { textField.withText(text) }
    if (textField == this.textField) return this
    return this.copy(textField = textField)
}
```

An Operation that returns a fact is a getter (`color()`), and one that returns a new State is a
setter (`withColor(…)`).

### Transitions

```kotlin
// in ColorInputHexCore
fun ColorInputHexState.submit(): Change<ColorInputHexState, ColorInputHexOutcome> {
    val submission = ColorInputSubmission.Pending(input = this.colorInput())
    val state = this.copy(submission = submission)
    return Change(state, ColorInputHexOutcome.Submitted)
}
```

A Transition returns an Outcome when the parent has something to decide, as `submit()` does.

### Reducers

```kotlin
// in ColorEditorCore
fun ColorEditorState.onHex(
    outcome: ColorInputHexOutcome?,
): Change<ColorEditorState, ColorEditorOutcome> {
    val color = with(hexCore) { hex.color() }
    val state = this.withInputColor(StateLenses.hex, color)
    return when (outcome) {
        null -> Change(state)
        ColorInputHexOutcome.Submitted -> state.onInputSubmitted(color)
    }
}
```

A Reducer runs inside the child's update. It gets the parent's State with the child's new State
already set in it, and the child's Outcome or `null`. An Outcome in the Change it returns goes to
the Reducer above, in the same update.

A child's State changes on four paths:

1. The child's own Change. The parent's Reducer runs.
2. The parent's initial State.
3. The parent's Operations, called from above.
4. The parent's Transitions.

The Reducer runs only on the first, since only an update through the child's Store view runs it.
On the others, the child's State is written as part of the parent's, either before the root Store
exists or in an update through the parent's Store or one above it.

### Feature

```kotlin
class ColorInputHexFeature @AssistedInject constructor(
    @Assisted private val core: ColorInputHexCore,
    @Assisted private val store: Store<ColorInputHexState, ColorInputHexOutcome>,
    @Assisted scope: CoroutineScope,
    textFieldFactory: TextFieldFeature.Factory,
) {

    val viewData: StateFlow<ColorInputHexViewData> =
        store.mapState { state -> with(core) { state.toViewData() } }

    val textField: TextFieldFeature = textFieldFactory.create(
        core = core.textFieldCore,
        store = store.slice(StateLenses.textField) { state -> Change(state) },
        scope = scope,
    )

    fun submit() {
        store.update { state ->
            with(core) { state.submit() }
        }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(…): ColorInputHexFeature
    }
}
```

### ViewData

```kotlin
data class ColorInputHsvViewData(
    val color: Color.Hsv,
)

// in ColorInputHsvCore
fun ColorInputHsvState.toViewData(): ColorInputHsvViewData =
    ColorInputHsvViewData(
        color = this.picked().orDefault(),
    )
```

ViewData is everything the View presents, already worked out, so the View derives nothing.

### Screen ViewModel

```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    coreFactory: HomeCore.Factory,
    colorEditorFactory: ColorEditorFeature.Factory,
    …
) : ViewModel() {

    private val core: HomeCore = coreFactory.create()
    private val store = Store(initialState = core.initialState())

    val viewData: StateFlow<HomeViewData?> =
        store.mapState { state -> with(core) { state.toViewData() } }

    val colorEditor: ColorEditorFeature = colorEditorFactory.create(
        core = core.colorEditorCore,
        store = store.slice(StateLenses.colorEditor) { state, outcome: ColorEditorOutcome? ->
            with(core) { state.onColorEditor(outcome) }
        },
        scope = viewModelScope,
    )
    …
}
```

The screen ViewModel plays the Feature's role at the root. Unlike a Feature, it creates its own Core
and the root Store of its hierarchy.

Creation runs top-down in four passes, each needing only the ones before it:

1. **Cores.** The root Core, then each Core below it, created by its parent's Core.
2. **Initial State.** The hierarchy's initial State, with each child's State in it.
3. **The root Store.** `Store(initialState = …)`, for the hierarchy the screen ViewModel is at the
   top of.
4. **Features.** Each Feature creates its children's Features, then starts its own work in `init`.

-----

## Example: a submission

A submission shows a child handing a decision up the tree and getting an answer back down.

1. ColorInputHex's `submit()` records `Pending(input)` and hands up `Submitted`.
2. ColorEditor's Reducer `onHex` gets `Submitted`. It rejects an invalid color itself with
   `withSubmissionRejected()`. It can't decide on a valid color, so it hands it up as
   `ColorSubmitted(color)`, an Outcome of its own.
3. Home's Reducer `onColorEditor` gets `ColorSubmitted` and accepts it through ColorEditor's
   Operation `withSubmissionAccepted()`.

All three steps commit as one update (advantage 1).

-----

## Trade-offs

### Advantages

1. **Atomic commit.** An update through any Store runs its Transition and then the Reducers from
   that Store up to the root Store, and commits the result to the root Store once. No reader sees a
   child's Change without its parents' reactions to it, and keeping features in step takes no locks
   and no collectors.
2. **No lag.** The `value` of every Store, and of every `StateFlow` built from one with `mapState`,
   is computed from the root Store's current State at the moment it is read. Once `update` returns,
   that `value` reflects the commit or a later one.
3. **No-op updates are invisible.** A Change with an equal State and no Outcome, from a Transition
   or a Reducer, runs no Reducer above it, so the update commits nothing.
4. **Agreements hold in every commit.** A rule between children holds in every State the root Store
   commits, the initial one included. Holds while parents follow the agreement rules.
5. **Pure Cores.** Every decision is a pure function of a Core, which describes how instances of
   the feature's State can change or what can be inferred from it. Holds while Cores follow the Core
   rules.
6. **Changes stay local.** A feature can gain a dependency, a child or an Action without any change
   to its parents. Holds while features follow the conventions.

### Disadvantages

1. Boilerplate. A feature has a State, a Core, a Feature, a ViewData and a View. Each child costs a
   Lens. Every call into a Core from outside it is wrapped in `with(core) { … }`.
2. Conventions that the compiler does not check. A side effect added to a Transition or a Reducer
   passes the tests and fails only under contention.
3. All updates in a hierarchy contend on its root Store's compare-and-set. Under contention, the
   whole chain from the Transition to the top Reducer runs again.
4. Every commit costs each active collector a Lens read and an `equals`. `toViewData()` runs on
   every read of `viewData.value` and once per collector on every change. Nothing caches it.
5. Children are fixed. Each child is created with its parent and lives as long as it does. There is
   no support for optional and/or dynamic children.
6. `Store` and the `StateFlow` that `mapState` returns implement `StateFlow`, which
   kotlinx.coroutines marks `ExperimentalForInheritanceCoroutinesApi`. A member that a later release
   adds to `StateFlow` must be added to them too.
