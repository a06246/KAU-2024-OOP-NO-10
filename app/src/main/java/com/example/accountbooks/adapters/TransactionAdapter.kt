package com.example.accountbooks.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.accountbooks.R
import com.example.accountbooks.models.Transaction
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionAdapter(private val transactions: List<Transaction>) :
    RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        val tvCategoryAndMerchant: TextView = view.findViewById(R.id.tvCategoryAndMerchant)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = transactions[position]
        val context = holder.itemView.context
        
        // 금액 포맷팅 및 색상 설정
        val amount = transaction.amount
        val formattedAmount = String.format("%,d원", Math.abs(amount))
        val prefix = if (amount < 0) "-" else "+"
        
        holder.tvAmount.text = "$prefix $formattedAmount"
        holder.tvAmount.setTextColor(if (amount < 0) 
            context.getColor(android.R.color.holo_red_dark)
        else 
            context.getColor(android.R.color.holo_blue_dark))

        // 카테고리와 거래처 표시
        val merchant = if (transaction.merchant.isNotEmpty()) transaction.merchant else "미입력"
        holder.tvCategoryAndMerchant.text = "${transaction.category} | $merchant"
    }

    override fun getItemCount() = transactions.size
} 