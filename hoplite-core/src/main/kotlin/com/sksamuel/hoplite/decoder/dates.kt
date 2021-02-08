package com.sksamuel.hoplite.decoder

import kotlin.reflect.KType

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.Year
import java.time.YearMonth
import java.util.Date

import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.LongNode
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.fp.Validated
import com.sksamuel.hoplite.fp.flatMapInvalid
import com.sksamuel.hoplite.parseDuration
import com.sksamuel.hoplite.parsePeriod
import java.time.MonthDay
import java.time.Period
import kotlin.time.ExperimentalTime
import kotlin.time.milliseconds

class LocalDateTimeDecoder : NonNullableLeafDecoder<LocalDateTime> {
  override fun supports(type: KType): Boolean = type.classifier == LocalDateTime::class
  override fun safeLeafDecode(node: Node,
                              type: KType,
                              context: DecoderContext): ConfigResult<LocalDateTime> = when (node) {
    is LongNode -> LocalDateTime.ofInstant(Instant.ofEpochMilli(node.value), ZoneOffset.UTC).valid()
    is StringNode ->
      runCatching { LocalDateTime.parse(node.value, DateTimeFormatter.ISO_DATE_TIME) }.toValidated {
        ConfigFailure.DecodeError(node, type)
      }
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class LocalDateDecoder : NonNullableLeafDecoder<LocalDate> {
  override fun supports(type: KType): Boolean = type.classifier == LocalDate::class
  override fun safeLeafDecode(node: Node,
                              type: KType,
                              context: DecoderContext): ConfigResult<LocalDate> = when (node) {
    is StringNode -> runCatching { LocalDate.parse(node.value) }.toValidated {
      ConfigFailure.DecodeError(node, type)
    }
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class DurationDecoder : NonNullableLeafDecoder<Duration> {
  override fun supports(type: KType): Boolean = type.classifier == Duration::class
  override fun safeLeafDecode(node: Node,
                              type: KType,
                              context: DecoderContext): ConfigResult<Duration> = when (node) {
    is StringNode -> parseDuration(node.value).flatMapInvalid {
      runCatching { Duration.parse(node.value) }.toValidated { ConfigFailure.DecodeError(node, type) }
    }.mapInvalid { ConfigFailure.DecodeError(node, type) }
    is LongNode -> Duration.ofMillis(node.value).valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

@OptIn(ExperimentalTime::class)
class KotlinDurationDecoder : NonNullableLeafDecoder<kotlin.time.Duration> {
  override fun supports(type: KType): Boolean = type.classifier == kotlin.time.Duration::class
  override fun safeLeafDecode(node: Node,
                              type: KType,
                              context: DecoderContext): ConfigResult<kotlin.time.Duration> = when (node) {
    is StringNode -> parseDuration(node.value).map { it.toMillis().milliseconds }.mapInvalid { ConfigFailure.DecodeError(node, type) }
    is LongNode -> node.value.milliseconds.valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}


class InstantDecoder : NonNullableLeafDecoder<Instant> {
  override fun supports(type: KType): Boolean = type.classifier == Instant::class
  override fun safeLeafDecode(node: Node,
                              type: KType,
                              context: DecoderContext): ConfigResult<Instant> = when (node) {
    is StringNode -> runCatching { Instant.ofEpochMilli(node.value.toLong()) }.recoverCatching { Instant.parse(node.value) }.toValidated {
      ConfigFailure.DecodeError(node, type)
    }
    is LongNode -> Instant.ofEpochMilli(node.value).valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class YearDecoder : NonNullableLeafDecoder<Year> {
  override fun supports(type: KType): Boolean = type.classifier == java.time.Year::class
  override fun safeLeafDecode(node: Node,
                              type: KType,
                              context: DecoderContext): ConfigResult<Year> = when (node) {
    is StringNode -> runCatching { Year.of(node.value.toInt()) }.toValidated {
      ConfigFailure.DecodeError(node, type)
    }
    is LongNode -> Year.of(node.value.toInt()).valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class JavaUtilDateDecoder : NonNullableLeafDecoder<Date> {
  override fun supports(type: KType): Boolean = type.classifier == java.util.Date::class
  override fun safeLeafDecode(node: Node,
                              type: KType,
                              context: DecoderContext): ConfigResult<Date> = when (node) {
    is StringNode -> runCatching { Date(node.value.toLong()) }.toValidated {
      ConfigFailure.DecodeError(node, type)
    }
    is LongNode -> Date(node.value).valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class YearMonthDecoder : NonNullableLeafDecoder<YearMonth> {
  override fun supports(type: KType): Boolean = type.classifier == YearMonth::class
  override fun safeLeafDecode(node: Node,
                              type: KType,
                              context: DecoderContext): ConfigResult<YearMonth> = when (node) {
    is StringNode -> runCatching { YearMonth.parse(node.value) }.toValidated {
      ConfigFailure.DecodeError(node, type)
    }
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

/**
 * Obtains an instance of {@code MonthDay} from a text string such as {@code --12-03}.
 *
 * The string must represent a valid month-day.
 * The format is {@code --MM-dd}.
 */
class MonthDayDecoder : NonNullableLeafDecoder<MonthDay> {
  override fun supports(type: KType): Boolean = type.classifier == MonthDay::class
  override fun safeLeafDecode(node: Node,
                              type: KType,
                              context: DecoderContext): ConfigResult<MonthDay> = when (node) {
    is StringNode -> runCatching {
      MonthDay.parse(node.value.removePrefix("--").prependIndent("--"))
    }.toValidated {
      ConfigFailure.DecodeError(node, type)
    }
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class PeriodDecoder : NonNullableLeafDecoder<Period> {
  override fun supports(type: KType): Boolean = type.classifier == Period::class
  override fun safeLeafDecode(node: Node,
                              type: KType,
                              context: DecoderContext): ConfigResult<Period> = when (node) {
    is StringNode -> parsePeriod(node.value).mapInvalid { ConfigFailure.DecodeError(node, type) }
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class SqlTimestampDecoder : NonNullableLeafDecoder<java.sql.Timestamp> {
  override fun supports(type: KType): Boolean = type.classifier == java.sql.Timestamp::class
  override fun safeLeafDecode(node: Node,
                              type: KType,
                              context: DecoderContext): ConfigResult<java.sql.Timestamp> = when (node) {
    is StringNode -> runCatching { java.sql.Timestamp(node.value.toLong()) }.toValidated {
      ConfigFailure.DecodeError(node, type)
    }
    is LongNode -> java.sql.Timestamp(node.value).valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

fun <E, T> Result<T>.toValidated(f: (Throwable) -> E): Validated<E, T> = fold({ it.valid() }, { f(it).invalid() })
