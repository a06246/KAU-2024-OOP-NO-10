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

/**
 * 거래내역을 RecyclerView에 표시하기 위한 어댑터
 * @param transactions 표시할 거래내역 목록
 */
class TransactionAdapter(
    private val transactions: List<Transaction>
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    // 날짜 표시 형식 지정
    private val dateFormat = SimpleDateFormat("HH:mm", Locale.KOREA)

    /**
     * 거래내역 아이템의 뷰를 보관하는 ViewHolder
     */
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        val tvCategoryAndMerchant: TextView = view.findViewById(R.id.tvCategoryAndMerchant)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
    }

    // ViewHolder 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(view)
    }

    // ViewHolder에 데이터 바인딩
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = transactions[position]
        val context = holder.itemView.context
        
        // 금액 포맷팅 및 색상 설정
        val amount = transaction.amount
        val formattedAmount = String.format("%,d원", Math.abs(amount))
        val prefix = if (amount < 0) "-" else "+"
        
        holder.tvAmount.text = "$prefix $formattedAmount"
        holder.tvAmount.setTextColor(
            if (amount < 0) context.getColor(android.R.color.holo_red_dark)
            else context.getColor(android.R.color.holo_blue_dark)
        )

        // 카테고리와 거래처 표시
        val merchant = if (transaction.merchant.isNotEmpty()) transaction.merchant else "미입력"
        holder.tvCategoryAndMerchant.text = "${transaction.category} | $merchant"
        
        // 시간 표시
        holder.tvDate.text = dateFormat.format(transaction.date)
    }

    // 전체 아이템 개수 반환
    override fun getItemCount() = transactions.size
} 