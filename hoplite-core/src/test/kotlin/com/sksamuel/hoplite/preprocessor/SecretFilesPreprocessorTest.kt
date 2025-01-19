


class SecretFilesPreprocessorTest : StringSpec() {
  init {
    "secret files preprocessor should replace values with secrets from files" {
        data class Config(
            val a: String,
            val b: String,
            )

        val preprocessor = SecretFilesPreprocessor("secrets")

        val config = ConfigLoaderBuilder.default()
            .addPreprocessor(preprocessor)
            .build()
            .loadConfigOrThrow<Config>("/myconfig.yaml")

        config shouldBe Config(a = "Im a config", b = "Im a secret")
    }
  }
}
