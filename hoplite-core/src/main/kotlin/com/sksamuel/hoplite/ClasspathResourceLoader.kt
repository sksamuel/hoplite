package com.sksamuel.hoplite

import java.io.InputStream
import java.net.URL

/**
 * Provides an abstraction over
 * [Java Classpath Resources](https://docs.oracle.com/javase/8/docs/technotes/guides/lang/resources.html)
 *
 * Allows you to use either methods on [Class]
 * [(see docs)](https://docs.oracle.com/javase/8/docs/technotes/guides/lang/resources.html#class)
 *
 * or methods on [ClassLoader]
 * [(see docs)](https://docs.oracle.com/javase/8/docs/technotes/guides/lang/resources.html#classloader)
 */
interface ClasspathResourceLoader {
  fun getResource(name: String): URL?
  fun getResourceAsStream(name: String): InputStream?

  companion object {
    fun <T> Class<T>.toClasspathResourceLoader() = object : ClasspathResourceLoader {
      override fun getResource(name: String) = this@toClasspathResourceLoader.getResource(name)
      override fun getResourceAsStream(name: String) = this@toClasspathResourceLoader.getResourceAsStream(name)
    }

    fun ClassLoader.toClasspathResourceLoader() = object : ClasspathResourceLoader {
      override fun getResource(name: String) = this@toClasspathResourceLoader.getResource(name)
      override fun getResourceAsStream(name: String) = this@toClasspathResourceLoader.getResourceAsStream(name)
    }
  }
}


