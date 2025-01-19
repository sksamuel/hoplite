# Changelog

### 3.0.0

* Breaking: the `EnvironmentVariablesPropertySource` now uses idiomatic environment variable format i.e. upper case (though lower
case is still accepted), letters, and digits. **Single underscores** now separate path hierarchies.
* Breaking: The `EnvironmentVariableOverridePropertySource` has been removed. The standard `EnvironmentVariablesPropertySource` is now
loaded by default, and takes precedence over other default sources just like the `EnvironmentVariableOverridePropertySource`
did. To maintain similar behavior, configure it with a filtering `prefix`.
* Add the ability to load a series of environment variables into arrays/lists via the `_n` syntax.

### 2.7.5

* Use daemon threads in `FileWatcher` to enable clean shutdown.

### 2.7.4

* Add withPrintFn option to the config builder to support logging config with a user defined function

### 2.7.3

* Add better yaml errors when invalid file #354
* Fix formatting for secrets which contain new lines #363
* Moves `GcpSecretManagerPreprocessor` from azure to gcp package #362

### 2.7.2

* Added BooleanArrayDecoder
* Updated to latest SnakeYML version to avoid CVE

### 2.7.1

* Added support for `ByteArray`.

### 2.7.0

* TOML now supports heterogenous arrays.

### 2.6.5

* Add `prefix` option to `EnvironmentVariablesPropertySource` which is then stripped from env vars before resolution is applied.

### 2.6.4

* Correct conversion between bytes and other units #347

### 2.6.3

* Fixed NPE in reporter with empty sources #345
* Do not add mask to prefix if string is less than prefix length

### 2.6.2

* Avoid reporting remote keys used in multiple places.

### 2.6.1

* Add fallback version of `Environment.forEnvVar`
* Provide `Environment` to decoders and preprocessors through the `DecoderContext`.

### 2.6.0

* Added `Environment` abstraction
* Added `XdgConfigPropertySource` for loading config from $XDG_CONFIG_HOME
* Added support for [GCP Secrets Manager](https://cloud.google.com/secret-manager). To use import `hoplite-gcp` and set your keys to be `gcpsm://mykey`.
* Added support for AWS Secrets Manager using Amazon's SDK version 2. The syntax is the same, but import `hoplite-aws2` instead of `hoplite-aws`.
* Added `CascadeMode` to control how config should cascade through files.
* Added decoder for `java.util.Locale`
* `PrefixObfuscator` can now be configured by prefix length and mask.
* Added remote-lookup details to the report to show keys used from AWS Secrets Manager, GCP cloud etc.
* Support squashing arrays into a comma delimited list by setting `flattenArraysToString` on the config builder. #339
* Support map lookup in AWS preprocessors #341
* Fix sealed subclass picking #338

**Breaking Changes**

These breaking changes are to advanced customization features. Most users will need to change no code.

* `ReporterBuilder` has been removed - specify the _obfuscator_ and _secrets policy_ directly on the `ConfigLoaderBuilder`.
* `SecretsPolicy` interface has been changed to directly use `Node`s and not just `Path`s.
* The `Preprocessor` interface has changed to have an extra parameter named `DecoderContext`.

### 2.5.2

* Fixed sealed class ordering in Kotlin 1.7.x #331
* Unable to decode Double and Float from a whole number #330

### 2.5.1

* Fixed snake/kebab case keys that end with a number [#238](https://github.com/sksamuel/hoplite/issues/328)

### 2.5.0

* Added an automatically registered `EnvironmentVariablesOverridePropertySource` which allows for environment variables of the form `config.override.foo` to be automatically resolved as `foo`.

### 2.4.0

* Added a `AzureKeyVaultPreprocessor` to support `azurekeyvault://key` syntax for fetching secrets from Azure Key Vault.
  Requires the `hoplite-azure` module.
* Added a `VaultSecretPreprocessor` to support `vault://key` syntax for fetching secrets from a Hashicorp Valut
  instance. Requires the `hoplite-vault` module.

### 2.3.3

* Fix for TOML keys containing dots are not decoded properly in maps #322

### 2.3.2

* Fix for unused config false-positive when using strict mode and maps #320

### 2.3.1

* Fixed anchors in yaml when using multiple files #307
* Added `subscribe` to `ReloadableConfig`

### 2.3.0

* Added `Base64` type and decoder
* Added `hoplite-micrometer-datadog` module
* Added `hoplite-micrometer-prometheus` module
* Added `hoplite-micrometer-statsd` module
* Updated AWS secrets preprocessor to support `awssm://key` syntax
* Updated Consul preprocessor to support `consul://key` syntax
* Added `withPreprocessingIterations` to support repeated applications of preprocessors
* Added `PropsPropertySource` to programmatically provide a `Properties` based source.

### 2.2.0

* Added alphabetical key sort in reports #306
* ***Breaking change*** Added `constructor` and `KClass` parameters to the `ParameterMapper` interface to allow inspection of
  annotations on fields #311
* Fixed snakecase variable throwing unused error with _strict()_ #312
* Fixed sealed type subclass decode failure when single string field #313

### 2.1.5

* Added `withClassLoader` option to the `ConfigLoaderBuilder`. This classloader is used when loading the service
  registry for decoders.

### 2.1.4

* Print report even when config fails to parse.

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

* Support for [yaml aliases](https://bitbucket.org/asomov/snakeyaml-engine/wiki/Documentation#markdown-header-aliases)
  #208.

### 1.4.0

* **Kotlin version is now 1.4.30**
* Breaking: The `PropertySource` interface has been changed to accept a `PropertySourceContext`. This only affects users
  who have written their own custom PropertySource.
* Added `@ConfigAlias` to allow a data class field to map to multiple values. #197
* Added strict option to `ConfigLoader.Builder` to throw an error if a config value is unused. Lenient mode is still the
  default. #187
* Bumped all module deps to latest versions

### 1.3.15

* Added support for ISO format in `Instant` decoder #192
* Added suport for objects in sealed classes #194

### 1.3.14

* Added {{style}} syntax for lookups across files.

### 1.3.13

* Fixed value types to allow for underlying types that are not strings. Eg, `data class Weight(val value: Int)`

### 1.3.12

* Allows data classes with a single field named `value` to be treated as inline classes.
  Eg `data class RetailerId(val value: String)` can be parsed directly from `retailerId: "SAKS"` without requiring
  another level of nesting #164

### 1.3.11

* Added `hoplite-vavr` module #185
* Added convenience methods to `PropertySource` to read from strings, streams and optional paths #186

### 1.3.10

* Fixed decoding of sets of enums #181

### 1.3.9

* Added `kotlinx-datetime` module #166
* Fix parser registry for custom mappings #179
