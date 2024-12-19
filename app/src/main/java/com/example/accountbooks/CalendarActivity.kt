package com.example.accountbooks

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager

import com.example.accountbooks.databinding.ActivityCalendarBinding
import com.example.accountbooks.extensions.setupToolbar
import com.example.accountbooks.adapters.TransactionAdapter
import com.example.accountbooks.models.Transaction

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot

import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs


class CalendarActivity : AppCompatActivity() {
    // View Binding 객체
    private lateinit var binding: ActivityCalendarBinding
    
    // 거래내역 목록을 위한 어댑터와 데이터 리스트
    private lateinit var transactionAdapter: TransactionAdapter
    private val transactions = mutableListOf<Transaction>()

    // Firebase 인스턴스
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    // 날짜 관련 변수
    private var selectedDate: Date = Date()  // 현재 선택된 날짜
    private var currentYearMonth: Calendar = Calendar.getInstance()  
    
    // 날짜 포시 형식 지정
    private val dayFormatter = SimpleDateFormat("dd일 EEEE", Locale.KOREA)  
    private val monthFormatter = SimpleDateFormat("M", Locale.KOREA) 

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()  // UI 초기화
        loadData()    // 초기 데이터 로드
    }

    
    private fun setupViews() {
        
        
        setupToolbar("개인 가계부")
        
        // 캘린더에서 날짜를 선택했을 때 실행될 동작을 설정합니다
        binding.calendarView.setOnDateChangeListener { _, year, month, day ->
            // 선택한 날짜의 시간을 0시 0분 0초로 설정합니다
            val calendar = Calendar.getInstance().apply {
                set(year, month, day, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            // 선택된 날짜를 저장하고 데이터를 불러옵니다
            selectedDate = calendar.time
            currentYearMonth = calendar
            loadData()
        }

        // 거래 내역을 보여줄 어댑터를 생성합니다
        transactionAdapter = TransactionAdapter(transactions)

        // RecyclerView의 기본 설정을 합니다
        binding.rvTransactions.apply {
            // 어댑터 연결
            adapter = transactionAdapter
            // 세로 방향으로 목록이 나열되도록 설정
            layoutManager = LinearLayoutManager(this@CalendarActivity)
        }
    }


    private fun loadData() {
        // 현재 로그인한 사용자의 ID를 가져옵니다. 없으면 함수 종료
        val userId = auth.currentUser?.uid ?: return
        
        // Firestore에서 사용자의 기본 가계부를 찾습니다
        db.collection("account_books")
            .whereEqualTo("userId", userId)     // 현재 사용자의 가계부만
            .whereEqualTo("isDefault", true)    // 기본 가계부인 것만
            .get()
            .addOnSuccessListener { documents ->
                // 찾은 가계부의 ID로 거래내역을 불러옵니다
                documents.documents.firstOrNull()?.id?.let { accountBookId ->
                    loadTransactions(accountBookId)
                }
            }
    }

  
    private fun loadTransactions(accountBookId: String) {
        // 선택한 날짜의 거래내역 조회
        val dailyRange = getDailyRange()    // 하루 시작과 끝 시간
        db.collection("items")
            .whereEqualTo("accountBookId", accountBookId)
            .whereGreaterThanOrEqualTo("date", dailyRange.first)   // 하루 시작시간 이후
            .whereLessThanOrEqualTo("date", dailyRange.second)     // 하루 끝시간 이전
            .orderBy("date", Query.Direction.DESCENDING)           // 최신순 정렬
            .get()
            .addOnSuccessListener { documents ->
                updateDailyTransactions(documents)    // 일별 거래내역 업데이트
            }

        // 선택한 월의 전체 거래내역 조회
        val monthlyRange = getMonthlyRange() // 월 시작과 끝 시간
        db.collection("items")
            .whereEqualTo("accountBookId", accountBookId)
            .whereGreaterThanOrEqualTo("date", monthlyRange.first) // 월 시작시간 이후
            .whereLessThanOrEqualTo("date", monthlyRange.second)   // 월 끝시간 이전
            .orderBy("date", Query.Direction.DESCENDING)           // 최신순 정렬
            .get()
            .addOnSuccessListener { documents ->
                updateMonthlyExpense(documents)       // 월별 지출 합계 업데이트
            }
    }


    private fun getDailyRange(): Pair<Date, Date> {
        val calendar = Calendar.getInstance().apply { time = selectedDate }
        return Pair(
            calendar.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }.time,
            calendar.apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }.time
        )
    }


    private fun getMonthlyRange(): Pair<Date, Date> {
        val calendar = currentYearMonth.clone() as Calendar
        return Pair(
            calendar.apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }.time,
            calendar.apply {
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }.time
        )
    }


    private fun updateDailyTransactions(documents: QuerySnapshot) {
        // 기존 거래내역 목록을 비우고 새로운 데이터로 채움
        transactions.clear()
        transactions.addAll(documents.mapNotNull { parseTransaction(it) })
        transactionAdapter.notifyDataSetChanged()

        // 수입과 지출 합계 계산
        val expenses = transactions.filter { it.amount < 0 }.sumOf { abs(it.amount) }
        val income = transactions.filter { it.amount > 0 }.sumOf { it.amount }

        // 화면 업데이트
        binding.apply {
            tvToday.text = dayFormatter.format(selectedDate)
            tvTodayExpense.text = "지출 | ${expenses.formatWithComma()}원"
            tvTodayIncome.text = "수입 | ${income.formatWithComma()}원"
        }
    }


    private fun updateMonthlyExpense(documents: QuerySnapshot) {
        // 월별 총 지출액 계산 (음수 금액만 합산)
        val totalExpense = documents.mapNotNull { doc ->
            val amount = (doc.get("amount") as? Number)?.toLong() ?: 0
            if (amount < 0) abs(amount) else 0L
        }.sum()

        // 월별 지출 표시
        val month = monthFormatter.format(currentYearMonth.time)
        binding.tvTotalMonthExpense.text = "${month}월 ${totalExpense.formatWithComma()}원"
    }


    private fun parseTransaction(document: DocumentSnapshot): Transaction? {
        return try {
            Transaction(
                id = document.id,
                amount = (document.get("amount") as? Number)?.toLong() ?: 0,
                category = document.getString("category") ?: "",
                date = (document.get("date") as? Timestamp)?.toDate() ?: Date(),
                memo = document.getString("memo") ?: "",
                userId = document.getString("userId") ?: "",
                merchant = document.getString("merchant") ?: "",
                isExpense = document.getBoolean("isExpense") ?: false
            )
        } catch (e: Exception) {
            Log.e("TransactionError", "Error parsing transaction", e)
            null
        }
    }


    private fun Long.formatWithComma(): String = String.format("%,d", this)
}