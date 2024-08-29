package cc.green.lsp4k

/*
 * @author   Alan Green
 * @email    alan_greens@outlook.com
 * @date     2024-08-29 14:11
 */
interface MessageConsumer {
    fun consume(message: Message)
}