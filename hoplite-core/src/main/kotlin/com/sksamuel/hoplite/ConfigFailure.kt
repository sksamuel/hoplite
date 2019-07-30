package com.sksamuel.hoplite

import kotlin.reflect.KClass
import kotlin.reflect.KType

val KType.simpleName: String
  get() = when (this.classifier) {
    String::class -> "String"
    Long::class -> "Long"
    Double::class -> "Double"
    Boolean::class -> "Boolean"
    else -> this.toString()
  }

sealed class ConfigFailure {

  /**
   * A human-readable description of the failure.
   */
  abstract fun description(): String

  /**
   * The optional location of the failure.
   */
  abstract fun pos(): Pos

  companion object {
    @Deprecated("Use sealed type")
    operator fun invoke(description: String): ConfigFailure = GenericFailure(description)
    inline fun <reified T> conversionFailure(v: Any?): ConfigFailure = ConversionFailure(T::class, v)
  }

  data class NoSuchParser(val file: String) : ConfigFailure() {
    override fun description(): String = "Could not detect parser for file extension $file"
    override fun pos(): Pos = Pos.NoPos
  }

  data class UnknownSource(val source: String) : ConfigFailure() {
    override fun description(): String = "Could not find config file $source"
    override fun pos(): Pos = Pos.NoPos
  }

  data class TypeConversionFailure(val node: Node, val path: String, val target: KType) : ConfigFailure() {
    override fun description(): String = "$path was defined as a ${target.simpleName} but was a ${node.simpleName} in config"
    override fun pos(): Pos = node.pos
  }

  data class UnsupportedListType(val node: Node, val path: String) : ConfigFailure() {
    override fun description(): String = "$path was defined as a list but ${node.simpleName} cannot be converted to a list"
    override fun pos(): Pos = node.pos
  }

  data class NullValueForNonNullField(val node: NullNode, val path: String) : ConfigFailure() {
    override fun description(): String = "Null value provided for non null path $node"
    override fun pos(): Pos = node.pos
  }

  data class NoSuchDecoder(val type: KType, val path: String) : ConfigFailure() {
    override fun description(): String = "Unable to locate decoder for type $type defined at $path"
    override fun pos(): Pos = Pos.NoPos
  }

  data class MissingValue(val path: String) : ConfigFailure() {
    override fun description(): String = "$path was missing from config"
    override fun pos(): Pos = Pos.NoPos
  }

  data class Generic(val msg: String) : ConfigFailure() {
    override fun description(): String = msg
    override fun pos(): Pos = Pos.NoPos
  }

  data class InvalidEnumConstant(val node: Node,
                                 val path: String,
                                 val type: KType,
                                 val value: String) : ConfigFailure() {
    override fun description(): String = "Enum constant $value is not valid for ${node.dotpath}"
    override fun pos(): Pos = node.pos
  }
}

data class ThrowableFailure(val throwable: Throwable) : ConfigFailure() {
  override fun description() = "${throwable.message}.${throwable.stackTrace.toList()}"
  override fun pos(): Pos = Pos.NoPos
}

data class GenericFailure(val description: String) : ConfigFailure() {
  override fun description(): String = description
  override fun pos(): Pos = Pos.NoPos
}

/**
 * A [ConfigFailure] used when a target type could not be created from a given value.
 * For example, if a field in data class was an int, but at runtime the configuration
 * tried to pass "hello" then this would result in a conversion failure.
 */
data class ConversionFailure(val description: String) : ConfigFailure() {
  constructor(klass: KClass<*>, value: Any?) :
    this("Cannot convert ${value?.javaClass?.name}:$value to ${klass.qualifiedName}")

  override fun description() = description
  override fun pos(): Pos = Pos.NoPos
}
