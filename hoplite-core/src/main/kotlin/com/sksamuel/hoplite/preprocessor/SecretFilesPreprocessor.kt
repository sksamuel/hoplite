package com.polecatworks.kotlin.k8smicro.utils

import com.sksamuel.hoplite.*
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.preprocessor.TraversingPrimitivePreprocessor
import java.nio.file.Path
import kotlin.io.path.readText

class SecretFilesPreprocessor(private val basePath: Path): TraversingPrimitivePreprocessor() {
    override fun handle(node: PrimitiveNode, context: DecoderContext): ConfigResult<Node> = when (node) {
        is StringNode -> {
            val value = regex.replace(node.value) {
                val key = it.groupValues[1]
                basePath.resolve(key).readText()
            }
            node.copy(value).valid()
        }
        else -> node.valid()
    }


    // Redundant escaping required for Android support.
    private val regex = "\\$\\{(.*?)\\}".toRegex()

    companion object {
        operator fun invoke(basePath: String): SecretFilesPreprocessor {
            val x = Path.of(basePath)
            println("Secrets DIR =$x")
            return SecretFilesPreprocessor(x)
        }

        operator fun invoke(path: Path) = SecretFilesPreprocessor(path)
    }
}
