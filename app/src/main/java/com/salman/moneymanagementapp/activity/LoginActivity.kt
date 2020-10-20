package com.salman.moneymanagementapp.activity

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.salman.moneymanagementapp.R
import com.salman.moneymanagementapp.database.DatabaseHelper
import com.salman.moneymanagementapp.model.User
import com.salman.moneymanagementapp.util.Utils
import kotlinx.android.synthetic.main.activity_login.*
import java.lang.Exception

class LoginActivity : AppCompatActivity() {
    val TAG = "LoginActivity"
    lateinit var databaseHelper: DatabaseHelper
    lateinit var doesEmailExist: DoesEmailExist
    lateinit var loginUser: LoginUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        databaseHelper = DatabaseHelper(this)

        btnLogin.setOnClickListener {
            initLogin()
        }

        txtRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        txtLicense.setOnClickListener {
            val intent = Intent(this, WebsiteActivity::class.java)
            startActivity(intent)
        }

    }

     fun initLogin() {
         Log.d(TAG, "initLogin: started")

         if (!edtTxtEmail.text.toString().equals("")) {
             if (!edtTxtPassword.text.toString().equals("")) {
                 txtWarning.visibility = View.GONE

                 doesEmailExist = DoesEmailExist()
                 doesEmailExist.execute(edtTxtEmail.text.toString())

             } else {
                 txtWarning.visibility = View.VISIBLE
                 txtWarning.text = "Please enter your Password!"
             }
         } else {
             txtWarning.setVisibility(View.VISIBLE);
             txtWarning.setText("Please enter your email address!");
         }
    }

    inner class DoesEmailExist : AsyncTask<String, Void, Boolean>() {

        override fun doInBackground(vararg strings: String?): Boolean {
            try {
                val db = databaseHelper.readableDatabase
                val cursor = db.query("users", arrayOf("email"), "email=?",
                    arrayOf(strings[0]),null, null, null)
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        cursor.close()
                        db.close()
                        return true
                    } else {
                        cursor.close()
                        db.close()
                        return false
                    }
                } else {
                    db.close()
                    return false
                }
            }catch (e: Exception) {
                e.printStackTrace()
                return false
            }
        }

        override fun onPostExecute(result: Boolean) {
            super.onPostExecute(result)
            if (result) {
                loginUser = LoginUser()
                loginUser.execute()
            } else {
                txtWarning.visibility = View.VISIBLE
                txtWarning.text = "No user found with this email!"
            }
        }
    }

    inner class LoginUser : AsyncTask<Void, Void, User>() {
        lateinit var email: String
        lateinit var password: String

        override fun onPreExecute() {
            super.onPreExecute()

            this.email = edtTxtEmail.text.toString()
            this.password = edtTxtPassword.text.toString()
        }

        override fun doInBackground(vararg p0: Void?): User? {
            try {
                val db = databaseHelper.readableDatabase
                val cursor = db.query("users", null, "email=? AND password=?",
                    arrayOf(email, password), null, null, null)

                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        val user = User(
                            id = cursor.getInt(cursor.getColumnIndex("id")),
                            email = cursor.getString(cursor.getColumnIndex("email")),
                            password = cursor.getString(cursor.getColumnIndex("password")),
                            first_name = cursor.getString(cursor.getColumnIndex("first_name")),
                            last_name = cursor.getString(cursor.getColumnIndex("last_name")),
                            image_url = cursor.getString(cursor.getColumnIndex("image_url")),
                            address = cursor.getString(cursor.getColumnIndex("address")),
                            remained_amount = cursor.getDouble(cursor.getColumnIndex("remained_amount"))
                        )
                        cursor.close()
                        db.close()
                        return user
                    } else {
                        cursor.close()
                        db.close()
                        return null
                    }
                } else {
                    db.close()
                    return null
                }
            }catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }

        override fun onPostExecute(user: User?) {
            super.onPostExecute(user)
            if (user != null) {
                val utils = Utils(this@LoginActivity)
                utils.addUserToSharedPreferences(user)
                val intent = Intent(applicationContext, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            } else {
                txtWarning.visibility = View.VISIBLE
                txtWarning.text = "Password is incorrect!"
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        if (doesEmailExist != null) {
            if (!doesEmailExist.isCancelled) {
                doesEmailExist.cancel(true)
            }
        }

        if (loginUser != null) {
            if (!loginUser.isCancelled) {
                loginUser.cancel(true)
            }
        }
    }
}