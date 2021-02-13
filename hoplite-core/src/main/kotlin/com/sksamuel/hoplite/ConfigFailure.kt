package com.sksamuel.hoplite

import com.sksamuel.hoplite.decoder.Decoder
import com.sksamuel.hoplite.fp.NonEmptyList
import com.sksamuel.hoplite.parsers.Parser
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.jvm.jvmName

val KType.simpleName: String
  get() = when (this.classifier) {
    String::class -> "String"
    Long::class -> "Long"
    Int::class -> "Int"
    Double::class -> "Double"
    Float::class -> "Float"
    Boolean::class -> "Boolean"
    else -> this.classifier.toString()
  }

sealed class ConfigFailure {

  /**
   * A human-readable description of the failure.
   */
  abstract fun description(): String

  data class NoSuchParser(
    val file: String,
    val map: Map<String, Parser>
  ) : ConfigFailure() {
    override fun description(): String =
      "Could not detect parser for file extension '.$file' - available parsers are $map"
  }

  data class InvalidConstructorParameters(
    val type: KType,
    val constructor: KFunction<*>,
    val args: Map<KParameter, Any?>,
    val e: Throwable
  ) : ConfigFailure() {
    override fun description(): String =
      "Could not instantiate ${type.simpleName} from args " +
        "${args.map { it.value?.javaClass?.name ?: "<null>" }}: " +
        "Expected args are ${constructor.parameters.map { it.type.simpleName }}. Underlying error was $e"
  }

  data class DataClassWithoutConstructor(val kclass: KClass<*>) : ConfigFailure() {
    override fun description(): String = "Data class ${kclass.qualifiedName} has no constructors"
  }

  data class UnusedConfigValues(val values: List<String>) : ConfigFailure() {
    override fun description(): String = "Config values were not used: ${values.joinToString(", ")}"
  }

  data class UnknownSource(val source: String) : ConfigFailure() {
    override fun description(): String = "Could not find config file $source"
  }

  data class MultipleFailures(val failures: NonEmptyList<ConfigFailure>) : ConfigFailure() {
    override fun description(): String = failures.map { it.description() }.list.joinToString("\n\n")
  }

  data class NoSealedClassSubtype(val type: KClass<*>, val node: Node) : ConfigFailure() {
    override fun description(): String {
      val subclasses = type.sealedSubclasses.joinToString(", ") { it.jvmName }
      return "Could not find appropriate subclass of $type: Tried $subclasses ${node.pos.loc()}"
    }
  }

  data class SealedClassWithoutImpls(val type: KClass<*>) : ConfigFailure() {
    override fun description(): String = "Sealed class $type does not define any subclasses"
  }

  data class MissingPrimaryConstructor(val type: KType) : ConfigFailure() {
    override fun description(): String = "$type does not implement a primary constructor"
  }

  object EmptyDecoderRegistry : ConfigFailure() {
    override fun description(): String = "No decoders are registered"
  }

  object NoDataClassDecoder : ConfigFailure() {
    override fun description(): String =
      "No data-class decoder. Did you build a fat-jar? If so, you must choose to merge service files"
  }

  data class IncompatibleInlineType(val type: KType, val node: Node) : ConfigFailure() {
    override fun description(): String = when (node) {
      is PrimitiveNode -> "Inline type $type is incompatible with a ${node.simpleName} value: ${node.value} ${node.pos.loc()}"
      else -> "Inline type is incompatible with $node ${node.pos.loc()}"
    }
  }

  /**
   * A [ConfigFailure] used when a target type could not be created from a given value.
   * For example, if a field in data class was an int, but at runtime the configuration
   * tried to pass "hello" then this would result in a conversion failure.
   */
  data class DecodeError(val node: Node, val target: KType) : ConfigFailure() {
    override fun description(): String = when (node) {
      is PrimitiveNode -> "Required type ${target.simpleName} could not be decoded from a ${node.simpleName} value: ${node.value} ${node.pos.loc()}"
      else -> "Required type ${target.simpleName} could not be decoded from a ${node.simpleName} ${node.pos.loc()}"
    }
  }

  data class UnsupportedCollectionType(val node: Node, val type: String) : ConfigFailure() {
    override fun description(): String =
      "Required a $type but a ${node.simpleName} cannot be converted to a collection ${node.pos.loc()}"
  }

  data class NullValueForNonNullField(val node: Node) : ConfigFailure() {
    override fun description(): String = "Type defined as not-null but null was loaded from config ${node.pos.loc()}"
  }

  data class NoSuchDecoder(
    val type: KType,
    val decoders: List<Decoder<*>>
  ) : ConfigFailure() {
    override fun description(): String =
      "Unable to locate a decoder for ${type.simpleName}"
  }

  data class NumberConversionError(val node: Node, val type: KType) : ConfigFailure() {
    override fun description(): String = when (node) {
      is PrimitiveNode -> "Could not decode ${node.value} into a ${type.simpleName} ${node.pos.loc()}"
      else -> "Could not decode a ${node.simpleName} into a number ${node.pos.loc()}"
    }
  }

  object MissingValue : ConfigFailure() {
    override fun description(): String = "Missing from config"
  }

  data class Generic(val msg: String) : ConfigFailure() {
    override fun description(): String = msg
  }

  data class CollectionElementErrors(val node: Node, val errors: NonEmptyList<ConfigFailure>) : ConfigFailure() {
    override fun description(): String = "Collection element decode failure ${node.pos.loc()}:\n\n" +
      errors.list.joinToString("\n\n") { it.description().indent(Constants.indent) }
  }

  data class TupleErrors(val node: Node, val errors: NonEmptyList<ConfigFailure>) : ConfigFailure() {
    override fun description(): String = "- Could not instantiate Tuple because:\n\n" +
      errors.list.joinToString("\n\n") { it.description().indent(Constants.indent) }
  }

  data class InvalidEnumConstant(
    val node: Node,
    val type: KType,
    val value: String
  ) : ConfigFailure() {
    override fun description(): String =
      "Required a value for the Enum type $type but given value was $value ${node.pos.loc()}"
  }

  data class DataClassFieldErrors(
    val errors: NonEmptyList<ConfigFailure>,
    val type: KType,
    val pos: Pos
  ) : ConfigFailure() {
    override fun description(): String = "- Could not instantiate '$type' because:\n\n" +
      errors.list.joinToString("\n\n") { it.description().indent(Constants.indent) }
  }

  data class ParamFailure(val param: KParameter, val error: ConfigFailure) : ConfigFailure() {
    override fun description(): String = "- '${param.name}': ${error.description()}"
  }

  data class ValueTypeFailure(
    val klass: KClass<*>,
    val param: KParameter,
    val error: ConfigFailure
  ) : ConfigFailure() {
    override fun description(): String = "Could not create value type for $klass at '${param.name}': ${error.description()}"
  }
}

data class ThrowableFailure(val throwable: Throwable) : ConfigFailure() {
  override fun description() = "${throwable.message}.${throwable.stackTrace.toList()}"
}

fun String.indent(indent: String = "    "): String {
  val lines = lineSequence()
    .map {
      when {
        it.isBlank() -> it.trim()
        else -> indent + it
      }
    }
  return lines.joinToString("\n")
}
