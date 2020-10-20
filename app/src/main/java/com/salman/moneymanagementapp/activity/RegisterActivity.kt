package com.salman.moneymanagementapp.activity

import android.content.ContentValues
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.salman.moneymanagementapp.R
import com.salman.moneymanagementapp.database.DatabaseHelper
import com.salman.moneymanagementapp.model.User
import com.salman.moneymanagementapp.util.Utils
import com.salman.moneymanagementapp.util.toast
import kotlinx.android.synthetic.main.activity_register.*


class RegisterActivity : AppCompatActivity() {
    val TAG = "RegisterActivity"
    lateinit var imageUrl: String
    lateinit var databaseHelper: DatabaseHelper
    lateinit var doesUserExist: DoesUserExist
    lateinit var registerUser: RegisterUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        databaseHelper = DatabaseHelper(this)
        imageUrl = "first"
        handleImageUrl()


        btnRegister.setOnClickListener {
            initRegister()
        }

        txtLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        txtLicense.setOnClickListener {
            val intent = Intent(this, WebsiteActivity::class.java)
            startActivity(intent)
        }

    }

    fun handleImageUrl() {
        Log.d(TAG, "handleImageUrl: started")

        firstImage.setOnClickListener {
            imageUrl = "first"
            toast("First image selected")
        }

        secondImage.setOnClickListener {
            imageUrl = "second"
            toast("Second image selected")
        }

        thirdImage.setOnClickListener {
            imageUrl = "third"
            toast("Third image selected")
        }

        forthImage.setOnClickListener {
            imageUrl = "forth"
            toast("Foth image selected")
        }

        fifthImage.setOnClickListener {
            imageUrl = "fifth"
            toast("Fifth image selected")
        }

        sixthImage.setOnClickListener {
            imageUrl = "sixth"
            toast("Sixth image selected")
        }
    }

    fun initRegister() {
        Log.d(TAG, "initRegister: started")

        val email = edtTxtEmail.text.toString()
        val password = edtTxtPassword.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            txtWarning.visibility = View.VISIBLE
            txtWarning.text = "Please enter the password and Email"
        } else {
            txtWarning.visibility = View.GONE
            doesUserExist = DoesUserExist()
            doesUserExist.execute(email)
        }
    }

    inner class DoesUserExist : AsyncTask<String, Void, Boolean>() {

        override fun doInBackground(vararg strings: String?): Boolean {
            try {
                val db = databaseHelper.readableDatabase
                val cursor = db!!.query("users", arrayOf("id", "email"), "email=?",
                    arrayOf(strings[0]), null, null, null)
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        if (cursor.getString(cursor.getColumnIndex("email")).equals(strings[0])) {
                            cursor.close()
                            db.close()
                            return true
                        } else {
                            cursor.close()
                            db.close()
                            return true
                        }
                    } else {
                        cursor.close()
                        db.close()
                        return true
                    }
                } else {
                    db.close()
                    return true
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return true
            }
        }

        override fun onPostExecute(result: Boolean) {
            super.onPostExecute(result)
            if (result) {
                txtWarning.visibility = View.VISIBLE
                txtWarning.text = "Email already exist try another one!"
            } else {
                txtWarning.visibility = View.GONE
                registerUser = RegisterUser()
                registerUser.execute()
            }
        }

    }

    inner class RegisterUser : AsyncTask<Void, Void, User>() {
        lateinit var first_name: String
        lateinit var last_name: String
        lateinit var email: String
        lateinit var password: String
        lateinit var address: String

        override fun onPreExecute() {
            super.onPreExecute()

             email = edtTxtEmail.text.toString()
             password = edtTxtPassword.text.toString()
             address = edtTxtAddress.text.toString()
             val name = edtTxtName.text.toString()

            val names = name.split(" ").toTypedArray()
            if (names.size > 1) {
                first_name = names[0]
                for (i in 1 until names.size) {
                    if (i > 1) {
                        last_name += " " + names[i]
                    } else {
                        last_name += names[i]
                    }
                }
            } else {
                first_name = names[0]
                last_name = "n/a"
            }
        }

        override fun doInBackground(vararg p0: Void?): User? {
            try {
                val db = databaseHelper.writableDatabase
                val values = ContentValues()
                values.put("email", email)
                values.put("password", password)
                values.put("first_name", first_name)
                values.put("last_name", last_name)
                values.put("remained_amount", 0.0)
                values.put("image_url", imageUrl)

                val userId = db!!.insert("users", null, values)
                Log.d(TAG, "doInBackground: $userId")

                val cursor = db.query("users", null, "id=?",
                    arrayOf(userId.toString()), null, null, null)

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

            }catch (e: java.lang.Exception) {
                e.printStackTrace()
                return null
            }
        }

        override fun onPostExecute(user: User?) {
            super.onPostExecute(user)
            if (user != null) {
                toast("User ${user.email} registered successfully")
                val utils = Utils(this@RegisterActivity)
                utils.addUserToSharedPreferences(user)
                val intent = Intent(applicationContext, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            } else {
                Toast.makeText(this@RegisterActivity, "Unable to register try again", Toast.LENGTH_SHORT).show()
            }
        }

    }


    override fun onDestroy() {
        super.onDestroy()
        if (doesUserExist != null) {
            if (!doesUserExist.isCancelled) {
                doesUserExist.cancel(true)
            }
        }

        if (registerUser != null) {
            if (!registerUser.isCancelled) {
                registerUser.cancel(true)
            }
        }
    }
}