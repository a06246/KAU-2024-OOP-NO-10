package com.example.accountbooks

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
//import com.example.accountbooks.R

class AccountBookAdapter(
    private val accountBookList: MutableList<String>,
    private val clickListener: (String) -> Unit
) : RecyclerView.Adapter<AccountBookAdapter.AccountBookViewHolder>() {

    inner class AccountBookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val accountBookName: TextView = itemView.findViewById(R.id.tvAccountBookName)

        fun bind(accountBook: String) {
            accountBookName.text = accountBook
            itemView.setOnClickListener {
                clickListener(accountBook)
            }

            // 항목을 길게 누를 때 삭제 기능 추가
            itemView.setOnLongClickListener {
                removeItem(adapterPosition)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountBookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_account_book_adapter, parent, false)
        return AccountBookViewHolder(view)
    }

    override fun onBindViewHolder(holder: AccountBookViewHolder, position: Int) {
        holder.bind(accountBookList[position])
    }

    override fun getItemCount(): Int = accountBookList.size

    // 항목 삭제 메소드
    private fun removeItem(position: Int) {
        if (position >= 0 && position < accountBookList.size) {
            accountBookList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, accountBookList.size)
        }
    }
}
