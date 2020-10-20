package com.salman.moneymanagementapp.activity

import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.salman.moneymanagementapp.R
import com.salman.moneymanagementapp.adapter.ItemsAdapter
import com.salman.moneymanagementapp.database.DatabaseHelper
import com.salman.moneymanagementapp.model.Item
import com.salman.moneymanagementapp.model.User
import com.salman.moneymanagementapp.util.SelectItemDialog
import com.salman.moneymanagementapp.util.Utils
import com.salman.moneymanagementapp.util.toast
import kotlinx.android.synthetic.main.activity_shopping.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import java.lang.*


class ShoppingActivity : AppCompatActivity(), ItemsAdapter.GetItem {
    val TAG = "ShoppingActivity"
    val calendar = Calendar.getInstance()
    var selectedItem: Item? = null
    lateinit var databaseHelper: DatabaseHelper
    lateinit var addShopping: AddShopping

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shopping)

        databaseHelper = DatabaseHelper(this)

        val dateSetLisener = DatePickerDialog.OnDateSetListener { datePicker, year, month, day ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)
            val formateDate = SimpleDateFormat("yyyy-MM-dd").format(calendar.time)
            edtTxtDate.setText(formateDate)
        }


        btnPickDate.setOnClickListener {
            DatePickerDialog(this, dateSetLisener, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnAdd.setOnClickListener {
            initAdd()
        }

        btnPick.setOnClickListener {
            val selectItemDialog = SelectItemDialog()
            selectItemDialog.show(supportFragmentManager, "select item dialog")
        }

    }

    override fun onDestroy() {
        super.onDestroy()

        if (addShopping != null) {
            if (!addShopping.isCancelled) {
                addShopping.cancel(true)
            }
        }
    }

    fun initAdd() {
        if (selectedItem != null) {
            if (!edtTxtPrice.text.toString().equals("")) {
                if (!edtTxtDate.text.toString().equals("")) {
                    addShopping = AddShopping()
                    addShopping.execute()

                } else {
                    txtWarning.visibility = View.VISIBLE
                    txtWarning.setText("Select a date!")
                }
            } else {
                txtWarning.visibility = View.VISIBLE
                txtWarning.setText("Add a price!")
            }
        } else {
            txtWarning.visibility = View.VISIBLE
            txtWarning.setText("Select an item!")
        }
    }

    override fun onGettingItemResult(item: Item) {
        Log.d(TAG, "onGettingItemResult: item" + item.toString())
        selectedItem = item
        itemRelLayout.visibility = View.VISIBLE
        Glide.with(this)
            .asBitmap()
            .load(item.image_url)
            .into(itemImg)
        txtItemName.setText(item.name)
        edtTxtDesc.setText(item.description)
    }

    inner class AddShopping : AsyncTask<Void, Void, Void>() {

        var loggedInUser: User? = null
        lateinit var date: String
        var price: Double = 0.0
        lateinit var store: String
        lateinit var description: String

        override fun onPreExecute() {
            super.onPreExecute()

            val utils = Utils(this@ShoppingActivity)
            this.loggedInUser = utils.isUserLoggedIn()
            this.date = edtTxtDate.text.toString()
            this.price = edtTxtPrice.text.toString().toDouble()
            this.store = edtTxtStore.text.toString()
            this.description = edtTxtDesc.text.toString()
        }

        override fun doInBackground(vararg p0: Void?): Void? {
            try {
                val db = databaseHelper.writableDatabase

                val transactionValue = ContentValues()
                transactionValue.put("amount", -price)
                transactionValue.put("description", description)
                transactionValue.put("user_id", loggedInUser?.get_id())
                transactionValue.put("type", "shopping")
                transactionValue.put("date", date)
                transactionValue.put("recipient", store)
                val id = db.insert("transactions", null, transactionValue)

                val shoppingValue = ContentValues()
                shoppingValue.put("item_id", selectedItem?.get_id())
                shoppingValue.put("transaction_id", id)
                shoppingValue.put("user_id", loggedInUser?.get_id())
                shoppingValue.put("price", price)
                shoppingValue.put("description", description)
                shoppingValue.put("date", date)
                val shoppingId = db.insert("shopping", null, shoppingValue)
                Log.d(TAG, "doInBackground: shopping id:" + shoppingId)

                val cursor = db.query("users", arrayOf("remained_amount"), "id=?",
                arrayOf(loggedInUser?.get_id().toString()), null, null, null)
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                       val remainedAmount = cursor.getDouble(cursor.getColumnIndex("remained_amount"))
                        val amountValue = ContentValues()
                        amountValue.put("remained_amount", remainedAmount - price)
                        val affectedRow = db.update("users", amountValue, "id=?",
                        arrayOf(loggedInUser?.get_id().toString()))
                        Log.d(TAG, "doInBackground: affected row: " + affectedRow)
                    }
                    cursor.close()
                }
                db.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)

            toast("${selectedItem?.name} is added")
            val intent = Intent(this@ShoppingActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}