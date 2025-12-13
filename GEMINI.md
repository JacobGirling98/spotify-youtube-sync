### **Project Summary**

This is a Kotlin-based application designed to synchronize music playlists from a user's Spotify account to their YouTube account.

**Core Functionality:**
*   Handles OAuth 2.0 authentication for both Spotify and YouTube.
*   Fetches playlists and tracks from both services using the `http4k` library.
*   Builds a central "song dictionary" to map songs to their respective IDs on each platform.
*   The intended final step is to use this dictionary to create/update YouTube playlists to mirror the user's Spotify playlists.

**Architecture:**
*   **Language:** Kotlin
*   **Libraries:** `http4k` for HTTP client/server, `Arrow` for functional error handling.
*   **Design:** A modular design centered around a `MusicService` interface, with separate, service-specific implementations for Spotify and YouTube. This keeps the core logic platform-agnostic.

**Status:**
The application is functional but incomplete. Authentication, data fetching, and dictionary building are implemented. The final synchronization logic (modifying YouTube playlists) is designed but not yet activated.

---

### **Project Style Guide**

This guide is based on the official [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html) with project-specific additions.

#### **1. Formatting**

*   **Indentation**: 4 spaces.
*   **Line Length**: Aim for a maximum of 120 characters per line.
*   **Braces**:
    *   Use Egyptian-style (opening brace on the same line) for class and function declarations.
    *   For control flow statements (`if`, `when`, `for`, `while`), the opening brace is also on the same line.
*   **Blank Lines**: Use a single blank line to separate methods, and logical blocks inside methods for readability.

#### **2. Naming Conventions**

*   **Packages**: Lowercase, multi-word names are concatenated (`org.example.domain.music`).
*   **Classes and Interfaces**: PascalCase (`SpotifyRestClient`, `MusicService`).
*   **Functions**: camelCase (`loadEnvironmentVariables`, `createDictionary`).
*   **Variables**: camelCase (`environment`, `serverUri`).
*   **Constants**: Use `val` for constants. If defined in a companion object, use `PascalCase` or `UPPER_SNAKE_CASE`. The project currently does not have a strong convention here, so I will prefer `UPPER_SNAKE_CASE` for clarity.
*   **Test Functions**: Use backticks for descriptive test names, e.g., `` `test that it should return a playlist` ``.

#### **3. Code Structure and Patterns**

*   **Functional Programming with Arrow:**
    *   Use `arrow.core.Either` for error handling in functions that can fail. The right side of the `Either` should be the success value, and the left side should be an error type.
    *   Use `flatMap`, `map`, and `getOrElse` to work with `Either` values.
    *   Use the `either` block for imperative-style error handling with `bind()`.
*   **Immutability**:
    *   Prefer `val` over `var` wherever possible.
    *   Use immutable collections (e.g., `List`, `Set`, `Map`) over their mutable counterparts.
*   **Dependency Injection**:
    *   Dependencies are passed manually as function or constructor parameters. Avoid using frameworks for dependency injection to maintain simplicity.
*   **`http4k` Usage**:
    *   Follow the existing patterns for creating `HttpHandler` clients and servers.
    *   Use `http4k-format-jackson` for JSON serialization/deserialization.
*   **Type Safety**:
    *   Use "tiny types" (value classes) for IDs, tokens, and other simple wrapper types to improve type safety (e.g., `UserId(val value: String)`). This is already in use in `TinyTypes.kt`.
*   **Null Safety**:
    *   Avoid nullable types (`?`) where possible. Use `Either` or other functional constructs to handle the absence of a value.