# Changelog

### 2.1.3

* Added aws2 module for AWS support using the AWS SDK version 2

### 2.1.2

* Report should print after preprocessors have run.

### 2.1.1

* Fixed bug in `DefaultObfuscator` to avoid obfuscating booleans/numbers in yml.

### 2.1.0

* Optionally make using an unresolved substitution value not an error (#300)
* Fixed unresolved error typo (#301)
* Trim input keys in `AwsSecretsManagerPreprocessor`
* Better errors when key are missing in `AwsSecretsManagerPreprocessor`
* Updated default obfuscator to not obfuscate non-strings (#299)
* AWS ParameterStorePathPropertySource should support GetParametersByPathResult's nextToken (#295)

### 2.0.3

* Added `allowEmptyTree` on `ConfigLoaderBuilder` to _not_ error if all property sources return no values.

### 2.0.2

* Added `SecretsPolicy` to `Reporter` to determine which fields should be obfuscated.
* Adedd `HashObfuscator` to use the first 8 characters of the SHA-256 for obfuscation.

### 2.0.1

* Improved error messages from AWS Secrets Manager
* Adjusted default obfuscator to obfuscate all fields
* Fixed arg names used in constructor error message

### 2.0.0

#### Breaking Changes

* Requires Kotlin 1.6 or higher
* Requires JDK 11 or higher
* `ConfigLoader.Builder` has been removed and replaced with `ConfigLoaderBuilder` which has clearer semantics around
  defaults, and how to override defaults.
* The env vars property source is no longer registered by default. Env vars are typically used to override specific
  config values, not as an entire source of values, so this avoids some subtle runtime bugs.
* Using an unresolved substitution value, eg ${foo} where foo doesn't exist, is now an error.
* The `ConfigLoader.loadConfig` functions that accept a File or Path have been removed to simply the config loader
  class. Instead, use the equivalent methods on `ConfigLoaderBuilder`
* `ParameterMapper`s now return `Set<String>` rather than `String` to allow each parameter mapper to return more than
  one alternative name. This is only of relevance if you have written custom parameter mappers.
* `Preprocessor` now returns errors as types, rather than throwing exceptions. This is only of relevance if you have
  written custom preprocessors.

#### New Features

* `ConfigLoaderBuilder.report` has been added to output a report of the property sources, the resolved config values (
  obfuscated), and which config values were unused.
* `ConfigLoaderBuilder.strict` mode reports unused config values at any level. If a property source provides a value
  that is not used, the config loader will error when strict mode is on.
* Multiple `@ConfigAlias` annotations are now supported per field
* Better error handling on preprocessors.

### 1.4.16

* Added support for sealed class objects in Json by defining empty maps #245

### 1.4.15

* Fixed regression with multiple constructors introduced in 1.4.10

### 1.4.14

* Moved LocalTimeDecoder to core module

### 1.4.13

* Fixed regression with ConfigSouce::fromPath #234

### 1.4.12

* Added ParameterStorePathPreprocessor
* Added ParameterStorePathPropertySource
* add LocalTimeDecoder (#233) - Jan Brezina

### 1.4.11

* Added order to defaults (Sources, Preprocessors and ParamMappers) (#229) - David Gomes

### 1.4.10

* Added extension methods for Minutes and Seconds to Kotlin durations.
* data class with multiple constructors can select appropriate constructor to load (#231) - alexis-airwallex

### 1.4.9

* Added `Minutes` and `Seconds` as types and added decoders for both.

### 1.4.8

* Added support for watchers via the `hoplite-watch` and `hoplite-watch-consul` modules.
* Added config builder option to not include the default decoders and processors.

### 1.4.7

* Re-added support for Java8

### 1.4.6

* Added [AwsSecretsManagerPreprocessor](https://aws.amazon.com/secrets-manager/) preprocessor.
* Bumped AWS client version to 1.12.36

### 1.4.5

* Added [Consul](https://www.consul.io/) preprocessor.

### 1.4.4

* Adds basic command line property source. (#217)

### 1.4.3

* Arrow moduled upgraded to arrow 0.13.2

### 1.4.2

* Added overload to support config when the config class is not reified. #213

### 1.4.1

* Support for [yaml aliases](https://bitbucket.org/asomov/snakeyaml-engine/wiki/Documentation#markdown-header-aliases) #208.

### 1.4.0

* **Kotlin version is now 1.4.30**
* Breaking: The `PropertySource` interface has been changed to accept a `PropertySourceContext`. This only affects users who have written their own custom PropertySource.
* Added `@ConfigAlias` to allow a data class field to map to multiple values. #197
* Added strict option to `ConfigLoader.Builder` to throw an error if a config value is unused. Lenient mode is still the default. #187
* Bumped all module deps to latest versions

### 1.3.15

* Added support for ISO format in `Instant` decoder #192
* Added suport for objects in sealed classes #194

### 1.3.14

* Added {{style}} syntax for lookups across files.

### 1.3.13

* Fixed value types to allow for underlying types that are not strings. Eg, `data class Weight(val value: Int)`

### 1.3.12

* Allows data classes with a single field named `value` to be treated as inline classes. Eg `data class RetailerId(val value: String)` can be parsed directly from `retailerId: "SAKS"` without requiring another level of nesting #164

### 1.3.11

* Added `hoplite-vavr` module #185
* Added convenience methods to `PropertySource` to read from strings, streams and optional paths #186

### 1.3.10

* Fixed decoding of sets of enums #181

### 1.3.9

* Added `kotlinx-datetime` module #166
* Fix parser registry for custom mappings #179
