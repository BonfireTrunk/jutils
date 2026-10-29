# Changelog

All notable changes to this project are documented here. The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and versions follow [Semantic Versioning](https://semver.org/).

## [Unreleased]

### Added

- Releases are also published to GitHub Packages. Maven Central remains the place to get jutils from, since GitHub Packages needs a token even for public packages.

### Changed

- Built on bonfire-oss-parent 2.1.0. Dependency versions move to commons-lang3 3.21.0 (from 3.17.0), Guava 33.7.2-jre (from 33.4.0-jre) and jsoup 1.23.2 (from 1.19.1).
- `GetValid.hashtag` and `GetValid.httpUrlString` mark their return value with `org.jspecify.annotations.Nullable` instead of `javax.annotation.Nullable`. jspecify 1.0.1 is now a direct dependency; Guava already depended on it.
