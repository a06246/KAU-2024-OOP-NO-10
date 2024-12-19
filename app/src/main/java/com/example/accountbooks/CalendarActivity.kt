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

/**
 * 달력 화면을 관리하는 액티비티
 * - 일별 거래내역 조회
 * - 월별 지출 합계 표시
 * - 캘린더를 통한 날짜 선택 기능
 */
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
    private var currentYearMonth: Calendar = Calendar.getInstance()  // 현재 표시 중인 년/월
    
    // 날짜 포시 형식 지정
    private val dayFormatter = SimpleDateFormat("dd일 EEEE", Locale.KOREA)  // 요일 포맷 (예: 19일 수요일)
    private val monthFormatter = SimpleDateFormat("M", Locale.KOREA)  // 월 포맷 (예: 12)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()  // UI 초기화
        loadData()    // 초기 데이터 로드
    }

    /**
     * UI 컴포넌트 초기화 및 이벤트 리스너 설정
     */
    private fun setupViews() {
        setupToolbar("개인 가계부")
        
        // 캘린더뷰 날짜 선택 리스너 설정
        binding.calendarView.setOnDateChangeListener { _, year, month, day ->
            val calendar = Calendar.getInstance().apply {
                set(year, month, day, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            selectedDate = calendar.time
            currentYearMonth = calendar
            loadData()
        }

        // RecyclerView 설정
        transactionAdapter = TransactionAdapter(transactions)
        binding.rvTransactions.apply {
            adapter = transactionAdapter
            layoutManager = LinearLayoutManager(this@CalendarActivity)
        }
    }

    /**
     * 사용자의 기본 가계부 데이터 로드
     */
    private fun loadData() {
        val userId = auth.currentUser?.uid ?: return
        
        // 사용자의 기본 가계부 ID 조회
        db.collection("account_books")
            .whereEqualTo("userId", userId)
            .whereEqualTo("isDefault", true)
            .get()
            .addOnSuccessListener { documents ->
                documents.documents.firstOrNull()?.id?.let { accountBookId ->
                    loadTransactions(accountBookId)
                }
            }
    }

    /**
     * 특정 가계부의 거래내역 조회
     * @param accountBookId 가계부 ID
     */
    private fun loadTransactions(accountBookId: String) {
        // 선택된 날짜의 거래내역 조회
        val dailyRange = getDailyRange()
        db.collection("items")
            .whereEqualTo("accountBookId", accountBookId)
            .whereGreaterThanOrEqualTo("date", dailyRange.first)
            .whereLessThanOrEqualTo("date", dailyRange.second)
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                updateDailyTransactions(documents)
            }

        // 선택된 월의 전체 거래내역 조회
        val monthlyRange = getMonthlyRange()
        db.collection("items")
            .whereEqualTo("accountBookId", accountBookId)
            .whereGreaterThanOrEqualTo("date", monthlyRange.first)
            .whereLessThanOrEqualTo("date", monthlyRange.second)
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                updateMonthlyExpense(documents)
            }
    }

    /**
     * 하루 시작과 끝 시간 범위 계산
     * @return Pair<시작시간, 종료시간>
     */
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

    /**
     * 월 시작과 끝 시간 범위 계산
     * @return Pair<시작시간, 종료시간>
     */
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

    /**
     * 일별 거래내역 UI 업데이트
     * @param documents Firestore 조회 결과
     */
    private fun updateDailyTransactions(documents: QuerySnapshot) {
        transactions.clear()
        transactions.addAll(documents.mapNotNull { parseTransaction(it) })
        transactionAdapter.notifyDataSetChanged()

        // 일별 수입/지출 합계 계산
        val expenses = transactions.filter { it.amount < 0 }.sumOf { abs(it.amount) }
        val income = transactions.filter { it.amount > 0 }.sumOf { it.amount }

        // UI 업데이트
        binding.apply {
            tvToday.text = dayFormatter.format(selectedDate)
            tvTodayExpense.text = "지출 | ${expenses.formatWithComma()}원"
            tvTodayIncome.text = "수입 | ${income.formatWithComma()}원"
        }
    }

    /**
     * 월별 지출 합계 UI 업데이트
     * @param documents Firestore 조회 결과
     */
    private fun updateMonthlyExpense(documents: QuerySnapshot) {
        val totalExpense = documents.mapNotNull { doc ->
            val amount = (doc.get("amount") as? Number)?.toLong() ?: 0
            if (amount < 0) abs(amount) else 0L
        }.sum()

        val month = monthFormatter.format(currentYearMonth.time)
        binding.tvTotalMonthExpense.text = "${month}월 ${totalExpense.formatWithComma()}원"
    }

    /**
     * Firestore 문서를 Transaction 객체로 변환
     * @param document Firestore 문서
     * @return Transaction? 변환된 거래내역 객체 또는 null
     */
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

    /**
     * 숫자를 천단위 구분자가 있는 문자열로 변환
     */
    private fun Long.formatWithComma(): String = String.format("%,d", this)
}