package com.fetch

import com.fetch.model.db.entity.CompanyTransactionsTable
import io.ktor.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.`java-time`.date
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month

fun Application.initDB() {
    Database.connect("jdbc:h2:mem:regular;DB_CLOSE_DELAY=-1", "org.h2.Driver")

    transaction {
        SchemaUtils.create(CompanyTransactionsTable)

        CompanyTransactionsTable.insert {
            it[payer] = "Dannon"
            it[points] = 5500
            it[timestamp] = LocalDateTime.now()
        }

        CompanyTransactionsTable.insert {
            it[payer] = "Dannon"
            it[points] = 300
            it[timestamp] = LocalDateTime.of(2016, Month.APRIL, 15, 3, 15)
        }

        CompanyTransactionsTable.insert {
            it[payer] = "LuluLemon"
            it[points] = 800
            it[timestamp] = LocalDateTime.now()
        }
    }
}