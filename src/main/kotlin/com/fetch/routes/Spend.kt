package com.fetch.routes

import com.fetch.model.db.entity.PointsDTO
import com.fetch.service.TransactionService
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI

fun Route.Spend(){
    val transactionService by closestDI().instance<TransactionService>()

    route("spend"){
        put {
            val spendPoints = call.receive<PointsDTO>()
            if (spendPoints.points!! < 0){
                call.respondText { "Please send a positive amount to spend" }
            }else { call.respond(HttpStatusCode.Accepted, transactionService.spendPoints(spendPoints).toString()) }
        }
    }
}