# Hoplite

Hoplite is a Kotlin library for loading configuration files and a port of the incredible Scala configuration library [pureconfig](https://github.com/pureconfig/pureconfig). It reads Typesafe Config configurations written in HOCON, Java .properties, Yaml or JSON to Kotlin classes in a boilerplate-free way. Sealed classes, data classes, collections, nullable values, and many other types are all supported out-of-the-box. Users also have many ways to add support for custom types or customize existing ones.

[![Build Status](https://travis-ci.org/sksamuel/hoplite.svg?branch=master)](https://travis-ci.org/sksamuel/hoplite)
[<img src="https://img.shields.io/maven-central/v/com.sksamuel.hoplite/hoplite.svg?label=latest%20release"/>](http://search.maven.org/#search%7Cga%7C1%7Ckquants)
[<img src="https://img.shields.io/nexus/s/https/oss.sonatype.org/com.sksamuel.hoplite/hoplite.svg?label=latest%20snapshot&style=plastic"/>](https://oss.sonatype.org/content/repositories/snapshots/com/sksamuel/hoplite/)

### Supported Types

| JVM Type  |
|---|
| String |
| Long |
| Int |
| Boolean |
| Double |
| Float |
