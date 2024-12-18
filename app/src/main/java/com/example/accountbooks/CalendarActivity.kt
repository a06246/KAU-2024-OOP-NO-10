package com.example.accountbooks

import android.os.Bundle
import android.widget.CalendarView
import android.widget.Spinner
import android.widget.TextView
import android.widget.ArrayAdapter
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.accountbooks.adapters.TransactionAdapter
import com.example.accountbooks.models.Transaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import android.widget.Toast
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.FieldPath
import com.example.accountbooks.databinding.ActivityCalendarBinding
import android.content.Intent
import android.widget.ImageButton
import com.example.accountbooks.extensions.setupToolbar

class CalendarActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCalendarBinding
    private lateinit var transactionAdapter: TransactionAdapter
    private val transactions = mutableListOf<Transaction>()
    private val db = FirebaseFirestore.getInstance()
    private var selectedDate: Date = Date()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar("개인 가계부")

        setupCalendarView()
        setupRecyclerView()
        loadTransactions(selectedDate)
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
                                    Transaction(
                                        id = doc.id,
                                        amount = (doc.get("amount") as? Number)?.toLong() ?: 0,
                                        category = doc.getString("category") ?: "",
                                        date = (doc.get("date") as? Timestamp)?.toDate() ?: Date(),
                                        description = doc.getString("description") ?: "",
                                        userId = doc.getString("userId") ?: "",
                                        merchant = doc.getString("merchant") ?: ""
                                    )
                                } catch (e: Exception) {
                                    null
                                }
                            }
                            updateTransactionsList(newTransactions)
                        }
                }
            }
    }

    private fun updateTransactionsList(newTransactions: List<Transaction>) {
        transactions.clear()
        transactions.addAll(newTransactions)
        transactionAdapter.notifyDataSetChanged()

        val totalAmount = transactions.sumOf { it.amount }
        val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA)
        binding.tvTotalAmount.text = "${dateFormat.format(selectedDate)} 총지출: ${totalAmount}원"
    }
}