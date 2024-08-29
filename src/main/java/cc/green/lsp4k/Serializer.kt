package cc.green.lsp4k

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/*
 * @author   Alan Green
 * @email    alan_greens@outlook.com
 * @date     2024-08-29 14:14
 */
class Serializer(private val gson: Gson) {

    fun serialize(message: Message): String = gson.toJson(message)

    fun deserialize(value: String): Message = gson.fromJson(value, TypeToken.get(Message::class.java))

}