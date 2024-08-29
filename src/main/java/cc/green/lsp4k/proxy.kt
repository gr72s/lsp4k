package cc.green.lsp4k

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KClass

/*
 * @author   Alan Green
 * @email    alan_greens@outlook.com
 * @date     2024-08-29 14:13
 */

class InterfaceEndpoint(private val endpoint: Endpoint, private val methodInfoMap: Map<String, MethodInfo>) :
    InvocationHandler {

    companion object {
        fun createProxy(endpoint: Endpoint, ks: List<KClass<*>>, methodInfoMap: Map<String, MethodInfo>): Any {
            val classes = ks.map {
                it.java
            }.toList()

            val arrayOfNulls = arrayOfNulls<Class<*>>(classes.size)
            for (i in classes.indices) {
                arrayOfNulls[i] = classes[i]
            }

            return Proxy.newProxyInstance(
                endpoint.javaClass.classLoader, arrayOfNulls, InterfaceEndpoint(endpoint, methodInfoMap)
            )
        }

    }

    override fun invoke(proxy: Any?, method: Method, args: Array<out Any>?): Any? {
        val methodInfo = methodInfoMap[method.name] ?: return when (method.name) {
            "toString" -> this.toString()
            else -> null
        }

        return methodInfo.let {
            val params = getParams(args ?: arrayOf())
            if (methodInfo.isNotification) {
                endpoint.notify(methodInfo.methodName, params)
                return null
            } else {
                endpoint.request(methodInfo.methodName, params)
            }
        }
    }

    private fun getParams(args: Array<out Any>): Any? {
        if (args.isEmpty()) {
            return null
        }
        if (args.size == 1) {
            return args[0]
        }
        return listOf(*args)
    }
}

class ServiceEndpoint(private val methodInvokeMap: Map<String, (Any?) -> CompletableFuture<*>>) : Endpoint {

    override fun request(method: String, params: Any?): CompletableFuture<*> {
        val function = methodInvokeMap[method]
        return function?.invoke(params) ?: CompletableFuture.completedFuture(null)
    }

    override fun notify(method: String, params: Any?) {
        val function = methodInvokeMap[method]
        function?.invoke(params)
    }

    companion object {
        fun createServiceEndpoint(services: List<Any>, logHandler: LogHandler): ServiceEndpoint {
            val methodInvokeMap = mutableMapOf<String, (Any?) -> CompletableFuture<*>>()
            services.forEach { service ->
                AnnotationUtil.findRpcMethods(service::class, mutableSetOf()) {
                    val serviceInvoke: (Any?) -> CompletableFuture<*> = { params ->
                        val result = it.func.call(service, params)
                        logHandler.debug("invoke $service:${it.func.name}($params)")
                        if (result !is Unit) result as CompletableFuture<*>
                        else {
                            CompletableFuture.completedFuture(null)
                        }
                    }
                    methodInvokeMap[it.methodName] = serviceInvoke
                }
            }
            return ServiceEndpoint(methodInvokeMap)
        }
    }

}