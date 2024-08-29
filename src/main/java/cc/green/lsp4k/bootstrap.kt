package cc.green.lsp4k

import cc.green.lsp4k.adapters.MessageTypeAdapter
import com.google.gson.GsonBuilder
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.ContextHandler
import org.eclipse.jetty.websocket.client.WebSocketClient
import org.eclipse.jetty.websocket.server.WebSocketUpgradeHandler
import java.net.URI
import java.time.Duration
import java.time.LocalTime
import kotlin.jvm.internal.Reflection
import kotlin.reflect.KClass

/*
 * @author   Alan Green
 * @email    alan_greens@outlook.com
 * @date     2024-08-29 14:14
 */

typealias MethodOriginName = String
typealias MethodName = String

class Bootstrap(private val proxy: Any, val isServer: Boolean) {

    var client: WebSocketClient? = null
    var server: Server? = null

    constructor(client: WebSocketClient, proxy: Any) : this(proxy, false) {
        this.client = client
    }

    constructor(server: Server, proxy: Any) : this(proxy, false) {
        this.server = server
    }

    // Java语法无法使用
    @Deprecated(
        "This method cannot be used in a java syntax. Use getProxy() instead.",
        replaceWith = ReplaceWith("getProxy()")
    )
    fun <T> get(): T {
        return proxy as T
    }

    fun getProxy(): Any {
        return proxy
    }

    companion object Builder {

        private lateinit var interfaces: List<KClass<*>>
        private lateinit var services: List<Any>
        private var logHandler: LogHandler = SimpleLogHandle
        private var port: Int = 28081
        private val maxTextMessageSize = 128 * 1024
        private lateinit var server: Server
        private var uri: URI = URI.create("ws://localhost:$port/ws/service")

        fun setInterfaces(interfaces: List<Class<*>>): Builder {
            Builder.interfaces = interfaces.map {
                Reflection.getOrCreateKotlinClass(it)
            }
            return this
        }

        fun setServices(services: List<Any>): Builder {
            Builder.services = services
            return this
        }

        fun setLogHandle(logHandler: LogHandler): Builder {
            Builder.logHandler = logHandler
            return this
        }

        fun setPort(port: Int): Builder {
            Builder.port = port
            return this
        }

        fun setURI(uri: URI): Builder {
            Builder.uri = uri
            return this
        }

        fun build(client: Boolean = true): Bootstrap {

            val methodInfoMap = mutableMapOf<MethodOriginName, MethodInfo>()
            val methodInfoMapByMethodName = mutableMapOf<MethodName, MethodInfo>()
            interfaces.forEach { `interface` ->
                AnnotationUtil.findRpcMethods(`interface`, mutableSetOf()) { methodInfo ->
                    methodInfoMap[methodInfo.func.name] = methodInfo
                    methodInfoMapByMethodName[methodInfo.methodName] = methodInfo
                }
            }

            val gson = GsonBuilder()
                .registerTypeAdapterFactory(
                    MessageTypeAdapter.Factory(
                        methodInfoMapByMethodName,
                        MethodProviderInstance
                    )
                )
                .create()
            val serializer = Serializer(methodInfoMap, gson)

            val serviceEndpoint = ServiceEndpoint.createServiceEndpoint(services, logHandler)

            val messageHandle = WebsocketMessageHandle(serializer, logHandler)
            val messageProcessor = MessageProcessor(messageHandle, serviceEndpoint, logHandler)
            messageHandle.callback = messageProcessor
            MethodProviderInstance.messageProcessor = messageProcessor

            val proxy = InterfaceEndpoint.createProxy(messageProcessor, interfaces, methodInfoMap)

            if (client) {
                val webSocketClient = WebSocketClient()
                webSocketClient.maxTextMessageSize = maxTextMessageSize.toLong()
                webSocketClient.idleTimeout = Duration.ofMinutes(30)
                webSocketClient.start()
                val future = webSocketClient.connect(messageHandle, uri)
                future.get()
                return Bootstrap(webSocketClient, proxy)
            } else {
                val server = Server(port)
                val contextHandler = ContextHandler("/ws")
                server.handler = contextHandler
                contextHandler.handler = WebSocketUpgradeHandler.from(server, contextHandler) {
                    it.idleTimeout = Duration.ofMinutes(30)
                    it.addMapping("/service") { _, _, _ -> messageHandle }
                }
                server.start()
                return Bootstrap(server, proxy)
            }
        }

    }

}

object SimpleLogHandle : LogHandler {

    override fun debug(msg: String) {
        println("[debug] [${Thread.currentThread().name}] [${now()}] $msg")
    }

    override fun info(msg: String) {
        println("[info] [${Thread.currentThread().name}] [${now()}] $msg")
    }

    override fun error(msg: String) {
        println("[error] [${Thread.currentThread().name}] [${now()}] $msg")
    }

    private fun now(): String {
        val now = LocalTime.now()
        return "${now.hour}:${now.minute}:${now.second}"
    }

}