package com.fetch.model.db.entity

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.`java-time`.datetime
import java.time.LocalDateTime

//DB Table for all Company Transactions, IntIdTable auto generates incrementing Int ID's
object CompanyTransactionsTable: IntIdTable(name = "Company_Transactions") {
    val payer = varchar("payer", 255)
    val points = integer("points")
    val timestamp = datetime("timestamp")
}
//Individual Row in the Table
class CompanyTransactionRow(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<CompanyTransactionRow>(CompanyTransactionsTable)

    var payer by CompanyTransactionsTable.payer
    var points by CompanyTransactionsTable.points
    var timestamp by CompanyTransactionsTable.timestamp

    // convert the row into a DTO
    fun toCompanyTransactionDTO() = CompanyTransactionDTO(payer, points, timestamp)
}