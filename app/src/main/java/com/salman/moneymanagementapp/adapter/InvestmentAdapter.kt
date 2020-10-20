package com.salman.moneymanagementapp.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.salman.moneymanagementapp.R
import com.salman.moneymanagementapp.model.Investment
import kotlinx.android.synthetic.main.activity_add_investment.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class InvestmentAdapter(val context: Context) : RecyclerView.Adapter<InvestmentAdapter.MyViewHolder>() {
    val TAG = "InvestmentAdapter"
    private lateinit var investmentList: ArrayList<Investment>
    var number = -1


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.listitem_investment, parent, false)
        return MyViewHolder(view)
    }



    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.name.setText(investmentList.get(position).name)
        holder.initDate.setText(investmentList.get(position).init_date)
        holder.finishDate.setText(investmentList.get(position).finish_date)
        holder.amount.setText(investmentList.get(position).amount.toString())
        holder.roi.setText(investmentList.get(position).monthly_roi.toString())
        holder.profit.setText(getTotalprofit(investmentList.get(position)).toString())

        if (number == -1) {
            holder.parent.setCardBackgroundColor(context.resources.getColor(R.color.light_green))
            number = 1
        } else {
            holder.parent.setCardBackgroundColor(context.resources.getColor(R.color.light_blue))
            number = -1
        }

    }

    fun getTotalprofit(investment: Investment): Double {
        Log.d(TAG, "getTotalProfit: calculating total profit for: " + investmentList.toString())

        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        var profit = 0.0

        try {
            val initDate = sdf.parse(investment.init_date!!)
            calendar.time = initDate!!
            val initMonth = calendar.get(Calendar.YEAR) * 12 + calendar.get(Calendar.MONTH)
            val finishDate = sdf.parse(investment.finish_date!!)
            calendar.time = finishDate!!
            val finishMonth = calendar.get(Calendar.YEAR) * 12 + calendar.get(Calendar.MONTH)

            val monthDifference = finishMonth - initMonth

            for (i in 0 until monthDifference) {
                profit += investment.amount * investment.monthly_roi/100
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return profit
    }


    override fun getItemCount(): Int {
        return investmentList.size
    }

    fun setInvestments(mInvestments: ArrayList<Investment>) {
        this.investmentList = mInvestments
        notifyDataSetChanged()
    }


    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.txtInvestmentName)
        val initDate: TextView = itemView.findViewById(R.id.txtInitDate)
        val finishDate: TextView = itemView.findViewById(R.id.txtFinishDate)
        val roi: TextView = itemView.findViewById(R.id.txtROI)
        val profit: TextView = itemView.findViewById(R.id.txtProfitAmount)
        val amount: TextView = itemView.findViewById(R.id.txtAmount)
        val parent: CardView = itemView.findViewById(R.id.parent)
    }

}