package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addCommandLineSource
import com.sksamuel.hoplite.addResourceOrFileSource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class WithCliArgs : FunSpec({
  test("should support cli args") {
    data class Server(
      val port: Int = 8080,
      val host: String = "localhost"
    )

    data class Consumer(
      val groupId: String = "default",
      val topic: String,
      val retry: String = "$topic-retry",
      val deadLetter: String = "$topic-error"
    )

    data class Consume(
      val groupId: String = "app_name",
      val heartbeatInterval: String = "2s",
      val enabled: Boolean = true,
      val consumers: Map<String, Consumer> = emptyMap()
    )

    data class Kafka(
      val bootstrapServers: String = "localhost:9092",
      val interceptorClasses: List<String> = emptyList(),
      val autoOffset: String = "Earliest",
      val autoCreateTopics: Boolean = true,
      val consume: Consume = Consume()
    )

    data class App(
      val kafka: Kafka
    )

    data class Test(
      val server: Server,
      val app: App
    )

    val config = ConfigLoaderBuilder.default()
      .addCommandLineSource(
        arrayOf(
          "--app.kafka.consume.consumers.BConsumer.group-id=456",
          "--app.kafka.consume.consumers.BConsumer.topic=b-topic",
          "--app.kafka.consume.consumers.BConsumer.retry=b-topic-retry",
          "--app.kafka.consume.consumers.BConsumer.dead-letter=b-topic-error"
        ),
        prefix = "--",
        delimiter = "="
      )
      .addResourceOrFileSource("/test_yaml_with_cliargs.yaml")
      .build()
      .loadConfigOrThrow<Test>()

    config shouldBe Test(
      Server(8080, "localhost"),
      App(
        Kafka(
          "localhost:9092",
          emptyList(),
          "Earliest",
          true,
          Consume(
            "app_name",
            "2s",
            true,
            mapOf(
              "AConsumer" to Consumer("123", "a-topic", "a-topic-retry", "a-topic-error"),
              "BConsumer" to Consumer("456", "b-topic", "b-topic-retry", "b-topic-error"),
            )
          )
        )
      )
    )
  }
})
