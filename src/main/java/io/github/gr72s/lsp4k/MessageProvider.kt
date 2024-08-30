package io.github.gr72s.lsp4k

/*
 * @author   Alan Green
 * @email    alan_greens@outlook.com
 * @date     2024-08-29 14:13
 */

interface MethodProvider {
    fun getMethodById(id: Int): String?
}

object MethodProviderInstance : MethodProvider {

    lateinit var messageProcessor: MessageProcessor

    override fun getMethodById(id: Int): String? =
        messageProcessor.getMethodById(id)

}