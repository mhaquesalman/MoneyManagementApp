package com.salman.moneymanagementapp.activity

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.salman.moneymanagementapp.R
import com.salman.moneymanagementapp.adapter.LoanAdapter
import com.salman.moneymanagementapp.database.DatabaseHelper
import com.salman.moneymanagementapp.model.Loan
import com.salman.moneymanagementapp.util.Utils
import com.salman.moneymanagementapp.util.toast
import kotlinx.android.synthetic.main.activity_loan.*
import kotlinx.android.synthetic.main.activity_loan.bottomNavView
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception
import java.util.ArrayList


class LoanActivity : AppCompatActivity() {
    val TAG = "LoanActivity"
    lateinit var loanAdapter: LoanAdapter
    lateinit var utils: Utils
    lateinit var databaseHelper: DatabaseHelper
    lateinit var getLoans: GetLoans

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan)


        initBottomNavView()

        databaseHelper = DatabaseHelper(this)
        utils = Utils(this)

        loanAdapter = LoanAdapter(this)
        loanRecView.layoutManager = LinearLayoutManager(this)
        loanRecView.adapter = loanAdapter

        val user = utils.isUserLoggedIn()
        getLoans = GetLoans()
        if (user != null) {
            getLoans.execute(user.get_id())
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (getLoans != null) {
            if (!getLoans.isCancelled) {
                getLoans.cancel(true)
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
//                    val intent = Intent(this, LoanActivity::class.java)
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
//                    startActivity(intent)
//                    return@OnNavigationItemSelectedListener true
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

    inner class GetLoans : AsyncTask<Int, Void, ArrayList<Loan>>() {

        override fun doInBackground(vararg integers: Int?): ArrayList<Loan>? {
            try {
                val db = databaseHelper.readableDatabase
                val cursor = db.query("loans", null, "user_id=?",
                arrayOf(integers[0].toString()), null, null, "init_date DESC")
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        val loansList = ArrayList<Loan>()
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

                            loansList.add(loan)
                            cursor.moveToNext()
                        }
                        cursor.close()
                        db.close()
                        return loansList
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
                loanAdapter.setLoans(loans)
            } else {
                loanAdapter.setLoans(ArrayList<Loan>())
            }
        }
    }
}