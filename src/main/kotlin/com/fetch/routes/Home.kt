package com.fetch.routes

import com.fetch.service.TransactionService
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI

fun Route.Home(){
    val transactionService by closestDI().instance<TransactionService>()

    route("/") {
        get {
            call.respond(HttpStatusCode.OK, transactionService.getCurrentPointTotal().toString())
        }
    }
}