package com.sksamuel.hoplite.aws

import arrow.core.Try
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Value
import com.sksamuel.hoplite.arrow.flatMap
import com.sksamuel.hoplite.arrow.toValidated
import com.sksamuel.hoplite.converter.Converter
import com.sksamuel.hoplite.converter.ParameterizedConverterProvider

class RegionConverterProvider : ParameterizedConverterProvider<Region>() {

  override fun converter(): Converter<Region> = object : Converter<Region> {

    fun regionFromName(name: String): ConfigResult<Region> =
        Try { Region.getRegion(Regions.fromName(name)) }
            .toValidated { ConfigFailure("Cannot create region from $name") }
            .toValidatedNel()

    override fun convert(value: Value): ConfigResult<Region> = value.string().flatMap { regionFromName(it) }
  }
}