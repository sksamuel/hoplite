package hoplite.preprocessor

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class PropsPreprocessorTest : StringSpec() {
  init {
    "should replace props from file" {

      data class Config(val a: String, val b: String)

      val preprocessor = PropsPreprocessor("/sample.properties")

      val config = ConfigLoader()
        .withPreprocessor(preprocessor)
        .loadConfigOrThrow<Config>("/processme.props")

      config shouldBe Config(a = "I'm on branch master", b = "this replacement doesn't exist \${foo}")
    }
  }
}
