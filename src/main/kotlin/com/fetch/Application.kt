package com.fetch

import io.ktor.application.*
import com.fetch.plugins.*
import com.fetch.routes.apiRoutes
import com.fetch.service.bindServices
import io.ktor.routing.*
import org.kodein.di.ktor.di

fun main(args: Array<String>): Unit =
    io.ktor.server.tomcat.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    initDB()
    di { bindServices() }
    configureMonitoring()
    configureHTTP()
    configureSerialization()

    routing {
        apiRoutes()
    }
}
