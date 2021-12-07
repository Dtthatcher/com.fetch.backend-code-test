package com.fetch.model.db.mapper

import com.fetch.model.db.entity.CompanyTransactionDTO
import com.fetch.model.db.entity.CompanyTransactionsTable
import com.fetch.model.db.entity.PayerAndPointsDTO
import com.fetch.model.db.entity.PointsDTO
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.sum

fun mapToCompanyTransactionDTO(it: ResultRow) = CompanyTransactionDTO(
    payer = it[CompanyTransactionsTable.payer],
    points = it[CompanyTransactionsTable.points],
    timestamp = it[CompanyTransactionsTable.timestamp]
)

fun mapToBasic(it: ResultRow) = PayerAndPointsDTO(
    payer = it[CompanyTransactionsTable.payer],
    points = it[CompanyTransactionsTable.points.sum()]
)

fun mapToTotal(it: ResultRow) = PointsDTO(
    points = it[CompanyTransactionsTable.points.sum()]
)