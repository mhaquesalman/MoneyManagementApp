package com.salman.moneymanagementapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.salman.moneymanagementapp.R
import com.salman.moneymanagementapp.model.Item
import com.salman.moneymanagementapp.model.Transaction
import java.lang.Exception
import java.util.ArrayList

class ItemsAdapter(val context: Context, val dialogFragment: DialogFragment) :
    RecyclerView.Adapter<ItemsAdapter.MyViewHolder>() {

    private lateinit var itemList: ArrayList<Item>
    lateinit var getItem: GetItem


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_items, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.name.setText(itemList.get(position).name)
        Glide.with(context).asBitmap().load(itemList.get(position).image_url).into(holder.image)
        holder.parent.setOnClickListener {
            try {
                getItem = dialogFragment as GetItem
                getItem.onGettingItemResult(itemList.get(position))
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    fun setItems(mItemList: ArrayList<Item>) {
        this.itemList = mItemList
        notifyDataSetChanged()
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.itemName)
        val image: ImageView = itemView.findViewById(R.id.itemImage)
        val parent: CardView = itemView.findViewById(R.id.parent)
    }

     interface GetItem {
         fun onGettingItemResult(item: Item)
     }
}