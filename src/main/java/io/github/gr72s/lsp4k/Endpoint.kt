package io.github.gr72s.lsp4k

import java.util.concurrent.CompletableFuture

/*
 * @author   Alan Green
 * @email    alan_greens@outlook.com
 * @date     2024-08-29 14:10
 */
interface Endpoint {
    fun request(method: String, params: Any?): CompletableFuture<*>
    fun notify(method: String, params: Any?)
}
