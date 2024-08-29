package cc.green.lsp4k

/*
 * @author   Alan Green
 * @email    alan_greens@outlook.com
 * @date     2024-08-29 14:11
 */

@Target(AnnotationTarget.FUNCTION)
annotation class Request(val value: String = "")

@Target(AnnotationTarget.FUNCTION)
annotation class Notification(val value: String = "")

private val JSON_RPC_VERSION = "2.0"

abstract class Message {
    val jsonrpc: String = JSON_RPC_VERSION
}

data class RequestMessage(
    val id: Int,
    val method: String,
    val params: Any?,
) : Message()

data class NotificationMessage(val method: String, val params: Any?) : Message()

data class ResponseMessage(
    val id: Int,
    val result: Any? = null,
    val error: ResponseError? = null,
) : Message() {
    constructor(id: Int, result: Any?) : this(id, result, null)
    constructor(id: Int, error: ResponseError?) : this(id, null, error)
}

data class ResponseError(val code: Int, val message: String, val data: Any?)

interface ErrorCodes {
    companion object {
        const val ParseError = -32700
        const val InvalidRequest = -32600;
        const val MethodNotFound = -32601;
        const val InvalidParams = -32602;
        const val InternalError = -32603;
    }
}