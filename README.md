# Hoplite <img src="logo.png" height=40>

Hoplite is a Kotlin library for loading configuration files and a port of the incredible Scala configuration library [pureconfig](https://github.com/pureconfig/pureconfig). It reads Typesafe Config configurations written in HOCON, Java .properties, Yaml or JSON to Kotlin classes in a boilerplate-free way. Sealed classes, data classes, collections, nullable values, and many other types are all supported out-of-the-box. Users also have many ways to add support for custom types or customize existing ones.

[![Build Status](https://travis-ci.org/sksamuel/hoplite.svg?branch=master)](https://travis-ci.org/sksamuel/hoplite)
[<img src="https://img.shields.io/maven-central/v/com.sksamuel.hoplite/hoplite.svg?label=latest%20release"/>](http://search.maven.org/#search%7Cga%7C1%7Choplite)
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
| Enums |
| LocalDateTime |
| LocalDate |
| Duration |
| UUID |
| arrow.data.NonEmptyList |
| X500Principal |
| KerberosPrincipal |
| JMXPrincipal |



### Pre-Processors

Hoplite supports what it calls preprocessors. These are just functions `(String) -> String` that are applied to every value as they are read from the underlying config file.
The preprocessor is able to transform the value (or return the input - aka identity) depending on the logic of that preprocessor. 

For example, a preprocessor may choose to perform environment variable substition, configure default values, 
perform database lookups, or whatever other custom action you need when the config is being resolved.

#### Built-in Preprocessors 

| Preprocessor | Function |
|--------------|--------|
| EnvVar Preprocessor | Replaces any strings of the form ${VAR} with the environment variable $VAR. These replacement strings can occur between other strings. For example `foo: hello ${USERNAME}!` would result in foo being assigned the value `hello Sam!` assuming the env var `USERNAME` was set to `SAM` | 
