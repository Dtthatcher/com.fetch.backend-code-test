package com.fetch.model.db.entity

import java.time.LocalDateTime


// Transaction DTO for DB
data class CompanyTransactionDTO(
    val payer: String,
    var points: Int?,
    val timestamp: LocalDateTime = LocalDateTime.now()
){
    override fun toString(): String {
        return "\nPayer: $payer, Points: $points, Timestamp: $timestamp"
    }
}

// Used for return payer points calls
data class PayerAndPointsDTO(
    val payer: String,
    val points: Int?
){
    override fun toString(): String {
        return "\nPayer: $payer, Points: $points"
    }
}

// used for spending points
data class PointsDTO(
    val points: Int?
)
// temp DTO used to take in string timestamp from JSON
data class TempDTO(
    val payer: String,
    var points: Int?,
    val timestamp: String? = LocalDateTime.now().toString()
){
    override fun toString(): String {
        return "\nPayer: $payer, Points: $points, Timestamp: $timestamp"
    }
}
