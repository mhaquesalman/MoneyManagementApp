package com.salman.moneymanagementapp.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.salman.moneymanagementapp.R
import com.salman.moneymanagementapp.model.Transaction
import java.util.ArrayList

private const val TAG = "TransactionAdapter"
class TransactionAdapter : RecyclerView.Adapter<TransactionAdapter.MyViewHolder>() {

    private lateinit var transactionList: ArrayList<Transaction>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.listitem_transaction, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder: started")
        holder.txtDate.text = transactionList.get(position).date
        holder.txtDesc.text = transactionList.get(position).description
        holder.txtType.text = transactionList.get(position).type
        holder.txtTransactionId.text = "Transaction Id: " + transactionList.get(position).get_id()
        holder.txtSender.text = transactionList.get(position).recipient

        val amount = transactionList.get(position).amount
        if (amount > 0) {
            holder.txtAmount.setText("+$amount")
            holder.txtAmount.setTextColor(Color.GREEN)
        } else {
            holder.txtAmount.setText("$amount")
            holder.txtAmount.setTextColor(Color.RED)
        }
    }

    override fun getItemCount(): Int {
        return transactionList.size
    }

    fun setTransactions(mTransactionList: ArrayList<Transaction>) {
        this.transactionList = mTransactionList
        notifyDataSetChanged()
    }


    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtAmount: TextView = itemView.findViewById(R.id.txtAmount)
        val txtDesc: TextView = itemView.findViewById(R.id.txtDesc)
        val txtDate: TextView = itemView.findViewById(R.id.txtDate)
        val txtType: TextView = itemView.findViewById(R.id.txtType)
        val txtSender: TextView = itemView.findViewById(R.id.txtSender)
        val txtTransactionId: TextView = itemView.findViewById(R.id.txtTransactionId)
//        val parent: CardView = itemView.findViewById(R.id.parent)
    }


}