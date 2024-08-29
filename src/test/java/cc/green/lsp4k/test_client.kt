package cc.green.lsp4k

import java.io.File

/*
 * @author   Alan Green
 * @email    alan_greens@outlook.com
 * @date     2024-08-29 14:18
 */

fun main() {
    val bootstrap =
        Bootstrap.Builder.setInterfaces(listOf(InterfaceA::class.java)).setServices(listOf(InterfaceAImpl())).build()

    val interfaceA = bootstrap.get<InterfaceA>()
    // val re = interfaceA.re(CancelParam(3))

    val params =
        FileParams(File("").toURI())
    val open = interfaceA.open(params)
    println(open)

    interfaceA.open2(params)

}