package com.example.accountbooks.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.accountbooks.R
import com.example.accountbooks.model.Transaction
import java.text.NumberFormat
import java.util.Locale

class TransactionAdapter(private val transactions: List<Transaction>) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val amountTextView: TextView = view.findViewById(R.id.tv_amount)
        private val categoryTextView: TextView = view.findViewById(R.id.tv_category)
        private val merchantTextView: TextView = view.findViewById(R.id.tv_merchant)

        fun bind(transaction: Transaction) {
            val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)
            val formattedAmount = numberFormat.format(Math.abs(transaction.amount))
            
            val amountText = when {
                transaction.amount < 0 -> "-${formattedAmount}원"
                else -> "+${formattedAmount}원"
            }
            
            amountTextView.text = amountText
            amountTextView.setTextColor(when {
                transaction.amount < 0 -> android.graphics.Color.RED
                else -> android.graphics.Color.BLUE
            })
            
            categoryTextView.text = transaction.category
            merchantTextView.text = transaction.merchant
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount() = transactions.size
} 