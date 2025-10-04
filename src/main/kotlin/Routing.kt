package com.example

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
    }
    routing {
        route("/api/v1") {
            get("/users") { }
            get("/users/{id}") { }
            post("/users") { }
        }
    }
}
