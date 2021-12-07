package com.fetch.routes

import com.fetch.model.db.entity.TempDTO
import com.fetch.service.TransactionService
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI

fun Route.Add(){
    val transactionService by closestDI().instance<TransactionService>()

    route("add"){
        post {
            val addNewPayerTransaction = call.receive<TempDTO>()
            if (addNewPayerTransaction.points!! < 0){
                call.respond(HttpStatusCode.Accepted ,transactionService.updateNegative(addNewPayerTransaction)?.toString() ?: "Invalid Negative Amount")
            }else {
                call.respond(
                    HttpStatusCode.Created,
                    transactionService.addPayerPointsCall(addNewPayerTransaction).toString()
                )
            }
        }
    }
}