package com.salman.moneymanagementapp.util

import android.app.Dialog
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.salman.moneymanagementapp.R
import com.salman.moneymanagementapp.adapter.ItemsAdapter
import com.salman.moneymanagementapp.database.DatabaseHelper
import com.salman.moneymanagementapp.model.Item
import kotlinx.android.synthetic.main.dialog_select_item.*
import java.lang.Exception
import java.util.ArrayList

class SelectItemDialog : DialogFragment(), ItemsAdapter.GetItem {
    val TAG = "SelectItemDialog"
    lateinit var edtTxtItemName: EditText
    lateinit var itemsRecView: RecyclerView
    lateinit var getItem: ItemsAdapter.GetItem
    lateinit var databaseHelper: DatabaseHelper
    lateinit var getAllItems: GetAllItems
    lateinit var searchForItems: SearchForItems
    lateinit var mAdapter: ItemsAdapter

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val view = activity!!.layoutInflater.inflate(R.layout.dialog_select_item, null)

        edtTxtItemName = view.findViewById(R.id.edtTxtItemName)
        itemsRecView = view.findViewById(R.id.itemsRecView)
        itemsRecView.layoutManager = LinearLayoutManager(activity)

        mAdapter = ItemsAdapter(activity!!, this)
        itemsRecView.adapter = mAdapter

        databaseHelper = DatabaseHelper(activity)

        edtTxtItemName.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {

                searchForItems = SearchForItems()
                searchForItems.execute(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })

        getAllItems = GetAllItems()
        getAllItems.execute()

        val builder = AlertDialog.Builder(activity!!)
            .setView(view)
            .setTitle("Select an item")

        return builder.create()
    }

    override fun onDestroy() {
        super.onDestroy()

        if (getAllItems != null) {
            if (!getAllItems.isCancelled) {
                getAllItems.cancel(true)
            }
        }

        if (searchForItems != null) {
            if (!searchForItems.isCancelled) {
                searchForItems.cancel(true)
            }
        }
    }

    override fun onGettingItemResult(item: Item) {
        Log.d(TAG, "onGettingItemResult: item: " + item.toString())
        try {
            getItem = activity as ItemsAdapter.GetItem
            getItem.onGettingItemResult(item)
            dismiss()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    inner class GetAllItems : AsyncTask<Void, Void, ArrayList<Item>>() {

        override fun doInBackground(vararg p0: Void?): ArrayList<Item>? {
            try {
                val db = databaseHelper.readableDatabase
                val cursor = db.query("items", null, null, null, null ,null, null)
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        val itemList = ArrayList<Item>()
                        for (i in 0 until cursor.count) {
                            val item = Item(
                                id = cursor.getInt(cursor.getColumnIndex("id")),
                                name = cursor.getString(cursor.getColumnIndex("name")),
                                image_url = cursor.getString(cursor.getColumnIndex("image_url")),
                                description = cursor.getString(cursor.getColumnIndex("description"))
                            )
                            itemList.add(item)
                            cursor.moveToNext()
                        }
                        cursor.close()
                        db.close()
                        return itemList
                    } else {
                        cursor.close()
                        db.close()
                        return null
                    }
                } else {
                    db.close()
                    return  null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }

        override fun onPostExecute(items: ArrayList<Item>?) {
            super.onPostExecute(items)

            if (items != null) {
                mAdapter.setItems(items)
            } else {
                mAdapter.setItems(ArrayList<Item>())
            }
        }
    }

    inner class SearchForItems : AsyncTask<String, Void, ArrayList<Item>>() {

        override fun doInBackground(vararg strings: String?): ArrayList<Item>? {
            try {
                val db = databaseHelper.readableDatabase
                val cursor = db.query("items", null, "name LIKE ?",
                    arrayOf(strings[0]), null ,null, null)

                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        val itemList = ArrayList<Item>()
                        for (i in 0 until cursor.count) {
                            val item = Item(
                                id = cursor.getInt(cursor.getColumnIndex("id")),
                                name = cursor.getString(cursor.getColumnIndex("name")),
                                image_url = cursor.getString(cursor.getColumnIndex("image_url")),
                                description = cursor.getString(cursor.getColumnIndex("description"))
                            )
                            itemList.add(item)
                            cursor.moveToNext()
                        }
                        cursor.close()
                        db.close()
                        return itemList
                    } else {
                        cursor.close()
                        db.close()
                        return null
                    }
                } else {
                    db.close()
                    return  null
                }

            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }

        override fun onPostExecute(items: ArrayList<Item>?) {
            super.onPostExecute(items)

            if (items != null) {
                mAdapter.setItems(items)
            } else {
                mAdapter.setItems(ArrayList<Item>())
            }
        }
    }
}