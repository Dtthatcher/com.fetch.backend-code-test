package com.fetch.routes

import io.ktor.routing.*

fun Routing.apiRoutes() {
    Home()
    Add()
    All()
    Spend()
}