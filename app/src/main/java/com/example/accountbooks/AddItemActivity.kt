package com.example.accountbooks

import androidx.appcompat.app.AppCompatActivity

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast

import com.example.accountbooks.databinding.ActivityAddItemBinding
import com.example.accountbooks.extensions.setupToolbar

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

import java.text.SimpleDateFormat
import java.util.*


class AddItemActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddItemBinding
    
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    
    private val accountBookList = mutableListOf<String>()
    private val accountBookIds = mutableListOf<String>()
    private var selectedAccountBookId: String? = null
    private var transactionDate: Date = Date()
    private lateinit var accountBookAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 화면 초기화
        binding = ActivityAddItemBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar("내역 추가")
        
        // Firebase 초기화
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setupSpinners() // 스피너(가계부 선택, 카테고리) 설정
        setupDatePicker() // 날짜 선택 설정
        setupSaveButton() // 저장 버튼 설정
    }

    // 스피너(가계부 선택, 카테고리) 설정
    private fun setupSpinners() {
        // 가계부 어댑터를 클래스 변수로 선언
        accountBookAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, accountBookList)
        accountBookAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.accountBookSpinner.adapter = accountBookAdapter

        binding.accountBookSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedAccountBookId = accountBookIds[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // 카테고리 스피너 설정
        ArrayAdapter.createFromResource(
            this,
            R.array.categories,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.categorySpinner.adapter = adapter
        }

        // 가계부 목록 로드
        val userId = auth.currentUser?.uid ?: return
        
        firestore.collection("account_books")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->    // 성공
                updateAccountBookList(documents)
            }
            .addOnFailureListener { e ->    // 실패
                showToast("가계부 목록 로드 실패: ${e.message}")
            }
    }

    private fun updateAccountBookList(documents: QuerySnapshot) {
        accountBookList.clear()
        accountBookIds.clear()
        
        for (document in documents) {
            accountBookList.add(document.getString("name") ?: "이름 없음")
            accountBookIds.add(document.id)
        }
        
        // 어댑터에 직접 변경 알림
        accountBookAdapter.notifyDataSetChanged()  // 이렇게 하면 더 간단!
        
        if (accountBookIds.isNotEmpty()) {
            selectedAccountBookId = accountBookIds[0]
        }
    }

    // 날짜 선택 설정
    private fun setupDatePicker() {
        updateDateDisplay()
        binding.etDate.setOnClickListener {
            val calendar = Calendar.getInstance().apply { time = transactionDate }

            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    TimePickerDialog(
                        this,
                        { _, hourOfDay, minute ->
                            calendar.apply {
                                set(Calendar.YEAR, year)
                                set(Calendar.MONTH, month)
                                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                set(Calendar.HOUR_OF_DAY, hourOfDay)
                                set(Calendar.MINUTE, minute)
                                set(Calendar.SECOND, 0)
                            }
                            transactionDate = calendar.time
                            updateDateDisplay()
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true
                    ).show()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }


    // 저장 버튼 설정
    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            val amount = binding.etAmount.text.toString()
            if (amount.isEmpty()) {
                showToast("금액을 입력해주세요")
                return@setOnClickListener
            }

            // 거래 데이터 생성 및 저장
            val isExpense = binding.rbExpense.isChecked
            val finalAmount = if (isExpense) -amount.toInt() else amount.toInt()

            val item = hashMapOf(
                "amount" to finalAmount,
                "description" to binding.etMemo.text.toString(),
                "category" to binding.categorySpinner.selectedItem.toString(),
                "merchant" to binding.etMerchant.text.toString().trim(),
                "date" to Timestamp(transactionDate),
                "userId" to (auth.currentUser?.uid),
                "accountBookId" to accountBookIds[binding.accountBookSpinner.selectedItemPosition],
                "isExpense" to isExpense
            )

            // Firestore에 저장
            firestore.collection("items")
                .add(item)
                .addOnSuccessListener {
                    showToast("내역이 추가되었습니다")
                    finish()
                }
                .addOnFailureListener { e ->
                    showToast("내역 추가 실패: ${e.message}")
                }
        }
    }



    // 날짜 표시 업데이트
    private fun updateDateDisplay() {
        val dateFormat = SimpleDateFormat("yyyy/MM/dd일 HH:mm", Locale.KOREA)
        binding.etDate.setText(dateFormat.format(transactionDate))
    }

    // 토스트 메시지 표시
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}


