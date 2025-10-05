package com.example

import io.ktor.server.application.*
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/user/{login}") {
            val login = call.parameters["login"]
            call.respondText { "Logged in as $login" }
        }
    }
}
