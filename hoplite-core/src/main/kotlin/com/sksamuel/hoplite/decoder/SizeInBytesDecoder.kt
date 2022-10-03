@file:Suppress("unused")

package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.ConfigFailure.DecodeError
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import kotlin.reflect.KType

data class SizeInBytes(val size: Long) {

  fun bytes() = size
  fun octets() = convert(InformationUnit.Octets)
  fun kilobytes() = convert(InformationUnit.Kilobytes)
  fun kibibytes() = convert(InformationUnit.Kibibytes)
  fun megabytes() = convert(InformationUnit.Megabytes)
  fun mebibytes() = convert(InformationUnit.Mebibytes)
  fun gigabytes() = convert(InformationUnit.Gigabytes)
  fun gibibytes() = convert(InformationUnit.Gibibytes)

  fun convert(unit: InformationUnit): Long = (size / unit.ratioToPrimary).toLong()

  companion object {
    fun parse(input: String): SizeInBytes? {
      val (size, symbol) = "(\\d+)(.*?)".toRegex().matchEntire(input.trim())?.destructured ?: return null
      val unit = InformationUnit::class.sealedSubclasses
        .mapNotNull { it.objectInstance }
        .find { it.symbol.equals(symbol.trim(), ignoreCase = true) }
        ?: return null
      val bytes = unit.ratioToPrimary * size.toLong()
      return SizeInBytes(bytes.toLong())
    }
  }
}

sealed class InformationUnit {

  abstract val symbol: String
  abstract val ratioToPrimary: Double

  object Bytes : InformationUnit() {
    override val symbol = "B"
    override val ratioToPrimary: Double = 1.0
  }

  object Octets : InformationUnit() {
    override val ratioToPrimary = 1 / 8.0
    override val symbol = "o"
  }

  object Kilobytes : InformationUnit() {
    override val ratioToPrimary = MetricSystem.Kilo
    override val symbol = "KB"
  }

  object Kibibytes : InformationUnit() {
    override val ratioToPrimary = BinarySystem.Kilo
    override val symbol = "KiB"
  }

  object Megabytes : InformationUnit() {
    override val ratioToPrimary = MetricSystem.Mega
    override val symbol = "MB"
  }

  object Mebibytes : InformationUnit() {
    override val ratioToPrimary = BinarySystem.Mega
    override val symbol = "MiB"
  }

  object Gigabytes : InformationUnit() {
    override val ratioToPrimary = MetricSystem.Giga
    override val symbol = "GB"
  }

  object Gibibytes : InformationUnit() {
    override val ratioToPrimary = BinarySystem.Giga
    override val symbol = "GiB"
  }

  object Terabytes : InformationUnit() {
    override val ratioToPrimary = MetricSystem.Tera
    override val symbol = "TB"
  }

  object Tebibytes : InformationUnit() {
    override val ratioToPrimary = BinarySystem.Tera
    override val symbol = "TiB"
  }

  object Petabytes : InformationUnit() {
    override val ratioToPrimary = MetricSystem.Peta
    override val symbol = "PB"
  }

  object Pebibytes : InformationUnit() {
    override val ratioToPrimary = BinarySystem.Peta
    override val symbol = "PiB"
  }

  object Exabytes : InformationUnit() {
    override val ratioToPrimary = MetricSystem.Exa
    override val symbol = "EB"
  }

  object Exbibytes : InformationUnit() {
    override val ratioToPrimary = BinarySystem.Exa
    override val symbol = "EiB"
  }

  object Zettabytes : InformationUnit() {
    override val ratioToPrimary = MetricSystem.Zetta
    override val symbol = "ZB"
  }

  object Zebibytes : InformationUnit() {
    override val ratioToPrimary = BinarySystem.Zetta
    override val symbol = "ZiB"
  }

  object Yottabytes : InformationUnit() {
    override val ratioToPrimary = MetricSystem.Yotta
    override val symbol = "YB"
  }

  object Yobibytes : InformationUnit() {
    override val ratioToPrimary = BinarySystem.Yotta
    override val symbol = "YiB"
  }

  object Bits : InformationUnit() {
    override val ratioToPrimary = 0.125
    override val symbol = "bit"
  }
}


class SizeInBytesDecoder : NonNullableLeafDecoder<SizeInBytes> {

  override fun supports(type: KType): Boolean = type.classifier == SizeInBytes::class
  override fun safeLeafDecode(node: Node,
                              type: KType,
                              context: DecoderContext): ConfigResult<SizeInBytes> {

    fun parse(input: String): ConfigResult<SizeInBytes> {
      val size = SizeInBytes.parse(input)
      return size?.valid() ?: DecodeError(node, type).invalid()
    }

    return when (node) {
      is StringNode -> parse(node.value)
      else -> DecodeError(node, type).invalid()
    }
  }
}
