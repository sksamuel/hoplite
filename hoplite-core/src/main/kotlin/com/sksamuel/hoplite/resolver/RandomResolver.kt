package com.sksamuel.hoplite.resolver

//private typealias Rule = (String) -> String

//class RandomResolver : ContextResolver() {
//
//  private val a = 'a'
//  private val z = 'z'
//
//  private val intRule: Rule = {
//    val regex = "\\$\\{random.int\\}".toRegex()
//    regex.replace(it) { abs(Random.nextInt()).toString() }
//  }
//
//  private val booleanRule: Rule = {
//    val regex = "\\$\\{random.boolean\\}".toRegex()
//    regex.replace(it) { Random.nextBoolean().toString() }
//  }
//
//  private val intWithMaxRule: Rule = {
//    val regex = "\\$\\{random.int\\(\\s*(\\d+)\\s*\\)\\}".toRegex()
//    regex.replace(it) { match ->
//      val max = match.groupValues[1].toInt()
//      Random.nextInt(0, max).toString()
//    }
//  }
//
//  private val intWithRangeRule: Rule = {
//    val regex = "\\$\\{random.int\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)\\}".toRegex()
//    regex.replace(it) { match ->
//      val min = match.groupValues[1].toInt()
//      val max = match.groupValues[2].toInt()
//      Random.nextInt(min, max).toString()
//    }
//  }
//
//  private val longRule: Rule = {
//    val regex = "\\$\\{random.long\\}".toRegex()
//    regex.replace(it) { abs(Random.nextLong()).toString() }
//  }
//
//  private val doubleRule: Rule = {
//    val regex = "\\$\\{random.double\\}".toRegex()
//    regex.replace(it) { Random.nextLong().toString() }
//  }
//
//  private val stringRule: Rule = {
//    val regex = "\\$\\{random.string\\(\\s*(\\d+)\\s*\\)\\}".toRegex()
//    regex.replace(it) { match ->
//      val length = match.groupValues[1].toInt()
//      val chars = CharArray(length) { Random.nextInt(a.code, z.code).toChar() }
//      String(chars)
//    }
//  }
//
//  private val uuidRule: Rule = {
//    val regex = "\\$\\{random.uuid\\}".toRegex()
//    regex.replace(it) {
//      UUID.randomUUID().toString()
//    }
//  }
//
//  private val rules = listOf(
//    intRule,
//    longRule,
//    intWithMaxRule,
//    booleanRule,
//    doubleRule,
//    intWithRangeRule,
//    stringRule,
//    uuidRule
//  )
//
//  override suspend fun resolve(node: Node, root: Node, context: DecoderContext): ConfigResult<Node> {
//    return rules.fold(value) { str, rule -> rule(str) }.valid()
//  }
//}
