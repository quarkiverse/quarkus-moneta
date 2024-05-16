# Quarkus Moneta

<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
[![All Contributors](https://img.shields.io/badge/all_contributors-2-orange.svg?style=flat-square)](#contributors-)
<!-- ALL-CONTRIBUTORS-BADGE:END -->
[![Build](https://github.com/quarkiverse/quarkus-moneta/workflows/Build/badge.svg)](https://github.com/quarkiverse/quarkus-moneta/actions?query=workflow%3ABuild)
[![Maven Central](https://img.shields.io/maven-central/v/io.quarkiverse.moneta/quarkus-moneta-parent.svg?label=Maven%20Central)](https://search.maven.org/artifact/io.quarkiverse.moneta/quarkus-moneta-parent)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

Integrate JavaMoney (JSR 354) into Quarkus for JDK and native builds.

## Usage

Add the following dependency to your build file:

### Maven pom.xml

```xml

<dependency>
    <groupId>io.quarkiverse.moneta</groupId>
    <artifactId>quarkus-moneta</artifactId>
    <version>${latest.version}</version>
</dependency>
```

### Gradle build.gradle
```groovy
implementation("io.quarkiverse.moneta:quarkus-moneta:$latestVersion")
```

## Compatibility

Quarkus APIstax provides multiple different version streams.

| Quarkus     | Quarkus Moneta |
|-------------|----------------|
| 3.8.x (LTS) | 1.x            |
| 3.10.x      | 2.x            |

## Contributors ✨

Thanks goes to these wonderful people ([emoji key](https://allcontributors.org/docs/en/emoji-key)):
<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tbody>
    <tr>
      <td align="center" valign="top" width="14.28%"><a href="http://instant-it.at"><img src="https://avatars.githubusercontent.com/u/1436448?v=4?s=100" width="100px;" alt="David Andlinger"/><br /><sub><b>David Andlinger</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-moneta/commits?author=andlinger" title="Code">💻</a> <a href="#maintenance-andlinger" title="Maintenance">🚧</a></td>
      <td align="center" valign="top" width="14.28%"><a href="http://instant-it.at"><img src="https://avatars.githubusercontent.com/u/3810635?v=4?s=100" width="100px;" alt="Max Holzleitner"/><br /><sub><b>Max Holzleitner</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-moneta/commits?author=holzleitner" title="Code">💻</a> <a href="#maintenance-holzleitner" title="Maintenance">🚧</a></td>
    </tr>
  </tbody>
</table>

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->

This project follows the [all-contributors](https://github.com/all-contributors/all-contributors) specification.
Contributions of any kind welcome!
