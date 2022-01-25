package com.sksamuel.hoplite.example

import com.sksamuel.hoplite.ConfigLoader

import java.nio.file.Path

data class MyConfig(
  val name: String,
  val nested: NestedConfig,
)

data class NestedConfig(
  val nums: List<Int>,
)

fun main(args: Array<String>) {
  val configPath = Path.of(args[0])
  val config = ConfigLoader().loadConfigOrThrow<MyConfig>(configPath)
  println("My config is: $config")
}
