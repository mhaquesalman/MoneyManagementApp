package com.salman.moneymanagementapp.activity

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.salman.moneymanagementapp.R
import com.salman.moneymanagementapp.adapter.InvestmentAdapter
import com.salman.moneymanagementapp.database.DatabaseHelper
import com.salman.moneymanagementapp.model.Investment
import com.salman.moneymanagementapp.util.Utils
import com.salman.moneymanagementapp.util.toast
import kotlinx.android.synthetic.main.activity_investment.*
import kotlinx.android.synthetic.main.activity_loan.*
import kotlinx.android.synthetic.main.activity_main.bottomNavView
import java.util.ArrayList

class InvestmentActivity : AppCompatActivity() {
    val TAG = "InvestmentActivity"
    lateinit var databaseHelper: DatabaseHelper
    lateinit var getInvestment: GetInvestment
    lateinit var investmentAdapter: InvestmentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_investment)


        databaseHelper = DatabaseHelper(this)

        initBottomNavView()
        investmentAdapter = InvestmentAdapter(this)
        investmentRecView.adapter = investmentAdapter
        investmentRecView.layoutManager = LinearLayoutManager(this)

        val utils = Utils(this)
        val user = utils.isUserLoggedIn()
        getInvestment = GetInvestment()
        if (user != null) {
           getInvestment.execute(user.get_id())
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        if (getInvestment != null) {
            if (!getInvestment.isCancelled) {
                getInvestment.cancel(true)
            }
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
//                    val intent = Intent(this, InvestmentActivity::class.java)
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
//                    startActivity(intent)
//                    return@OnNavigationItemSelectedListener true
                }
                // else -> toast("something is wrong!")
            }
            false
        }
        bottomNavView.selectedItemId = R.id.menu_item_investment
        bottomNavView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
    }

    inner class GetInvestment : AsyncTask<Int, Void, ArrayList<Investment>>() {
        override fun doInBackground(vararg integers: Int?): ArrayList<Investment>? {
            try {
                val db = databaseHelper.readableDatabase
                val cursor = db.query(
                    "investments", null, "user_id=?",
                    arrayOf(integers[0].toString()), null, null, "init_date DESC"
                )
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        val investmentList = ArrayList<Investment>()
                        for (i in 0 until cursor.count) {
                            val investment = Investment(
                                id = cursor.getInt(cursor.getColumnIndex("id")),
                                user_id = cursor.getInt(cursor.getColumnIndex("user_id")),
                                transaction_id = cursor.getInt(cursor.getColumnIndex("transaction_id")),
                                amount = cursor.getDouble(cursor.getColumnIndex("amount")),
                                finish_date = cursor.getString(cursor.getColumnIndex("finish_date")),
                                init_date = cursor.getString(cursor.getColumnIndex("init_date")),
                                monthly_roi = cursor.getDouble(cursor.getColumnIndex("monthly_roi")),
                                name = cursor.getString(cursor.getColumnIndex("name"))
                            )
                            investmentList.add(investment)
                            cursor.moveToNext()
                        }
                        cursor.close()
                        db.close()
                        return investmentList
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

        override fun onPostExecute(result: ArrayList<Investment>?) {
            super.onPostExecute(result)

            if (result != null) {
                investmentAdapter.setInvestments(result)
            } else {
                investmentAdapter.setInvestments(ArrayList<Investment>())
            }
        }
    }
}