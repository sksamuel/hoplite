package com.sksamuel.hoplite.report

import com.sksamuel.hoplite.BooleanNode
import com.sksamuel.hoplite.DoubleNode
import com.sksamuel.hoplite.LongNode
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.decoder.DotPath
import com.sksamuel.hoplite.secrets.PrefixObfuscator
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class PrefixObfuscatorTest : FunSpec({

  test("happy path") {
    PrefixObfuscator(3).obfuscate(StringNode("foo", Pos.NoPos, DotPath.root)).shouldBe("foo")
    PrefixObfuscator(3).obfuscate(StringNode("foobar", Pos.NoPos, DotPath.root)).shouldBe("foo*****")
    PrefixObfuscator(3).obfuscate(StringNode("foobarfoobar", Pos.NoPos, DotPath.root)).shouldBe("foo*****")
    PrefixObfuscator(3).obfuscate(StringNode("f", Pos.NoPos, DotPath.root)).shouldBe("f")
    PrefixObfuscator(3).obfuscate(StringNode("", Pos.NoPos, DotPath.root)).shouldBe("")
  }

  test("non-strings should not be obfucscatd") {
    PrefixObfuscator(3).obfuscate(BooleanNode(true, Pos.NoPos, DotPath.root)).shouldBe("true")
    PrefixObfuscator(3).obfuscate(LongNode(324, Pos.NoPos, DotPath.root)).shouldBe("324")
    PrefixObfuscator(3).obfuscate(DoubleNode(1.23, Pos.NoPos, DotPath.root)).shouldBe("1.23")
  }
})
