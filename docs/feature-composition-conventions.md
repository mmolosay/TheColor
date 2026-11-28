# Feature composition conventions

This document states the rules for writing features with the
[feature composition](feature-composition.md) approach. Terms are defined in the design document.

The key words MUST, MUST NOT, SHOULD, SHOULD NOT and MAY are to be interpreted as described in
[RFC 2119](https://www.rfc-editor.org/rfc/rfc2119).

## Table of Contents

* [Rules](#rules)
  * [Feature or View](#feature-or-view)
  * [State](#state)
  * [Outcomes](#outcomes)
  * [Core](#core)
  * [Initial State](#initial-state)
  * [Operations](#operations)
  * [Transitions](#transitions)
  * [Reducers and agreements](#reducers-and-agreements)
  * [Feature](#feature)
  * [Hierarchies](#hierarchies)
  * [ViewData](#viewdata)
  * [View](#view)
  * [Kotlin and Dagger](#kotlin-and-dagger)
* [Names and layout](#names-and-layout)
  * [Names](#names)
  * [Parameter order](#parameter-order)
  * [Layout of a Core](#layout-of-a-core)

-----

## Rules

### Feature or View

* A piece of functionality MUST get a State, a Core and a Feature only where it has data of its own
  or something to decide about its children, since without either they would be empty. Otherwise it
  MUST be only a View, as a wrapper that only adds looks is.

### State

* A State MUST be immutable, so that it changes for its readers only through a commit.
* A State's `equals` MUST compare by value, because the Store and collectors tell a change by
  `equals`.
* A State MUST NOT hold anything that depends on where the feature sits, so that the feature is the
  same in every place. The parent gives such things to the child's Core as choices, or passes them
  to the child's View.

### Outcomes

* A parent MUST NOT pass a child's Outcome up. What it cannot decide, it hands up as an Outcome of
  its own type, in its own terms, so that its parent doesn't change when its children do.
  ColorEditor turns `ColorInputHexOutcome.Submitted` into
  `ColorEditorOutcome.ColorSubmitted(color)`.

### Core

* A Core's functions MUST be pure, and a Core MUST NOT hold anything with side effects, such as a
  scope, a Store or a repository, because Transitions and Reducers can run more than once per update
  and only the run that commits takes effect.
* Each place MUST have its own Core, created by the parent's Core. A Core MUST NOT be shared between
  places, so that a parent's choices apply to one place only.
* A dependency MUST live where this list puts it.
  * The same in every place, with no side effects (`ColorConverter`). A parameter of the Core's
    constructor, supplied by dependency injection.
  * Chosen by the parent for one place (`TextFormatter`). An `@Assisted` parameter of the Core,
    supplied by the parent's Core, as in `textFieldFactory.create(formatter = HexFormatter)`.
  * With side effects, used only by work (`ColorRepository`). A parameter of the Feature's
    constructor, supplied by dependency injection.
  * Chosen once, only to start (the first color). An argument of `initialState(…)` or of the State's
    constructor, supplied by the parent's Core, as in `hexCore.initialState(color)`.

### Initial State

* A feature's initial State SHOULD be created by its Core's `initialState(…)` when it is built from
  values that the Core's dependencies provide, since those dependencies live in the Core. Otherwise
  it MAY be created with the State's constructor, by the parent or, for a screen ViewModel's own
  State, where the screen ViewModel gives the root Store its initial value.

### Operations

* An Operation MUST NOT return an Outcome, because its caller is the feature itself or its parent,
  and an Outcome would go to that parent.
* Outside the feature, its State MUST be read and changed only through its Operations, so that the
  child's rules apply to every read and write from outside.

### Transitions

* A feature's Transitions MUST NOT be called by anything but its own Feature, inside `store.update`,
  because only the feature's own Store carries its Change and Outcome to its parent's Reducer.
* A Transition MUST be built on the feature's Operations, so that the feature's own writes and its
  parent's writes follow the same rules.
* When several children change for one reason, the change MUST be one Transition of their parent,
  which sets each child through its Operations, so that no committed State holds one change without
  the other.

### Reducers and agreements

A Reducer runs on only one of the four paths that change a child's State
([Reducers](feature-composition.md#reducers)), so a rule kept only in a Reducer is skipped on the
others.

* A Reducer SHOULD correct the State it gets, for example by bringing the other children into
  agreement with the changed one, rather than undo the child's Change, since it gets only the
  child's new State, not the old one.
* A rule about one child's data MUST be given to the child's Core as a choice, so that the child
  applies it on every write. ColorInputHex gives TextField `HexFormatter`.
* A rule between children MUST be one Operation on the parent's State, and every path that writes
  those children MUST leave the rule holding, by ending with that Operation or with a write that
  keeps it (advantage 4). ColorEditor's `withInputColor(lens, color)` keeps all its inputs on one
  color while the edited input keeps its own State. `onHex`, `onRgb` and `onHsv` end with it, and
  `withColor` sets every input from one color. Home keeps any session on the editor's color by
  ending each write with `withoutStaleSession()`, or with `withSession(color)`, which sets both to
  one color.

### Feature

* Every Feature MUST take a scope, even one that runs no work, so that gaining work later doesn't
  change its parents' `create` calls.
* A parent's Feature MUST offer its children's Features only to its own View. It MUST NOT call
  their Actions, because each Action is an update of its own, so the parent's change and the
  child's would commit apart. It MUST NOT read their `viewData` or start their work either. It
  reads and changes a child through the child's Operations, inside its own updates, and the child's
  Feature starts the work that its State calls for.
* An Action MUST make at most one `store.update`, so that it commits once.
* The Store MUST be private, and the State MUST NOT be exposed, so that a View reads only its
  ViewData and writes only through Actions.
* The class body MUST start with the properties, `viewData` among them, then the children's
  Features, then the `init` blocks, so that code run during construction, such as a child's `init`,
  reads only what is already initialized. The Actions follow, in the order of their Transitions in
  the Core.

### Hierarchies

* A child whose State the parent's functions read or write MUST be in the parent's hierarchy,
  because the parent's functions run inside one update, and one update commits to one root Store.
  Its State is then a field of the parent's State, and its Feature gets a Store view.
* A child whose State the parent's functions never read or write MAY be at the top of a hierarchy of
  its own.

### ViewData

* A feature whose View shows anything from its own State MUST have a ViewData, even one that repeats
  the State, so that every such View reads the same way. A feature whose View shows only its
  children's Views (ColorCenter) MUST NOT have one. It would be empty, and its collector would wake
  for nothing.

### View

* A View MUST show only its children's Views, so that a feature can change its children without its
  parent's View changing. For a child that has no View, it shows that child's children's Views, as
  Home's View shows `colorEditor.hex`, `colorEditor.rgb` and `colorEditor.hsv`.
* A parent's View MUST NOT read a child's ViewData, so that a child can change its ViewData without
  its parent's View changing.
* Code between the Store and the View MUST NOT combine flows or keep a value of its own, so that
  advantage 2 reaches the View.

### Kotlin and Dagger

* For a feature with no Outcomes, `XFeature` MUST take `Store<S, *>`, not `Store<S, Nothing>`,
  because Kotlin compiles `Store<S, Nothing>` to a raw `Store` and Dagger's generated Java writes
  `Store<S, Void>`, so the two `create` signatures clash.

-----

## Names and layout

### Names

For a feature `X`:

* **State, Lenses and Outcome.** `XState`, `XStateLenses` (imported as `StateLenses`) and
  `XOutcome`, in `XState.kt`, in that order.
* **Core.** `XCore` and `XCore.Factory`, in `XCore.kt`.
* **ViewData.** `XViewData`, built by `XCore.toViewData()`, in `XViewData.kt`.
* **Feature.** `XFeature` and `XFeature.Factory`, in `XFeature.kt`.
* **View.** `X(feature: XFeature, …)`, in `X.kt`. A screen's View takes its screen ViewModel as
  `viewModel`.
* **Screen ViewModel.** `XViewModel`, with `XCore` and `XState`, in `XViewModel.kt`.
* **Getter.** The fact's name, as in `color()` and `isInitialized()`.
* **Setter.** `with<Fact>(…)` or `without<Fact>()`.
* **Transition.** The Action's name, as in `submit()` and `clearText()`.
* **Reducer.** `on<Child>`, as in `onHex`.
* **Child.** Its role in the parent, as in `hex`, `textField`, `r` and `selectedSwatch`. That name
  is used for the State field, the Lens, the Feature property and the Reducer (`onHex`). The child's
  Core is `<child>Core`, as in `hexCore`, `rCore` and `selectedSwatchCore`. A Feature's own Core is
  `core`.

### Parameter order

* A Core's constructor takes its children's Core factories first, then dependencies from injection,
  then the parent's assisted choices.
* A Feature's constructor and its `Factory` take its Core, its Store and its scope, in that order,
  then its children's Feature factories, then other dependencies from injection.

### Layout of a Core

A Core lays out its members in this order:

1. Child Cores, as properties.
2. `initialState`, if the Core has one, its overloads from the fewest parameters to the most.
3. `// region Getters`, with functions on the State that return something other than a State or a
   Change.
4. `// region Setters`, with functions on the State that return a State, private ones included.
5. `// region Transitions`, with functions on the State that return a Change, Reducers included.
6. Helpers that are not functions on the State.
7. `Factory`.

Private top-level declarations follow the class. Each region ends with `// endregion` and is present
whenever it has at least one function.

Within each region, functions go by their main field, in the order the State declares its fields.
A getter's main field is the field its result is about. A setter's or a Transition's main field is
the field it writes, and a Reducer's is its child's field. A function with several main fields goes
after the rest. Ties go alphabetically.

```kotlin
class TextFieldCore @AssistedInject constructor(
    @Assisted private val formatter: TextFormatter,
) {

    fun initialState(text: String): TextFieldState

    // region Getters

    fun TextFieldState.toViewData(): TextFieldViewData  // several fields

    // endregion

    // region Setters

    fun TextFieldState.withText(text: String): TextFieldState  // text

    // endregion

    // region Transitions

    fun TextFieldState.clearText(): Change<…>                   // text
    fun TextFieldState.enterText(…): Change<…>                  // text
    fun TextFieldState.applySelectAllTextOnFocus(…): Change<…>  // selectAllTextOnFocus

    // endregion

    @AssistedFactory
    fun interface Factory {
        fun create(formatter: TextFormatter): TextFieldCore
    }
}
```
