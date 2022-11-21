@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.sksamuel.hoplite.toml

import com.sksamuel.hoplite.ArrayNode
import com.sksamuel.hoplite.BooleanNode
import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.LongNode
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.decoder.DotPath
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

data class Database(val server: String, val ports: List<Int>, val connectionMax: Int, val enabled: Boolean)
data class Server(val ip: String, val dc: String)
data class Servers(val alpha: Server, val beta: Server)
data class Owner(val name: String,
                 val dob: String)

data class Test(val title: String,
                val owner: Owner,
                val database: Database,
                val servers: Servers)

class TomlParserTest : StringSpec({

  "toml example document parser" {

    val test = ConfigLoader().loadConfigOrThrow<Test>("/basic.toml")
    test.title shouldBe "TOML Example"
    test.owner.name shouldBe "Tom Preston-Werner"
    test.owner.dob shouldBe "1979-05-27T07:32-08:00"
    test.database shouldBe Database("192.168.1.1", listOf(8001, 8001, 8002), 5000, true)
    test.servers shouldBe Servers(alpha = Server("10.0.0.1", "eqdc10"), beta = Server("10.0.0.2", "eqdc10"))
  }

  "toml parser should populate paths" {
    TomlParser().load(javaClass.getResourceAsStream("/basic.toml"), "toml") shouldBe MapNode(
      map = mapOf(
        "owner" to MapNode(
          mapOf(
            "dob" to StringNode("1979-05-27T07:32-08:00", Pos.LineColPos(6, 1, "toml"), DotPath("owner", "dob")),
            "name" to StringNode("Tom Preston-Werner", Pos.LineColPos(5, 1, "toml"), DotPath("owner", "name")),
          ),
          Pos.LineColPos(4, 1, "toml"),
          DotPath("owner")
        ),
        "database" to MapNode(
          mapOf(
            "server" to StringNode("192.168.1.1", Pos.LineColPos(9, 1, "toml"), DotPath("database", "server")),
            "connection_max" to LongNode(5000, Pos.LineColPos(11, 1, "toml"), DotPath("database", "connection_max")),
            "ports" to ArrayNode(
              listOf(
                LongNode(8001, Pos.LineColPos(line = 10, col = 1, source = "toml"), DotPath("database", "ports")),
                LongNode(8001, Pos.LineColPos(line = 10, col = 1, source = "toml"), DotPath("database", "ports")),
                LongNode(8002, Pos.LineColPos(line = 10, col = 1, source = "toml"), DotPath("database", "ports"))
              ),
              Pos.LineColPos(10, 1, "toml"),
              DotPath("database", "ports")
            ),
            "enabled" to BooleanNode(true, Pos.LineColPos(12, 1, "toml"), DotPath("database", "enabled")),
          ),
          Pos.LineColPos(8, 1, "toml"),
          DotPath("database")
        ),
        "servers" to MapNode(
          mapOf(
            "alpha" to MapNode(
              mapOf(
                "ip" to StringNode("10.0.0.1", Pos.LineColPos(18, 1, "toml"), DotPath("servers", "alpha", "ip")),
                "dc" to StringNode("eqdc10", Pos.LineColPos(19, 1, "toml"), DotPath("servers", "alpha", "dc")),
              ),
              Pos.LineColPos(17, 1, "toml"),
              DotPath("servers", "alpha"),
            ),
            "beta" to MapNode(
              mapOf(
                "ip" to StringNode("10.0.0.2", Pos.LineColPos(22, 1, "toml"), DotPath("servers", "beta", "ip")),
                "dc" to StringNode("eqdc10", Pos.LineColPos(23, 1, "toml"), DotPath("servers", "beta", "dc")),
              ),
              Pos.LineColPos(21, 1, "toml"),
              DotPath("servers", "beta"),
            ),
          ),
          Pos.LineColPos(14, 1, "toml"),
          DotPath("servers")
        ),
        "title" to StringNode("TOML Example", Pos.LineColPos(2, 1, "toml"), DotPath("title")),
      ),
      pos = Pos.SourcePos("toml"),
      DotPath.root,
    )
  }
})
