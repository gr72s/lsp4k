package io.github.gr72s.lsp4k

import java.io.File

/*
 * @author   Alan Green
 * @email    alan_greens@outlook.com
 * @date     2024-08-29 14:18
 */

fun main() {
    val bootstrap =
        Bootstrap.Builder.setInterfaces(listOf(InterfaceA::class.java)).setServices(listOf(InterfaceAImpl())).build()

    // val re = interfaceA.re(CancelParam(3))
    val interfaceA = bootstrap.get<InterfaceA>()

   val params =
       FileParams(File(ResourceHelper.getExistFile("test1").absolutePath).toURI())
    val open = interfaceA.open(params)
    println(open)

    println("$$$$$$$$$$$$$$$$$$$$$$$$$$")
    try {
        val future = interfaceA.getB222222(false)
        val get = future.get()
        println(get)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    println("$$$$$$$$$$$$$$$$$$$$$$$$$$")
//    val get2 = interfaceA.getBoolean(true).get()
//    println(get2)

//    interfaceA.open2(params)

}