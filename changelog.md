# Changelog

## 1.3.14

* Added {{style}} syntax for lookups across files.

## 1.3.13

* Fixed value types to allow for underlying types that are not strings. Eg, `data class Weight(val value: Int)`

## 1.3.12

* Allows data classes with a single field named `value` to be treated as inline classes. Eg `data class RetailerId(val value: String)` can be parsed directly from `retailerId: "SAKS"` without requiring another level of nesting #164

## 1.3.11

* Added `hoplite-vavr` module #185
* Added convenience methods to `PropertySource` to read from strings, streams and optional paths #186

## 1.3.10

* Fixed decoding of sets of enums #181

## 1.3.9

* Added `kotlinx-datetime` module #166
* Fix parser registry for custom mappings #179
