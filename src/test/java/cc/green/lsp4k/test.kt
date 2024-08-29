package cc.green.lsp4k

import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.CompletableFuture

/*
 * @author   Alan Green
 * @email    alan_greens@outlook.com
 * @date     2024-08-29 14:17
 */

data class CancelParam(val id: Int)

data class FileParams(val location: URI)

interface InterfaceA {
    @Request("testService/re")
    fun re(p: CancelParam): CompletableFuture<String>

    @Notification
    fun getData(a: Int)

    @Request
    fun open(fileParams: FileParams): CompletableFuture<Boolean>

    @Notification
    fun open2(fileParams: FileParams)

}

class InterfaceAImpl : InterfaceA {
    override fun re(p: CancelParam): CompletableFuture<String> = CompletableFuture.completedFuture("bb ".repeat(3))

    override fun getData(a: Int) {
        println("bb getData ".repeat(a))
    }

    override fun open(fileParams: FileParams): CompletableFuture<Boolean> {
        val path = Paths.get(fileParams.location)
        return CompletableFuture.completedFuture(Files.exists(path))
    }

    override fun open2(fileParams: FileParams) {
        val path = Paths.get(fileParams.location)
        println(Files.exists(path))
    }

}