package com.sksamuel.hoplite

import com.sksamuel.hoplite.fp.Try
import com.sksamuel.hoplite.fp.flatMap
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import java.time.Period

enum class PeriodUnit {
  Day, Week, Month, Year
}

object Periods {
  val unitMappings = listOf(
    "d day days" to PeriodUnit.Day,
    "w week weeks" to PeriodUnit.Week,
    "m month months" to PeriodUnit.Month,
    "y year years" to PeriodUnit.Year
  ).flatMap { (tokens, duration) ->
    tokens.split(' ').map { it to duration }
  }.toMap()
}

fun parsePeriod(s: String): ConfigResult<Period> {
  val unit = s.trim().reversed().takeWhile { it.isLetter() }.reversed()
  return Try { s.trim().dropLast(unit.length).trim().toInt() }
    .toValidated { ConfigFailure.Generic("Cannot parse $s to into value/units") }
    .flatMap { value ->
      when (Periods.unitMappings[unit]) {
        PeriodUnit.Day -> Period.ofDays(value).valid()
        PeriodUnit.Week -> Period.ofWeeks(value).valid()
        PeriodUnit.Month -> Period.ofMonths(value).valid()
        PeriodUnit.Year -> Period.ofYears(value).valid()
        null -> ConfigFailure.Generic("Unknown period unit $unit").invalid()
      }
    }
}
