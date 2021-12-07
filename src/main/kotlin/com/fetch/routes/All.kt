package com.fetch.routes

import com.fetch.service.TransactionService
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI

fun Route.All(){
    val transactionService by closestDI().instance<TransactionService>()

    route("all"){
        get {
            call.respond(HttpStatusCode.OK ,transactionService.getAllTransactionsByTimestamp().toString())
        }
    }
}