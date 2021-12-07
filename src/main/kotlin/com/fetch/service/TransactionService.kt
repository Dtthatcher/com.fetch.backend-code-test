package com.fetch.service

import com.fetch.model.db.entity.*
import com.fetch.model.db.mapper.mapToBasic
import com.fetch.model.db.mapper.mapToCompanyTransactionDTO
import com.fetch.model.db.mapper.mapToTotal
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import kotlin.math.absoluteValue

class TransactionService {
    /* Delete Transaction By Timestamp */
    private fun deleteTransactionByTimestamp(transaction: CompanyTransactionDTO) {
        transaction {
            CompanyTransactionsTable
                .deleteWhere {
                    CompanyTransactionsTable.timestamp eq transaction.timestamp }
        }
    }
    /* Get Transaction By Timestamp */
    fun getAllTransactionsByTimestamp(): List<CompanyTransactionDTO> = transaction {
        CompanyTransactionRow.all()
            .orderBy(CompanyTransactionsTable.timestamp to SortOrder.ASC)
            .map(CompanyTransactionRow::toCompanyTransactionDTO)
    }

    /* Returns the Current Point Totals per Payer*/
    fun getCurrentPointTotal(): List<PayerAndPointsDTO> {
        val res: List<PayerAndPointsDTO> = transaction {
            CompanyTransactionsTable
                .slice(CompanyTransactionsTable.payer, CompanyTransactionsTable.points.sum())
                .selectAll()
                .groupBy(CompanyTransactionsTable.payer)
                .map { mapToBasic(it) }
        }
        return res
    }

    /* Helper function to check if a payer is in the DB */
    private fun checkForExisting(companyTransactionObject: CompanyTransactionDTO): Boolean{
        val list = transaction {
            CompanyTransactionsTable
                .select { CompanyTransactionsTable.payer eq companyTransactionObject.payer }
                .map{ mapToCompanyTransactionDTO(it)}
        }
        if (list.isEmpty()) return false
        return true
    }

    /* Helper function to Update the points on a specific transaction */
    private fun updateTransaction(transactionDTO: CompanyTransactionDTO){
        transaction {
            CompanyTransactionsTable.update (
                { CompanyTransactionsTable.timestamp eq transactionDTO.timestamp }) {
                it[points] = transactionDTO.points!!
            }
        }
    }

    /* When adding individual transactions that are negative
    * This function gets a list of all transactions of the same payer that are before the new objects timestamp
    * it then updates those transactions so those payer transactions reflect the reversed point charge
    * It then returns an updated point total list
    *  */
    fun updateNegative(companyTransactionObject: TempDTO): List<PayerAndPointsDTO>?{
        // create temp obj to accept datetime in string format
        val tempObj = TempDTO(
            companyTransactionObject.payer,
            companyTransactionObject.points,
            companyTransactionObject.timestamp ?: LocalDateTime.now().toString()
        )
        // create new object with datetime format
        val obj = CompanyTransactionDTO(
            payer = companyTransactionObject.payer,
            points = companyTransactionObject.points,
            timestamp = LocalDateTime.parse(tempObj.timestamp)
        )
        val newPayer = obj.payer
        val newPoints = obj.points
        val newTime = obj.timestamp
        // get the total points we have so that we can do a points check against the database
        // this is to insure there is not an improper negative points call
        val totalPoints = transaction {
            CompanyTransactionsTable
                .slice(CompanyTransactionsTable.points.sum())
                .selectAll()
                .map { mapToTotal(it) }
        }[0].points

        if (newPoints!!.absoluteValue > totalPoints!!) return null
        // check for existing payer in the DB
        if (checkForExisting(obj)) {
            // get a list of all transactions before new timestamp
            val oldTransactions = transaction {
                CompanyTransactionsTable
                    .select { CompanyTransactionsTable.payer eq newPayer }
                    .orderBy(CompanyTransactionsTable.timestamp)
                    .map { mapToCompanyTransactionDTO(it) }
            }.filter { it.timestamp.isBefore(newTime) }

            var points = newPoints.absoluteValue
            var index = 0
            // adjust the transactions to deduct points
            while (points != 0){
                when {
                    oldTransactions[index].points!! > points -> { // if an old transactions points are more than points being deducted
                        oldTransactions[index].points = oldTransactions[index].points?.minus(points) // deduct points from current transaction
                        points = 0
                        updateTransaction(oldTransactions[index]) // update it in the DB
                    }
                    points > oldTransactions[index].points!! -> { // if points to deduct are more than current transactions points
                        points -= oldTransactions[index].points!! // deduct transaction points from total
                        oldTransactions[index].points = 0 // set that transaction to 0, we don't delete for log purposes
                        updateTransaction(oldTransactions[index]) // update in DB
                        index++ // next index for next iteration
                    }
                    else -> {
                        points = 0
                        oldTransactions[index].points = 0
                        updateTransaction(oldTransactions[index])
                    }
                }
            }
        }
        // do a final check, if the payer is not in DB and you try to add negative points then return null
        else {if (newPoints < 0) return null}
        // return the new current point totals
        return getCurrentPointTotal()
    }

    /* add a positive points transaction to DB */
    fun addPayerPointsCall(companyTransactionObject: TempDTO): List<PayerAndPointsDTO> {

        // temp object to get timestamp string from post call
        val obj = TempDTO(
            companyTransactionObject.payer,
            companyTransactionObject.points,
            companyTransactionObject.timestamp ?: LocalDateTime.now().toString()
        )
        val newPayer = companyTransactionObject.payer
        val newPoints = companyTransactionObject.points
        val newTime = LocalDateTime.parse(obj.timestamp)

        transaction {
            CompanyTransactionsTable.insert {
                it[payer] = newPayer
                it[points] = newPoints ?: 0
                it[timestamp] = newTime
            }
        }
        // return current points totals by payer
        return getCurrentPointTotal()
    }
    /* User spends points, using oldest transaction first */
    fun spendPoints(pointsToSpend: PointsDTO): List<PayerAndPointsDTO> {
        val currentPointsList = getAllTransactionsByTimestamp()
        val resultList = mutableListOf<PayerAndPointsDTO>()
        var pointsTotal = pointsToSpend.points
        var index = 0
        // keep looping until we are out of bounds or points are exhausted
        while (pointsTotal != 0){
            if (index > currentPointsList.lastIndex)break

            val currentTransaction = currentPointsList[index]
            val currentPoints = currentPointsList[index].points

            when {
                currentPoints!! > pointsTotal!! -> {
                    val updatedPoints = currentPoints - pointsTotal
                    currentPointsList[index].points = updatedPoints // set the current obj points to new updated point vall
                    updateTransaction(currentPointsList[index]) // update that transactions points in the db
                    val negativeNumber = pointsTotal.unaryMinus() // get the negative representation of deducted points
                    currentPointsList[index].points = negativeNumber // set that to the obj points in the list
                    resultList.add( // then add a new obj to the results list with the negative points value
                        PayerAndPointsDTO(
                            currentPointsList[index].payer,
                            currentPointsList[index].points
                        )
                    )
                    pointsTotal = 0 // break condition met

                }
                pointsTotal > currentPoints -> { // if theres more points that the current obj has
                    pointsTotal -= currentPoints // deduct current points from points total
                    val negativeNumber = currentPoints.unaryMinus() // get neg representation of what was deducted
                    currentPointsList[index].points = negativeNumber // set that in the current list
                    deleteTransactionByTimestamp(currentTransaction) // delete old transaction from db
                    resultList.add( // add new obj with updated neg value to result list
                        PayerAndPointsDTO(
                            currentPointsList[index].payer,
                            currentPointsList[index].points
                        )
                    )
                    index++ // increment for next loop
                }
                else -> { // if points value and total points are equal
                    val negativeNumber = currentPoints.unaryMinus()
                    currentPointsList[index].points = negativeNumber
                    resultList.add(
                        PayerAndPointsDTO(
                            currentPointsList[index].payer,
                            currentPointsList[index].points
                        )
                    )
                    pointsTotal = 0
                    deleteTransactionByTimestamp(currentTransaction)
                }
            }
        }
        return resultList // return the results list for serialization
    }
}