package com.salman.moneymanagementapp.util

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.salman.moneymanagementapp.activity.RegisterActivity
import com.salman.moneymanagementapp.model.User
import java.lang.reflect.Type


class Utils(val context: Context) {
    private val TAG = "Utils"


    fun addUserToSharedPreferences(user: User) {
        Log.d(TAG, "addUserToSharedPrefrences: $user")
        val sharedPreferences = context.getSharedPreferences("logged_in_user", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val gson = Gson()

        editor.putString("user", gson.toJson(user))
        editor.apply()
    }

    fun isUserLoggedIn(): User? {
        Log.d(TAG, "isUserLoggedIn: started")
        val sharedPreferences = context.getSharedPreferences("logged_in_user", Context.MODE_PRIVATE)
        val gson = Gson()
        val type: Type = object : TypeToken<User>(){}.type
        val user = gson.fromJson<User>(sharedPreferences.getString("user", null), type)
        return user

    }

    fun signOutUser() {
        Log.d(TAG, "signOutUser: started")
        val sharedPreferences = context.getSharedPreferences("logged_in_user", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("user")
        editor.commit()
        val intent = Intent(context, RegisterActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }
}