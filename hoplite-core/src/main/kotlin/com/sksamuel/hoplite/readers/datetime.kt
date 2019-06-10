package com.sksamuel.hoplite.readers

import arrow.data.invalidNel
import arrow.data.validNel
import com.sksamuel.hoplite.ConfigCursor
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class LocalDateTimeReader : Reader<LocalDateTime> {
  override fun read(cursor: ConfigCursor): ConfigResult<LocalDateTime> =
      when (
        val v = cursor.value()) {
        is java.util.Date -> LocalDateTime.ofInstant(v.toInstant(), ZoneOffset.UTC).validNel()
        is LocalDateTime -> v.validNel()
        is Long -> LocalDateTime.ofInstant(Instant.ofEpochMilli(v), ZoneOffset.UTC).validNel()
        is String -> LocalDateTime.parse(v).validNel()
        else -> ConfigFailure.conversionFailure<LocalDateTime>(v).invalidNel()
      }
}