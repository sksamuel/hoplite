package com.sksamuel.hoplite

import arrow.data.valid

interface ConfigValue

/**
 * A wrapper for a `ConfigValue` providing safe navigation through the config and holding positional data for better
 * error handling.
 */
interface ConfigCursor {

  /**
   * The `ConfigValue` to which this cursor points to.
   */
  fun value(): ConfigValue = TODO()

  /**
   * The path in the config to which this cursor points as a list of keys in reverse order (deepest key first).
   */
  fun pathElems(): List<String> = emptyList()

  /**
   * The path in the config to which this cursor points.
   */
  fun path(): String = pathElems().reversed().joinToString(".")

  /**
   * The file system location of the config to which this cursor points.
   */
  fun location(): ConfigLocation? = null

  /**
   * Returns whether this cursor points to an undefined value. A cursor can point to an undefined value when a missing
   * config key is requested or when a `null` `ConfigValue` is provided, among other reasons.
   *
   * @return `true` if this cursor points to an undefined value, `false` otherwise.
   */
  fun isUndefined(): Boolean = false

  /**
   * Returns whether this cursor points to a `null` config value. An explicit `null` value is different than a missing
   * value - `isUndefined` can be used to check for the latter.
   *
   * @return `true` if this cursor points to a `null` value, `false` otherwise.
   */
  fun isNull(): Boolean = false

  /**
   * Casts this cursor to a string.
   *
   * @return a `Right` with the string value pointed to by this cursor if the cast can be done, `Left` with a list of
   *         failures otherwise.
   */
  fun asString(): ConfigResult<String> = ConfigResults.failed("Cannot convert ${this.javaClass.name} to a String")
  fun asBoolean(): ConfigResult<Boolean> = ConfigResults.failed("Cannot convert ${this.javaClass.name} to a Boolean")
  fun asFloat(): ConfigResult<Float> = ConfigResults.failed("Cannot convert ${this.javaClass.name} to a Float")
  fun asDouble(): ConfigResult<Double> = ConfigResults.failed("Cannot convert ${this.javaClass.name} to a Double")
  fun asInt(): ConfigResult<Int> = ConfigResults.failed("Cannot convert ${this.javaClass.name} to a Int")
  fun asLong(): ConfigResult<Long> = ConfigResults.failed("Cannot convert ${this.javaClass.name} to a Long")

  //  /**
//   * Casts this cursor to a long.
//   *
//   * @return a `Right` with the long value pointed to by this cursor if the cast can be done, `Left` with a list of
//   *         failures otherwise.
//   */
//  fun asLong: ConfigReader.Result[Long] =
//  castOrFail(NUMBER, _.unwrapped match
//  {
//    case i : java . lang . Number if i.longValue() == i => Right(i.longValue())
//    case v => Left (ConversionFailure(v.toString, "Long", "Unable to convert Number to Long"))
//  })
//
//  /**
//   * Casts this cursor to an int.
//   *
//   * @return a `Right` with the int value pointed to by this cursor if the cast can be done, `Left` with a list of
//   *         failures otherwise.
//   */
//  fun asInt: ConfigReader.Result[Int] =
//  castOrFail(NUMBER, _.unwrapped match
//  {
//    case i : java . lang . Number if i.intValue() == i => Right(i.intValue())
//    case v => Left (ConversionFailure(v.toString, "Int", "Unable to convert Number to Int"))
//  })
//
//  /**
//   * Casts this cursor to a short.
//   *
//   * @return a `Right` with the short value pointed to by this cursor if the cast can be done, `Left` with a list of
//   *         failures otherwise.
//   */
//  fun asShort: ConfigReader.Result[Short] =
//  castOrFail(NUMBER, _.unwrapped match
//  {
//    case i : java . lang . Number if i.shortValue() == i => Right(i.shortValue())
//    case v => Left (ConversionFailure(v.toString, "Short", "Unable to convert Number to Short"))
//  })
//
//  /**
//   * Casts this cursor to a double.
//   *
//   * @return a `Right` with the double value pointed to by this cursor if the cast can be done, `Left` with a list of
//   *         failures otherwise.
//   */
//  fun asDouble: ConfigReader.Result[Double] =
//  castOrFail(NUMBER, _.unwrapped match
//  {
//    case i : java . lang . Number if i.doubleValue() == i => Right(i.doubleValue())
//    case v => Left (ConversionFailure(v.toString, "Double", "Unable to convert Number to Double"))
//  })
//
//  /**
//   * Casts this cursor to a float.
//   *
//   * @return a `Right` with the float value pointed to by this cursor if the cast can be done, `Left` with a list of
//   *         failures otherwise.
//   */
//  fun asFloat: ConfigReader.Result[Float] =
//  castOrFail(NUMBER, _.unwrapped match
//  {
//    case i : java . lang . Number if i.floatValue() == i => Right(i.floatValue())
//    case v => Left (ConversionFailure(v.toString, "Float", "Unable to convert Number to Float"))
//  })
//
//  /**
//   * Casts this cursor to a `ConfigListCursor`.
//   *
//   * @return a `Right` with this cursor as a list cursor if the cast can be done, `Left` with a list of failures
//   *         otherwise.
//   */
//  fun asListCursor: ConfigReader.Result[ConfigListCursor] =
//  castOrFail(LIST, v => Right(v.asInstanceOf[ConfigList])).right.map(ConfigListCursor(_, pathElems))
//
//  /**
//   * Casts this cursor to a list of cursors.
//   *
//   * @return a `Right` with the list pointed to by this cursor if the cast can be done, `Left` with a list of failures
//   *         otherwise.
//   */
//  fun asList: ConfigReader.Result[List[ConfigCursor]] =
//  asListCursor.right.map(_.list)
//
//  /**
//   * Casts this cursor to a `ConfigObjectCursor`.
//   *
//   * @return a `Right` with this cursor as an object cursor if it points to an object, `Left` with a list of failures
//   *         otherwise.
//   */
//  fun asObjectCursor: ConfigReader.Result[ConfigObjectCursor] =
//  castOrFail(OBJECT, v => Right(v.asInstanceOf[ConfigObject])).right.map(ConfigObjectCursor(_, pathElems))
//
//  /**
//   * Casts this cursor to a map from config keys to cursors.
//   *
//   * @return a `Right` with the map pointed to by this cursor if the cast can be done, `Left` with a list of failures
//   *         otherwise.
//   */
//  fun asMap: ConfigReader.Result[Map[String, ConfigCursor]] =
//  asObjectCursor.right.map(_.map)
//
//  /**
//   * Returns a cursor to the config at the path composed of given path segments.
//   *
//   * @param pathSegments the path of the config for which a cursor should be returned
//   * @return a `Right` with a cursor to the config at `pathSegments` if such a config exists, a `Left` with a list of
//   *         failures otherwise.
//   */
//  //@deprecated("Use `.fluent.at(pathSegments).cursor` instead", "0.10.2")
  fun atPath(path: String): ConfigResult<ConfigCursor> = ConfigResults.failed("Cannot nest path for primitive type")// = fluent.at(pathSegments: _*).cursor
//
//  /**
//   * Casts this cursor as either a `ConfigListCursor` or a `ConfigObjectCursor`.
//   *
//   * @return a `Right` with this cursor as a list or object cursor if the cast can be done, `Left` with a list of
//   *         failures otherwise.
//   */
//  @deprecated("Use `asListCursor` and/or `asObjectCursor` instead", "0.10.1")
//  fun asCollectionCursor(): ConfigReader.Result[Either[ConfigListCursor, ConfigObjectCursor]] =
//  {
//    if (isUndefined) {
//      failed(KeyNotFound.forKeys(path, Set()))
//    } else {
//      val listAtLeft = asListCursor.right.map(Either.Left.apply)
//      lazy
//      val mapAtRight = asObjectCursor.right.map(Either.Right.apply)
//      listAtLeft
//          .left.flatMap(_ => mapAtRight)
//      .left.flatMap(_ => failed (WrongType(value.valueType, Set(LIST, OBJECT))))
//    }
//  }
//
//  fun fluent(): FluentConfigCursor =
//      if (isUndefined) FluentConfigCursor(failed(KeyNotFound.forKeys(path, Set())))
//      else FluentConfigCursor(Right(this))
//
//  /**
//   * Returns a failed `ConfigReader` result resulting from scoping a `FailureReason` into the context of this cursor.
//   *
//   * This operation is the easiest way to return a failure from a `ConfigReader`.
//   *
//   * @param reason the reason of the failure
//   * @tparam A the returning type of the `ConfigReader`
//   * @return a failed `ConfigReader` result built by scoping `reason` into the context of this cursor.
//   */
//  fun failed[A](reason: FailureReason): ConfigReader.Result[A] =
//  Left(ConfigReaderFailures(failureFor(reason)))
//
//  /**
//   * Returns a `ConfigReaderFailure` resulting from scoping a `FailureReason` into the context of this cursor.
//   *
//   * This operation is useful for constructing `ConfigReaderFailures` when there are multiple `FailureReason`s.
//   *
//   * @param reason the reason of the failure
//   * @return a `ConfigReaderFailure` built by scoping `reason` into the context of this cursor.
//   */
//  fun failureFor(reason: FailureReason): ConfigReaderFailure =
//      ConvertFailure(reason, this)
//
//  /**
//   * Returns a failed `ConfigReader` result resulting from scoping a `Either[FailureReason, A]` into the context of
//   * this cursor.
//   *
//   * This operation is needed when control of the reading process is passed to a place without a `ConfigCursor`
//   * instance providing the nexessary context (for example, when `ConfigReader.fromString` is used. In those scenarios,
//   * the call should be wrapped in this method in order to turn `FailureReason` instances into `ConfigReaderFailures`.
//   *
//   * @param result the result of a config reading operation
//   * @tparam A the returning type of the `ConfigReader`
//   * @return a `ConfigReader` result built by scoping `reason` into the context of this cursor.
//   */
//  fun scopeFailure[A](result: Either[FailureReason, A]): ConfigReader.Result[A] =
//  result.left.map
//  { reason => ConfigReaderFailures(failureFor(reason), Nil) }
//
//  fun castOrFail<A>(expectedType: ConfigValueType, cast: ConfigValue => Either<FailureReason, A>): ConfigResult<A> =
//  {
//    if (isUndefined)
//      failed(KeyNotFound.forKeys(path, Set()))
//    else
//      scopeFailure(ConfigCursor.transform(value, expectedType).right.flatMap(cast))
//  }
}

data class StringConfigCursor(val value: String) : ConfigCursor {
  override fun asString(): ConfigResult<String> = value.valid()
}

data class BooleanConfigCursor(val value: Boolean) : ConfigCursor {
  override fun asBoolean(): ConfigResult<Boolean> = value.valid()
}

data class LongConfigCursor(val value: Long) : ConfigCursor {
  override fun asLong(): ConfigResult<Long> = value.valid()
}

data class IntConfigCursor(val value: Int) : ConfigCursor {
  override fun asInt(): ConfigResult<Int> = value.valid()
}

data class FloatConfigCursor(val value: Float) : ConfigCursor {
  override fun asFloat(): ConfigResult<Float> = value.valid()
}

data class DoubleConfigCursor(val value: Double) : ConfigCursor {
  override fun asDouble(): ConfigResult<Double> = value.valid()
  override fun asFloat(): ConfigResult<Float> = value.toFloat().valid()
}