package com.salman.moneymanagementapp.activity

import android.content.Intent
import android.graphics.Color
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.room.Database
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.salman.moneymanagementapp.R
import com.salman.moneymanagementapp.database.DatabaseHelper
import com.salman.moneymanagementapp.model.Loan
import com.salman.moneymanagementapp.model.Transaction
import com.salman.moneymanagementapp.util.Utils
import com.salman.moneymanagementapp.util.toast
import kotlinx.android.synthetic.main.activity_loan.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.bottomNavView
import kotlinx.android.synthetic.main.activity_stats.*
import java.lang.Exception
import java.lang.String
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class StatsActivity : AppCompatActivity() {
    val TAG = "StatsActivity"
    lateinit var utils: Utils
    lateinit var databaseHelper: DatabaseHelper
    var getTransaction: GetTransaction? = null
    var getLoans: GetLoans? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        initBottomNavView()

        databaseHelper = DatabaseHelper(this)
        utils = Utils(this)
        val  user = utils.isUserLoggedIn()
        getTransaction = GetTransaction()
        getLoans = GetLoans()
        if (user != null) {
            getTransaction?.execute(user.get_id())
            getLoans?.execute(user.get_id())
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (getTransaction != null) {
            if (!getTransaction!!.isCancelled) {
                getTransaction!!.cancel(true)
            }
        }

        if (getLoans != null) {
            if (!getLoans!!.isCancelled) {
                getLoans!!.cancel(true)
            }
        }
    }

    fun initBottomNavView() {
        Log.d(TAG, "initBottomNavView: started")
        val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_item_stats -> {
//                    val intent = Intent(this, StatsActivity::class.java)
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
//                    startActivity(intent)
//                    return@OnNavigationItemSelectedListener true
                }
                R.id.menu_item_transaction -> {
                    val intent = Intent(this, TransactionActivity::class.java)
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.menu_item_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.menu_item_loan -> {
                    val intent = Intent(this, LoanActivity::class.java)
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.menu_item_investment -> {
                    val intent = Intent(this, InvestmentActivity::class.java)
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    return@OnNavigationItemSelectedListener true
                }
                // else -> toast("something is wrong!")
            }
            false
        }
        bottomNavView.selectedItemId = R.id.menu_item_loan
        bottomNavView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
    }

    inner class GetTransaction : AsyncTask<Int, Void, ArrayList<Transaction>>() {

        override fun doInBackground(vararg integers: Int?): ArrayList<Transaction>? {
            try {
                val db = databaseHelper.readableDatabase
                val cursor = db.query(
                    "transactions", null, "user_id=?",
                    arrayOf(integers[0].toString()), null, null, null)
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        val transactionList = java.util.ArrayList<Transaction>()
                        for (i in 0 until cursor.count) {
                            val transaction = Transaction(
                                id = cursor.getInt(cursor.getColumnIndex("id")),
                                amount = cursor.getDouble(cursor.getColumnIndex("amount")),
                                date = cursor.getString(cursor.getColumnIndex("date")),
                                description = cursor.getString(cursor.getColumnIndex("description")),
                                recipient = cursor.getString(cursor.getColumnIndex("recipient")),
                                type = cursor.getString(cursor.getColumnIndex("type")),
                                user_id = cursor.getInt(cursor.getColumnIndex("user_id"))
                            )
                            transactionList.add(transaction)
                            cursor.moveToNext()
                        }
                        cursor.close()
                        db.close()
                        return transactionList
                    } else {
                        cursor.close()
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
        }

        override fun onPostExecute(transactions: ArrayList<Transaction>?) {
            super.onPostExecute(transactions)

            if (transactions != null) {

                val sdf = SimpleDateFormat("yyyy-MM-dd")
                val calendar = Calendar.getInstance()
                val currentMonth = calendar.get(Calendar.MONTH) + 1
                val currentYear = calendar.get(Calendar.YEAR)
                val entries = ArrayList<BarEntry>()

                for (t in transactions) {
                    try {
                        val date = sdf.parse(t.date)
                        calendar.time = date!!
                        val month = calendar.get(Calendar.MONTH) + 1
                        val year = calendar.get(Calendar.YEAR)
                        val day = calendar.get(Calendar.DAY_OF_MONTH)

                        if (month == currentMonth && year == currentYear) {
                            var doesDayExist = false
                            for (e in entries) {
                                doesDayExist = e.x == day.toFloat()
                            }
                            if (!doesDayExist) {
                                entries.add(BarEntry(day.toFloat(), t.amount.toFloat()))
                            } else {
                                for (e in entries) {
                                    if (e.x == day.toFloat()) {
                                        e.y = e.y + t.amount.toFloat()
                                    }
                                }
                            }
                        }
                    } catch (e: ParseException) {
                        e.printStackTrace()
                    }
                }

                val dataset = BarDataSet(entries, "Account chart")
                dataset.color = Color.GREEN
                val data = BarData(dataset)
                //get rightY axis
                val righYAxis = barChartActivity.axisRight
                righYAxis.isEnabled = false
                //get x axis
                val xAxis = barChartActivity.xAxis
                xAxis.spaceMax = 1F
                xAxis.spaceMin = 1F
                xAxis.axisMaximum = 31F
                xAxis.isEnabled = false
                //get leftY axis
                val leftYAxis = barChartActivity.axisLeft
                leftYAxis.axisMinimum = 10F
                leftYAxis.setDrawGridLines(false)
                //setting barchart
                val description = Description()
                description.text = "all account transaction"
                barChartActivity.description = description
                barChartActivity.data = data
                barChartActivity.invalidate()
                barChartActivity.animateY(2000)
            }
        }
    }

    inner class GetLoans : AsyncTask<Int, Void, ArrayList<Loan>>() {
        override fun doInBackground(vararg integers: Int?): ArrayList<Loan>? {
            try {
                val db = databaseHelper.readableDatabase
                val cursor = db.query(
                    "loans", null, "user_id=?",
                    arrayOf(integers[0].toString()), null, null, null)
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        val loanList = java.util.ArrayList<Loan>()
                        for (i in 0 until cursor.count) {
                            val loan = Loan()
                            loan.set_id(cursor.getInt(cursor.getColumnIndex("id")))
                            loan.user_id = cursor.getInt(cursor.getColumnIndex("user_id"))
                            loan.transaction_id = cursor.getInt(cursor.getColumnIndex("transaction_id"))
                            loan.name = cursor.getString(cursor.getColumnIndex("name"))
                            loan.init_date = cursor.getString(cursor.getColumnIndex("init_date"))
                            loan.finish_date = cursor.getString(cursor.getColumnIndex("finish_date"))
                            loan.init_amount = cursor.getDouble(cursor.getColumnIndex("init_amount"))
                            loan.monthly_roi = cursor.getDouble(cursor.getColumnIndex("monthly_roi"))
                            loan.monthly_payment = cursor.getDouble(cursor.getColumnIndex("monthly_payment"))
                            loan.remained_amount = cursor.getDouble(cursor.getColumnIndex("remained_amount"))
                            loanList.add(loan)
                            cursor.moveToNext()
                        }
                        cursor.close()
                        db.close()
                        return loanList
                    } else {
                        cursor.close()
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
        }

        override fun onPostExecute(loans: ArrayList<Loan>?) {
            super.onPostExecute(loans)

            if (loans != null) {
                val entries = ArrayList<PieEntry>()
                var totalLoanAmount = 0.0
                var totalRemainedAmount = 0.0

                for (l in loans) {
                    totalLoanAmount += l.init_amount
                    totalRemainedAmount += l.remained_amount
                }

                entries.add(PieEntry(totalLoanAmount.toFloat(), "Total loans"))
                entries.add(PieEntry(totalRemainedAmount.toFloat(), "Remained loans"))
                val dataSet = PieDataSet(entries, "Loan chart")
                //dataSet.setColors(ColorTemplate.JOYFUL_COLORS, applicationContext)
                dataSet.colors = ColorTemplate.createColors(ColorTemplate.JOYFUL_COLORS)
                dataSet.sliceSpace = 5f
                val data = PieData(dataSet)
                //pieChartActivity.isDrawHoleEnabled = false
                pieChartActivity.data = data
                pieChartActivity.invalidate()
                pieChartActivity.animateY(2000, Easing.EaseInOutCubic)
            }
        }
    }
}