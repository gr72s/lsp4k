package cc.green.lsp4k

import org.eclipse.jetty.server.handler.ContextHandler
import org.eclipse.jetty.websocket.api.Callback
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.WebSocketSessionListener
import org.eclipse.jetty.websocket.server.WebSocketUpgradeHandler

/*
 * @author   Alan Green
 * @email    alan_greens@outlook.com
 * @date     2024-08-29 14:18
 */

fun main() {


    val bootstrap =
        Bootstrap.Builder.setInterfaces(listOf(InterfaceA::class.java)).setServices(listOf(InterfaceAImpl()))
            .build(false)
    val handler = bootstrap.server?.handler as? ContextHandler
    val container = (handler?.handler as? WebSocketUpgradeHandler)?.serverWebSocketContainer
    container?.addSessionListener(object : WebSocketSessionListener {
        override fun onWebSocketSessionOpened(session: Session?) {
//            println("onWebSocketSessionOpened")
//            session?.sendText("{\"jsonrpc\":\"2.0\",\"id\":\"1\",\"method\":\"getBoolean\",\"params\":true}", Callback.NOOP)
            println("*****************")
            val interfaceA = bootstrap.get<InterfaceA>()
            val test1 = interfaceA.getB111111(true)
            println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!${test1.get()}")
        }

        override fun onWebSocketSessionClosed(session: Session?) {
            println("onWebSocketSessionClosed")
        }
    })
    bootstrap.start()
    println(bootstrap)
}