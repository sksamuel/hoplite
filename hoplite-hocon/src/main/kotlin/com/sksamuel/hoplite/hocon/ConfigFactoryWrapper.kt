package com.sksamuel.hoplite.hocon

import arrow.core.None
import arrow.core.Some
import arrow.core.toOption
import arrow.data.invalidNel
import arrow.data.validNel
import com.sksamuel.hoplite.ConfigLocation
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.ThrowableFailure
import com.typesafe.config.Config
import com.typesafe.config.ConfigException
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigOrigin
import com.typesafe.config.ConfigParseOptions
import java.nio.file.Path

/**
 * A wrapper of `com.typesafe.config.ConfigFactory` whose methods return [ConfigResult] instead
 * of throwing exceptions
 */
object ConfigFactoryWrapper {

  private val strictSettings = ConfigParseOptions.defaults().setAllowMissing(false)

  /** @see `com.typesafe.config.ConfigFactory.invalidateCaches()` */
  fun invalidateCaches(): ConfigResult<Unit> =
      unsafeToReaderResult { ConfigFactory.invalidateCaches() }

  /** @see `com.typesafe.config.ConfigFactory.load()` */
  fun load(): ConfigResult<Config> =
      unsafeToReaderResult { ConfigFactory.load() }

  /** @see `com.typesafe.config.ConfigFactory.parseString()` */
  fun parseString(s: String): ConfigResult<Config> =
      unsafeToReaderResult { ConfigFactory.parseString(s) }

  /** @see `com.typesafe.config.ConfigFactory.parseFile()` */
  fun parseFile(path: Path): ConfigResult<Config> =
      unsafeToReaderResult {
        ConfigFactory.parseFile(path.toFile(),
            strictSettings)
      }

  private fun <A> unsafeToReaderResult(f: () -> A): ConfigResult<A> =
      try {
        f().validNel()
      } catch (e: ConfigException) {
        ThrowableFailure(e, e.origin().toConfigLocation()).invalidNel()
      } catch (e: Throwable) {
        ThrowableFailure(e, null).invalidNel()
      }
}

private fun ConfigOrigin.toConfigLocation(): ConfigLocation? {
  return toOption().flatMap {
    if (url() != null && lineNumber() != -1)
      Some(ConfigLocation(url(), lineNumber()))
    else
      None
  }.orNull()
}

