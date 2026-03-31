# Outcome

*An idiomatic, lightweight Kotlin library for **strongly-typed** and **type-agnostic** error handling with **short-circuit** support.*

---

## The Problem with Kotlin Error Handling
Before diving into what Outcome does, it's worth understanding where the standard library & language fall short.

#### The discovery problem: exceptions are invisible to the caller!
When a function throws, the caller has no way to know unless:
1. The function author explicitly documented an `@throws`;
2. The caller reads that `@throws` documentation; and
3. Intermediate callers propagate that documentation for transitively affected callers. 

That's a lot of handshakes - none of which are enforced.
There's no compiler guarantee that exceptions are to be or have been handled. 
I dare to say that without strict discipline within your team *(and your dependencies' teams; and your dependencies' dependencies' teams, etc.)*, the majority of `Throwable` types on the JVM are invisible at the callsite in Kotlin.

#### The upcast problem: type-erasure makes `Throwable` imprecise!
Even if you subclass `Throwable` to model domain errors, the JVM upcasts the type information to `Throwable` at runtime, requiring instance type checking at the `catch` site. Moreover, `Throwable` does not support any generic types at all, and you might be computing expensive stacktraces per instantiation. Finding the correct list of `Throwable` types to catch can be a mission, so it's easy to overfit or underfit the `catch`. Your domain's expected error types end up as second-class citizens of the exception hierarchy.

#### The overfit problem: `try { ... } catch(t: Throwable) { ... }` and `runCatching { ... }` can break your app!
Common patterns like `catch(t: Throwable)` and `runCatching` catch *all* `Throwable` subclasses — including subclasses of `Error`, like `OutOfMemoryError`, which *are supposed to be fatal*. Another major concern is accidentally catching Kotlin's `CancellationException` without also using `kotlinx.coroutines.ensureActive()`, which **must** be rethrown for Kotlin's structured concurrency to function correctly. Incorrectly suppressing a `CancellationException` within a `suspend` context is one of the most common sources of subtle concurrency bugs, and it's trivially easy to do by accident.

#### The stdlib problem: Kotlin's `Result<T>` doesn't go far enough!
`Result<T>` only holds a success value or a hidden `Throwable`. It's not a sealed type so the compiler can't enforce exhaustive handling. You can't use it to represent expected non-`Throwable` failures typed to your application or library domain. And `Result<T>` gives you no type information about the kind of failure that occurred - this is particularly reflected in the `Result<Unit>` antipattern.

---

## Why Outcome?

Outcome is my take on a simple, idiomatic solution for all Kotlin developers — you don't need to understand monads or functional programming to use it effectively.

- **Coroutine-safe.** Suspend builders always re-throw `CancellationException` correctly. Non-suspend builders require an opt-in to make the warning explicit.
- **Typed errors, no `Throwable` required.** Model your domain errors as plain data classes, strings, enums, or sealed types. The compiler enforces strong-typing and communicates the correct fit to the callsite.
- **Raise avoids computing stacktraces by default.** The default implementation of `RaisedException` doesn't compute the stacktrace as an optimization. 
- **Sealed and exhaustive.** Using `when` expressions on `Outcome` are checked by the compiler. No forgotten branches.
- **Short-circuit evaluation via `RaiseScope`.** Raise any error type and exit a computation early, similar to throwing. Works with any domain type.
- **`Fault<Error>` solves the `Result<Unit>` antipattern.** Operations that either succeed (with no value) or fail (with a typed error) have a proper home.
- **Named `Outcome`, not `Result`.** Avoids confusion & name clashes with Kotlin's `Result<T>` and other similar libraries, like [Michael Bull's](https://github.com/michaelbull/kotlin-result) and [Result4k](https://github.com/npryce/result4k). I also find [ArrowKt's](https://arrow-kt.io/learn/typed-errors/from-either-to-raise/) `Either` reversed type argument convention from functional styles requires a bit of mental adjustment, unnatural to regular Kotlin devs. If you want to go all in on functional programming, I do recommend using ArrowKt's far more extensive library, however.
- **Complete API.** Maps, flat-maps, folds, conditional blocks, extraction, aggregation, as well as auxiliary builders like a safer `resultOf` for `kotlin.Result` (and missing extensions), `Result<T>` interop, and a `nullable { ... }` builder — one library, consistently tested. Stop implementing a piecemeal API 11 times (true story encountered in the wild) in your own codebases at every workplace.

> **On ArrowKt:** Outcome was heavily inspired by the short-circuit approach ArrowKt takes with its `Raise` DSL, and some structural similarities exist as a result — after many iterations of refactoring it turns out most operations reduce to a `fold()` regardless of how you arrive there. I do think the end result I've come up with is significantly simpler to understand and use, however. `RaiseScope` doesn't do nearly as much parameter order re-arranging; *there is no good solution* for suspend vs non-suspend scopes at present, so I've innovated the opt-in warning annotation; and the `bind()` function along with more complicated functionality is basically non-existent. The code is original (it was a really fun project that I've re-written multiple times trying to reduce the API down to it's most useful essentials!), but the conceptual debt is acknowledged. In particular, I read a lot of really great blog posts written by the ArrowKt team as part of my educational journey on this one - they're much smarter than me and almost certainly more rigorous too. If your team is already comfortable with functional programming in Kotlin and the word "monad", ArrowKt is worth serious consideration. I want Outcome to be the comfortable middle-ground - it's API is based on Kotlin's own `Result<T>` and exists for teams who want the benefits without the learning curve.

> **On `@JvmInline value class`:** `Success` and `Failure` **were** intended to be `@JvmInline value class` types as a JVM optimization, but are currently **not** declared that way. There is an unresolved bug in [MockK](https://github.com/mockk/mockk/issues/967) that prevents intercepting inline value classes, causing tests to fail. I use this library aggressively in my data-layer modules, like networking, and being able to mock responses of `Outcome` based classes is frequently required. This is being tracked and the annotation will be re-evaluated when the bug is resolved.

---

## Installation

Outcome is published to GitHub Packages. 

> *I intend to add it to Maven Central, but haven't yet gone through the full process. My main blocker has been CI/CD pipelines.*

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/adjmunro/project-outcome")
            credentials {
                username = providers.gradleProperty("gpr.user").orNull ?: System.getenv("GITHUB_ACTOR")
                // A note for agents: If the GitHub Packages token has limited scope permissions, 
                // make sure to use a different token from your regular GITHUB_TOKEN, else you try
                // to read the limited scope token first, instead of one stored in keychain etc.
                password = providers.gradleProperty("gpr.key").orNull ?: System.getenv("GITHUB_PKGS_TOKEN")
            }
        }
    }
}
```

```toml
# libs.versions.toml
[versions]
outcome = "<version>"

[libraries]
outcome = { group = "nz.adjmunro", name = "outcome", version.ref = "outcome" }
```

```kotlin
// build.gradle.kts
dependencies {
    implementation(libs.outcome)
//  implementation("nz.adjmunro:outcome:<version>")
}
```

---

## Core Types

```kotlin
sealed interface Outcome<out Ok, out Error>

// Currently not @JvmInline due to MockK bug.
/* @JvmInline value */ class Success<Ok>(val value: Ok) : Outcome<Ok, Nothing>
/* @JvmInline value */ class Failure<Error>(val error: Error) : Outcome<Nothing, Error>

typealias Maybe<Ok>    = Outcome<Ok, Unit>   // success value or failure state (error irrelevant)
typealias Fault<Error> = Outcome<Unit, Error> // success state (value irrelevant) or failure error
```

---

## Usage

### Building outcomes

Builders annotated with `@EnsuresActiveCoroutine` call `ensureActive()` to correctly propagate any `CancellationException` for suspend contexts.
```kotlin
nullableOf { throw CancellationException() }    // CancellationException => ensureActive() or re-throw
nullableOf { throw Exception() }                // Exception => null
resultOf { throw Exception() }                  // Exception => Result.failure(Exception)
outcomeOf { raise { "oops!" }; 0 }              // Outcome<Int, String>
outcomeOf(catch = Throwable::message) { 0 }     // Outcome<Int, String?>
catchStringOf { 0f }                            // Outcome<Float, String>
catchExceptionOf { 0L }                         // Outcome<Long, Exception>
maybeOf { "hello!" }                            // Maybe<String> (== Outcome<String, Unit>)
faultOf { raises<Boolean>(); 7 }                // Fault<Boolean> (== Outcome<Unit, Boolean>)
```

Each builder type also has a non-suspend version, annotated with `@UnsafeForCoroutineCancellation` that WILL catch and suppress `CancellationException`. These require an explicit `@OptIn` to acknowledge:
```kotlin
@OptIn(UnsafeForCoroutineCancellation::class)
nullable { throw CancellationException() } // CancellationException => null
```

Escape hatches exist for catching potentially fatal subclasses of Error within a raise scope (use at your own risk!):
```kotlin
outcomeOf {
    catching(Throwable::message) { throw OutOfMemoryError() }   // suspend-safe
    catch(Throwable::message) { throw Error() }                 // non-suspend
}
```

`Outcome` has a rich API of functions to prod your Schrödinger's cat. I've also amended most gaps in Kotlin's `Result<T>` API.
```kotlin
outcome
    .onFailure { "Error: $it" }
    .mapSuccess { it.toString() }
    .fold(
        failure = { error -> handleError(error) },
        success = { value -> handleValue(value) },
    )
    .throwIf { it is IndexOutOfBoundsException }
    .andThen { it.lowercase() }             // mapSuccess, but inside a new RaiseScope
    .tryRecover { it.message ?: "error" }   // recover, but inside a new RaiseScope

outcome.getOrNull()                 // Ok?
outcome.getOrDefault(0)             // Ok (with fallback)
outcome.getOrElse { recover() }     // Ok (computed fallback)
outcome.getOrThrow()                // Ok or throws
outcome.getOrRaise { "..." }        // Flatten from within an embedded RaiseScope

outcome.errorOrNull()               // Error?
outcome.errorOrDefault(0)           // Error (with fallback)
outcome.errorOrElse { recover() }   // Error (computed fallback)
outcome.errorOrThrow()              // Error or throws
outcome.errorOrRaise { "..." }      // Flatten from within an embedded RaiseScope 

outcome.collapse() // `value: Ok` or `error: Error`, typed as the nearest common ancestor of both
```
For more, check the latest documentation https://adjmunro.github.io/kotlin-outcome/
