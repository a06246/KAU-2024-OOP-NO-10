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
    private lateinit var firestore: FirebaseFirestore
    private lateinit var priceEditText: EditText
    private lateinit var memoEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var categorySpinner: Spinner
    private lateinit var paymentMethodSpinner: Spinner
    private lateinit var ledgerTypeSpinner: Spinner
    private lateinit var radioGroup: RadioGroup
    private lateinit var dateTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item)

        // Firebase 초기화
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // UI 요소 초기화
        initializeViews()
        setupSpinners()
        setupSaveButton()
        setupBackButton()
    }

    private fun initializeViews() {
        priceEditText = findViewById(R.id.et_price)
        memoEditText = findViewById(R.id.et_memo)
        saveButton = findViewById(R.id.btn_save)
        categorySpinner = findViewById(R.id.spinner_category)
        paymentMethodSpinner = findViewById(R.id.spinner_payment_method)
        ledgerTypeSpinner = findViewById(R.id.spinner_ledger_type)
        radioGroup = findViewById(R.id.rg_payment_method)
        dateTextView = findViewById(R.id.tv_date_value)
    }

    private fun setupSpinners() {
        // 카테고리 스피너 설정
        ArrayAdapter.createFromResource(
            this,
            R.array.categories,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categorySpinner.adapter = adapter
        }

        // 결제수단 스피너 설정
        ArrayAdapter.createFromResource(
            this,
            R.array.payment_methods,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            paymentMethodSpinner.adapter = adapter
        }

        // 가계부 종류 스피너 설정
        ArrayAdapter.createFromResource(
            this,
            R.array.ledger_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            ledgerTypeSpinner.adapter = adapter
        }
    }

    private fun setupSaveButton() {
        saveButton.setOnClickListener {
            saveTransactionToFirebase()
        }
    }

    private fun setupBackButton() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                startActivity(Intent(this@AddItem, SharedBudgetActivity::class.java))
                finish()
            }
        })
    }

    private fun saveTransactionToFirebase() {
        val price = priceEditText.text.toString()
        val memo = memoEditText.text.toString()
        val category = categorySpinner.selectedItem.toString()
        val paymentMethod = paymentMethodSpinner.selectedItem.toString()
        val ledgerType = ledgerTypeSpinner.selectedItem.toString()
        val type = when (radioGroup.checkedRadioButtonId) {
            R.id.rb_card -> "수입"
            R.id.rb_account -> "지출"
            R.id.rb_cash -> "이체"
            else -> "기타"
        }

        if (price.isEmpty()) {
            Toast.makeText(this, "금액을 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        val transaction = mutableMapOf<String, Any>(
            "price" to (price.toDoubleOrNull() ?: 0.0),
            "memo" to memo,
            "category" to category,
            "paymentMethod" to paymentMethod,
            "ledgerType" to ledgerType,
            "type" to type,
            "date" to Date(),
            "userId" to (auth.currentUser?.uid ?: "")
        )

        firestore.collection("transactions")
            .add(transaction)
            .addOnSuccessListener {
                Toast.makeText(this, "거래가 저장되었습니다", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, SharedBudgetActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "저장 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}