# jutils

Common utilities for Java applications, collected from what we use repeatedly in our own projects. Needs Java 21 or newer.

```xml
<dependency>
    <groupId>today.bonfire.oss</groupId>
    <artifactId>jutils</artifactId>
    <version>1.3.0</version>
</dependency>
```

Changes in each release are listed in the [changelog](CHANGELOG.md).

## What's in it

| Class | What it does |
|---|---|
| `Encoder` | URL and HTML escaping; Base64 (URL-safe, standard, MIME, sortable), Base32 (RFC 4648, sortable) and hex, both ways |
| `Digest` | SHA-2, SHA-3 and HMAC digests as bytes, hex, Base64 or Base32, and constant-time comparison |
| `RandUtils` | Secure random numbers, tokens, nano IDs, sortable time-based IDs (TUIDs) and passphrases |
| `BitSetHelpers` | `BitSet` to and from Base64 |
| `Please` | Runs code that may throw, without a try/catch block |
| `helpers.GetValid` | Sanitizes file names, slugs, tags, hashtags, usernames, URLs, locales and time zones |
| `helpers.Is` | Checks emails, hex colours, DNS labels and empty JSON |
| `parallel.Run`, `Contexts`, `ContextExecutors` | Runs tasks in parallel with a timeout, carrying a `Context` and the SLF4J MDC across threads |
| `web.QueryParams`, `web.HttpStatus` | Parses and builds query strings, and classifies HTTP status codes |
| `constants` | Time (`TC`) and separator (`CC`) constants, hash and HMAC algorithms, and country and continent codes |

Every package is `@NullMarked` with [JSpecify](https://jspecify.dev): parameters and return values are non-null unless annotated `@Nullable`.

`RandUtils.passphrase` uses a word list adapted from the EFF Large Wordlist, and `CountryCode` is built from GeoNames data. Both are licensed under Creative Commons Attribution; see [NOTICE](NOTICE).

## Contributing

Follow the [contributing guide](https://github.com/BonfireTrunk/.github/blob/prime/CONTRIBUTING.md). What is specific to this repository:

- **Build.** Needs Java 21 or newer. The Maven Wrapper downloads the pinned Maven version, so no local Maven install is needed:
  ```bash
  ./mvnw verify
  ```
- **Formatting.** Local builds format Java sources with palantir-java-format, so build once before committing. CI only checks, and fails on unformatted code.
- **API compatibility.** The build compares the public API with the latest release on Maven Central (japicmp) and fails when a change needs a bigger version bump than the one in `pom.xml`. The report is in `target/japicmp/`.
- **Version.** Leave the version in `pom.xml` alone, with one exception: if it is a release version (no `-SNAPSHOT` suffix), yours is the first change since that release, so bump it to the next snapshot. For a bug fix, `1.3.0` becomes `1.3.1-SNAPSHOT`; for a new feature, `1.3.0` becomes `1.4.0-SNAPSHOT`.
