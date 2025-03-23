# Knomadic Kotlin
---
The goal of this library (besides satisfying my own curiosity & requirements) is to find a middle ground. Something simpler and more Kotlin idiomatic than ArrowKt, for regular, non-functional programmers to get their head around, while still supplying some great features not present in other implementations. I'm only posing as a functional programmer, so take my implementation with a large grain of salt.

## Notable Features:
---
TL;DR: idiomatic kotlin; `sealed` & `inline`; `<Error: Any>`; short-circuit via `throw`; definitely not-null; easy `fetch`.

- All my `BinaryResult` types are backed by a `RasieScope`, which means you can *short-circuit* the scope, by *throwing `Any` error type!*.
    - Disclaimer: `RaiseScope` was heavily influenced by ArrowKt, as well as blogposts which I can only imagine were also written by their team. I tried to work out how to short-circuit myself, but it just turned out pretty much the same. (Please don't sue me. LMK if I need to sort out the licenses better, but raise and the inline hacks should be the only extremely similar parts -- I even think my implementation makes it a bit easier to understand the logic flow, that bind() stuff and the parameter order swapping when injecting the scope was confusing AF!)
    - To reiterate, `RaiseScope` allows you to `throw` generic type `<Error: Any>`, not just `Throwable`.
- `Outcome`, `Maybe`, `Faulty` are all `sealed interfaces` with `value class` children, providing a lightweight wrapper *and* exhaustive states.
- `Outcome` - My solution to Kotlin's `Result` problem. 
  - Holds *both* the data and error types, unlike Kotlin's `Result`.
  - Uses `RaiseScope`.
  - Avoids name clash confusion unlike most other result libraries.
  - I generally choose to rethrow the catch block by default. 
    - Firstly, in a Raise, if your error type isn't Throwable then this is possibly a real exception that should be handled or mapped to your domain. 
    - Secondly, because I personally, find that `catch` lambda before the scope lambda a step outside the norm for idiomatic kotlin code (although less so now I've written it hundreds of times, and you probably will get used to it to0. And when you do, try jumping to a proper library like ArrowKt!). Therefore, providing a default value is imperative to making the context runners easy to use, read, and understand for newcomers. Also, directly mapping to `catch = ::Failure` etc, forces the Error type to `Throwable` so it can't be helped `--__(-_-;)__--`.
  - I didn't want to call it `Either`, since this probably is a poor monad implementation and I don't want to confuse people (or name clash) if/when they advance to ArrowKt and need to swap their type arguments around.
- `Maybe` - Effectively Java's `Option` class, or a nullable type. 
  - Holds some data or nothing.
  - Uses `RaiseScope`.
- `Faulty` - The opposite of `Maybe`.
  - Holds nothing or some error.
  - Uses `RaiseScope`.
  - AFAIK, an inverse-option type is unique to this library. But I seriously think returning `Result<Unit>` for success in an anti-pattern of no value.
- Taking the advice "to keep your nulls at the exterior surface of your program, not allowing them into your program's core domain" to heart, all my generic types extend `Any` and explicitly *do not* support nullable types.
  - I actually, think this definitely not null restriction adds interesting constraints that force your to rethink and write better, type-safe code.
  - Also, the functions are much easier to write without null considerations (believe me, I tried that too, 2-3 iterations ago. Not difficult, just ...messy?).
- `Fetch` represents 3 async states: `NotStarted`, `InProgress`, and `Finished`.
  - It has it's own `FlowCollector` runner which automatically produces `InProgress` when called, and wraps the `return` / tail in `Finished`, completely absolving you of `Fetch` state management.
  - Following the single-responsibility principle, the intention here is to wrap a `BinaryResult` in a `Fetch`, with each providing it's own behaviour.
  - Basically this was made because we inherited a stupid quasi `Fetch`/`Result` at work that doubled up all the success/failure function maintenance (and also I was curious about making a custom `FlowCollector` context runner).

### Why suspend?
---
At first I thought that inline functions would inherit the surrounding continuation context, but I soon discovered that their lambdas seemingly cannot. Therefore, instead of providing a suspending an non-suspending version of very function, I've decided to take another page out of ArrowKt's book and *always suspend*. 

The premise for this is that suspending gives control of how to suspend to YOU!, the library consumer. You can `launch`, `async`, or `runBlocking` - that's not my responsibility. But all the utilities here should inherit the continuation context properly without breaking your code. 

It's not a bug 🐛, ~~it's a shiny bug! 🐛✨~~ it's a feature! ✨

### Notes & Quirks
---
> 1. I've noticed that if you specify `outcomeOf(catch = ::Failure)`, it forces `RaiseScope<Throwable>`, regardless of what you try to raise inside the scope. 
> 
>   Ngl, I tried really hard to see if I could get some sort of smooth closest common ancestor typecasting going on here, like my collapse functions, or even to have the raise block take precedent, but I couldn't for the life of me fix it, so we'll just have to map Throwable to Outcome etc. properly without cheating. 
