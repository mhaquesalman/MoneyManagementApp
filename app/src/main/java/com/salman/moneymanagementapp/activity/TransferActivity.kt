package com.salman.moneymanagementapp.activity

import android.app.DatePickerDialog
import android.content.ContentValues
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.salman.moneymanagementapp.R
import com.salman.moneymanagementapp.database.DatabaseHelper
import com.salman.moneymanagementapp.util.Utils
import kotlinx.android.synthetic.main.activity_transfer.*
import java.text.SimpleDateFormat
import java.util.*


class TransferActivity : AppCompatActivity() {
    val TAG = "TransferActivity"
    val calendar = Calendar.getInstance()
    lateinit var databaseHelper: DatabaseHelper
    lateinit var utils: Utils
    lateinit var addTransaction: AddTransaction

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transfer)

        databaseHelper = DatabaseHelper(this)
        utils = Utils(this)
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
    }

    val dateListener = DatePickerDialog.OnDateSetListener { datePicker, year, month, day ->
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, day)
        val formateDate = SimpleDateFormat("yyyy-MM-dd").format(calendar.time)
        edtTxtDate.setText(formateDate)
    }

    private fun setOnclickListener() {

        btnPickDate.setOnClickListener {
            DatePickerDialog(
                this, dateListener, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnAdd.setOnClickListener {
            if (validateData()) {
                val user = utils.isUserLoggedIn()
                addTransaction = AddTransaction()
                if (user != null) {
                    addTransaction.execute(user.get_id())
                }
            } else {
                txtWarning.visibility = View.VISIBLE
                txtWarning.setText("Fields can't be empty")
            }
        }
    }

    fun validateData(): Boolean {
        if (edtTxtAmount.text.toString().equals("")) return false
        if (edtTxtDate.text.toString().equals("")) return false
        if (edtTxtRecipient.text.toString().equals("")) return false
        return true
    }


    inner class AddTransaction : AsyncTask<Int, Void, Void>() {
        var amount: Double = 0.0
        var description = ""
        lateinit var recipient: String
        lateinit var date: String
        lateinit var type: String

        override fun onPreExecute() {
            super.onPreExecute()

            this.amount = edtTxtAmount.text.toString().toDouble()
            this.recipient = edtTxtRecipient.text.toString()
            this.date = edtTxtDate.text.toString()
            this.description = edtTxtDescription.text.toString()

            when (rgType.checkedRadioButtonId) {
                R.id.btnReceive -> this.type = "receive"
                R.id.btnSend -> {
                    this.type = "send"
                    amount = -amount
                }
            }
        }

        override fun doInBackground(vararg integers: Int?): Void? {

            try {
                val db = databaseHelper.writableDatabase
                val values = ContentValues()
                values.put("amount", amount)
                values.put("recipient", recipient)
                values.put("date", date)
                values.put("type", type)
                values.put("description", description)
                values.put("user_id", integers[0])

                val id = db.insert("transactions", null, values)

                val errVal: Long = -1
                if (id != errVal) {
                    val cursor= db.query("users", arrayOf("remained_amount"), "id=?",
                        arrayOf(integers[0].toString()), null, null,null)

                    if (null != cursor) {
                        if (cursor.moveToFirst()) {
                            val currentRemainedAmount: Double = cursor.getDouble(cursor.getColumnIndex("remained_amount"))
                            cursor.close()
                            val newValues = ContentValues()
                            newValues.put("remained_amount", currentRemainedAmount + amount)
                            val affectedRows = db.update("users", newValues, "id=?",
                                arrayOf(java.lang.String.valueOf(integers[0])))
                            Log.d(TAG, "doInBackground: updatedRows: $affectedRows")
                            db.close()
                        } else {
                            cursor.close()
                            db.close()
                        }
                    } else {
                        db.close()
                    }
                } else {
                    db.close()
                }
            } catch (e : Exception) {
                e.printStackTrace()
            }
            return null
        }

        }
    }

