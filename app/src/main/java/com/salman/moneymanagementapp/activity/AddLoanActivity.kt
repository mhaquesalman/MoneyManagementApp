package com.salman.moneymanagementapp.activity

import android.app.DatePickerDialog
import android.content.ContentValues
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
import com.salman.moneymanagementapp.util.LoanWorker
import com.salman.moneymanagementapp.util.Utils
import kotlinx.android.synthetic.main.activity_add_investment.*
import kotlinx.android.synthetic.main.activity_add_loan.*
import kotlinx.android.synthetic.main.activity_add_loan.btnPickFinishDate
import kotlinx.android.synthetic.main.activity_add_loan.btnPickInitDate
import kotlinx.android.synthetic.main.activity_add_loan.edtTxtFinishDate
import kotlinx.android.synthetic.main.activity_add_loan.edtTxtInitAmount
import kotlinx.android.synthetic.main.activity_add_loan.edtTxtInitDate
import kotlinx.android.synthetic.main.activity_add_loan.edtTxtMonthlyROI
import kotlinx.android.synthetic.main.activity_add_loan.edtTxtName
import kotlinx.android.synthetic.main.activity_add_loan.txtWarning


import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class AddLoanActivity : AppCompatActivity() {
    val TAG = "AddLoanActivity"
    val initCalendar = Calendar.getInstance()
    val finishCalendar = Calendar.getInstance()
    lateinit var databaseHelper: DatabaseHelper
    lateinit var utils: Utils
    lateinit var addTransaction: AddTransaction
    lateinit var addLoan: AddLoan

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_loan)

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

        if (addLoan != null) {
            if (!addLoan.isCancelled) {
                addLoan.cancel(true)
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

        btnAddLoan.setOnClickListener {
            if (validateData()) {
                val user = utils.isUserLoggedIn()
                if (user != null) {
                   addTransaction = AddTransaction()
                    addTransaction.execute(user.get_id())
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
         if (edtTxtInitAmount.text.toString().equals("")) return false
         if (edtTxtMonthlyROI.text.toString().equals("")) return false
         if (edtTxtMonthlyPayment.text.toString().equals("")) return false
         return true
    }

    inner class AddTransaction : AsyncTask<Int, Void, Int>() {
        var amount = 0.0
        lateinit var name: String
        lateinit var date: String

        override fun onPreExecute() {
            super.onPreExecute()

            this.amount = edtTxtInitAmount.text.toString().toDouble()
            this.name = edtTxtName.text.toString()
            this.date = edtTxtInitDate.text.toString()
        }

        override fun doInBackground(vararg integers: Int?): Int? {
            try {
                val db = databaseHelper.writableDatabase;
                val values = ContentValues()
                values.put("amount", amount)
                values.put("recipient", name)
                values.put("date", date)
                values.put("user_id", integers[0])
                values.put("description", "Received amount for " + name + " Loan");
                values.put("type", "loan")
                val transactionId = db.insert("transactions", null, values);
                db.close();
                return transactionId.toInt()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)

            if (result != null) {
                addLoan = AddLoan()
                addLoan.execute(result)
            }
        }
    }

    inner class AddLoan : AsyncTask<Int, Void, Int>() {
        var userId: Int = 0
        var initAmount: Double = 0.0
        var monthlyROI: Double = 0.0
        var monthlyPayment: Double = 0.0
        lateinit var initDate: String
        lateinit var finishDate: String
        lateinit var name: String

        override fun onPreExecute() {
            super.onPreExecute()

            this.name = edtTxtName.text.toString()
            this.initDate = edtTxtInitDate.text.toString()
            this.finishDate = edtTxtFinishDate.text.toString()
            this.initAmount = edtTxtInitAmount.text.toString().toDouble()
            this.monthlyROI = edtTxtMonthlyROI.text.toString().toDouble()
            this.monthlyPayment = edtTxtMonthlyPayment.text.toString().toDouble()
            val user = utils.isUserLoggedIn()
            if (user != null) {
                this.userId = user.get_id()
            } else {
                this.userId = -1
            }
        }

        override fun doInBackground(vararg integers: Int?): Int? {
            if (userId != -1) {
                try {
                    val db = databaseHelper.writableDatabase
                    val values = ContentValues()
                    values.put("name", name);
                    values.put("init_date", initDate);
                    values.put("finish_date", finishDate);
                    values.put("init_amount", initAmount);
                    values.put("remained_amount", initAmount);
                    values.put("monthly_roi", monthlyROI);
                    values.put("monthly_payment", monthlyPayment);
                    values.put("user_id", userId);
                    values.put("transaction_id", integers[0]);

                    val loanId = db.insert("loans", null, values)

                    val errVal: Long = -1
                    if (loanId != errVal) {
                        val cursor = db.query("users", arrayOf("remained_amount"), "id=?",
                            arrayOf(userId.toString()), null, null, null)
                        if (cursor != null) {
                            if (cursor.moveToFirst()) {
                                val currentRemainedAmount = cursor.getDouble(cursor.getColumnIndex("remained_amount"))
                                val newValues = ContentValues()
                                newValues.put("remained_amount", currentRemainedAmount + initAmount)
                                db.update("users", newValues, "id=?", arrayOf(userId.toString()))
                                cursor.close()
                                db.close()
                                return loanId.toInt()
                            } else {
                                cursor.close()
                                db.close()
                                return null
                            }
                        } else {
                            db.close()
                            return null
                        }
                    } else {
                        db.close()
                        return null
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    return null
                }
            } else {
                return null
            }
        }

        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)

            if (result != null) {
                val calendar = Calendar.getInstance()
                val sdf = SimpleDateFormat("yyyy-MM-dd")
                try {
                    val initDate = sdf.parse(edtTxtInitDate.text.toString())
                    calendar.time = initDate!!
                    val initMonth = calendar.get(Calendar.YEAR)*12 + calendar.get(Calendar.MONTH)
                    val finishDate = sdf.parse(edtTxtFinishDate.text.toString())
                    calendar.time = finishDate!!
                    val finishMonth = calendar.get(Calendar.YEAR)*12 + calendar.get(Calendar.MONTH)

                    val monthDifference = finishMonth - initMonth
                    var days = 0
                    for (i in 0 until monthDifference) {
                        days += 30

                        val data = Data.Builder()
                            .putInt("loan_id", result)
                            .putInt("user_id", userId)
                            .putDouble("monthly_payment", monthlyPayment)
                            .putString("name", name)
                            .build()

                        val constraints = Constraints.Builder()
                            .setRequiresBatteryNotLow(true)
                            .build()

                        val request = OneTimeWorkRequest.Builder(LoanWorker::class.java)
                            .setInputData(data)
                            .setConstraints(constraints)
                            .setInitialDelay(days.toLong(), TimeUnit.DAYS)
                            .addTag("loan_payment")
                            .build()

                        WorkManager.getInstance(this@AddLoanActivity).enqueue(request)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}