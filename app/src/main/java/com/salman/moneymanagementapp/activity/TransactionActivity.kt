package com.salman.moneymanagementapp.activity

import android.content.Intent
import android.database.Cursor
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.salman.moneymanagementapp.R
import com.salman.moneymanagementapp.adapter.TransactionAdapter
import com.salman.moneymanagementapp.database.DatabaseHelper
import com.salman.moneymanagementapp.model.Transaction
import com.salman.moneymanagementapp.util.Utils
import com.salman.moneymanagementapp.util.toast
import kotlinx.android.synthetic.main.activity_loan.*
import kotlinx.android.synthetic.main.activity_loan.bottomNavView
import kotlinx.android.synthetic.main.activity_transaction.*
import java.lang.Exception
import java.util.ArrayList

class TransactionActivity : AppCompatActivity() {
    val TAG = "TransactionActivity"
    lateinit var utils: Utils
    lateinit var databaseHelper: DatabaseHelper
    lateinit var transactionAdapter: TransactionAdapter
    lateinit var getTransactions: GetTransactions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction)

        initBottomNavView()

        databaseHelper = DatabaseHelper(this)
        utils = Utils(this)

        transactionAdapter = TransactionAdapter()
        transactionRecView.layoutManager = LinearLayoutManager(this)
        transactionRecView.adapter = transactionAdapter

        // call method
        initSearch()

        btnSearch.setOnClickListener {
            initSearch()
        }

        rgType.setOnCheckedChangeListener { radioGroup, i ->
            initSearch()
        }
    }

    fun initBottomNavView() {
        Log.d(TAG, "initBottomNavView: started")
        bottomNavView.selectedItemId = R.id.menu_item_transaction
        bottomNavView.setOnNavigationItemReselectedListener {
            when (it.itemId) {
                R.id.menu_item_stats -> {
                }
                R.id.menu_item_transaction -> {
                }
                R.id.menu_item_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                }
                R.id.menu_item_loan -> {
                    val intent = Intent(this, LoanActivity::class.java)
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                }
                R.id.menu_item_investment -> {
                    val intent = Intent(this, InvestmentActivity::class.java)
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                }
                else -> toast("something is wrong!")
            }
        }
    }

    fun initSearch() {
        val user = utils.isUserLoggedIn()
        getTransactions = GetTransactions()
        if (user != null) {
            getTransactions.execute(user.get_id())
        }
    }

    inner class GetTransactions : AsyncTask<Int, Void, ArrayList<Transaction>>() {
        var type = "all"
        var min = 0.0

        override fun onPreExecute() {
            super.onPreExecute()

            this.min = edtTxtMin.text.toString().toDouble()
            when (rgType.checkedRadioButtonId) {
                R.id.rbInvestment -> type = "investment"
                R.id.rbLoan -> type = "loan"
                R.id.rbLoanPayment -> type = "loan payment"
                R.id.rbProfit -> type = "profit"
                R.id.rbShopping -> type = "shopping"
                R.id.rbSend -> type = "send"
                R.id.rbReceive -> type = "receive"
                else -> type = "all"
            }
        }

        //rawQuery("SELECT id, name FROM people WHERE name = ? AND id = ?", new String[] {"David", "2"});
        override fun doInBackground(vararg integers: Int?): ArrayList<Transaction>? {
            try {
                var cursor: Cursor
                val db = databaseHelper.readableDatabase
                if (type.equals("all")) {
//                    val q = "SELECT * FROM transactions WHERE ABS(amount)>? AND type=? AND id=? ORDER BY date DESC"
//                    db.rawQuery(q, arrayOf(min.toString(), type, integers[0].toString()))

                    cursor = db.query("transactions", null, "user_id=?",
                    arrayOf(integers[0].toString()), null, null, "date DESC")
                } else {
                    cursor = db.query("transactions", null, "type=? AND user_id=?",
                        arrayOf(type, integers[0].toString()), null, null, "date DESC")
                }

                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        val transactionList = ArrayList<Transaction>()
                        for (i in  0 until cursor.count) {
                            val transaction = Transaction(
                                id = cursor.getInt(cursor.getColumnIndex("id")),
                                amount = cursor.getDouble(cursor.getColumnIndex("amount")),
                                date = cursor.getString(cursor.getColumnIndex("date")),
                                description = cursor.getString(cursor.getColumnIndex("description")),
                                recipient = cursor.getString(cursor.getColumnIndex("recipient")),
                                type = cursor.getString(cursor.getColumnIndex("type")),
                                user_id = cursor.getInt(cursor.getColumnIndex("user_id"))
                            )

                            var absoluteAmount = transaction.amount
                            if (absoluteAmount < 0) {
                                absoluteAmount = -absoluteAmount
                            }
                            if (absoluteAmount > min) {
                                transactionList.add(transaction)
                            }
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
                transactionAdapter.setTransactions(transactions)
            } else {
                txtNoTransaction.visibility = View.INVISIBLE
                transactionAdapter.setTransactions(ArrayList<Transaction>())
            }
        }
    }
}