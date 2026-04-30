package com.sksamuel.hoplite

/**
 * Annotates an enum class with a default value to use when the configured value
 * does not match any enum constant.
 *
 * For example, given the following enum:
 *
 * ```
 * @ConfigEnumDefault("Unknown")
 * enum class Color { Red, Blue, Green, Unknown }
 * ```
 *
 * a config that supplies `bgColor: Yellow` will decode to `Color.Unknown` instead
 * of failing with an invalid enum constant error. Useful when consumers may
 * encounter values produced by newer versions of a system that have not yet been
 * added to the enum.
 *
 * The [name] must match an enum constant declared in the annotated class.
 * Matching honours the loader's case-insensitive setting.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ConfigEnumDefault(val name: String)
