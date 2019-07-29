# Hoplite <img src="logo.png" height=40>

Hoplite is a Kotlin library for loading configuration files into typesafe classes in a boilerplate-free way. Define your config using Kotlin data classes, and at startup Hoplite will read from one or more config files, mapping the values in those files into your config classes. Any missing values, or values that cannot be converted into the required type will cause the config to fail with detailed error messages.

[![Build Status](https://travis-ci.org/sksamuel/hoplite.svg?branch=master)](https://travis-ci.org/sksamuel/hoplite)
[<img src="https://img.shields.io/maven-central/v/com.sksamuel.hoplite/hoplite.svg?label=latest%20release"/>](http://search.maven.org/#search%7Cga%7C1%7Choplite)
[<img src="https://img.shields.io/nexus/s/https/oss.sonatype.org/com.sksamuel.hoplite/hoplite.svg?label=latest%20snapshot&style=plastic"/>](https://oss.sonatype.org/content/repositories/snapshots/com/sksamuel/hoplite/)

### Features

- **Multiple formats:** Write your configuration in Yaml, JSON, Toml, or Java .properties files and even mix and match multiple formats in the same system.
- **Batteries included:** Support for many standard types such as primitives, enums, collection types, uuids, nullable types, as well as popular Kotlin third party library types such as `NonEmptyList` and `Option` from Arrow.
- **Custom Data Types:** The `Decoder` interface makes it easy to add support for your custom domain types or standard library types not covered out of the box.
- **Cascading:** Config files can be stacked. Start with a default file and then layer new configurations on top. When resolving config, lookup of values falls through to the first file that contains a definition. Can be used to have a default config file and then an environment specific file.
- **Helpful errors:** Fail fast when the config objects are built, with helpful errors on why a value was incorrect and the location of that erroneous value.

### Supported Types

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



### Pre-Processors

Hoplite supports what it calls preprocessors. These are just functions `(String) -> String` that are applied to every value as they are read from the underlying config file.
The preprocessor is able to transform the value (or return the input - aka identity) depending on the logic of that preprocessor. 

For example, a preprocessor may choose to perform environment variable substition, configure default values, 
perform database lookups, or whatever other custom action you need when the config is being resolved.

#### Built-in Preprocessors 

| Preprocessor        | Function                                                                                                                                                                                                                                                                                |
|:--------------------|:----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| EnvVar Preprocessor | Replaces any strings of the form ${VAR} with the environment variable $VAR if defined. These replacement strings can occur between other strings.<br/><br/>For example `foo: hello ${USERNAME}!` would result in foo being assigned the value `hello Sam!` assuming the env var `USERNAME` was set to `SAM` |
| System Property Preprocessor | Replaces any strings of the form ${VAR} with the system property $VAR if defined. These replacement strings can occur between other strings.<br/><br/>For example `debug: ${DEBUG}` would result in debug being assigned the value `true` assuming the application had been started with `-Ddebug=true` |
| Random Preprocessor | Inserts random strings into the config whenever you use the placeholder `$RANDOM_STRING(length)` where length is the length of the generated random string.
| UUID Preprocessor | Generates UUIDS and replaces placeholders of the form `$uuid()`.<br/><br/>For example, the config `foo: $uuid()` would result in foo being assigned a generated UUID.
| 

### Masked values

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
