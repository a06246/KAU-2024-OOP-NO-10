package com.example.accountbooks

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class CalendarActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var repository: TransactionRepository
    private lateinit var calculator: TransactionCalculator
    private lateinit var calendarManager: CalendarViewManager
    private val calendar = java.util.Calendar.getInstance()
    
    private val addTransactionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            loadInitialData()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)
        
        initializeDependencies()
        setupViews()
        loadInitialData()
    }
    
    private fun initializeDependencies() {
        auth = FirebaseAuth.getInstance()
        repository = TransactionRepository(FirebaseFirestore.getInstance(), auth.currentUser?.uid ?: "")
        calculator = TransactionCalculator()
    }
    
    private fun setupViews() {
        setupToolbar()
        setupCalendarView()
        setupRecyclerView()
    }
    
    private fun setupToolbar() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        findViewById<TextView>(R.id.toolbarTitle).text = "캘린더"
        
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        
        findViewById<ImageButton>(R.id.btnHome).setOnClickListener {
            startActivity(Intent(this, SharedBudgetActivity::class.java))
            finish()
        }
        
        findViewById<ImageButton>(R.id.btnAddTransaction).setOnClickListener {
            val intent = Intent(this, AddItem::class.java).apply {
                putExtra("selectedDate", calendar.timeInMillis)
            }
            addTransactionLauncher.launch(intent)
        }
    }
    
    private fun setupCalendarView() {
        calendarManager = CalendarViewManager(findViewById(R.id.calendarView)) { selectedCalendar ->
            calendar.timeInMillis = selectedCalendar.timeInMillis
            loadDailyTransactions()
        }
        calendarManager.setup()
    }
    
    private fun setupRecyclerView() {
        findViewById<RecyclerView>(R.id.dailyTransactionsRecyclerView).apply {
            layoutManager = LinearLayoutManager(this@CalendarActivity)
            adapter = TransactionAdapter(emptyList())
        }
    }
    
    private fun loadInitialData() {
        loadMonthlyTotal()
        loadDailyTransactions()
    }
    
    private fun loadDailyTransactions() {
        // 선택된 날짜의 거래 내역을 로드하는 로직
        val startOfDay = calendar.clone() as java.util.Calendar
        startOfDay.set(java.util.Calendar.HOUR_OF_DAY, 0)
        startOfDay.set(java.util.Calendar.MINUTE, 0)
        startOfDay.set(java.util.Calendar.SECOND, 0)

        val endOfDay = calendar.clone() as java.util.Calendar
        endOfDay.set(java.util.Calendar.HOUR_OF_DAY, 23)
        endOfDay.set(java.util.Calendar.MINUTE, 59)
        endOfDay.set(java.util.Calendar.SECOND, 59)

        repository.getMonthlyTransactions(startOfDay.time, endOfDay.time) { transactions ->
            findViewById<RecyclerView>(R.id.dailyTransactionsRecyclerView).adapter = 
                TransactionAdapter(transactions)
        }
    }
    
    private fun loadMonthlyTotal() {
        val (startOfMonth, endOfMonth) = calendar.getMonthStartEndDates()
        repository.getMonthlyTransactions(startOfMonth, endOfMonth) { transactions ->
            val totals = calculator.calculateMonthlyTotals(transactions)
            updateMonthlyTotalView(totals)
        }
    }
    
    private fun updateMonthlyTotalView(totals: MonthlyTotal) {
        val monthFormat = SimpleDateFormat("yyyy년 MM월", Locale.getDefault())
        val formattedIncome = String.format("%,d", totals.income.toLong())
        val formattedExpense = String.format("%,d", totals.expense.toLong())
        
        findViewById<TextView>(R.id.monthlyTotalTextView).text = """
            ${monthFormat.format(calendar.time)}
            이번 달 총 지출: ${formattedExpense}원
            (수입: +${formattedIncome}원)
        """.trimIndent()
    }

    companion object {
        private const val ADD_TRANSACTION_REQUEST = 1001
    }
}

// Calendar 확장 함수
fun Calendar.getMonthStartEndDates(): Pair<Date, Date> {
    val start = clone() as Calendar
    start.set(Calendar.DAY_OF_MONTH, 1)
    start.set(Calendar.HOUR_OF_DAY, 0)
    start.set(Calendar.MINUTE, 0)
    start.set(Calendar.SECOND, 0)

    val end = clone() as Calendar
    end.set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
    end.set(Calendar.HOUR_OF_DAY, 23)
    end.set(Calendar.MINUTE, 59)
    end.set(Calendar.SECOND, 59)

    return Pair(start.time, end.time)
}

// TransactionRepository - 데이터 접근 계층 분리
class TransactionRepository(private val firestore: FirebaseFirestore, private val userId: String) {
    fun getMonthlyTransactions(startDate: Date, endDate: Date, onSuccess: (List<Transaction>) -> Unit) {
        firestore.collection("transactions")
            .whereEqualTo("userId", userId)
            .whereGreaterThanOrEqualTo("date", startDate)
            .whereLessThanOrEqualTo("date", endDate)
            .get()
            .addOnSuccessListener { documents ->
                val transactions = documents.mapNotNull { doc ->
                    Transaction(
                        type = doc.getString("type") ?: return@mapNotNull null,
                        amount = doc.getDouble("price") ?: 0.0,
                        memo = doc.getString("memo") ?: "",
                        category = doc.getString("category") ?: ""
                    )
                }
                onSuccess(transactions)
            }
    }
}

// TransactionCalculator - 비즈니스 로직 분리
class TransactionCalculator {
    fun calculateMonthlyTotals(transactions: List<Transaction>): MonthlyTotal {
        var totalIncome = 0.0
        var totalExpense = 0.0
        
        transactions.forEach { transaction ->
            when (transaction.type) {
                "수입" -> totalIncome += transaction.amount
                "지출" -> totalExpense += transaction.amount
            }
        }
        
        return MonthlyTotal(totalIncome, totalExpense)
    }
}

// UI 관련 클래스들
class CalendarViewManager(
    private val calendarView: CalendarView,
    private val onDateSelected: (Calendar) -> Unit
) {
    fun setup() {
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                onDateSelected(this)
            }
        }
    }
}

// 데이터 클래스들
data class MonthlyTotal(val income: Double, val expense: Double)

data class Transaction(
    val type: String,
    val amount: Double,
    val memo: String,
    val category: String
)

class TransactionAdapter(private val transactions: List<Transaction>) : 
    RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {
    
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val transactionText: TextView = view.findViewById(R.id.transactionText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = transactions[position]
        val amountPrefix = if (transaction.type == "수입") "+" else "-"
        holder.transactionText.text = 
            "${transaction.category}: ${amountPrefix}${transaction.amount}원\n${transaction.memo}"
    }

    override fun getItemCount() = transactions.size
}