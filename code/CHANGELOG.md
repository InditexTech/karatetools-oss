# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

- [#219](https://github.com/InditexTech/karatetools-oss/pull/219) \[Karate-Tools] Fix the OpenAPI generator interactive console on macOS Apple Silicon

- [#186](https://github.com/InditexTech/karatetools-oss/pull/186) \[Karate-Tools] Improve Kafka Avro client compatibility with generated Avro records

- [#212](https://github.com/InditexTech/karatetools-oss/pull/212) \[Karate-Tools] Migrate JMS clients to Jakarta Messaging (`jakarta.jms`) and update ActiveMQ Artemis and RabbitMQ JMS dependencies

- [#169](https://github.com/InditexTech/karatetools-oss/pull/169) \[Karate-Tools] Upgrade the Schema Registry test container to 8.3.2 and improve health checks and credential handling

- [#256](https://github.com/InditexTech/karatetools-oss/pull/256) \[Karate-Tools] Update MariaDB Java Client from 3.5.9 to 3.5.11

- [#110](https://github.com/InditexTech/karatetools-oss/pull/110), [#100](https://github.com/InditexTech/karatetools-oss/pull/100), [#113](https://github.com/InditexTech/karatetools-oss/pull/113) \[Karate-Tools] Upgrade the Boot module to Spring Boot 4 and Spring Framework 7, and modernize the OpenAPI stack

- [#194](https://github.com/InditexTech/karatetools-oss/issues/194) \[Karate-Tools] Insert system property values literally in SystemPropertiesParser

- [#208](https://github.com/InditexTech/karatetools-oss/pull/208) \[Karate-Tools] Remediate Scorecard security findings and document scoped OSV vulnerability exceptions

- [#97](https://github.com/InditexTech/karatetools-oss/pull/97), [#222](https://github.com/InditexTech/karatetools-oss/pull/222) \[Karate-Tools] Renew the documentation platform with versioned documentation and improved release flows

- [#166](https://github.com/InditexTech/karatetools-oss/pull/166) \[Karate-Tools] Pass `github.ref` via `env` in docouture-release instead of interpolating it into `run`

- [#177](https://github.com/InditexTech/karatetools-oss/pull/177) \[Karate-Tools] Harden the docouture workflows to least-privilege GITHUB_TOKENs

- \[Karate-Tools] Refresh remaining Java dependencies and Maven tooling, and improve CI governance

## [6.0.0] - 2026-06-26

- [#71](https://github.com/InditexTech/karatetools-oss/issues/71) \[Karate-Tools] JDK 25

- [#83](https://github.com/InditexTech/karatetools-oss/issues/83) Release workflows update

## [5.4.0] - 2026-05-21

- [#75](https://github.com/InditexTech/karatetools-oss/issues/75) \[Karate-Tools] Updated features background description

- [#73](https://github.com/InditexTech/karatetools-oss/issues/73) \[Karate-Tools] Bump Node related actions to Node 24

- [#72](https://github.com/InditexTech/karatetools-oss/pull/72) \[Karate-Tools] Add exclusions and update dependencies in pom.xml

## [5.3.0] - 2026-04-14

- [#64](https://github.com/InditexTech/karatetools-oss/issues/64) \[Karate-Tools] Update github workflows asdf install version to v4

- [#63](https://github.com/InditexTech/karatetools-oss/issues/63) \[Karate-Tools] Containers Update - confluentinc/cp-kafka:7.6.2 -> apache/kafka:3.8.1, kafka zookeeper deprecated and removed

- [#61](https://github.com/InditexTech/karatetools-oss/issues/61) \[Karate-Tools] Add support RabbitMQ - AMQP

- [#59](https://github.com/InditexTech/karatetools-oss/issues/59) \[Karate-Tools] update GPG signing to non-interactive pin entry loopback across all workflows

## [5.2.0] - 2026-03-03

- [#48](https://github.com/InditexTech/karatetools-oss/issues/48) \[Karate-Tools] Fixed Open Api Generation - Operation ID Sanitize

## [5.1.0] - 2025-10-09

- [#42](https://github.com/InditexTech/karatetools-oss/issues/42) \[Karate-Tools] Add HttpLogMasking

- [#40](https://github.com/InditexTech/karatetools-oss/issues/40) \[Karate-Tools] RabbitMQ Support

- [#38](https://github.com/InditexTech/karatetools-oss/issues/38) \[Karate-Tools] Versions update (Maven Deps and Docker Containers)

- [#30](https://github.com/InditexTech/karatetools-oss/issues/30) \[Karate Tools] \[CI/CD] Add test results annotations

## [5.0.1] - 2025-01-17

- [#19](https://github.com/InditexTech/karatetools-oss/issues/19) \[Karate Tools] Bug-Fix - exec-maven-plugin:3.5.0 causes Could not load library for *jansi*

## [5.0.0] - 2025-01-14

- [#10](https://github.com/InditexTech/karatetools-oss/issues/10) \[Karate-Tools] Update to JDK 21
- [#7](https://github.com/InditexTech/karatetools-oss/issues/7) \[Karate-Tools] Sonar Issues Fix

## [4.0.0] - 2024-11-26

- First Open Source Release

[Unreleased]: https://github.com/InditexTech/karatetools-oss/compare/6.0.0...HEAD

[6.0.0]: https://github.com/InditexTech/karatetools-oss/compare/5.4.0...6.0.0

[5.4.0]: https://github.com/InditexTech/karatetools-oss/compare/5.3.0...5.4.0

[5.3.0]: https://github.com/InditexTech/karatetools-oss/compare/5.2.0...5.3.0

[5.2.0]: https://github.com/InditexTech/karatetools-oss/compare/5.1.0...5.2.0

[5.1.0]: https://github.com/InditexTech/karatetools-oss/compare/5.0.1...5.1.0

[5.0.1]: https://github.com/InditexTech/karatetools-oss/compare/5.0.0...5.0.1

[5.0.0]: https://github.com/InditexTech/karatetools-oss/compare/4.0.0...5.0.0

[4.0.0]: https://github.com/InditexTech/karatetools-oss/releases/tag/4.0.0
