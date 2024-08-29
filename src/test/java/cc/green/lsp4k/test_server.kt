package cc.green.lsp4k

/*
 * @author   Alan Green
 * @email    alan_greens@outlook.com
 * @date     2024-08-29 14:18
 */

fun main() {
    val bootstrap =
        Bootstrap.Builder.setInterfaces(listOf(InterfaceA::class.java)).setServices(listOf(InterfaceAImpl()))
            .build(false)
}