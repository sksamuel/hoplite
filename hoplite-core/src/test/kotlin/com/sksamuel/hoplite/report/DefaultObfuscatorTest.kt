package com.sksamuel.hoplite.report

import com.sksamuel.hoplite.BooleanNode
import com.sksamuel.hoplite.DoubleNode
import com.sksamuel.hoplite.LongNode
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.decoder.DotPath
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class DefaultObfuscatorTest : FunSpec({

  test("happy path") {
    DefaultObfuscator.obfuscate(StringNode("foo", Pos.NoPos, DotPath.root)).shouldBe("foo*****")
    DefaultObfuscator.obfuscate(StringNode("foobar", Pos.NoPos, DotPath.root)).shouldBe("foo*****")
    DefaultObfuscator.obfuscate(StringNode("foobarfoobar", Pos.NoPos, DotPath.root)).shouldBe("foo*****")
    DefaultObfuscator.obfuscate(StringNode("f", Pos.NoPos, DotPath.root)).shouldBe("f*****")
    DefaultObfuscator.obfuscate(StringNode("", Pos.NoPos, DotPath.root)).shouldBe("*****")
  }

  test("non-strings should not be obfucscatd") {
    DefaultObfuscator.obfuscate(BooleanNode(true, Pos.NoPos, DotPath.root)).shouldBe("true")
    DefaultObfuscator.obfuscate(LongNode(324, Pos.NoPos, DotPath.root)).shouldBe("324")
    DefaultObfuscator.obfuscate(DoubleNode(1.23, Pos.NoPos, DotPath.root)).shouldBe("1.23")
  }
})
