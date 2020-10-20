package com.salman.moneymanagementapp.util

import android.content.ContentValues
import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.salman.moneymanagementapp.database.DatabaseHelper
import java.text.SimpleDateFormat
import java.util.*

class InvestmentWorker : Worker {
    val TAG = "InvestmentWorker"
    lateinit var databaseHelper: DatabaseHelper

    constructor(context: Context, workerParameters: WorkerParameters) : super(context, workerParameters) {

        databaseHelper = DatabaseHelper(context)
    }

    override fun doWork(): Result {
        Log.d(TAG, "doWork: called")
        val data = inputData
        val amount = data.getDouble("amount", 0.0)
        val recipient = data.getString("recipient")
        val description = data.getString("description")
        val userId = data.getInt("user_id", -1)
        val type = "profit"
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val date = sdf.format(calendar.time)

        try {
            val db = databaseHelper.writableDatabase
            val values = ContentValues()
            values.put("amount", amount)
            values.put("recipient", recipient)
            values.put("descrption", description)
            values.put("user_id", userId)
            values.put("type", type)
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
                        newValues.put("remained_amount", currentRemainedAmount - amount)
                        db.update("users", newValues, "id=?", arrayOf(userId.toString()))
                        cursor.close()
                        return Result.success()
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
        }catch (e: Exception) {
            e.printStackTrace()
            return Result.failure()
        }
    }
}