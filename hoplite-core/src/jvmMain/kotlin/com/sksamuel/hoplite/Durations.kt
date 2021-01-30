package com.sksamuel.hoplite

import com.sksamuel.hoplite.fp.Try
import com.sksamuel.hoplite.fp.flatMap
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
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
    "ms milli millis millisecond milliseconds" to DurationUnit.Millisecond,
    "µs micro microsecond micros microseconds" to DurationUnit.Microsecond,
    "ns nano nanos nanosecond nanoseconds" to DurationUnit.Nanosecond
  ).flatMap { (tokens, duration) ->
    tokens.split(' ').map { it to duration }
  }.toMap()
}

fun parseDuration(s: String): ConfigResult<Duration> {
  val unit = s.trim().reversed().takeWhile { it.isLetter() }.reversed()
  return Try { s.trim().dropLast(unit.length).trim().toLong() }
    .toValidated { ConfigFailure.Generic("Cannot parse $s to into value/units") }
    .flatMap { value ->
      when (Durations.unitMappings[unit]) {
        DurationUnit.Day -> Duration.ofDays(value).valid()
        DurationUnit.Hour -> Duration.ofHours(value).valid()
        DurationUnit.Minute -> Duration.ofMinutes(value).valid()
        DurationUnit.Second -> Duration.ofSeconds(value).valid()
        DurationUnit.Millisecond -> Duration.ofMillis(value).valid()
        DurationUnit.Microsecond -> Duration.ofNanos(value * 1000).valid()
        DurationUnit.Nanosecond -> Duration.ofNanos(value).valid()
        null -> ConfigFailure.Generic("Unknown duration unit $unit").invalid()
      }
    }
}
