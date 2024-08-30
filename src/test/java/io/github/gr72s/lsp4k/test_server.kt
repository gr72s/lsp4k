package io.github.gr72s.lsp4k

import org.eclipse.jetty.server.handler.ContextHandler
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.WebSocketSessionListener
import org.eclipse.jetty.websocket.server.WebSocketUpgradeHandler
import java.util.concurrent.Executors

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
            val executor = Executors.newSingleThreadExecutor()
            executor.execute {
                println("${Thread.currentThread().name} *****************1")
                val interfaceA = bootstrap.get<InterfaceA>()
                println("${Thread.currentThread().name} *****************2")
                val test1 = interfaceA.getB111111(true)
                println("${Thread.currentThread().name} *****************3")
                val r = test1.get()
                println(r)
                println("${Thread.currentThread().name} *****************")
            }
        }

        override fun onWebSocketSessionClosed(session: Session?) {
            println("onWebSocketSessionClosed")
        }
    })
    bootstrap.start()
    println(bootstrap)
}