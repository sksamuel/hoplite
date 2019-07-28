package com.sksamuel.hoplite.toml

import com.sksamuel.hoplite.ConfigLoader
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

data class Database(val server: String, val ports: List<Int>, val connection_max: Int, val enabled: Boolean)
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
})
