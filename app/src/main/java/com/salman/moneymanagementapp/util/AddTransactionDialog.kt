package com.salman.moneymanagementapp.util

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.salman.moneymanagementapp.R
import com.salman.moneymanagementapp.activity.AddInvestmentActivity
import com.salman.moneymanagementapp.activity.AddLoanActivity
import com.salman.moneymanagementapp.activity.ShoppingActivity
import kotlinx.android.synthetic.main.dialog_add_transaction.*

class AddTransactionDialog : DialogFragment() {
    lateinit var shopping: RelativeLayout
    lateinit var investment: RelativeLayout
    lateinit var loan: RelativeLayout
    lateinit var transaction: RelativeLayout

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = activity!!.layoutInflater.inflate(R.layout.dialog_add_transaction, null)
            shopping = view.findViewById(R.id.shoppingRelLayout)
            investment = view.findViewById(R.id.investmentRelLayout)
            loan = view.findViewById(R.id.loanRelLayout)
            transaction = view.findViewById(R.id.transactionRelLayout)

        shopping.setOnClickListener {
            val intent = Intent(activity, ShoppingActivity::class.java)
            startActivity(intent)
        }

        investment.setOnClickListener {
            val intent = Intent(activity, AddInvestmentActivity::class.java)
            startActivity(intent)
        }
        loan.setOnClickListener {
            val intent = Intent(activity, AddLoanActivity::class.java)
            startActivity(intent)
        }
        transaction.setOnClickListener {

        }

        val builder = AlertDialog.Builder(activity!!)
        builder.setTitle("Add Transaction")
            .setView(view)
            .setNegativeButton("Dismiss", { dialogInterface, i ->
              // dialogInterface.dismiss()
            })

        return builder.create()
    }
}