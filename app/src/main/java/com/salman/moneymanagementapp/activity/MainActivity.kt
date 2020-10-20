package com.salman.moneymanagementapp.activity

import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.WorkManager
import com.github.mikephil.charting.data.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.salman.moneymanagementapp.R
import com.salman.moneymanagementapp.adapter.TransactionAdapter
import com.salman.moneymanagementapp.database.DatabaseHelper
import com.salman.moneymanagementapp.model.Shopping
import com.salman.moneymanagementapp.model.Transaction
import com.salman.moneymanagementapp.util.AddTransactionDialog
import com.salman.moneymanagementapp.util.Utils
import com.salman.moneymanagementapp.util.toast
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception
import java.lang.String
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"
    lateinit var utils: Utils
    lateinit var databaseHelper: DatabaseHelper
    lateinit var getAccountAmount: GetAccountAmount
    lateinit var getTransactions: GetTransactions
    lateinit var getProfit: GetProfit
    lateinit var getSpending: GetSpending
    lateinit var mAdapter: TransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        databaseHelper = DatabaseHelper(this)

//      val db = databaseHelper.readableDatabase
/*        val cursor = db.query("items", null, null, null,
            null, null, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                Log.d(TAG, "onCreate: name " + cursor.getString(cursor.getColumnIndex("name")))
            }
        }*/

        utils = Utils(this)
        val user = utils.isUserLoggedIn()
        if (user != null) {
            toast(message = "User ${user.first_name} is logged in")
        } else {
            val intent = Intent(applicationContext, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }


        fbAddTransaction.setOnClickListener {
            val addTransactionDialog = AddTransactionDialog()
            addTransactionDialog.show(supportFragmentManager, "add transaction dialog")
        }

        initBottomNavView()
        setUpAmount()
        initTransactionRV()
        initLineChart()
        initBarChart()

        Log.d(TAG, "onCreate: investmentWork" + WorkManager.getInstance(this).getWorkInfosByTag("profit"))
        Log.d(TAG, "onCreate: loanWork" + WorkManager.getInstance(this).getWorkInfosByTag("loan"))

    }

    fun initBarChart() {
        getSpending = GetSpending()
        val user = utils.isUserLoggedIn()
        if (user != null) {
            getSpending.execute(user.get_id())
        }
    }

    fun initLineChart() {
        getProfit = GetProfit()
        val user = utils.isUserLoggedIn()
        if (user != null) {
            getProfit.execute(user.get_id())
        }
    }

    fun initTransactionRV() {
        Log.d(TAG, "initTransactionRV: started")
        mAdapter = TransactionAdapter()
        transactionRecView.adapter = mAdapter
        transactionRecView.layoutManager = LinearLayoutManager(this)
        getTransactions()
    }

    fun getTransactions() {
        getTransactions = GetTransactions()
        val user = utils.isUserLoggedIn()
        if (user != null) {
            getTransactions.execute(user.get_id())
        }
    }

    fun setUpAmount() {
        getAccountAmount = GetAccountAmount()
        val user = utils.isUserLoggedIn()
        if (user != null) {
            getAccountAmount.execute(user.get_id())
        }
    }

    fun initBottomNavView() {
        Log.d(TAG, "initBottomNavView: started")
        val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.menu_item_stats -> {
                        val intent = Intent(this, StatsActivity::class.java)
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                        return@OnNavigationItemSelectedListener true
                    }
                    R.id.menu_item_transaction -> {
                        val intent = Intent(this, TransactionActivity::class.java)
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                        return@OnNavigationItemSelectedListener true
                    }
                    R.id.menu_item_home -> {
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
        bottomNavView.selectedItemId = R.id.menu_item_home
        bottomNavView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_share -> {
                val builder = AlertDialog.Builder(this)
                    .setTitle("Share..")
                    .setMessage("Share with friends and family..")
                    .setNegativeButton("Dismiss", DialogInterface.OnClickListener { dialogInterface, i ->

                    })
                    .setPositiveButton("Invite", DialogInterface.OnClickListener { dialogInterface, i ->
                        val message = "Hey, How are you? Hope everything is fine\nCheckout this app it helps " +
                                "to manage money stuff & to contact plz visit\nhttps:www.facebook.com/salman619"

                        val intent = Intent(Intent.ACTION_SEND)
                        intent.putExtra(Intent.EXTRA_TEXT, message)
                        intent.setType("text/plain")
                        val chooserIntent = Intent.createChooser(intent, "Send message via:")
                        startActivity(chooserIntent)
                    })
                builder.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        setUpAmount()
        getTransactions()
        initLineChart()
        initBarChart()

    }

    override fun onResume() {
        super.onResume()
        setUpAmount()
        getTransactions()
        initLineChart()
        initBarChart()

    }

    override fun onDestroy() {
        super.onDestroy()

        if (getAccountAmount != null) {
            if (!getAccountAmount.isCancelled) {
                getAccountAmount.cancel(true)
            }
        }

        if (getTransactions != null) {
            if (!getTransactions.isCancelled) {
                getTransactions.cancel(true)
            }
        }

        if (getProfit != null) {
            if (!getProfit.isCancelled) {
                getProfit.cancel(true)
            }
        }

        if (getSpending != null) {
            if (!getSpending.isCancelled) {
                getSpending.cancel(true)
            }
        }
    }

    inner class GetAccountAmount : AsyncTask<Int, Void, Double>() {
         
        override fun doInBackground(vararg intergers: Int?): Double? {
            try {
                val db = databaseHelper.readableDatabase
                val cursor: Cursor? = db.query(
                    "users", arrayOf("remained_amount"), "id=?",
                    arrayOf(String.valueOf(intergers.get(0))), null, null, null
                )
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        val amount = cursor.getDouble(cursor.getColumnIndex("remained_amount"))
                        cursor.close()
                        db.close()
                        return amount
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

        override fun onPostExecute(result: Double?) {
            super.onPostExecute(result)

            if (result != null) {
                txtAmount.setText(String.valueOf(result) + " $")
            } else {
                txtAmount.setText("0.0 $")
            }
        }
    }

    inner class GetTransactions : AsyncTask<Int, Void, ArrayList<Transaction>>() {

        override fun doInBackground(vararg integers: Int?): ArrayList<Transaction>? {
            try {
                val db = databaseHelper.readableDatabase
                val cursor = db.query(
                    "transactions", null, "user_id=?",
                    arrayOf(String.valueOf(integers[0])), null, null, "date DESC"
                )
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        val transactionList = ArrayList<Transaction>()
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
                mAdapter.setTransactions(transactions)
            } else {
                mAdapter.setTransactions(ArrayList<Transaction>())
            }
        }
    }

    inner class GetProfit : AsyncTask<Int, Void, ArrayList<Transaction>>() {

        override fun doInBackground(vararg integers: Int?): ArrayList<Transaction>? {
            try {
                val db = databaseHelper.readableDatabase
                val cursor = db.query(
                    "transactions", null, "user_id=? AND type=?",
                    arrayOf(integers[0].toString(), "profit"), null, null, null
                )
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        val transactionList = ArrayList<Transaction>()
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

                val entries = ArrayList<Entry>()
                for (t: Transaction in transactions) {
                    val date = SimpleDateFormat("yyyy-MM-dd").parse(t.date!!)
                    val calendar = Calendar.getInstance()
                    val year = calendar.get(Calendar.YEAR)
                    calendar.time = date!!

                    // month start from 0, so add 1 to count 12 months
                    val month = calendar.get(Calendar.MONTH) + 1
                    Log.d(TAG, "onPostExecute: month" + month)

                    if (calendar.get(Calendar.YEAR) == year) {
                        var doesMonthExist = false
                        for (e in entries) {
                            doesMonthExist = e.x == month.toFloat()
                        }
                        if (!doesMonthExist) {
                            entries.add(Entry(month.toFloat(), t.amount.toFloat()))
                        } else {
                            for (e in entries) {
                                if (e.x == month.toFloat()) {
                                    e.y = e.y + t.amount.toFloat()
                                }
                            }
                        }
                    }
                }

                for (e in entries) {
                    Log.d(TAG, "LineChart - x: " + e.x + "/ y: " + e.y)
                }

                val dataSet = LineDataSet(entries, "Profit chart")
                dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
                dataSet.setDrawFilled(true)
                dataSet.color = Color.GREEN
                val data = LineData(dataSet)
                //get x axis o
                val xAxis = profitChart.xAxis
                xAxis.spaceMin = 1F
                xAxis.spaceMax = 1F
                xAxis.axisMaximum = 12F
                xAxis.isEnabled = false
                //get rightY axis
                val rightYAxis = profitChart.axisRight
                rightYAxis.isEnabled = false
                //get leftY axis
                val leftYAxis = profitChart.axisLeft
                leftYAxis.axisMaximum = 100F
                leftYAxis.axisMinimum = 10F
                leftYAxis.setDrawGridLines(false)
                //val description = Description()
                //description.text = "Description"
                //setting linechart
                profitChart.description = null
                profitChart.data = data
                profitChart.invalidate()
                profitChart.animateY(2000)
            } else {
                toast("transactions array list was null")
                Log.d(TAG, "onPostExecute: transactions array list was null")
            }
        }
    }

    inner class GetSpending : AsyncTask<Int, Void, ArrayList<Shopping>>() {

        override fun doInBackground(vararg intergers: Int?): ArrayList<Shopping>? {
            try {
                val db = databaseHelper.readableDatabase
                val cursor = db.query("shopping", arrayOf("date", "price"), "user_id=?",
                arrayOf(String.valueOf(intergers[0])), null, null, null)
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        val shoppingList = ArrayList<Shopping>()
                        for (i in 0 until cursor.count) {
                            val shopping = Shopping()
                            shopping.date = cursor.getString(cursor.getColumnIndex("date"))
                            shopping.price = cursor.getDouble(cursor.getColumnIndex("price"))
                            shoppingList.add(shopping)
                            cursor.moveToNext()
                        }
                        cursor.close()
                        db.close()
                        return shoppingList
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

        override fun onPostExecute(shoppings: ArrayList<Shopping>?) {
            super.onPostExecute(shoppings)

            if (shoppings != null) {
                val entries = ArrayList<BarEntry>()
                for (s in shoppings){
                    val date = SimpleDateFormat("yyyy-MM-dd").parse(s.date!!)
                    val calendar = Calendar.getInstance()
                    val month = calendar.get(Calendar.MONTH) + 1
                    calendar.time = date!!
                    val day = calendar.get(Calendar.DAY_OF_MONTH) + 1
                    if (calendar.get(Calendar.MONTH) + 1 == month) {
                        var doesDayExist = false
                        for (e in entries) {
                            doesDayExist = e.x == day.toFloat()
                        }
                        if (!doesDayExist) {
                            entries.add(BarEntry(day.toFloat(), s.price.toFloat()))
                        } else {
                            for (e in entries) {
                                if (e.x == day.toFloat()) {
                                    e.y = e.y + s.price.toFloat()
                                }
                            }
                        }
                    }
                }

                val dataset = BarDataSet(entries, "Shopping chart")
                dataset.color = Color.CYAN
                val data = BarData(dataset)
                //get rightY axis
                val righYAxis = dailySpentChart.axisRight
                righYAxis.isEnabled = false
                //get x axis
                val xAxis = dailySpentChart.xAxis
                xAxis.spaceMax = 1F
                xAxis.spaceMin = 1F
                xAxis.axisMaximum = 31F
                xAxis.isEnabled = false
                //get leftY axis
                val leftYAxis = dailySpentChart.axisLeft
                leftYAxis.axisMaximum = 40F
                leftYAxis.axisMinimum = 10F
                leftYAxis.setDrawGridLines(false)
                //setting barchart
                dailySpentChart.description = null
                dailySpentChart.data = data
                dailySpentChart.invalidate()
                dailySpentChart.animateY(2000)

            } else {
                toast("shoppings array list was null")
                Log.d(TAG, "onPostExecute: shoppings array list was null")
            }
        }
    }
}