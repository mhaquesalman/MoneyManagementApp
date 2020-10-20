package com.salman.moneymanagementapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.salman.moneymanagementapp.R
import com.salman.moneymanagementapp.model.Loan
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class LoanAdapter(val context: Context) : RecyclerView.Adapter<LoanAdapter.MyViewHolder>() {
    val TAG = "LoanAdapter"
    private lateinit var loanList: ArrayList<Loan>
    var number = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.listitem_loan, parent, false)
        return MyViewHolder(view)
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.name.setText(loanList.get(position).name)
        holder.initDate.setText(loanList.get(position).init_date)
        holder.finishDate.setText(loanList.get(position).finish_date)
        holder.amount.setText(loanList.get(position).init_amount.toString())
        holder.roi.setText(loanList.get(position).monthly_roi.toString())
        holder.remained_amount.setText(loanList.get(position).remained_amount.toString())
        holder.monthly_payment.setText(loanList.get(position).monthly_payment.toString())
        holder.loss.setText(getTotalLoss(loanList.get(position)).toString())

        if (number == -1) {
            holder.parent.setCardBackgroundColor(context.resources.getColor(R.color.light_green))
            number = 1
        } else {
            holder.parent.setCardBackgroundColor(context.resources.getColor(R.color.light_blue))
            number = -1
        }

    }

    override fun getItemCount(): Int {
        return loanList.size
    }


    fun setLoans(mLoans: ArrayList<Loan>) {
        this.loanList = mLoans
        notifyDataSetChanged()
    }

    fun getTotalLoss(loan: Loan): Double {
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        var loss = 0.0

        try {
            val initDate = sdf.parse(loan.init_date!!)
            calendar.time = initDate!!
            val initMonth = calendar.get(Calendar.YEAR) * 12 + calendar.get(Calendar.MONTH)
            val finishDate = sdf.parse(loan.finish_date!!)
            calendar.time = finishDate!!
            val finishMonth = calendar.get(Calendar.YEAR) * 12 + calendar.get(Calendar.MONTH)

            val monthDifference = finishMonth - initMonth

            for (i in 0 until monthDifference) {
                loss += loan.init_amount * loan.monthly_roi/100
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return loss
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val name: TextView = itemView.findViewById(R.id.txtLoanName)
        val initDate: TextView = itemView.findViewById(R.id.txtInitDate)
        val finishDate: TextView = itemView.findViewById(R.id.txtFinishDate)
        val roi: TextView = itemView.findViewById(R.id.txtROI)
        val loss: TextView = itemView.findViewById(R.id.txtLossAmount)
        val amount: TextView = itemView.findViewById(R.id.txtAmount)
        val remained_amount: TextView = itemView.findViewById(R.id.txtRemainedAmount)
        val monthly_payment: TextView = itemView.findViewById(R.id.txtMonthlyPayment)

        val parent: CardView = itemView.findViewById(R.id.parent)

    }


}