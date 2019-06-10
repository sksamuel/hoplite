package com.sksamuel.hoplite

import arrow.core.Try
import arrow.data.invalidNel
import arrow.data.validNel
import com.sksamuel.hoplite.arrow.flatMap
import com.sksamuel.hoplite.arrow.toValidated
import java.time.Duration

enum class DurationUnit {
  Day, Hour, Minute, Second, Millisecond, Microsecond, Nanosecond
}

object Durations {
  val unitMappings = listOf(
      "d day days" to DurationUnit.Day,
      "h hour hours" to DurationUnit.Hour,
      "m min mins minute minutes" to DurationUnit.Minute,
      "s sec secs second seconds" to DurationUnit.Second,
      "ms milli millis milliseconds milliseconds" to DurationUnit.Millisecond,
      "µs micro microsecond micros microseconds" to DurationUnit.Microsecond,
      "ns nano nanos nanosecond nanoseconds" to DurationUnit.Nanosecond
  ).flatMap { (tokens, duration) ->
    tokens.split(' ').map { it to duration }
  }.toMap()
}

fun parseDuration(s: String): ConfigResult<Duration> {
  val unit = s.trim().reversed().takeWhile { it.isLetter() }.reversed()
  return Try { s.trim().dropLast(unit.length).trim().toLong() }
      .toValidated { ConfigFailure("Cannot parse $s to duration") }
      .toValidatedNel()
      .flatMap { value ->
        when (Durations.unitMappings[unit]) {
          DurationUnit.Day -> Duration.ofDays(value).validNel()
          DurationUnit.Hour -> Duration.ofHours(value).validNel()
          DurationUnit.Minute -> Duration.ofMinutes(value).validNel()
          DurationUnit.Second -> Duration.ofSeconds(value).validNel()
          DurationUnit.Millisecond -> Duration.ofMillis(value).validNel()
          DurationUnit.Microsecond -> Duration.ofNanos(value * 1000).validNel()
          DurationUnit.Nanosecond -> Duration.ofNanos(value).validNel()
          null -> ConfigFailure("Unknown duration unit $unit").invalidNel()
        }
      }
}