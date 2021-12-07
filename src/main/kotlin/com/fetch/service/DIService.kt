package com.fetch.service

import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton

fun DI.MainBuilder.bindServices() {
    bind<TransactionService>() with singleton { TransactionService() }
}