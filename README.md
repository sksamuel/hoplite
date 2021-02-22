# Hoplite <img src="logo.png" height=40>

Hoplite is a Kotlin library for loading configuration files into typesafe classes in a boilerplate-free way. Define your config using Kotlin data classes, and at startup Hoplite will read from one or more config files, mapping the values in those files into your config classes. Any missing values, or values that cannot be converted into the required type will cause the config to fail with detailed error messages.

![master](https://github.com/sksamuel/hoplite/workflows/master/badge.svg)
[<img src="https://img.shields.io/maven-central/v/com.sksamuel.hoplite/hoplite-core.svg?label=latest%20release"/>](http://search.maven.org/#search%7Cga%7C1%7Choplite)
[<img src="https://img.shields.io/nexus/s/https/oss.sonatype.org/com.sksamuel.hoplite/hoplite-core.svg?label=latest%20snapshot&style=plastic"/>](https://oss.sonatype.org/content/repositories/snapshots/com/sksamuel/hoplite/)

## Features

- **Multiple formats:** Write your configuration in [several formats](#supported-formats): Yaml, JSON, Toml, Hocon, or
  Java .properties files or even mix and match formats in the same system.
- **Property Sources:** Per-system [overrides](#property-sources-new-in-110) are possible from JVM system properties,
  environment variables, JDNI or a per-user local config file.
- **Batteries included:** Support for many [standard types](#decoders) such as primitives, enums, dates, collection
  types, inline classes, uuids, nullable types, as well as popular Kotlin third party library types such
  as `NonEmptyList`, `Option` and `TupleX` from [Arrow](https://arrow-kt.io/).
- **Custom Data Types:** The `Decoder` interface makes it easy to add support for your custom domain types or standard
  library types not covered out of the box.
- **Cascading:** Config files can be [stacked](#cascading-config). Start with a default file and then layer new
  configurations on top. When resolving config, lookup of values falls through to the first file that contains a
  definition. Can be used to have a default config file and then an environment specific file.
- **Beautiful errors:** Fail fast when the config objects are built, with detailed
  and [beautiful errors](#beautiful-errors) showing exactly what went wrong and where.

## Changelog

See the list of changes in each release [here](changelog.md).

## Getting Started

Add Hoplite to your build:

```groovy
implementation 'com.sksamuel.hoplite:hoplite-core:<version>'
```

You will also need to include a module for the [format(s)](#supported-formats) you to use.

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
  host: staging.wibble.com
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

If the values in the config file are compatible, then an instance of `Config` will be returned.
Otherwise an exception will be thrown containing details of the errors.




## Config Loader

As you have seen from the getting started guide, `ConfigLoader` is the entry point to using Hoplite.
We can create an instance of this loader directly for simple cases, or use the `ConfigLoader.Builder` if we need to customize how the loader works.
Then we can load config into data classes from resources on the classpath, `java.io.File`, `java.nio.Path`, or URLS.

There are two ways to retrieve a populated data class from config.
The first is to throw an exception if the config could not be resolved via the `loadConfigOrThrow<T>` function.
Another is to return a `ConfigResult` via the `loadConfig<T>` function.

For most cases, when you are resolving config at application startup, the exception based approach is better.
This is because you typically want any errors in config to abort application bootstrapping, dumping errors to the console.




## Beautiful Errors

When an error does occur, if you choose to throw an exception, the errors will be formatted in a human readable way
along with as much location information as possible.
No more trying to track down a `NumberFormatException` in a 400 line config file.

Here is an example of the error formatting for a test file used by the unit tests.

```
Error loading config because:

    - Could not instantiate 'com.sksamuel.hoplite.json.Foo' because:

        - 'bar': Required type Boolean could not be decoded from a Long (/error1.json:2:19)

        - 'baz': Missing from config

        - 'hostname': Type defined as not-null but null was loaded from config (/error1.json:6:18)

        - 'season': Required a value for the Enum type com.sksamuel.hoplite.json.Season but given value was Fun (/error1.json:8:18)

        - 'users': Defined as a List but a Boolean cannot be converted to a collection (/error1.json:3:19)

        - 'interval': Required type java.time.Duration could not be decoded from a String (/error1.json:7:26)

        - 'nested': - Could not instantiate 'com.sksamuel.hoplite.json.Wibble' because:

            - 'a': Required type java.time.LocalDateTime could not be decoded from a String (/error1.json:10:17)

            - 'b': Unable to locate a decoder for java.time.LocalTime
```

## Supported Formats

Hoplite supports config files in several formats. You can mix and match formats if you really want to.
For each format you wish to use, you must include the appropriate hoplite module on your classpath.
The format that hoplite uses to parse a file is determined by the file extension.

| Format | Module | File Extensions |
|:---|:---|:---|
| Json | [`hoplite-json`](https://search.maven.org/search?q=hoplite-json) | .json |
| [Yaml](https://yaml.org/) | [`hoplite-yaml`](https://search.maven.org/search?q=hoplite-yaml) | .yml, .yaml |
| [Toml](https://github.com/toml-lang/toml) | [`hoplite-toml`](https://search.maven.org/search?q=hoplite-toml) | .toml |
| [Hocon](https://github.com/lightbend/config) | [`hoplite-hocon`](https://search.maven.org/search?q=hoplite-hocon) | .conf |
| Java Properties files | built-in | .props, .properties |

If you wish to add another format you can extend `Parser` and provide an instance of that implementation to the `ConfigLoader.Builder` via `withFileExtensionMapping`.

That same function can be used to map non-default file extensions to an existing parser. For example, if you wish to have your config in files called `application.data` but in yaml format, then you can register .data with the Yaml parser like this:

`ConfigLoader.Builder().addFileExtensionMapping("data", YamlParser).build()`



## Property Sources

The `PropertySource` interface is how Hoplite reads configuration values.

Hoplite supports several built in property source implementations, and you can write your own if required.

The `EnvironmentVariablesPropertySource`, `SystemPropertiesPropertySource` and `UserSettingsPropertySource` sources are automatically registered, with
precedence in that order. Other property sources can be passed to the config loader builder.

### EnvironmentVariablesPropertySource

The `EnvironmentVariablesPropertySource` reads config from environment variables. It does not map cases so `HOSTNAME` does *not* provide a value for a field with the name `hostname`.

For nested config, use a period to seperate keys, for example `topic.name` would override `name` located in a `topic` parent.
Alternatively, in some environments a `.` is not supported in ENV names, so you can also use double underscore `__`. Eg `topic__name` would override name in a Topic object.

Optionally you can also create a `EnvironmentVariablesPropertySource` with `allowUppercaseNames` set to `true` to allows for uppercase-only names.

### SystemPropertiesPropertySource

The `SystemPropertiesPropertySource` provides config through system properties that are prefixed with `config.override.`.
For example, starting your JVM with `-Dconfig.override.database.name` would override a config key of `database.name` residing in a file.


### UserSettingsPropertySource

The `UserSettingsPropertySource` provides config through a config file defined at ~/.userconfig.[ext] where ext is one of the [supported formats](#supported-formats).


### InputStreamPropertySource

The `InputStreamPropertySource` provides config from an input stream. This source requires a parameter that indicates what the format is. For example, `InputStreamPropertySource(input, "yml")`


### ConfigFilePropertySource

Config from files or resources are retrieved via instances of `ConfigFilePropertySource`. This property source is added automatically when we pass
strings, `File`s or `Path`s to the `loadConfigOrThrow` or `loadConfig` functions.

There are convenience methods on `PropertySource` to construct `ConfigFilePropertySource`s from resources on the classpath or files.

For example, the following are equivalent:

```kotlin
ConfigLoader().loadConfig<MyConfig>("config.json")
```

and

```kotlin
ConfigLoader.Builder()
   .addSource(PropertySource.resource("/config.json"))
   .build()
   .loadConfig<MyConfig>()
```

The advantage of the second approach is that we can specify a file can be optional, for example:

```kotlin
ConfigLoader.Builder()
  .addSource(PropertySource.resource("/missing.yml", optional = true))
  .addSource(PropertySource.resource("/config.json"))
  .build()
  .loadConfig<MyConfig>()
```

### JsonPropertySource

To use a JSON string as a property source, we can use the `JsonPropertySource` implementation.
For example,

```kotlin
ConfigLoader.Builder()
   .addSource(JsonPropertySource(""" { "database": "localhost", "port": 1234 } """))
   .build()
   .loadConfig<MyConfig>()
```

### YamlPropertySource

To use a Yaml string as a property source, we can use the `YamlPropertySource` implementation.

```kotlin
ConfigLoader.Builder()
   .addSource(YamlPropertySource(
     """
        database: "localhost"
        port: 1234
     """))
   .build()
   .loadConfig<MyConfig>()
```

### TomlPropertySource

To use a Toml string as a property source, we can use the `TomlPropertySource` implementation.

```kotlin
ConfigLoader.Builder()
  .addSource(TomlPropertySource(
    """
        database = "localhost"
        port = 1234
     """))
  .build()
  .loadConfig<MyConfig>()
```

## Cascading Config

Hoplite has the concept of cascading or layered or fallback config.
This means you can pass more than one config file to the ConfigLoader.
When the config is resolved into Kotlin classes, a lookup will cascade or fall through one file to another in the order they were passed to the loader, until the first file that defines that key.

For example, if you had the following two files in yaml:

`application.yaml`:
```yaml
elasticsearch:
  port: 9200
  clusterName: product-search
```

`application-prod.yaml`:
```yaml
elasticsearch:
  host: production-elasticsearch.mycompany.internal
  port: 9202
```

And both were passed to the ConfigLoader like this: `ConfigLoader().loadConfigOrThrow<Config>("/application-prod.yaml", "/application.yaml")`, then lookups will be attempted in the order the files were declared.
So in this case, the config would be resolved like this:
```
elasticsearch.port = 9202 // the value in application-prod.yaml takes priority over the value in application.yaml
elasticsearch.host = production-elasticsearch.mycompany.internal
elasitcsearch.clusterName = product-search // not defined in application-prod.yaml so falls through to application.yaml
```

Let's see a more complicated example. In JSON this time.

`default.json`
```json
{
  "a": "alice",
  "b": {
    "c": true,
    "d": 123
  },
  "e": [
    {
      "x": 1,
      "y": true
    },
    {
      "x": 2,
      "y": false
    }
  ],
  "f": "Fall"
}
```

`prod.json`
```json
{
  "a": "bob",
  "b": {
    "d": 999
  },
  "e": [
    {
      "y": true
    }
  ]
}
```

And we will parse the above config files into these data classes:

```kotlin
enum class Season { Fall, Winter, Spring, Summer }
data class Foo(val c: Boolean, val d: Int)
data class Bar(val x: Int?, val y: Boolean)
data class Config(val a: String, val b: Foo, val e: List<Bar>, val f: Season)
```

```kotlin
val config = ConfigLoader.load("prod.json", "default.json")
println(config)
```

The resolution rules are as follows:

- "a" is present in both files and so is resolved from the first file - which was "prod.json"
- "b" is present in both files and therefore resolved from the file file as well
- "c" is a nested value of "b" and is not present in the first file so is resolved from the second file "default.json"
- "d" is a nested value of "b" present in both files and therefore resolved from the first file
- "e" is present in both files and so the entire list is resolved from the first file. This means that the list only contains a single element, and x is null despite being present in the list in the first file. List's cannot be merged.
- "f" is only present in the second file and so is resolved from the second file.



## Strict Mode

Hoplite can be configured to throw an error if a config value is not used. This is useful to detect stale configs.

To enable this setting, use `.strict()` on the config builder. For example:

```kotlin
ConfigLoader.Builder()
  .addSource(PropertySource.resource("/config-prd.yml", true))
  .addSource(PropertySource.resource("/config.yml"))
  .strict()
  .build()
  .loadConfig<MyConfig>()
```


## Aliases

If you wish to refactor your config classes and rename a field, but you don't want to have to update all your config files,
you can add a migration path by allowing a field to use more than one name. To do this we use the @ConfigAlias annotation.

For example, with this config file:

```yml
database:
  host: String
```

We can marshall this into the following data classes.

```kotlin
data class Database(val host: String)
data class MyConfig(val database: Database)
```

or

```kotlin
data class Database(@ConfigAlias("host") val hostname: String)
data class MyConfig(val database: Database)
```


## Param Mappers

Hoplite provides an interface `ParameterMapper` which allows the parameter name to be modified before it is looked up
inside a config source. This allows hoplite to find config keys which don't match the exact name. The main use case for
this is to allow `snake_case` or `kebab-case` names to be used as config keys.

For example, given the following config class:

```kotlin
data class Database(val instanceHostName: String)
```

Then we can of course define our config file (using YML as an example):

```yml
database:
    instanceHostName: server1.prd
```

But because Hoplite registers `KebabCaseParamMapper` and `SnakeCaseParamMapper` automatically, we can just as easily use:

```yml
database:
  instance-host-name: server1.prd
```

## Decoders

Hoplite converts the raw value in config files to JDK types using instances of the `Decoder` interface.
There are built in decoders for all the standard day to day types, such as primitives, dates, lists, sets, maps, enums, arrow types and so on. The full list is below:

| Basic JDK Types  | Conversion Notes |
|---|---|
| `String` |
| `Long` |
| `Int` |
| `Short` |
| `Byte` |
| `Boolean` | Creates a Boolean from the following values: `"true"`, `"t"`, `"1"`, `"yes"` map to `true` and `"false"`, `"f"`, `"0"`, `"no"` map to `false` |
| `Double` |
| `Float` |
| `Enums` | Java and Kotlin enums are both supported. An instance of the defined Enum class will be created with the constant value given in config. |
| `BigDecimal` | Converts from a String, Long, Int, Double, or Float into a BigDecimal |
| `BigInteger` | Converts from a String, Long or Int into a BigInteger. |
| `UUID` | Creates a `java.util.UUID` from a String |
| **java.time types** | |
| `LocalDateTime` |
| `LocalDate` |
| `LocalTime` |
| `Duration` | Converts a String into a Duration, where the string uses a value and unit such as "10 seconds" or "5m". The set of units supported is the same as [here](https://github.com/lightbend/config/blob/master/HOCON.md#duration-format). Also supports a long value which will be interpreted as a Duration of milliseconds. |
| `Instant` | Creates an instance of `Instant` from an offset from the unix epoc in milliseconds. |
| `Year` | Creates an instance of `Year` from a String in the format `2007` |
| `YearMonth` | Creates an instance of `YearMonth` from a String in the format `2007-12` |
| `MonthDay` | Creates an instance of `MonthDay` from a String in the format `08-18` |
| `java.util.Date` | |
| **java.net types** | |
| `URI` |  |
| `URL` |  |
| `InetAddress` |  |
| **JDK IO types** | |
| `File` | Creates a java.io.File from a String path |
| `Path` | Creates a java.nio.Path from a String path |
| **Kotlin stdlib types** | |
| `Pair<A,B>` | Converts from an array of three two into an instance of `Pair<A,B>`. Will fail if the array does not have exactly two elements. |
| `Triple<A,B,C>` | Converts from an array of three elements into an instance of `Triple<A,B,C>`. Will fail if the array does not have exactly three elements. |
| `kotlin.text.Regex` | Creates a `kotlin.text.Regex` from a regex compatible string |
| **Collections** | |
| `List<A>` | Creates a List from either an array or a string delimited by commas. |
| `Set<A>` | Creates a Set from either an array or a string delimited by commas. |
| `SortedSet<A>` | Creates a SortedSet from either an array or a string delimited by commas. |
| `Map<K,V>` | |
| `LinkedHashMap<K,V>` | A Map that mains the order defined in config |
| **hoplite types** | |
| `Masked` | Wraps a String in a Masked object that redacts toString() |
| `SizeInBytes` | Returns a SizeInBytes object which parses values like 12Mib or 9KB |
| **javax.security.auth** | |
| `X500Principal` | Creates an instance of `X500Principal` for String values |
| `KerberosPrincipal` | Creates an instance of `KerberosPrincipal` for String values |
| `JMXPrincipal` | Creates an instance of `JMXPrincipal` for String values |
| `Principal` | Creates an instance of `BasicPrincipal` for String values |
| **Arrow** | Requires `hoplite-arrow` module |
| `arrow.data.NonEmptyList<A>` | Converts arrays into a `NonEmptyList<A>` if the array is non empty. If the array is empty then an error is raised. |
| `arrow.core.Option<A>` | A `None` is used for null or undefined values, and present values are converted to a `Some<A>`. |
| `arrow.core.Tuple2<A,B>` | Converts an array of two elements into an instance of `Tuple2<A,B>`.  Will fail if the array does not have exactly two elements.|
| `arrow.core.Tuple3<A,B,C>` | Converts an array of three elements into an instance of `Tuple3<A,B,C>`. Will fail if the array does not have exactly three elements. |
| `arrow.core.Tuple4<A,B,C,D>` | Converts an array of four elements into an instance of `Tuple4<A,B,C,D>`. Will fail if the array does not have exactly four elements. |
| `arrow.core.Tuple5<A,B,C,D,E>` | Converts an array of five elements into an instance of `Tuple5<A,B,C,D,E>`. Will fail if the array does not have exactly five elements. |
| **Hikari Connection Pool** | Requires `hoplite-arrow` module |
| `HikariDataSource` | Converts nested config into a `HikariDataSource`. Any keys nested under the field name will be passed through to the `HikariConfig` object as the datasource is created. Requires `hoplite-hikaricp` module |
| **Hadoop Types** | Requires `hoplite-hdfs` module |
| `org.apache.hadoop.fs.Path` | Returns instances of HDFS Path objects |
| **CronUtils types** | Requires `hoplite-cronutils` module |
| `com.cronutils.model.Cron` | Returns parsed instance of a cron expression |
| **kotlinx datetime Types** | Requires `hoplite-datetime` module |
| `kotlinx.datetime.LocalDateTime` |  |
| `kotlinx.datetime.LocalDate` |  |
| `kotlinx.datetime.Instant` |  |
| **AWS SDK types** | Requires `hoplite-aws` module |
| `com.amazonaws.regions.Region` |

## Preprocessors

Hoplite supports what it calls preprocessors. These are just functions that are applied to every value as they are read from the underlying config file.
The preprocessor is able to transform the value (or return the input - aka identity function) depending on the logic of that preprocessor.

For example, a preprocessor may choose to perform environment variable substitution, configure default values,
perform database lookups, or whatever other custom action you need when the config is being resolved.

You can add custom pre-processors in addition to the built in ones, by using the function `withPreprocessor` on the `ConfigLoader` class, and passing in an instance of the `Preprocessor` interface.
A typical use case of a custom preprocessor is to lookup some values in a database, or from a third party secrets store such as [Vault](https://www.vaultproject.io/) or [Amazon Parameter Store](https://docs.aws.amazon.com/systems-manager/latest/userguide/systems-manager-parameter-store.html).

One way this can be implemented is to have a prefix, and then use a preprocessor to look for the prefix in strings, and if the prefix is present, use the rest of the string as a key to the service. The `PrefixProcessor` abstract class implements this by handling the node traversal, while leaving the specific processing as an exercise for the reader.

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
| EnvVar Preprocessor | Replaces any strings of the form ${VAR} with the environment variable $VAR if defined. These replacement strings can occur between other strings.<br/><br/>For example `foo: hello ${USERNAME}!` would result in foo being assigned the value `hello Sam!` assuming the env var `USERNAME` was set to `SAM`. Also the expressions can have default values using the usual bash expression style syntax `foo: hello ${USERNAME:-fallback}!` |
| System Property Preprocessor | Replaces any strings of the form ${VAR} with the system property $VAR if defined. These replacement strings can occur between other strings.<br/><br/>For example `debug: ${DEBUG}` would result in debug being assigned the value `true` assuming the application had been started with `-Ddebug=true` |
| Random Preprocessor | Inserts random strings into the config. See the section on Random Preprocessor for syntax. |
| Props File Preprocessor | Replaces any strings of the form ${key} with the value of the key in a provided `java.util.Properties` file. The file can be specified by a `Path` or a resource on the classpath. |
| Lookup Preprocessor | Replaces any strings of the form {{key}} with the value of that node in the already parsed config. In other words, this allow substitution from config in one place to another place (even across files). |
| AWS Parameter Store Preprocessor | Replaces strings of the form ${ssm:key} by looking up the value of key from the AWS parameter store.<br/><br/>This preprocessor requires the `hoplite-aws` module to be added to the classpath. |

### Random Preprocessor

The random preprocessor replaces placeholder strings with random values.

| Placeholder           | Generated random value |
|:----------------------|:------------------------------------------------------------|
| ${random.int}         | A random int |
| ${random.int(k)}      | A positive random int between 0 and k |
| ${random.int(k, j)}   | A random int between k and j |
| ${random.double}      | A random double |
| ${random.boolean      | A random boolean |
| ${random.string(k)}   | A random alphanumeric string of length k |
| ${random.uuid}        | A randomly generated type 4 UUID |

For example:

```
my.number=${random.int}
my.bignumber=${random.long}
my.uuid=${random.uuid}
my.number.less.than.ten=${random.int(10)}
my.number.in.range=${random.int[1024,65536]}
```

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

Note: The masking effect only happens if you use `toString`.
If you marshall your config to a String using a reflection based tool like Jackson, it will still be able to see the underlying value.
In these cases, you would need to register a custom serializer.
For the Jackson project, a `HopliteModule` object is available in the `hoplite-json` module.
Register this with your Jackson mapper, like `mapper.registerModule(HopliteModule)` and then `Masked` values will be ouputted into Json as "****"

## Inline Classes

Some developers, this writer included, like to have strong types wrapping simple values. For example, a `Port` object rather than an Int.
This helps to alleviate Stringy typed development.
Kotlin has support for what it calls inline classes which fulfil this need.

Hoplite directly supports inline classes.
When using inline classes, you don't need to nest config keys.

For example, given the following config classes:

```kotlin
inline class Port(val value: Int)
inline class Hostname(val value: String)
data class Database(val port: Port, val host: Hostname)
```

And then this config file:

```yaml
port: 9200
host: locahost
```

We can parse directly:

```kotlin
val config = ConfigLoader().loadConfigOrThrow<Database>("config.file")
println(config.port) // Port(9200)
println(config.host) // Hostname("localhost")
```


## Sealed Classes

Hoplite will support sealed classes where it is able to match up the available config keys with the parameters of one of the implementations. For example, lets create a a config hierarchy as implementations of a sealed class.

```kotlin
sealed class Database {
  data class Elasticsearch(val host: String, val port: Int, val index: String) : Database()
  data class Postgres(val host: String, val port: Int, val schema: String, val table: String) : Database()
}

data class TestConfig(val databases: List<Database>)
```

For the above implementations, if hoplite encountered a `host`, `port`, and `index` then it would be clear that it should instantiate an `Elasticsearch` instance. Similarly, if the config keys were `host`, `port`, `schema`, and `table`, then the `Postgres` implementation should be used. If the keys don't match an implementation, the config loader would fail. If keys match multiple implementations then the first match is taken.

For example, the following yaml config file could be used:

```yaml
databases:
  - host: localhost
    port: 9200
    index: foo
  - host: localhost
    port: 9300
    index: bar
  - host: localhost
    port: 5234
    schema: public
    table: faz
```

And the output would be:

```
TestConfig(
  databases=[
    Elasticsearch(host=localhost, port=9200, index=foo),
    Elasticsearch(host=localhost, port=9300, index=bar),
    Postgres(host=localhost, port=5234, schema=public, table=faz)
  ]
)
```

## Add on Modules

Hoplite makes available several other modules that add functionality outside of the main core module. They are in seperate modules because they bring in dependencies from those projects and so the modules are optional.

| Module         | Function          |
|:---------------|:------------------|
| hoplite-arrow  | Provides decoders for common arrow types |
| hoplite-aws    | Provides decoder for aws `Region` and a preprocessor for Amazon's parameter store |
| hoplite-datetime  | Provides decoders for [kotlinx datetime](https://github.com/Kotlin/kotlinx-datetime). Requires Kotlin 1.4.x |
| hoplite-hdfs   | Provides decoder for hadoop `Path` |
| hoplite-hikaricp | Provides decoder for `HikariDataSource` |
| hoplite-javax  | Provides decoders for Principals |
| hoplite-vavr  | Provides decoders for [vavr](https://github.com/vavr-io/vavr) |

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
