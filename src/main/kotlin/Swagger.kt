package com.example

import io.ktor.server.application.Application
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.routing.routing

fun Application.swagger() {
    routing {
        swaggerUI(path = "swagger", swaggerFile = "build/open-api.json")
    }
}