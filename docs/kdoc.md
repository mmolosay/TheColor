# KDoc

Conventions for KDoc. 
What is worth documenting, and how the documentation is written.

## Table of Contents

* [What KDoc is for](#what-kdoc-is-for)
  * [Contract, not implementation](#contract-not-implementation)
  * [KDoc or a comment](#kdoc-or-a-comment)
* [Style](#style)
* [References to other components](#references-to-other-components)
* [Links to external sources](#links-to-external-sources)
* [Interfaces and their implementations](#interfaces-and-their-implementations)

-----

## What KDoc is for

KDoc describes the **idea** of a component: why it exists, which problem it solves, and what it is
responsible for. That is the part a reader cannot recover from the code. The body shows what a
component *does*; its KDoc says what it is *for*.

Past the idea, a KDoc adds only what a caller cannot infer — the guarantees they may rely on, and the
obligations they must meet.

The default is none: a component gets KDoc only when it has an idea or a contract that the code does
not already convey.

### Contract, not implementation

The test for a KDoc:

> Would it have to change if the body were rewritten without changing the behaviour?

```kotlin
// no — names the data structure, and goes stale the moment the lookup changes
/**
 * Iterates over the articles and returns the first one whose 'id' matches.
 * Backed by a 'LinkedHashMap'.
 */
fun find(id: ArticleId): Article?

// yes — the caller relies on the result and on the complexity, not on how they are achieved
/**
 * Returns the article with the specified [id], or `null` if there is none.
 * The lookup is performed in constant time.
 */
fun find(id: ArticleId): Article?
```

The rule generalizes: what a caller relies on is contract; how it is achieved is not. What the
signature shows is contract too, but the code already states it.

What gets left out most often is the part nothing in the signature hints at. 

Guarantees: the meaning of `null` and of the degenerate cases, ordering, whether a result is a snapshot or a live view,
thread-safety and atomicity, whether a call blocks, what is thrown. 

Obligations: the order calls must be made in, what must not be done inside a block the component invokes, and who disposes of what.

### KDoc or a comment

- KDoc answers *"what is this for?"* and *"what may I rely on?"*. It is read by a caller, and it is part of the API.
- A `//` comment answers *"why is it written this way?"*. It is read by whoever edits the body.

A sentence that turns out to be about the body moves out of the KDoc and next to the line it explains:

```kotlin
return _items.toList() // new instance for a truly immutable copy
```

-----

## Style

KDoc follows the style of the Kotlin standard library and of other official JetBrains libraries:
a short summary first, details after, and no words that carry nothing. The syntax itself is
documented in [Kotlin's KDoc reference](https://kotlinlang.org/docs/kotlin-doc.html).

- The first sentence is a standalone summary — the IDE shows it on its own, without the rest of the
  block. It does not open with the name of the component, nor with "This class" / "This function".
- A fact appears exactly once. What a class KDoc states for all of its members, the members do not restate.
- A tag is written only when it adds something the summary does not. A summary that reads
  *"Returns the article with the specified [id], or `null` if there is none"* needs no `@return`.
- Properties declared in a constructor are documented with `@property` in the class KDoc, and only the
  ones that need explaining get a tag:
  ```kotlin
  /**
   * Text that a user is currently editing.
   *
   * @property isClearFeatureEnabled depicts whether the 'clear text' feature is enabled in this field.
   */
  data class DraftData(
      val text: String,
      val isClearFeatureEnabled: Boolean,
  )
  ```
- An obligation on the caller is "must" or "must not", never "should", so that a requirement is not
  read as advice: *"the [block] must not cancel or join coroutines"*.
- Names of features and of user-facing parts of the app are in single quotes, the same way as in
  test names: `'Color Input'`, `'auto save'`.

-----

## References to other components

A `[Reference]` couples the KDoc to a declaration that can quietly change meaning. The IDE reports a
reference that no longer *resolves*; nothing reports a reference that still resolves and is no longer *true*.
The rule that follows from this:

> A KDoc references only what its contract is already expressed in terms of.
> Everything else is named in prose, without a link.

What may be referenced:

- Members of the documented component, and other declarations in the same file.
  Whoever changes them is working in the same file.
- The types that appear in the signature, and the supertype being implemented.
  The compiler already keeps these in sync, so a reference adds no coupling that does not exist.
- A type from the same package that the contract is defined in terms of.

Clients of the component are not named at all, not even in prose: a contract does not depend on who
calls it. A type that lists the two repositories currently using it is wrong as soon as a third appears.
Describe what the type represents instead.

-----

## Links to external sources

A component that implements an established concept may provide a web link to a description of that concept, once.
The link answers *"what is this, in general?"*, so that the KDoc does not have to teach the concept and
can stay about this component's contract.

A link is added only when the concept is **established but not widely known**. A factory or an
observer needs none. A prism, a circuit breaker, a token bucket, reentrancy or a CRDT does.

Trusted sources, in order of preference:
  1. Wikipedia;
  2. official documentation of the language or library that coined the term;
  3. a specification or an RFC.

It is a Markdown link whose text names the concept:
```kotlin
/**
 * An implementation of a [lens](https://en.wikipedia.org/wiki/Bidirectional_transformation).
 */
 ```

A URL that answers a narrow implementation question — a platform bug, a behaviour change in an SDK
version — is a `//` comment next to the code it explains, not KDoc.

-----

## Interfaces and their implementations

- The contract lives on the interface. That is what callers depend on.
- An implementation documents only what it *adds*: where the data comes from, which guarantees it
  strengthens, which trade-offs it makes.
