package com.sksamuel.hoplite.converter

import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Value
import javax.management.remote.JMXPrincipal
import javax.security.auth.kerberos.KerberosPrincipal
import javax.security.auth.x500.X500Principal

fun <T> viaString(cursor: Value, f: (String) -> T): ConfigResult<T> {
  return cursor.string().map { f(it) }
}

class KeberosPrincipalConverterProvider : ParameterizedConverterProvider<KerberosPrincipal>() {
  override fun converter(): Converter<KerberosPrincipal> = object : Converter<KerberosPrincipal> {
    override fun apply(value: Value): ConfigResult<KerberosPrincipal> = viaString(value) { KerberosPrincipal(it) }
  }
}

class X500PrincipalConverterProvider : ParameterizedConverterProvider<X500Principal>() {
  override fun converter(): Converter<X500Principal> = object : Converter<X500Principal> {
    override fun apply(value: Value): ConfigResult<X500Principal> = viaString(value) { X500Principal(it) }
  }
}

class JMXPrincipalConverterProvider : ParameterizedConverterProvider<JMXPrincipal>() {
  override fun converter(): Converter<JMXPrincipal> = object : Converter<JMXPrincipal> {
    override fun apply(value: Value): ConfigResult<JMXPrincipal> = viaString(value) { JMXPrincipal(it) }
  }
}