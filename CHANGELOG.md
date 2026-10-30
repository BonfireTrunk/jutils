# Changelog

All notable changes to this project are documented here. The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and versions follow [Semantic Versioning](https://semver.org/).

## [Unreleased]

A major release with breaking changes. [Migrating from 1.x](#migrating-from-1x) lists every replacement.

### Added

- Releases are also published to GitHub Packages. Maven Central remains the place to get jutils from, since GitHub Packages needs a token even for public packages.
- Every package is `@NullMarked` (JSpecify): parameters and return values are non-null unless annotated `@Nullable`.
- `Encoder`: `escapeHtml`, `htmlToText`, `toBase64Basic(byte[])`, sortable Base64 (`toBase64Lex`, `fromBase64Lex`), and decoders that return bytes: `fromBase64Url`, `fromBase64Basic`, `fromBase64Mime`, `fromBase32`, `fromBase32Lex`, `fromBase16`.
- `Digest`: `hash`, `hashHex`, `hashBase64Url`, `hashBase32`, `hmac`, `hmacHex` and `hmacBase64Url` for any `HashAlgorithm` or `HmacAlgorithm`, and a constant-time `isEqual`.
- `Digest.verifyHmac`, `verifyHmacHex` and `verifyHmacBase64Url` check a signature, such as a webhook's, in one call. They compare bytes in constant time, and a missing or malformed signature returns false instead of throwing.
- `RandUtils.passphrase(words)` and `passphrase(words, separator)`, using an 8,192-word list bundled in the jar (13 bits per word), adapted from the EFF Large Wordlist without its hyphenated, profane or sensitive words, alcohol words and company names, plus other common English words. No word is a prefix of another, so an empty separator is safe.
- `RandUtils.nanoId(size)` and `RandUtils.tuidTimestamp(tuid, base32)`.
- `Run.all(...)`, which waits for a group of tasks with a real timeout and fails fast, and `Run.submitAll(...)`.
- `Contexts` (`current`, `open`, `wrap`) carries a `Context` and the SLF4J MDC across threads, including on pooled executors. `ContextExecutors` creates thread-per-task executors that do the same.
- `QueryParams`: `asMap`, `isEmpty` and `toQueryString`.
- `HttpStatus.equals` and `hashCode`.
- `CountryCode`: `fromCode`, `hasPostCodes` and `isValidPostCode`.
- The jar contains `META-INF/LICENSE` and `META-INF/NOTICE`.

### Changed

- Built on bonfire-oss-parent 2.1.1. Dependency versions move to commons-lang3 3.21.0 (from 3.17.0) and jsoup 1.23.2 (from 1.19.1).
- `GetValid.hashtag` and `GetValid.httpUrlString` mark their return value with `org.jspecify.annotations.Nullable` instead of `javax.annotation.Nullable`. jspecify 1.0.1 is now a direct dependency.
- Classes with only static members are `final` with a private constructor: `BitSetHelpers`, `Digest`, `Encoder`, `Please`, `RandUtils`, `GetValid`, `Is`, `Run`, `CC`, `CC.Separator` and `TC`. `QueryParams`, `HttpStatus` and `ContextAwareThreadFactory` are `final` too.
- `Encoder.urlEncode` and `urlDecode` no longer return blank input unchanged: `urlEncode(" ")` is `"+"`.
- `Encoder.toStr` returns `""` for an empty array (it returned null).
- `RandUtils` uses `new SecureRandom()` instead of `SecureRandom.getInstanceStrong()`, which can block waiting for entropy.
- `RandUtils.tuid(size)` and `tuid(size, false)` use a sortable Base64 alphabet, so they sort by creation time like the base32 form. They don't sort together with IDs from 1.x. `tuid()` output is unchanged.
- `RandUtils.time()` returns 10 sortable characters: the 48-bit millisecond timestamp in the base32 alphabet of `toBase32Lex`.
- `RandUtils.getUrlPath()` returns 44 uniformly chosen letters and digits. `nanoId(char[], int)` and the helpers built on it pick characters uniformly.
- `QueryParams.get` returns an unmodifiable list, empty when the key is absent (it returned the internal list, or null). A key without a value maps to `""` (it was null), keys keep their order, and the maps are unmodifiable.
- `GetValid.username` keeps only Unicode letters, digits, combining marks and `_`, as documented, after normalizing to NFC. Emoji and symbols are removed.
- `GetValid.urlSlug` lowercases with `Locale.ROOT`, so the result no longer depends on the default locale.
- `GetValid.httpUrlString` accepts internationalized domain names and returns the host in ASCII: `https://例え.jp/path` gives `https://xn--r8jz45g.jp/path` (it gave null). A host that is not a valid domain name, such as one with a label longer than 63 characters, gives null.
- `GetValid.locale` reads `_` as `-`, so `en_US` gives `en-US` (it gave `en`).
- `ContextAwareThreadFactory` restores the thread's previous context and MDC after the task.
- `Please.tryThis` overloads that take a `Callable` restore the thread's interrupt flag when they catch an `InterruptedException`.
- `ParallelException` requires a non-null context.
- `CountryCode` data is reconciled with GeoNames (countryInfo.txt of 2026-10-29). Every postal code regex is anchored. 24 countries gained a regex: BT, CD, DJ, EH, GA, GH, GN, GY, KI, KN, KY, LC, MS, MU, NA, PS, TF, TL, TT, TZ, UG, VC, VG and XK. Regexes now follow current formats for AM, CY, EC, HN, KH, LI, MN, NI and PY. Wrong patterns are fixed for AE, AI, AS, BM, CU, FK, GB, GG, GI, GR, GS, IE, IM, IO, JE, KP, LB, LT, LV, MH, NR, PN, SH, SO and SV. The capital of GQ is now Ciudad de la Paz.

### Deprecated

- `CountryCode.CS` (Serbia and Montenegro) and `CountryCode.AN` (Netherlands Antilles), which no longer exist. They are kept because stored data may still contain them.

### Removed

- The Guava dependency. jutils used only its `BaseEncoding`. Guava's own dependencies leave your classpath with it: with jutils 1.3.0 these were failureaccess, listenablefuture, jsr305, checker-qual, error_prone_annotations and j2objc-annotations. If you used any of them (for example `javax.annotation.Nullable` from jsr305) through jutils, declare it yourself.
- `ArrayItem`, `Is.True`, `Is.False`, `Is.Falsy`, `Encoder.htmlSafe`, `Encoder.toBase64`, `Encoder.toBase64String`, `Encoder.decodeBase64`, `Encoder.decodeBase64Mime`, `Encoder.encodeBase64Mime`, `DigestHash`, every 1.x `Digest` method, `RandUtils.nanoIdFast`, `Run.parallel`, `Run.shutdown`, `parallel.Executors` and `ContextAwareThreadFactory.CONTEXT_THREAD_LOCAL`. See [Migrating from 1.x](#migrating-from-1x).

### Fixed

- `GetValid.fileName` returned `"."` and `".."` unchanged, and its 255-character cut could split a `%XX` escape.
- `GetValid.userNameFromEmail` threw `ArrayIndexOutOfBoundsException` for `""` and `"@"`, and turned `"@x.com"` into `"xcom"`.
- `GetValid.urlSlug` and `hashtag` dropped the vowel signs and viramas of Devanagari, Tamil and other scripts, and the accents of decomposed text: `urlSlug("नमस्ते दुनिया")` gave `नमस-त-द-न-य`. They now keep combining marks and normalize the input to NFC.
- `GetValid.tagName` left a double space where it removed a character (`"foo & bar"` gave `"foo  bar"`) and kept tabs and newlines. Each run of whitespace now becomes one space after the removal, and the 100-character cut counts code points, so it never splits a surrogate pair.
- `Is.email` rejected uppercase domains and 64-character local parts.
- `QueryParams` cut values at a second `=` (`"a=b=c"` gave `"b"`) and threw on `"="`.
- `RandUtils.newInt` and `newLong` threw for negative ranges.
- `RandUtils.tuid(0, ...)` was accepted and produced an ID with no random part.
- `RandUtils.nanoIdFast` favoured some characters, and with alphabets longer than 256 never picked others.
- `Run.parallel` ignored its timeout, and failed tasks carried an empty context instead of the submitter's.
- `ContextAwareThreadFactory` gave every task on a reused pool thread the context of the task that created the thread. Use `Contexts.wrap` for pooled executors.
- `Please.tryThis` cleared the interrupt flag of a thread interrupted inside the callable.
- `CountryCode.CA` never matched a Canadian postal code (its regex ended in a space), and `CountryCode.TD` carried the pattern of TC.
- The postal code patterns of AD, AI, AX, AZ, BB, CY, FI, FO, HR, HT, KN, MS, SI, TL, VC and VG let the optional country prefix repeat, so `ADAD100` matched. The prefix may now appear once.

### Security

- `Encoder.htmlSafe` decoded HTML entities, so escaped input such as `&lt;img src=x onerror=alert(1)&gt;` came back as live markup. It is replaced by `escapeHtml`, which is safe for HTML output, and `htmlToText`, which returns plain text that is not safe for HTML.
- `GetValid.httpUrlString` accepted any scheme it did not block, including `ftp:` and `javascript://`. It now accepts only http and https URLs with a host.
- `GetValid.httpUrlString` accepted URLs with user info, such as `https://evil.com@good.com/`, which reads as evil.com but opens good.com. It now rejects them.

### Migrating from 1.x

| 1.x | 2.0 |
|---|---|
| `ArrayItem.FIRST` ... `TENTH` | the index (`0` ... `9`) |
| `CC.Separator.URL` | `CC.Separator.SLASH` |
| `Is.True(b)`, `Is.False(b)`, `Is.Falsy(b)` | commons-lang3 `BooleanUtils.isTrue(b)`, `isFalse(b)`, `isNotTrue(b)` |
| `Encoder.htmlSafe(s)` | `Encoder.escapeHtml(s)` to put text into HTML, or `Encoder.htmlToText(s)` to strip markup |
| `Encoder.toBase64(bytes)`, `toBase64String(s)` | `Encoder.toBase64Url(...)` (same output) |
| `Encoder.decodeBase64(s)` | `Encoder.toStr(Encoder.fromBase64Url(s))` |
| `Encoder.encodeBase64Mime(...)` | `Encoder.toBase64Mime(...)` |
| `Encoder.decodeBase64Mime(s)` | `Encoder.toStr(Encoder.fromBase64Mime(s))` |
| `DigestHash.SHA_3_256` and the other constants | `HashAlgorithm.SHA3_256`, `HmacAlgorithm.HMAC_SHA_256`, ... |
| `Digest.sha256(s)` | `Digest.hashHex(HashAlgorithm.SHA_256, s)` |
| `Digest.base64Sha256(s)` and the other `base64Sha*` methods | `Digest.hashBase64Url(HashAlgorithm.SHA_256, s)` and so on |
| `Digest.base32Sha3_256(s)` | `Digest.hashBase32(HashAlgorithm.SHA3_256, s)` |
| `Digest.base64Sha3_256AsBytes(s)` | `Digest.hash(HashAlgorithm.SHA3_256, s)` |
| `Digest.hmacSha256(data, key)` | `Digest.hmac(HmacAlgorithm.HMAC_SHA_256, data, key)` |
| `Digest.hmacSha256Hex(data, key)` | `Digest.hmacHex(HmacAlgorithm.HMAC_SHA_256, data, key)`. The hex is now lowercase (it was uppercase). |
| `Digest.hmacSha384(data, key)`, `hmacSha512(data, key)` | `Digest.hmacBase64Url(HmacAlgorithm.HMAC_SHA_384, data, key)` and so on |
| `RandUtils.nanoIdFast(size)` | `RandUtils.nanoId(size)` |
| `RandUtils.nanoIdFast(alphabet, size)` | `RandUtils.nanoId(alphabet, size)` |
| `Run.parallel(tasks)` | `Run.submitAll(tasks)` for futures, or `Run.all(tasks)` to wait for the results |
| `Run.shutdown()` | nothing to do; pass your own executor to `Run.all` or `Run.submitAll` to control its lifecycle |
| `Executors.getNewExecutor(prefix)` | `ContextExecutors.newVirtualThreadExecutor(prefix)` |
| `Executors.getNewExecutor(prefix, builder)` | `ContextExecutors.newThreadPerTaskExecutor(builder.name(prefix, 0).factory())` |
| `ContextAwareThreadFactory.CONTEXT_THREAD_LOCAL.set(ctx)` | `try (var scope = Contexts.open(ctx)) { ... }` |
| `ContextAwareThreadFactory.CONTEXT_THREAD_LOCAL.get()` | `Contexts.current()` |
| `Please.checkIfthrows(r)` | `Please.checkIfThrows(r)` |
| `Digest` methods returning null for null input | pass non-null values; null now throws `NullPointerException` |
