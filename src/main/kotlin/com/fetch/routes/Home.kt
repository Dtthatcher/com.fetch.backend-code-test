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
    val intro = """
        Welcome to the homepage!
        
        The routes are as follows:
        / = Home
        /all = Get call to all transactions in DB ordered by timestamp
        /add = Post call manually add new transaction
        /spend = Put call to spend the points!!
        
        I recommend PostMan (or some other http handler) for accessing your post and put request.
        Post requests respond with updated payer and points as does the Put calls.
        
        Enjoy!
        
        Here's a few preloaded payer and points totals
        
        ${transactionService.getCurrentPointTotal().toString()}
        
        and here's their transactions
        
        ${transactionService.getAllTransactionsByTimestamp().toString()}
    """.trimIndent()
    route("/") {
        get {
            call.respond(HttpStatusCode.OK, intro)
        }
    }
}