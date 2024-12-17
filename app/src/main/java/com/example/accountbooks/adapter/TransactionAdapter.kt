package com.example.accountbooks.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.accountbooks.R
import com.example.accountbooks.model.Transaction
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionAdapter(private val transactions: List<Transaction>) : 
    RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val amountText: TextView = view.findViewById(R.id.tv_amount)
        val categoryText: TextView = view.findViewById(R.id.tv_category)
        val dateText: TextView = view.findViewById(R.id.tv_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.amountText.text = transaction.amount.toString()
        holder.categoryText.text = transaction.category
        holder.dateText.text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(transaction.date)
    }

    override fun getItemCount() = transactions.size
} 