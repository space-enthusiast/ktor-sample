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
        /**
         * path parameter example api
         *
         * @path parameter path parameter that is a string
         */
        get("/path/{parameter}") {
            val parameter = call.parameters["parameter"]
            call.respondText { "path parameter is: $parameter" }
        }

        /**
         * Multiple path parameters example
         *
         * @path parameter First path parameter
         * @path parameter2 Second path parameter
         */
        get("/path/{parameter}/{parameter2}") {
            val parameter = call.parameters["parameter"]
            val parameter2 = call.parameters["parameter2"]
            call.respondText {
                "path parameter is: $parameter and parameter2 is: $parameter2"
            }
        }

        /**
         * Query parameter example with optional parameter
         *
         * @param parameter1 Optional string parameter
         */
        get("/query-parameter") {
            val parameter1 = call.request.queryParameters["parameter1"]
            call.respondText { "query parameters is: $parameter1" }
        }

        /**
         * Query parameters example with multiple parameters
         *
         * @query parameter1 parameter1 of example api
         * @query parameter2 parameter2 of example api
         * @query parameter3 parameter3 of example api
         * @query parameter4 parameter4 of example api
         * @query parameter5 parameter5 of example api
         */
        get("/query-parameters") {
            val parameter1 = call.request.queryParameters["parameter1"]
            val parameter2 = call.request.queryParameters["parameter2"]
            val parameter3 = call.request.queryParameters["parameter3"]!!
            val parameter4 = call.request.queryParameters["parameter4"]!!.toLong()
            val parameter5 = call.request.queryParameters["parameter5"]?.toLong()
            call.respondText {
                """
                    query parameter1 is: $parameter1
                    query parameter2 is: $parameter2
                    query parameter3 is: $parameter3
                    query parameter4 is: $parameter4
                    query parameter5 is: $parameter5
                """.trimIndent()
            }
        }

        /**
         * Receive text in request body
         *
         * @body Plain text content
         */
        post("/text-in-body") {
            val text = call.receiveText()
            call.respondText { "text in body is: $text" }
        }

        /**
         * Receive bytes in request body
         *
         * @body Byte array content
         */
        post("/bytes-in-body") {
            val bytes = call.receive<ByteArray>()
            call.respond { String(bytes) }
        }

        /**
         * Receive bytes asynchronously using channels
         *
         * @body Stream of bytes
         */
        post("/bytes-in-body/asynchronous") {
            val readChannel = call.receiveChannel()
            val text = readChannel.readRemaining().readText()
            call.respondText{ "text in body is: $text" }
        }

        /**
         * Receive bytes asynchronously and save to file
         *
         * @body Stream of bytes to save
         */
        post("/bytes-in-body/asynchronous-save-file") {
            val file = File("temp/file.txt")
            file.parentFile?.mkdirs() // Create parent directories if they don't exist
            call.receiveChannel().copyAndClose(file.writeChannel())
            val fileExists = file.exists()
            val absolutePath = file.absolutePath
            call.respondText("File uploaded to: $absolutePath\nFile exists: $fileExists")
        }

        /**
         * Receive JSON object in request body
         *
         * @body ObjectA JSON object with int and string fields
         */
        post("/object-in-body") {
            val objectA = call.receive<ObjectA>()
            call.respondText { "object is received: $objectA" }
        }

        /**
         * Receive form parameters from request body
         *
         * @body application/x-www-form-urlencoded form data
         */
        post("/form-parameters") {
            val formParameters = call.receiveParameters().entries()
            call.respondText { "form parameters: ${formParameters.joinToString()}" }
        }

        /**
         * Upload file with multipart form data
         *
         * @body multipart/form-data with file and description fields
         */
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
