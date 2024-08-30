package io.github.gr72s.lsp4k.adapters

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import io.github.gr72s.lsp4k.*
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.javaType

/*
 * @author   Alan Green
 * @email    alan_greens@outlook.com
 * @date     2024-08-29 14:15
 */
class MessageTypeAdapter(
    val gson: Gson,
    val methodInfoMap: Map<String, MethodInfo>,
    val methodProvider: MethodProvider,
) : TypeAdapter<Message>() {

    class Factory(private val methodInfoMap: Map<String, MethodInfo>, val methodProvider: MethodProvider) :
        TypeAdapterFactory {
        override fun <T : Any?> create(p0: Gson?, p1: TypeToken<T>?): TypeAdapter<T>? {
            if (!Message::class.java.isAssignableFrom(p1?.getRawType())) return null
            return (MessageTypeAdapter(p0!!, methodInfoMap, methodProvider) as TypeAdapter<T>)
        }
    }

    override fun write(out: JsonWriter, message: Message?) {
        message?.apply {
            out.beginObject()
            out.name("jsonrpc")
            out.value(message.jsonrpc)

            when (message) {
                is RequestMessage -> {
                    out.name("id")
                    out.value(message.id.toString())
                    out.name("method")
                    out.value(message.method)
                    out.name("params")
                    message.params?.let {
                        handleParameter(out, message.params, message.method)
                    } ?: out.nullValue()
                }

                is NotificationMessage -> {
                    out.name("method")
                    out.value(message.method)
                    out.name("params")
                    message.params?.let {
                        handleParameter(out, message.params, message.method)
                    } ?: out.nullValue()
                }

                is ResponseMessage -> {
                    out.name("id")
                    out.value(message.id.toString())
                    // 用error!=null更合理， 因为有返回值为null的情况
                    if (message.result != null) {
                        out.name("result")
                        gson.toJson(message.result, message.result.javaClass, out)
                    }
                }

            }
            out.endObject()
        }
    }

    override fun read(`in`: JsonReader): Message? {
        if (`in`.peek() == JsonToken.NULL) {
            `in`.nextNull()
            return null
        }

        `in`.beginObject()

        var jsonrpc: String
        var id: String? = null
        var method: String? = null
        var params: Any? = null
        var result: Any? = null

        while (`in`.hasNext()) {
            when (`in`.nextName()) {
                "jsonrpc" -> {
                    jsonrpc = `in`.nextString()
                }

                "id" -> {
                    id = `in`.nextString()
                }

                "method" -> {
                    method = `in`.nextString()
                }

                "params" -> {
                    params = method?.let { parseParams(`in`, it) }
                }

                "result" -> {
                    result = id?.let {
                        parseResult(`in`, id)
                    }
                }

                else -> `in`.skipValue()
            }
        }
        `in`.endObject()

        val message = createMessage(id, method, params, result)
        return message
    }

    private fun handleParameter(out: JsonWriter, params: Any, method: String) {
        when (params) {
            is Array<*> -> gson.toJson(params.toList(), List::class.java, out)
            else -> gson.toJson(params, params.javaClass, out)
        }
    }

    private fun parseParams(`in`: JsonReader, method: String): Any? {
        val next = `in`.peek()
        if (next == JsonToken.NULL) {
            `in`.nextNull()
            return null
        }
        val parameterType = getParameterType(method)
        parameterType?.let {
            return gson.fromJson(`in`, TypeToken.get(parameterType.type.javaType))
        }
        return null
    }

    private fun parseResult(`in`: JsonReader, id: String): Any? {
        val methodName = methodProvider.getMethodById(id.toInt())
        val methodInfo = methodInfoMap[methodName]
        return gson.fromJson(`in`, TypeToken.get(methodInfo?.actualReturnType?.javaType))
    }

    private fun getParameterType(method: String): KParameter? {
        val methodInfo = methodInfoMap[method]
        val parameters = methodInfo?.func?.parameters
        return parameters?.get(1)
    }

    private fun createMessage(id: String?, method: String?, params: Any?, result: Any?): Message? {
        return if (id != null) {
            if (method != null) {
                return RequestMessage(id.toInt(), method, params)
            } else {
                return ResponseMessage(id.toInt(), result)
            }
        } else if (method != null) {
            NotificationMessage(method, params)
        } else {
            return null
        }
    }

}
