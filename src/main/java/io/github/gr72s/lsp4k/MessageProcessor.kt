package io.github.gr72s.lsp4k

import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicInteger

/*
 * @author   Alan Green
 * @email    alan_greens@outlook.com
 * @date     2024-08-29 14:12
 */
class MessageProcessor(
    private val callServiceConsumer: MessageConsumer,
    private val serviceEndpoint: Endpoint,
    private val logHandler: LogHandler,
) :
    Endpoint, MessageConsumer, MethodProvider {

    private val callOnMap = mutableMapOf<Int, WaitingFuture>()
    private val requestId = AtomicInteger(0)

    data class WaitingFuture(val requestMessage: RequestMessage, val result: CompletableFuture<Any>)

    override fun request(method: String, params: Any?): CompletableFuture<*> {
        val requestMessage = createRequestMessage(method, params)
        logHandler.debug("request $requestMessage")
        val result = CompletableFuture<Any>()
        synchronized(callOnMap) {
            callOnMap[requestMessage.id] = WaitingFuture(requestMessage, result)
        }
        callServiceConsumer.consume(requestMessage)
        return result
    }

    override fun notify(method: String, params: Any?) {
        val notificationMessage = createNotificationMessage(method, params)
        logHandler.debug("notify $notificationMessage")
        callServiceConsumer.consume(notificationMessage)
    }

    override fun consume(message: Message) {
        when (message) {
            is RequestMessage -> handleRequest(message)
            is NotificationMessage -> handleNotification(message)
            is ResponseMessage -> handleResponse(message)
            else -> TODO()
        }
    }

    private fun createRequestMessage(method: String, params: Any?): RequestMessage =
        RequestMessage(requestId.incrementAndGet(), method, params)

    private fun createNotificationMessage(method: String, params: Any?): NotificationMessage =
        NotificationMessage(method, params)

    private fun handleRequest(requestMessage: RequestMessage) {
        logHandler.debug("receive $requestMessage")
        val future = serviceEndpoint.request(requestMessage.method, requestMessage.params)
        future.thenAccept { t ->
            callServiceConsumer.consume(ResponseMessage(requestMessage.id, t))
        }
    }

    private fun handleNotification(notificationMessage: NotificationMessage) {
        logHandler.debug("receive $notificationMessage")
        serviceEndpoint.notify(notificationMessage.method, notificationMessage.params)
    }

    private fun handleResponse(responseMessage: ResponseMessage) {
        logHandler.debug("receive $responseMessage")
        val future = synchronized(callOnMap) {
            val waitingFuture = callOnMap.remove(responseMessage.id)
            waitingFuture
        }
        future?.result?.complete(responseMessage.result)
    }

    override fun getMethodById(id: Int): String? =
        synchronized(callOnMap) {
            val waitingFuture = callOnMap[id]
            waitingFuture?.requestMessage?.method
        }

}