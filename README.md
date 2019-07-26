# Hoplite <img src="logo.png" height=40>

Hoplite is a Kotlin library for loading configuration files into typesafe classes in a boilerplate-free way. Define your config using Kotlin data classes, and at runtime, Hoplite will read from one or more config files, mapping the values in those files into your config classes. Any missing values, or values that cannot be converted into the required type will cause the config to fail with detailed error messages.

[![Build Status](https://travis-ci.org/sksamuel/hoplite.svg?branch=master)](https://travis-ci.org/sksamuel/hoplite)
[<img src="https://img.shields.io/maven-central/v/com.sksamuel.hoplite/hoplite.svg?label=latest%20release"/>](http://search.maven.org/#search%7Cga%7C1%7Choplite)
[<img src="https://img.shields.io/nexus/s/https/oss.sonatype.org/com.sksamuel.hoplite/hoplite.svg?label=latest%20snapshot&style=plastic"/>](https://oss.sonatype.org/content/repositories/snapshots/com/sksamuel/hoplite/)

### Features

- **Multiple formats:** Write your configuration in Yaml, JSON, Toml, or Java .properties files and even mix and match multiple formats in the same system.
- **Batteries included:** Support for many standard types such as `Long`, `Double`, `UUID`, `LocalDate`, collection types, nullable types, as well as popular Kotlin third party library types such as `NonEmptyList` and `Option` from Arrow.
- **Custom Data Types:** The `Decoder` interface makes it easy to add support for your custom domain types or standard library types not covered out of the box.
- **Cascading:** Config files can be stacked. Start with a default file and then layer new configurations on top. When resolving config, lookup of values falls through to the first file that contains a definition. Can be used to have a default config file and then an environment specific file.
- **Helpful errors:** Fail fast when the config objects are built, with helpful errors on why a value was incorrect and the location of that erroneous value.

### Supported Types

| JVM Type  |
|---|
| `String` |
| `Long` |
| `Int` |
| `Boolean` |
| `Double` |
| `Float` |
| `Enums` |
| `LocalDateTime` |
| `LocalDate` |
| `Duration` |
| `UUID` |
| `List<A>` |
| `Map<K,V>` |
| `arrow.data.NonEmptyList<A>` |
| `X500Principal` |
| `KerberosPrincipal` |
| `JMXPrincipal` |



### Pre-Processors

Hoplite supports what it calls preprocessors. These are just functions `(String) -> String` that are applied to every value as they are read from the underlying config file.
The preprocessor is able to transform the value (or return the input - aka identity) depending on the logic of that preprocessor. 

For example, a preprocessor may choose to perform environment variable substition, configure default values, 
perform database lookups, or whatever other custom action you need when the config is being resolved.

#### Built-in Preprocessors 

| Preprocessor        | Function                                                                                                                                                                                                                                                                                |
|:--------------------|:----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| EnvVar Preprocessor | Replaces any strings of the form ${VAR} with the environment variable $VAR. These replacement strings can occur between other strings.<br/><br/>For example `foo: hello ${USERNAME}!` would result in foo being assigned the value `hello Sam!` assuming the env var `USERNAME` was set to `SAM` |
