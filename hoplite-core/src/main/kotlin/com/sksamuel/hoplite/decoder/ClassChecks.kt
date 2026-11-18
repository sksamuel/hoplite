package com.sksamuel.hoplite.decoder

import java.lang.reflect.Modifier
import kotlin.reflect.KClass
import kotlin.reflect.KType

// Cheap answers to questions decoders ask about a type in supports(). The registry asks every decoder about every
// type it looks up, so answering through kotlin-reflect (isSubtypeOf, isData, isSealed) loads the reflection
// metadata of each type, including String, Int and the collection interfaces. These use java.lang.Class instead,
// and fall back to kotlin-reflect only for classes that could actually match.

/** True if this type's class is [target] or a subclass of it, ignoring nullability and type arguments. */
internal fun KType.isSubclassOf(target: Class<*>): Boolean =
  (classifier as? KClass<*>)?.let { target.isAssignableFrom(it.javaObjectType) } ?: false

/** False for any class that cannot have been compiled from a Kotlin class declaration. */
internal fun Class<*>.isKotlinClass(): Boolean =
  !isPrimitive && !isArray && !isEnum && isAnnotationPresent(Metadata::class.java)

/**
 * A necessary condition for [KClass.isData]: a data class always has a `component1` function (its JVM name gains a
 * suffix when the property's type is a value class), and a data object is a singleton with an `INSTANCE` field.
 */
internal fun Class<*>.mayBeDataClass(): Boolean =
  isKotlinClass() && !isInterface &&
    (declaredMethods.any { it.name.startsWith("component1") } || declaredFields.any { it.name == "INSTANCE" })

/** A necessary condition for [KClass.isSealed]: a sealed class or interface is abstract. */
internal fun Class<*>.mayBeSealed(): Boolean = isKotlinClass() && Modifier.isAbstract(modifiers)
