package com.salman.moneymanagementapp.util

import android.content.ContentValues
import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.salman.moneymanagementapp.database.DatabaseHelper
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class LoanWorker : Worker {
    val TAG = "LoanWorker"
    lateinit var databaseHelper: DatabaseHelper

    constructor(context: Context, workerParameters: WorkerParameters) : super(context, workerParameters) {

        databaseHelper = DatabaseHelper(context)
    }

    override fun doWork(): Result {
       val data = inputData
        val loanId = data.getInt("loan_id", -1)
        val userId = data.getInt("user_id", -1)
        val monthlyPayment = data.getDouble("monthly_payment", 0.0)
        val name = data.getString("name")
        val type = "loan payment"
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val date = sdf.format(calendar.time)

        if (loanId == -1 || userId == -1 || monthlyPayment ==0.0) {
            return Result.failure()
        }

        try {
            val db = databaseHelper.writableDatabase
            val values = ContentValues()
            values.put("amount", -monthlyPayment)
            values.put("user_id", userId)
            values.put("type", type)
            values.put("description", "Monthly payment for " + name + " Loan")
            values.put("recipient", name)
            values.put("date", date)
            val id = db.insert("transactions", null, values)

            val errVal: Long = -1
            if (id != errVal) {
                val cursor = db.query("users", arrayOf("remained_amount"), "id=?",
                    arrayOf(userId.toString()), null, null, null)
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        val currentRemainedAmount = cursor.getDouble(cursor.getColumnIndex("remained_amount"))
                        val newValues = ContentValues()
                        newValues.put("remained_amount", currentRemainedAmount - monthlyPayment)
                        db.update("users", newValues, "id=?", arrayOf(userId.toString()))
                        cursor.close()

                        val secondCursor = db.query("loans", arrayOf("remained_amount"), "id=?",
                            arrayOf(loanId.toString()), null, null, null)
                        if (secondCursor != null) {
                            if (secondCursor.moveToFirst()) {
                                val currentLoanAmount = secondCursor.getDouble(secondCursor.getColumnIndex("remained_amount"))
                                val secondValues = ContentValues()
                                secondValues.put("remained_amount", currentLoanAmount - monthlyPayment)
                                db.update("loans", newValues, "id=?", arrayOf(loanId.toString()))
                                secondCursor.close()
                                return Result.success()
                            } else {
                                secondCursor.close()
                                db.close()
                                return Result.failure()
                            }
                        } else {
                            db.close()
                            return Result.failure()
                        }
                    } else {
                        cursor.close()
                        db.close()
                        return Result.failure()
                    }
                } else {
                    db.close()
                    return Result.failure()
                }
            } else {
                return Result.failure()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure()
        }
    }
}