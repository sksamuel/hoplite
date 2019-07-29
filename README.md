# Hoplite <img src="logo.png" height=40>

Hoplite is a Kotlin library for loading configuration files into typesafe classes in a boilerplate-free way. Define your config using Kotlin data classes, and at startup Hoplite will read from one or more config files, mapping the values in those files into your config classes. Any missing values, or values that cannot be converted into the required type will cause the config to fail with detailed error messages.

[![Build Status](https://travis-ci.org/sksamuel/hoplite.svg?branch=master)](https://travis-ci.org/sksamuel/hoplite)
[<img src="https://img.shields.io/maven-central/v/com.sksamuel.hoplite/hoplite.svg?label=latest%20release"/>](http://search.maven.org/#search%7Cga%7C1%7Choplite)
[<img src="https://img.shields.io/nexus/s/https/oss.sonatype.org/com.sksamuel.hoplite/hoplite.svg?label=latest%20snapshot&style=plastic"/>](https://oss.sonatype.org/content/repositories/snapshots/com/sksamuel/hoplite/)

## Features

- **Multiple formats:** Write your configuration in Yaml, JSON, Toml, or Java .properties files and even mix and match multiple formats in the same system.
- **Batteries included:** Support for many standard types such as primitives, enums, dates, collection types, uuids, nullable types, as well as popular Kotlin third party library types such as `NonEmptyList` and `Option` from [Arrow](https://arrow-kt.io/).
- **Custom Data Types:** The `Decoder` interface makes it easy to add support for your custom domain types or standard library types not covered out of the box.
- **Cascading:** Config files can be stacked. Start with a default file and then layer new configurations on top. When resolving config, lookup of values falls through to the first file that contains a definition. Can be used to have a default config file and then an environment specific file.
- **Beautiful errors:** Fail fast when the config objects are built, with detailed and beautiful errors showing exactly what went wrong and where.

## Getting Started

Add Hoplite to your build:

```groovy
implementation 'com.sksamuel.hoplite:hoplite-core:<version>'
```

Next define the data classes that are going to contain the config. 
You should create a top level class which can be named simply Config, or ProjectNameConfig. This class then defines a field for each config value you need. It can include nested data classes for grouping together related configs.

For example, if we had a project that needed database config, config for an embedded HTTP server, and a field which contained which environment we were running in (staging, QA, production etc), then we may define our classes like this:

```kotlin
data class Database(val host: String, val port: Int, val user: String, val pass: String)
data class Server(val port: Int, val redirectUrl: String) 
data class Config(val env: String, val database: Database, val server: Server)
```

For our staging environment, we may create a YAML (or Json, etc) file called `application-staging.yaml`:

```yaml
env: staging

database:
  url: staging.wibble.com
  port: 3306
  user: theboss
  pass: 0123abcd

server:
  port: 8080
  redirectUrl: /404.html
```

Finally, to build an instance of `Config` from this file, and assuming the config file was on the classpath, we can simply execute:

```kotlin
val config = ConfigLoader().loadConfigOrThrow<Config>("/application-staging.yaml")
```

If the values in the config file are compatible, then an instance of `Config` will be returned. Otherwise an exception will be thrown containing details of the errors.

## Config Loader

As you have seen from the getting started guide, `ConfigLoader` is the entry point to using Hoplite.
Create an instance of this and then you can load config into your data classes from resources on the classpath, `java.io.File`, `java.nio.Path`, or URLS.

There are two ways to use the config loader. 
One is to throw an exception if the config could not be resolved via the `loadConfigOrThrow<T>` function. 
Another is to return an `arrow.data.Validated` via the `loadConfig<T>` function. 

For most cases, when you are resolving config at application startup, the exception based approach is better. 
This is because you typically want any errors in config to abort application bootstrapping, dumping errors to the console.

## Supported Formats

Hoplite supports config files in several formats. You can mix and match formats if you really want to.
For each format you wish to use, you must include the appropriate hoplite module on your classpath.
The format that hoplite uses to parser a file is determined by the file extension.

| Format | Module | File Extensions |
|:---|:---|:---|
| Json | [`hoplite-json`](https://search.maven.org/search?q=hoplite-json) | .json |
| [Yaml](https://yaml.org/) | [`hoplite-yaml`](https://search.maven.org/search?q=hoplite-yaml) | .yml, .yaml |
| [Toml](https://github.com/toml-lang/toml) | [`hoplite-toml`](https://search.maven.org/search?q=hoplite-toml) | .toml |
| Java Properties files | [`hoplite-props`](https://search.maven.org/search?q=hoplite-props) | .props |

If you wish to add another format you can extend `Parser` and provide an instance of that implementation to the `ConfigLoader` via `withFileExtensionMapping`.

That same function can be used to map non-default file extensions to an existing parser. For example, if you wish to have your config in files called `application.conf` but in yaml format, then you can register .conf with the Yaml parser like this:

`ConfigLoader().withFileExtensionMapping("conf", YamlParser)`

## Decoders

Hoplite converts the raw value in config files to JDK types using instances of the `Decoder` interface.
There are built in decoders for all the standard day to day types, such as primitives, dates, lists, sets, maps, enums, arrow types and so on. The full list is below: 

| JDK Type  | Conversion Notes |
|---|---|
| `String` |
| `Long` |
| `Int` |
| `Boolean` |
| `Double` |
| `Float` |
| `Enums` | Java and Kotlin enums are both supported. An instance of the defined Enum class will be created with the constant value given in config. |
| `LocalDateTime` |
| `LocalDate` |
| `Duration` | Converts a String into a Duration, where the string uses a value and unit such as "10 seconds" or "5m". Also supports a long value which will be interpreted as a Duration of milliseconds. |
| `Instant` |  |
| `Regex` | |
| `UUID` | Creates a `java.util.UUID` from a String |
| `List<A>` | Creates a List from either an array or a string delimited by commas. 
| `Set<A>` | Creates a Set from either an array or a string delimited by commas. 
| `Map<K,V>` |
| `arrow.data.NonEmptyList<A>` | Converts arrays into a `NonEmptyList<A>` if the array is non empty. If the array is empty then an error is raised.
| `X500Principal` | Creates an instance of `X500Principal` for String values |
| `KerberosPrincipal` | Creates an instance of `KerberosPrincipal` for String values |
| `JMXPrincipal` | Creates an instance of `JMXPrincipal` for String values |
| `Principal` | Creates an instance of `BasicPrincipal` for String values |
| `File` | Creates a java.io.File from a String path |
| `Path` | Creates a java.nio.Path from a String path |
| `BigInteger` | Converts from a String, Long or Int into a BigInteger. |
| `BigDecimal` | Converts from a String, Long, Int, Double, or Float into a BigDecimal |
| `arrow.core.Option<A>` | A `None` is used for null or undefined values, and present values are converted to a `Some<A>` |
| `arrow.core.Tuple2<A,B>` | Converts from an array of two elements into an instance of `Tuple2<A,B>`.  Will fail if the array does not have exactly two elements.|
| `arrow.core.Tuple3<A,B,C>` | Converts from an array of three elements into an instance of `Tuple2<A,B,C>`. Will fail if the array does not have exactly three elements. |

## Pre-Processors

Hoplite supports what it calls preprocessors. These are just functions `(String) -> String` that are applied to every value as they are read from the underlying config file.
The preprocessor is able to transform the value (or return the input - aka identity function) depending on the logic of that preprocessor. 

For example, a preprocessor may choose to perform environment variable substitution, configure default values, 
perform database lookups, or whatever other custom action you need when the config is being resolved.

You can add custom pre-processors in addition to the builtt in ones, by using the function `withPreprocessor` on the `ConfigLoader` class, and passing in an instance of the `Preprocessor` interface.
A typical use case of a custom preprocessor is to lookup some values in a database, or from a third party secrets store such as [Vault](https://www.vaultproject.io/) or [Amazon Parameter Store](https://docs.aws.amazon.com/systems-manager/latest/userguide/systems-manager-parameter-store.html).
One way this can be implemented is to have a prefix, and then use a preprocessor to look for the prefix in strings, and if the prefix is present, use the rest of the string as a key to the service.

For example

```yaml
database:
  user: root
  password: vault:/my/key/path
```

### Built-in Preprocessors 

These built-in preprocessors are registered automatically.

| Preprocessor        | Function                                                                                                                                                                                                                                                                                |
|:--------------------|:----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| EnvVar Preprocessor | Replaces any strings of the form ${VAR} with the environment variable $VAR if defined. These replacement strings can occur between other strings.<br/><br/>For example `foo: hello ${USERNAME}!` would result in foo being assigned the value `hello Sam!` assuming the env var `USERNAME` was set to `SAM` |
| System Property Preprocessor | Replaces any strings of the form ${VAR} with the system property $VAR if defined. These replacement strings can occur between other strings.<br/><br/>For example `debug: ${DEBUG}` would result in debug being assigned the value `true` assuming the application had been started with `-Ddebug=true` |
| Random Preprocessor | Inserts random strings into the config whenever you use the placeholder `$RANDOM_STRING(length)` where length is the length of the generated random string. |
| UUID Preprocessor | Generates UUIDS and replaces placeholders of the form `$uuid()`.<br/><br/>For example, the config `foo: $uuid()` would result in foo being assigned a generated UUID. |

## Masked values

It is quite common to output the resolved config at startup for reference when debugging. In this case, the default `toString` generated by Kotlin's data classes is very useful.
However configuration typically includes sensitive information such as passwords or keys which normally you would not want to appear in logs.

To avoid sensitive fields appearing in the log output, Hoplite provides a built in type called `Masked` which is a wrapper around a String.
By declaring a field to have this type, the value will still be loaded from configuration files, but will not be included in the generated `toString`.

For example, you may define a config class like this: 

`data class Database(val host: String, val user: String, val password: Masked)`

And corresponding json config:

```json
{
  "host": "localhost",
  "user": "root",
  "password": "letmein"
}
```

And then the output of the Database config class via `toString` would be `Database(host=localhost, user=root, password=****)`

## License
```
This software is licensed under the Apache 2 license, quoted below.

Copyright 2019 Stephen Samuel

Licensed under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License. You may obtain a copy of
the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
License for the specific language governing permissions and limitations under
the License.
```
