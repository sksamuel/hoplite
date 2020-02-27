package com.sksamuel.hoplite.decoder

object MetricSystem {
  const val Yocto = 1e-24
  const val Zepto = 1e-21
  const val Atto = 1e-18
  const val Femto = 1e-15
  const val Pico = 1e-12
  const val Nano = 1e-9
  const val Micro = 1e-6
  const val Milli = 1e-3
  const val Centi = 1e-2
  const val Deci = 1e-1

  const val Deca = 1e1
  const val Hecto = 1e2
  const val Kilo = 1e3
  const val Mega = 1e6
  const val Giga = 1e9
  const val Tera = 1e12
  const val Peta = 1e15
  const val Exa = 1e18
  const val Zetta = 1e21
  const val Yotta = 1e24
}

object BinarySystem {
  const val Kilo = 1024.0
  const val Mega = 1024.0 * Kilo
  const val Giga = 1024.0 * Mega
  const val Tera = 1024.0 * Giga
  const val Peta = 1024.0 * Tera
  const val Exa = 1024.0 * Peta
  const val Zetta = 1024.0 * Exa
  const val Yotta = 1024.0 * Zetta
}
