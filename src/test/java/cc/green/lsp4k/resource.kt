package cc.green.lsp4k

import java.io.File
import java.io.FileNotFoundException

/*
 * @author   Alan Green
 * @email    alan_greens@outlook.com
 * @date     2024-08-29 14:44
 */


class ResourceHelper {

    companion object {

        fun getExistFile(filePath: String): File {
            val classLoader = ResourceHelper::class.java.classLoader
            val fileResource = classLoader.getResource(filePath)?.file ?: throw FileNotFoundException()
            val file = File(fileResource)
            return if (file.exists()) file else throw FileNotFoundException()
        }

    }

}