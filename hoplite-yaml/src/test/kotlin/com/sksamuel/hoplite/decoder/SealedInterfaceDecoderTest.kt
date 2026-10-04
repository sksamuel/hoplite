package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.containExactlyInAnyOrder
import io.kotest.matchers.should
import io.kotest.matchers.types.shouldBeInstanceOf
import java.io.File

class SealedInterfaceDecoderTest : FunSpec({
  test("sealed interface decoding") {
    data class OrtConfig(val storages: Map<String, ScanStorageConfiguration>)

    val config = ConfigLoader().loadConfigOrThrow<OrtConfig>("/sealed_ort_storages.yml")

    with(config.storages) {
      keys should containExactlyInAnyOrder(
        "local", "http", "aws", "clearlyDefined", "postgres"
      )

      get("local").shouldBeInstanceOf<FileBasedStorageConfiguration>()
      get("http").shouldBeInstanceOf<FileBasedStorageConfiguration>()
      get("aws").shouldBeInstanceOf<FileBasedStorageConfiguration>()
      get("clearlyDefined").shouldBeInstanceOf<ClearlyDefinedStorageConfiguration>()
      get("postgres").shouldBeInstanceOf<PostgresStorageConfiguration>()
    }
  }
})

sealed interface ScanStorageConfiguration

data class ClearlyDefinedStorageConfiguration(
  val serverUrl: String
) : ScanStorageConfiguration

data class FileBasedStorageConfiguration(
  val backend: FileStorageConfiguration,
  val type: StorageType = StorageType.PROVENANCE_BASED
) : ScanStorageConfiguration

data class PostgresStorageConfiguration(
  val connection: PostgresConnection,
  val type: StorageType = StorageType.PROVENANCE_BASED
) : ScanStorageConfiguration

enum class StorageType {
  PACKAGE_BASED,
  PROVENANCE_BASED
}

data class FileStorageConfiguration(
  val httpFileStorage: HttpFileStorageConfiguration? = null,
  val localFileStorage: LocalFileStorageConfiguration? = null,
  val s3FileStorage: S3FileStorageConfiguration? = null
)

data class HttpFileStorageConfiguration(
  val url: String,
  val query: String = "",
  val headers: Map<String, String> = emptyMap()
)

data class LocalFileStorageConfiguration(
  val directory: File,
  val compression: Boolean = true
)

data class S3FileStorageConfiguration(
  val accessKeyId: String? = null,
  val awsRegion: String? = null,
  val bucketName: String,
  val compression: Boolean = false,
  val customEndpoint: String? = null,
  val pathStyleAccess: Boolean = false,
  val secretAccessKey: String? = null
)

data class PostgresConnection(
  val url: String,
  val schema: String = "public",
  val username: String,
  val password: String = "",
  val sslmode: String = "verify-full",
  val sslcert: String? = null,
  val sslkey: String? = null,
  val sslrootcert: String? = null,
  val connectionTimeout: Long? = null,
  val idleTimeout: Long? = null,
  val keepaliveTime: Long? = null,
  val maxLifetime: Long? = null,
  val maximumPoolSize: Int? = null,
  val minimumIdle: Int? = null
)
