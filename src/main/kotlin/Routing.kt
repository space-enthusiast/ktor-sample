package com.example

import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.application.*
import io.ktor.server.http.content.file
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.request.receive
import io.ktor.server.request.receiveChannel
import io.ktor.server.request.receiveMultipart
import io.ktor.server.request.receiveParameters
import io.ktor.server.request.receiveText
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import io.ktor.utils.io.readRemaining
import io.ktor.utils.io.readText
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class ObjectA(
    val intField: Int,
    val stringField: String,
    val nullableIntField: Int?,
    val nullableStringField: String?,
)

fun Application.configureRouting() {
    routing {
        get("/path/{parameter}") {
            val parameter = call.parameters["parameter"]
            call.respondText { "path parameter is: $parameter" }
        }

        get("/query-parameters") {
            val price = call.request.queryParameters["price"]
            call.respondText { "query parameter is $price" }
        }

        post("/text-in-body") {
            val text = call.receiveText()
            call.respondText { "text in body is: $text" }
        }

        post("/bytes-in-body") {
            val bytes = call.receive<ByteArray>()
            call.respond { String(bytes) }
        }

        post("/bytes-in-body/asynchronous") {
            val readChannel = call.receiveChannel()
            val text = readChannel.readRemaining().readText()
            call.respondText{ "text in body is: $text" }
        }

        post("/bytes-in-body/asynchronous-save-file") {
            val file = File("temp/file.txt")
            file.parentFile?.mkdirs() // Create parent directories if they don't exist
            call.receiveChannel().copyAndClose(file.writeChannel())
            val fileExists = file.exists()
            val absolutePath = file.absolutePath
            call.respondText("File uploaded to: $absolutePath\nFile exists: $fileExists")
        }

        post("/object-in-body") {
            val objectA = call.receive<ObjectA>()
            call.respondText { "object is received: $objectA" }
        }

        post("/form-parameters") {
            val formParameters = call.receiveParameters().entries()
            call.respondText { "form parameters: ${formParameters.joinToString()}" }
        }

        post("/multipart-form-data") {
            var fileDescription = ""
            var fileName = ""
            var fileExists = false
            var filePath = ""
            val multipartData = call.receiveMultipart(formFieldLimit = 1024 * 1024 * 100)

            multipartData.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        fileDescription = part.value
                    }

                    is PartData.FileItem -> {
                        fileName = part.originalFileName as String
                        val file = File("temp/$fileName")
                        file.parentFile?.mkdirs() // Create parent directories if they don't exist
                        part.provider().copyAndClose(file.writeChannel())
                        fileExists = file.exists()
                        filePath = file.absolutePath
                    }

                    else -> {}
                }
                part.dispose()
            }

            call.respondText("$fileDescription is uploaded to: $filePath\nFile exists: $fileExists")
        }
    }
}
