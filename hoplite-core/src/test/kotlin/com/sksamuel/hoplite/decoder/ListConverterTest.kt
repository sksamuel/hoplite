//package com.sksamuel.hoplite.converter
//
//import arrow.data.valid
//import com.sksamuel.hoplite.PrimitiveCursor
//import io.kotlintest.shouldBe
//import io.kotlintest.specs.StringSpec
//
//class ListConverterTest : StringSpec() {
//  init {
//    "list converter should support delimited strings" {
//      data class Foo(val a: List<String>)
//
//      val type = Foo::class.constructors.first().parameters[0].type
//      ListConverterProvider().provide<List<String>>(type)?.apply(PrimitiveCursor("1,2,  3")) shouldBe
//          listOf("1","2","3").valid()
//    }
//  }
//}