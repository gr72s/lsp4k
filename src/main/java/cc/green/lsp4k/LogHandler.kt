package cc.green.lsp4k

/*
 * @author   Alan Green
 * @email    alan_greens@outlook.com
 * @date     2024-08-29 14:10
 */
interface LogHandler {

    fun debug(msg: String)

    fun info(msg: String)

    fun error(msg: String)

}