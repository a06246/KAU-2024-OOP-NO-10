package com.example.accountbooks

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class AddItem : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var selectedDate: Long = 0

    private lateinit var radioGroupType: RadioGroup
    private lateinit var radioIncome: RadioButton
    private lateinit var etAmount: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var etMemo: EditText
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        selectedDate = intent.getLongExtra("selectedDate", System.currentTimeMillis())

        setupToolbar()
        initializeViews()
        setupSpinner()
        setupSaveButton()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        findViewById<TextView>(R.id.toolbarTitle).text = "내역 추가"
        
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish() // Calendar로 돌아가기
        }
        
        findViewById<ImageButton>(R.id.btnHome).setOnClickListener {
            val intent = Intent(this, SharedBudgetActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivity(intent)
            finish()
        }
    }

    private fun initializeViews() {
        radioGroupType = findViewById(R.id.radioGroupType)
        radioIncome = findViewById(R.id.radioIncome)
        etAmount = findViewById(R.id.etAmount)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        etMemo = findViewById(R.id.etMemo)
        btnSave = findViewById(R.id.btnSave)
    }

    private fun setupSpinner() {
        ArrayAdapter.createFromResource(
            this,
            R.array.categories,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerCategory.adapter = adapter
        }
    }

    private fun setupSaveButton() {
        btnSave.setOnClickListener {
            saveTransaction()
        }
    }

    private fun saveTransaction() {
        val type = if (radioIncome.isChecked) "수입" else "지출"
        val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
        val category = spinnerCategory.selectedItem.toString()
        val memo = etMemo.text.toString()

        val transaction = hashMapOf(
            "userId" to (auth.currentUser?.uid ?: ""),
            "type" to type,
            "price" to amount,
            "category" to category,
            "memo" to memo,
            "date" to Date(selectedDate)
        )

        db.collection("transactions")
            .add(transaction)
            .addOnSuccessListener {
                setResult(RESULT_OK)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "저장 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}