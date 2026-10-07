package com.sksamuel.hoplite.report

import com.sksamuel.hoplite.BooleanNode
import com.sksamuel.hoplite.DoubleNode
import com.sksamuel.hoplite.LongNode
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.decoder.DotPath
import com.sksamuel.hoplite.secrets.HashObfuscator
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.security.MessageDigest

class HashObfuscatorTest : FunSpec({

  fun sha256Hex(value: String): String =
    MessageDigest.getInstance("SHA-256")
      .digest(value.encodeToByteArray())
      .joinToString("") { "%02x".format(it) }

  test("should show exactly hashCharsToShow characters of the hex hash") {
    val value = "super-secret-password"
    val expected = sha256Hex(value)

    HashObfuscator(8).obfuscate(StringNode(value, Pos.NoPos, DotPath.root))
      .shouldBe("hash(${expected.take(8)}...)")

    HashObfuscator(12).obfuscate(StringNode(value, Pos.NoPos, DotPath.root))
      .shouldBe("hash(${expected.take(12)}...)")
  }

  test("non-strings and numeric strings should not be obfuscated") {
    HashObfuscator(8).obfuscate(BooleanNode(true, Pos.NoPos, DotPath.root)).shouldBe("true")
    HashObfuscator(8).obfuscate(LongNode(324, Pos.NoPos, DotPath.root)).shouldBe("324")
    HashObfuscator(8).obfuscate(DoubleNode(1.23, Pos.NoPos, DotPath.root)).shouldBe("1.23")
    HashObfuscator(8).obfuscate(StringNode("42", Pos.NoPos, DotPath.root)).shouldBe("42")
  }
})
