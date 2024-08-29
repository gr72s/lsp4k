package cc.green.lsp4k

import org.eclipse.jetty.websocket.api.Callback
import org.eclipse.jetty.websocket.api.Session

/*
 * @author   Alan Green
 * @email    alan_greens@outlook.com
 * @date     2024-08-29 14:14
 */

class WebsocketMessageHandle(
    private val serializer: Serializer,
    private val logHandler: LogHandler,
) : Session.Listener.AutoDemanding, MessageConsumer {

    private lateinit var session: Session
    lateinit var callback: MessageConsumer

    override fun consume(message: Message) {
        val msg = serializer.serialize(message)
        session.sendText(msg, Callback.NOOP)
    }

    override fun onWebSocketOpen(session: Session) {
        this.session = session
        logHandler.debug("ws: OnOpen ${session.remoteSocketAddress}")
    }

    override fun onWebSocketText(value: String?) {
        value?.let {
            logHandler.debug("ws: OnText $value")
            val message = serializer.deserialize(value)
            callback.consume(message)
        }
    }

    override fun onWebSocketClose(statusCode: Int, reason: String?) {
        logHandler.debug("ws: OnClose $statusCode: $reason")
    }

}