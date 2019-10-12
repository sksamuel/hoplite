package com.sksamuel.hoplite

import arrow.data.NonEmptyList
import kotlin.reflect.KType

val KType.simpleName: String
  get() = when (this.classifier) {
    String::class -> "String"
    Long::class -> "Long"
    Int::class -> "Int"
    Double::class -> "Double"
    Float::class -> "Float"
    Boolean::class -> "Boolean"
    else -> this.toString()
  }

sealed class ConfigFailure {

  /**
   * A human-readable description of the failure.
   */
  abstract fun description(): String

  data class NoSuchParser(val file: String) : ConfigFailure() {
    override fun description(): String = "Could not detect parser for file extension $file"
  }

  data class UnknownSource(val source: String) : ConfigFailure() {
    override fun description(): String = "Could not find config file $source"
  }

  data class MultipleFailures(val failures: NonEmptyList<ConfigFailure>) : ConfigFailure() {
    override fun description(): String = failures.map { it.description() }.all.joinToString("\n\n")
  }

  /**
   * A [ConfigFailure] used when a target type could not be created from a given value.
   * For example, if a field in data class was an int, but at runtime the configuration
   * tried to pass "hello" then this would result in a conversion failure.
   */
  data class DecodeError(val value: TreeNode, val target: KType) : ConfigFailure() {
    override fun description(): String = when (value) {
      is PrimitiveNode -> "Required type ${target.simpleName} could not be decoded from a ${value.simpleName} value: ${value.value()} ${value.pos.loc()}"
      else -> "Required type ${target.simpleName} could not be decoded from a ${value.simpleName} ${value.pos.loc()}"
    }
  }

  data class UnsupportedCollectionType(val value: TreeNode, val type: String) : ConfigFailure() {
    override fun description(): String = "Defined as a $type but a ${value.simpleName} cannot be converted to a collection ${value.pos.loc()}"
  }

  data class NullValueForNonNullField(val node: NullNode) : ConfigFailure() {
    override fun description(): String = "Type defined as not-null but null was loaded from config ${node.pos.loc()}"
  }

  data class NoSuchDecoder(val type: KType) : ConfigFailure() {
    override fun description(): String = "Unable to locate a decoder for $type"
  }

  data class NumberConversionError(val value: TreeNode, val type: KType) : ConfigFailure() {
    override fun description(): String = when (value) {
      is PrimitiveNode -> "Could not decode ${value.value()} into a ${type.simpleName} ${value.pos.loc()}"
      else -> "Could not decode a ${value.simpleName} into a number ${value.pos.loc()}"
    }
  }

  object MissingValue : ConfigFailure() {
    override fun description(): String = "Missing from config"
  }

  data class Generic(val msg: String) : ConfigFailure() {
    override fun description(): String = msg
  }

  data class CollectionElementErrors(val value: TreeNode, val errors: NonEmptyList<ConfigFailure>) : ConfigFailure() {
    override fun description(): String = "Collection element decode failure"
  }

  data class TupleErrors(val value: TreeNode, val errors: NonEmptyList<ConfigFailure>) : ConfigFailure() {
    override fun description(): String = "Collection element decode failure"
  }

  data class InvalidEnumConstant(val node: TreeNode,
                                 val type: KType,
                                 val value: String) : ConfigFailure() {
    override fun description(): String = "Required a value for the Enum type $type but given value was $value ${node.pos.loc()}"
  }

  data class DataClassFieldErrors(val errors: NonEmptyList<ConfigFailure>,
                                  val type: KType,
                                  val pos: Pos) : ConfigFailure() {
    override fun description(): String = "- Could not instantiate '$type' because:\n\n" +
      errors.all.joinToString("\n\n") { it.description().prependIndent(Constants.indent) }
  }

  data class ParamFailure(val param: String, val error: ConfigFailure) : ConfigFailure() {
    override fun description(): String = "- '$param': ${error.description()}"
  }
}

data class ThrowableFailure(val throwable: Throwable) : ConfigFailure() {
  override fun description() = "${throwable.message}.${throwable.stackTrace.toList()}"
}
