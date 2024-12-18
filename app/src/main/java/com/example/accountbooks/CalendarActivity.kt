package com.example.accountbooks

import android.os.Bundle
import android.util.Log

import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager

import com.example.accountbooks.adapters.TransactionAdapter
import com.example.accountbooks.models.Transaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query

import com.example.accountbooks.databinding.ActivityCalendarBinding

import com.example.accountbooks.extensions.setupToolbar

import kotlin.math.abs

class CalendarActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCalendarBinding
    private lateinit var transactionAdapter: TransactionAdapter
    private val transactions = mutableListOf<Transaction>()
    private val db = FirebaseFirestore.getInstance()
    private var selectedDate: Date = Date()
    private var currentYearMonth: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar("개인 가계부")

        setupCalendarView()
        setupRecyclerView()
        loadTransactions(selectedDate)
        loadMonthlyTransactions(currentYearMonth)
    }

    private fun setupCalendarView() {
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            selectedDate = calendar.time
            currentYearMonth = calendar
            loadMonthlyTransactions(calendar)
            loadTransactions(selectedDate)
        }
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter(transactions)
        binding.rvTransactions.apply {
            adapter = transactionAdapter
            layoutManager = LinearLayoutManager(this@CalendarActivity)
        }
    }

    private fun loadTransactions(date: Date) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startDate = calendar.time

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endDate = calendar.time

        db.collection("account_books")
            .whereEqualTo("userId", userId)
            .whereEqualTo("isDefault", true)
            .get()
            .addOnSuccessListener { documents ->
                val myAccountBookId = documents.documents.firstOrNull()?.id
                if (myAccountBookId != null) {
                    db.collection("items")
                        .whereEqualTo("accountBookId", myAccountBookId)
                        .whereGreaterThanOrEqualTo("date", startDate)
                        .whereLessThanOrEqualTo("date", endDate)
                        .orderBy("date", Query.Direction.DESCENDING)
                        .get()
                        .addOnSuccessListener { itemDocuments ->
                            val newTransactions = itemDocuments.mapNotNull { doc ->
                                try {
                                    Log.d("FirestoreData", """
                                        Document ID: ${doc.id}
                                        Amount: ${doc.get("amount")}
                                        Category: ${doc.getString("category")}
                                        Type: ${doc.getString("type")}
                                    """.trimIndent())

                                    Transaction(
                                        id = doc.id,
                                        amount = (doc.get("amount") as? Number)?.toLong() ?: 0,
                                        category = doc.getString("category") ?: "",
                                        date = (doc.get("date") as? Timestamp)?.toDate() ?: Date(),
                                        memo = doc.getString("memo") ?: "",
                                        userId = doc.getString("userId") ?: "",
                                        merchant = doc.getString("merchant") ?: "",
                                        isExpense = doc.getBoolean("isExpense") ?: false
                                    )
                                } catch (e: Exception) {
                                    Log.e("TransactionError", "Error parsing transaction", e)
                                    null
                                }
                            }
                            updateTransactionsList(newTransactions)
                        }
                }
            }
    }

    private fun loadMonthlyTransactions(calendar: Calendar) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        
        val startOfMonth = calendar.clone() as Calendar
        startOfMonth.set(Calendar.DAY_OF_MONTH, 1)
        startOfMonth.set(Calendar.HOUR_OF_DAY, 0)
        startOfMonth.set(Calendar.MINUTE, 0)
        startOfMonth.set(Calendar.SECOND, 0)

        val endOfMonth = calendar.clone() as Calendar
        endOfMonth.set(Calendar.DAY_OF_MONTH, endOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH))
        endOfMonth.set(Calendar.HOUR_OF_DAY, 23)
        endOfMonth.set(Calendar.MINUTE, 59)
        endOfMonth.set(Calendar.SECOND, 59)

        Log.d("MonthlyExpense", "Loading expenses for ${startOfMonth.time} to ${endOfMonth.time}")

        db.collection("account_books")
            .whereEqualTo("userId", userId)
            .whereEqualTo("isDefault", true)
            .get()
            .addOnSuccessListener { documents ->
                val myAccountBookId = documents.documents.firstOrNull()?.id
                Log.d("MonthlyExpense", "AccountBookId: $myAccountBookId")
                
                if (myAccountBookId != null) {
                    db.collection("items")
                        .whereEqualTo("accountBookId", myAccountBookId)
                        .whereGreaterThanOrEqualTo("date", startOfMonth.time)
                        .whereLessThanOrEqualTo("date", endOfMonth.time)
                        .orderBy("date", Query.Direction.DESCENDING)
                        .get()
                        .addOnSuccessListener { itemDocuments ->
                            val monthlyExpense = itemDocuments.mapNotNull { doc ->
                                val amount = (doc.get("amount") as? Number)?.toLong() ?: 0
                                Log.d("MonthlyExpense", "Document amount: $amount")
                                if (amount < 0) abs(amount) else 0L
                            }.sum()
                            
                            Log.d("MonthlyExpense", "Total monthly expense: $monthlyExpense")
                            updateMonthlyExpense(monthlyExpense, calendar)
                        }
                        .addOnFailureListener { e ->
                            Log.e("MonthlyExpense", "Error loading items", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("MonthlyExpense", "Error loading account book", e)
            }
    }

    private fun updateTransactionsList(newTransactions: List<Transaction>) {
        transactions.clear()
        transactions.addAll(newTransactions)
        transactionAdapter.notifyDataSetChanged()

        val expenses = transactions.filter { it.amount < 0 }.sumOf { abs(it.amount) }
        val income = transactions.filter { it.amount > 0 }.sumOf { it.amount }

        val dateFormat = SimpleDateFormat("dd일 EEEE", Locale.KOREA)
        binding.tvToday.text = dateFormat.format(selectedDate)
        binding.tvTodayExpense.text = "지출 | ${expenses.formatWithComma()}원"
        binding.tvTodayIncome.text = "수입 | ${income.formatWithComma()}원"
    }

    private fun Long.formatWithComma(): String {
        return String.format("%,d", this)
    }

    private fun updateMonthlyExpense(amount: Long, calendar: Calendar) {
        val monthFormat = SimpleDateFormat("M", Locale.KOREA)
        val month = monthFormat.format(calendar.time)
        val formattedText = "${month}월 | ${amount.formatWithComma()}원"
        Log.d("MonthlyExpense", "Updating UI with text: $formattedText")
        binding.tvTotalMonthExpense.text = formattedText
    }
}