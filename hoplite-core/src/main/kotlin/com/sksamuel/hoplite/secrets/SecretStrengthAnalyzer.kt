package com.sksamuel.hoplite.secrets

interface SecretStrengthAnalyzer {

  companion object {
    val default = DefaultSecretStrengthAnalyzer()
  }

  /**
   * Returns [SecretStrength] to indicate if this secret is strong or weak.
   */
  fun strength(value: String): SecretStrength
}

sealed interface SecretStrength {
  object Strong : SecretStrength
  data class Weak(val reason: String) : SecretStrength
}

/**
 * A [SecretStrengthAnalyzer] that rates a value as strong if:
 *
 * It contains at least one lowercase a-z character.
 * It contains at least one uppercase A-Z character.
 * It contains at least one digit.
 * It contains at least one character that is not a-z, A-Z, 0-9.
 * The length is at least [minLength] characters.
 */
class DefaultSecretStrengthAnalyzer(private val minLength: Int = 12) : SecretStrengthAnalyzer {

  override fun strength(value: String): SecretStrength {
    return when {
      value.length < minLength -> SecretStrength.Weak("Too short")
      !value.contains("\\d".toRegex()) -> SecretStrength.Weak("Does not contain a digit")
      !value.contains("[^a-zA-Z\\d_-]".toRegex()) -> SecretStrength.Weak("Does not contain a non-alphanumeric character")
      else -> SecretStrength.Strong
    }
  }
}
