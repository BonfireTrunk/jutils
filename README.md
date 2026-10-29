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

## Contributing

Follow the [contributing guide](https://github.com/BonfireTrunk/.github/blob/prime/CONTRIBUTING.md). What is specific to this repository:

- **Build.** Needs Java 21 or newer. The Maven Wrapper downloads the pinned Maven version, so no local Maven install is needed:
  ```bash
  ./mvnw verify
  ```
- **Formatting.** Local builds format Java sources with palantir-java-format, so build once before committing. CI only checks, and fails on unformatted code.
- **Version.** Leave the version in `pom.xml` alone, with one exception: if it is a release version (no `-SNAPSHOT` suffix), yours is the first change since that release, so bump it to the next snapshot. For a bug fix, `1.3.0` becomes `1.3.1-SNAPSHOT`; for a new feature, `1.3.0` becomes `1.4.0-SNAPSHOT`.
