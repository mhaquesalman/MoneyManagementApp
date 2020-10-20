package com.salman.moneymanagementapp.activity

import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.salman.moneymanagementapp.R
import com.salman.moneymanagementapp.database.DatabaseHelper
import com.salman.moneymanagementapp.util.InvestmentWorker
import com.salman.moneymanagementapp.util.Utils
import kotlinx.android.synthetic.main.activity_add_investment.*
import java.lang.Exception
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class AddInvestmentActivity : AppCompatActivity() {
    val TAG = "AddInvestmentActivity"
    val initCalendar: Calendar = Calendar.getInstance()
    val finishCalendar: Calendar = Calendar.getInstance()
    lateinit var databaseHelper: DatabaseHelper
    lateinit var utils: Utils
    lateinit var addTransaction: AddTransaction
    lateinit var addInvestment: AddInvestment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_investment)

        utils = Utils(this)
        databaseHelper = DatabaseHelper(this)

        // handle click event
        setOnclickListener()
    }

    override fun onDestroy() {
        super.onDestroy()

        if (addTransaction != null) {
            if (!addTransaction.isCancelled) {
                addTransaction.cancel(true)
            }
        }

        if (addInvestment != null) {
            if (!addInvestment.isCancelled) {
                addInvestment.cancel(true)
            }
        }
    }

    val initDateListener = DatePickerDialog.OnDateSetListener { datePicker, year, month, day ->
        initCalendar.set(Calendar.YEAR, year)
        initCalendar.set(Calendar.MONTH, month)
        initCalendar.set(Calendar.DAY_OF_MONTH, day)
        val formateDate = SimpleDateFormat("yyyy-MM-dd").format(initCalendar.time)
        edtTxtInitDate.setText(formateDate)
    }

    val finishDateListener = DatePickerDialog.OnDateSetListener { datePicker, year, month, day ->
        finishCalendar.set(Calendar.YEAR, year)
        finishCalendar.set(Calendar.MONTH, month)
        finishCalendar.set(Calendar.DAY_OF_MONTH, day)
        val formateDate = SimpleDateFormat("yyyy-MM-dd").format(finishCalendar.time)
        edtTxtFinishDate.setText(formateDate)
    }

    fun setOnclickListener() {

        btnPickInitDate.setOnClickListener {
            DatePickerDialog(this, initDateListener, initCalendar.get(Calendar.YEAR),
                initCalendar.get(Calendar.MONTH), initCalendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnPickFinishDate.setOnClickListener {
            DatePickerDialog(this, finishDateListener, finishCalendar.get(Calendar.YEAR),
                finishCalendar.get(Calendar.MONTH), finishCalendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnAddInvestment.setOnClickListener {
            if (validateData()) {
                addTransaction = AddTransaction()
                val user = utils.isUserLoggedIn()
                if (user != null) {
                    addTransaction.execute()
                }
            } else {
                txtWarning.visibility = View.VISIBLE
                txtWarning.setText("Fields can't be empty")
            }
        }

    }

    fun validateData(): Boolean {
        if (edtTxtName.text.toString().equals("")) return false
        if (edtTxtInitDate.text.toString().equals("")) return false
        if (edtTxtFinishDate.text.toString().equals("")) return false
        if (edtTxtMonthlyROI.text.toString().equals("")) return false
        return true
    }


    inner class AddTransaction : AsyncTask<Int, Void, Int>() {

        lateinit var name: String
        lateinit var date: String
        var amount: Double = 0.0

        override fun onPreExecute() {
            super.onPreExecute()

            this.name = edtTxtName.text.toString()
            this.date = edtTxtInitDate.text.toString()
            this.amount = edtTxtInitAmount.text.toString().toDouble()

        }

        override fun doInBackground(vararg integers: Int?): Int? {
            try {
                val db = databaseHelper.writableDatabase
                val values = ContentValues()
                values.put("amount", -amount)
                values.put("recipient", name)
                values.put("date", date)
                values.put("description", "Initial amount for $name investment")
                values.put("user_id", integers[0])
                values.put("type", "investment")

                val transactionId = db.insert("transactions", null, values)
                return transactionId.toInt()

            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }

        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)
            if (result != null) {
                addInvestment = AddInvestment()
                addInvestment.execute(result)
            }
        }
    }

    inner class AddInvestment : AsyncTask<Int, Void, Void>() {
        var userId: Int = 0
        var amount: Double = 0.0
        var monthlyROI: Double = 0.0
        lateinit var initDate: String
        lateinit var finishDate: String
        lateinit var name: String

        override fun onPreExecute() {
            super.onPreExecute()

            this.amount = edtTxtInitAmount.text.toString().toDouble()
            this.monthlyROI = edtTxtMonthlyROI.text.toString().toDouble()
            this.initDate = edtTxtInitDate.text.toString()
            this.finishDate = edtTxtFinishDate.text.toString()
            this.name = edtTxtName.text.toString()
            val user = utils.isUserLoggedIn()
            if (user != null) {
                this.userId = user.get_id()
            } else {
                this.userId = -1
            }
        }

        override fun doInBackground(vararg integers: Int?): Void? {
            if (userId != -1) {
                try {
                    val db = databaseHelper.writableDatabase
                    val values = ContentValues()
                    values.put("name", name)
                    values.put("init_date", initDate)
                    values.put("finish_date", finishDate)
                    values.put("amount", amount)
                    values.put("user_id", userId)
                    values.put("transaction_id", integers[0])
                    val id = db.insert("investments", null, values)

                    val errVal: Long = -1
                    if (id != errVal) {
                        val cursor = db.query("users", arrayOf("remained_amount"), "id=?",
                        arrayOf(userId.toString()), null, null, null)
                        if (cursor != null) {
                            if (cursor.moveToFirst()) {
                                val currentRemainedAmount = cursor.getDouble(cursor.getColumnIndex("remained_amount"))
                                cursor.close()
                                val newValues = ContentValues()
                                newValues.put("remained_amount", currentRemainedAmount - amount)
                                val updateRow = db.update("users", newValues, "id=?", arrayOf(userId.toString()))
                                Log.d(TAG, "doInBackground: updateRow" + updateRow)
                            } else {
                                cursor.close()
                            }
                        } else {
                            db.close()
                            return null
                        }
                    }
                    db.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)

            val calendar = Calendar.getInstance()
            val sdf = SimpleDateFormat("yyyy-MM-dd")
            try {
                val initDate = sdf.parse(edtTxtInitDate.text.toString())
                calendar.time = initDate!!
                val initMonth = calendar.get(Calendar.YEAR) * 12 + calendar.get(Calendar.MONTH)
                val finishDate = sdf.parse(edtTxtFinishDate.text.toString())
                calendar.time = finishDate!!
                val finishMonth = calendar.get(Calendar.YEAR) * 12 + calendar.get(Calendar.MONTH)

                val monthDifference = finishMonth - initMonth
                var days = 0
                for (i in 0 until monthDifference) {
                    days += 30

                    val data = Data.Builder()
                        .putDouble("amount", amount + monthlyROI/100)
                        .putString("description", "Profit for $name")
                        .putInt("user_id", userId)
                        .putString("recipient", name)
                        .build()

                    val constraints = Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build()

                    val request = OneTimeWorkRequest.Builder(InvestmentWorker::class.java)
                        .setInputData(data)
                        .setConstraints(constraints)
                        .setInitialDelay(days.toLong(), TimeUnit.DAYS)
                        .addTag("profit")
                        .build()

                    WorkManager.getInstance(this@AddInvestmentActivity).enqueue(request)
                }
            } catch (e: ParseException) {
                e.printStackTrace()
            }

            val intent = Intent(this@AddInvestmentActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}