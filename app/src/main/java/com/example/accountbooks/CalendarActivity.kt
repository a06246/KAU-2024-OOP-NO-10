package com.example.accountbooks

import android.os.Bundle
import android.widget.CalendarView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.text.NumberFormat
import java.util.Locale
import com.google.firebase.Timestamp
import android.widget.ImageButton
import androidx.appcompat.widget.Toolbar
import android.widget.Toast
import java.util.Date

class CalendarActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var tvMonthlyTotal: TextView
    private lateinit var tvDailyExpense: TextView
    private lateinit var tvDailyIncome: TextView
    private lateinit var calendarView: CalendarView
    private lateinit var rvTransactions: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        // 초기화
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        
        // 툴바 설정
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        
        // 타이틀 설정
        val tvTitle = toolbar.findViewById<TextView>(R.id.tvTitle)
        tvTitle.text = "가계부"
        
        // 뒤로가기 버튼
        toolbar.findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // 홈 버튼
        toolbar.findViewById<ImageButton>(R.id.btnHome).setOnClickListener {
            finish()
        }

        // UI 요소 초기화
        tvMonthlyTotal = findViewById(R.id.tvMonthlyTotal)
        tvDailyExpense = findViewById(R.id.tvDailyExpense)
        tvDailyIncome = findViewById(R.id.tvDailyIncome)
        calendarView = findViewById(R.id.calendarView)
        rvTransactions = findViewById(R.id.rvTransactions)

        // RecyclerView 설정
        rvTransactions.layoutManager = LinearLayoutManager(this)

        // 현재 월의 지출액 표시
        val currentDate = Calendar.getInstance()
        loadMonthlyExpenses(
            currentDate.get(Calendar.YEAR),
            currentDate.get(Calendar.MONTH)
        )

        // 달력 날짜 변경 리스너
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            loadDailyTransactions(year, month, dayOfMonth)
            loadMonthlyExpenses(year, month)
        }
    }

    private fun loadMonthlyExpenses(year: Int, month: Int) {
        val userId = auth.currentUser?.uid ?: return
        
        val startDate = Calendar.getInstance().apply {
            set(year, month, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val endDate = Calendar.getInstance().apply {
            set(year, month + 1, 1, 0, 0, 0)
            add(Calendar.DATE, -1)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        firestore.collection("items")
            .whereEqualTo("userId", userId)
            .whereEqualTo("accountBookId", "myAccountBook")
            .whereGreaterThanOrEqualTo("date", Timestamp(startDate.timeInMillis / 1000, 0))
            .whereLessThanOrEqualTo("date", Timestamp(endDate.timeInMillis / 1000, 999999999))
            .get()
            .addOnSuccessListener { documents ->
                var totalExpense = 0
                
                for (document in documents) {
                    val amount = document.getLong("amount")?.toInt() ?: 0
                    if (amount < 0) {
                        totalExpense += amount
                    }
                }

                val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)
                val formattedAmount = numberFormat.format(Math.abs(totalExpense))
                val displayText = "${year}년 ${month + 1}월 총 지출: -${formattedAmount}원"
                
                tvMonthlyTotal.text = displayText
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "월간 지출 로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadDailyTransactions(year: Int, month: Int, day: Int) {
        val userId = auth.currentUser?.uid ?: return
        
        val startDate = Calendar.getInstance().apply {
            set(year, month, day, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val endDate = Calendar.getInstance().apply {
            set(year, month, day, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }

        firestore.collection("items")
            .whereEqualTo("userId", userId)
            .whereEqualTo("accountBookId", "myAccountBook")
            .whereGreaterThanOrEqualTo("date", Timestamp(startDate.timeInMillis / 1000, 0))
            .whereLessThanOrEqualTo("date", Timestamp(endDate.timeInMillis / 1000, 999999999))
            .get()
            .addOnSuccessListener { documents ->
                var dailyExpense = 0
                var dailyIncome = 0
                
                for (document in documents) {
                    val amount = document.getLong("amount")?.toInt() ?: 0
                    if (amount < 0) {
                        dailyExpense += amount
                    } else {
                        dailyIncome += amount
                    }
                }

                val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)
                tvDailyExpense.text = "지출: -${numberFormat.format(Math.abs(dailyExpense))}원"
                tvDailyIncome.text = "수입: ${numberFormat.format(dailyIncome)}원"
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "일간 거래 로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadMonthlyTransactions(accountBookId: String, startDate: Date, endDate: Date) {
        val userId = auth.currentUser?.uid ?: return
        
        firestore.collection("items")
            .whereEqualTo("userId", userId)
            .whereEqualTo("accountBookId", accountBookId)
            .whereGreaterThanOrEqualTo("date", Timestamp(startDate.time / 1000, 0))
            .whereLessThanOrEqualTo("date", Timestamp(endDate.time / 1000, 999999999))
            .orderBy("date")
            .get()
            .addOnSuccessListener { _ ->
                // 필요한 경우 여기에 처리 로직 추가
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "데이터 로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}