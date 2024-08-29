package cc.green.lsp4k

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KType
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.defaultType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.superclasses

/*
 * @author   Alan Green
 * @email    alan_greens@outlook.com
 * @date     2024-08-29 14:12
 */
object AnnotationUtil {
    fun findRpcMethods(
        kClass: KClass<*>,
        visited: MutableSet<KClass<*>>,
        acceptor: (MethodInfo) -> Unit,
    ) {
        if (!visited.add(kClass)) return

        kClass.superclasses.forEach {
            findRpcMethods(it, visited, acceptor)
        }

        for (method in kClass.declaredMemberFunctions) {
            val methodInfo = createMethodInfo(method)
            methodInfo?.let {
                acceptor(it)
            }
        }
    }

    private fun createMethodInfo(
        kFunction: KFunction<*>,
    ): MethodInfo? {

        kFunction.findAnnotation<Request>()?.let { request ->
            return MethodInfo(kFunction, false, request.value)
        }

        kFunction.findAnnotation<Notification>()?.let { notification ->
            return MethodInfo(kFunction, true, notification.value)
        }
        return null
    }
}

class MethodInfo(val func: KFunction<*>, val isNotification: Boolean, methodAlias: String) {

    val methodName: String
    val actualReturnType: KType

    init {
        this.methodName = getMethodName(func.name, methodAlias)

        val returnType = func.returnType
        if (returnType.arguments.isNotEmpty()) this.actualReturnType = returnType.arguments[0].type!!
        else this.actualReturnType = Unit::class.defaultType
    }

    private fun getMethodName(funcName: String, annotationValue: String): String = annotationValue.ifEmpty { funcName };
}