package com.example.accountbooks

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class  AccountBookAdapter(
    private val accountBookList: MutableList<String>, // 가계부 목록을 저장하는 리스트
    private val clickListener: (String) -> Unit // 각 항목이 클릭되었을 때 실행될 함수
) : RecyclerView.Adapter<AccountBookAdapter.AccountBookViewHolder>() { // 리사이클뷰 위임받은 어댑터

    inner class AccountBookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) { // RecyclerView의 각 항목을 나타내는 뷰 홀더
        val accountBookName: TextView = itemView.findViewById(R.id.tvAccountBookName) // 각 항목에 표시될 가계부 이름을 나타내는 TextView

        fun bind(accountBook: String) { // 가계부 이름을 해당 뷰에 바인딩하는 메소드
            accountBookName.text = accountBook
            itemView.setOnClickListener { // 항목이 클릭될 때, clickListener를 호출
                clickListener(accountBook)
            }

            // 항목을 길게 누를 때 삭제 기능 추가
            itemView.setOnLongClickListener {
                removeItem(adapterPosition) // 항목이 길게 눌릴 때 removeItem() 메소드를 호출하여 해당 항목을 삭제
                true
            }
        }
    }

    // 각 항목의 뷰를 생성하는 메소드
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountBookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_account_book_adapter, parent, false)
        return AccountBookViewHolder(view)
    }

    // 각 뷰 홀더에 데이터를 바인딩하는 메소드입니다. position에 해당하는 데이터를 가져와 bind() 메소드를 호출하여 데이터와 뷰를 연결
    override fun onBindViewHolder(holder: AccountBookViewHolder, position: Int) {
        holder.bind(accountBookList[position])
    }

    // RecyclerView에 표시할 항목의 총 개수를 반환하는 메소드
    override fun getItemCount(): Int = accountBookList.size

    // 항목 삭제 메소드
    private fun removeItem(position: Int) {
        if (position >= 0 && position < accountBookList.size) {
            val accountBookName = accountBookList[position]

            accountBookList.removeAt(position)
            notifyItemRemoved(position) // 특정 위치에서 항목이 삭제되었음을 RecyclerView에 알립니다.
            notifyItemRangeChanged(position, accountBookList.size) // 데이터 변경 후 RecyclerView에 알림을 주어 목록을 갱신
        }
    }
}