package com.salman.moneymanagementapp.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log


private const val DB_VERSION: Int = 1
private const val DB_NAME: String = "MoneyManageDB"

class DatabaseHelper(val context: Context?) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    val TAG = "DatabaseHelper"

    override fun onCreate(sqLiteDatabase: SQLiteDatabase?) {
        val createUserTable =
            "CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, email TEXT, password TEXT," +
                    "first_name TEXT, last_name TEXT, address TEXT, image_url TEXT, remained_amount DOUBLE)"

        val createShoppingTable =
            "CREATE TABLE shopping (id INTEGER PRIMARY KEY AUTOINCREMENT, item_id INTEGER, " +
                    "user_id INTEGER, transaction_id INTEGER, price DOUBLE, date DATE, description TEXT)"


        val createInvestmentTable =
            "CREATE TABLE investments (id INTEGER PRIMARY KEY AUTOINCREMENT, amount DOUBLE, " +
                    "monthly_roi DOUBLE, name TEXT, init_date DATE, finish_date DATE, user_id INTEGER, transaction_id INTEGER)"

        val createLoansTable =
            "CREATE TABLE loans (id INTEGER PRIMARY KEY AUTOINCREMENT, init_date DATE, " +
                    "finish_date DATE, init_amount DOUBLE, remained_amount DOUBLE, monthly_payment DOUBLE, monthly_roi DOUBLE," +
                    "name TEXT, user_id INTEGER, transaction_id INTEGER)"

        val createTransactionTable =
            "CREATE TABLE transactions (id INTEGER PRIMARY KEY AUTOINCREMENT, amount double, " +
                    "date DATE, type TEXT, user_id INTEGER, recipient TEXT, description TEXT)"

        val createItemsTable =
            "CREATE TABLE items (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, image_url TEXT," +
                    "description TEXT)"

        sqLiteDatabase?.execSQL(createUserTable)
        sqLiteDatabase?.execSQL(createShoppingTable)
        sqLiteDatabase?.execSQL(createInvestmentTable)
        sqLiteDatabase?.execSQL(createLoansTable)
        sqLiteDatabase?.execSQL(createTransactionTable)
        sqLiteDatabase?.execSQL(createItemsTable)

        addTestTransaction(sqLiteDatabase)
        addTestItems(sqLiteDatabase)
    }

    fun addTestTransaction(db: SQLiteDatabase?) {
        Log.d(TAG, "addTestTransaction: started")
        val values = ContentValues()
        values.put("id", 0)
        values.put("amount", 10.5)
        values.put("date", "2019-10-04")
        values.put("type", "shopping")
        values.put("user_id", 1)
        values.put("description", "Grocery shopping")
        values.put("recipient", "Walmart")
        val newTransactionId = db?.insert("transactions", null, values)
        Log.d(TAG, "addTestTransaction: transaction id: $newTransactionId")
    }

    fun addTestItems(db: SQLiteDatabase?) {
        Log.d(TAG, "addInitialItems: started")
        val values = ContentValues()
        values.put("name", "Bike")
        values.put(
            "image_url",
            "https://cdn.shopify.com/s/files/1/0903/4494/products/Smashing-Pumpkin-GX-Eagle-complete-front-white.jpg"
        )
        values.put("description", "The perfect mountain bike")
        db?.insert("items", null, values)
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase?, i: Int, j: Int) {

    }

}