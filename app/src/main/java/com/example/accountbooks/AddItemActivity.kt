package com.example.accountbooks

import android.content.Intent
import android.os.Bundle 
import androidx.appcompat.app.AppCompatActivity

import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.widget.Button        
import android.widget.EditText      
import android.widget.Spinner       
import android.widget.ArrayAdapter  
import android.widget.Toast       

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import java.util.Date 
import java.text.SimpleDateFormat
import java.util.Locale
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.DatePicker
import android.widget.TimePicker
import java.util.Calendar
import com.google.firebase.Timestamp
import android.view.View
import android.widget.AdapterView
import com.example.accountbooks.databinding.ActivityAddItemBinding
import android.widget.TextView
import android.widget.ImageButton

class AddItemActivity : AppCompatActivity() {
    // Firebase 인증과 Firestore 인스턴스를 저장할 변수 선언
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var binding: ActivityAddItemBinding
    private val accountBookList = mutableListOf<String>()
    private val accountBookIds = mutableListOf<String>()
    private var selectedAccountBookId: String? = null
    private lateinit var transactionDate: Date
    private lateinit var btnSave: Button

    // 액티비티가 생성될 때 호출되는 onCreate 메서드
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 툴바 설정
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // 타이틀 설정
        val tvTitle = toolbar.findViewById<TextView>(R.id.tvTitle)
        tvTitle.text = "내역 추가"

        // 뒤로가기 버튼
        toolbar.findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // 홈 버튼
        toolbar.findViewById<ImageButton>(R.id.btnHome).setOnClickListener {
            finish()
        }

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setupSpinner()
        loadAccountBooks()

        // 카테고리 스피너 설정
        val categorySpinner: Spinner = binding.categorySpinner
        ArrayAdapter.createFromResource(
            this,
            R.array.categories,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categorySpinner.adapter = adapter
        }

        // 저장 버튼 클릭 이벤트
        binding.btnSave.setOnClickListener {
            saveItem()
        }

        transactionDate = Date()
        updateDateDisplay()
        
        binding.etDate.setOnClickListener {
            showDateTimePickerDialog()
        }
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, accountBookList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.accountBookSpinner.adapter = adapter

        binding.accountBookSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedAccountBookId = accountBookIds[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun loadAccountBooks() {
        val userId = auth.currentUser?.uid ?: return
        
        firestore.collection("account_books")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                accountBookList.clear()
                accountBookIds.clear()
                
                for (document in documents) {
                    val accountBookName = document.getString("name") ?: "이름 없음"
                    accountBookList.add(accountBookName)
                    accountBookIds.add(document.id)
                }
                
                (binding.accountBookSpinner.adapter as ArrayAdapter<*>).notifyDataSetChanged()
                
                if (accountBookIds.isNotEmpty()) {
                    selectedAccountBookId = accountBookIds[0]
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "가계부 목록 로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveItem() {
        val amount = binding.etAmount.text.toString().toIntOrNull() ?: 0
        val description = binding.etDescription.text.toString()
        val category = binding.categorySpinner.selectedItem.toString()

        // 선택된 가계부 ID 가져오기
        val selectedPosition = binding.accountBookSpinner.selectedItemPosition
        if (selectedPosition == -1 || accountBookIds.isEmpty()) {
            Toast.makeText(this, "가계부를 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }
        val selectedAccountBookId = accountBookIds[selectedPosition]

        // Firestore에 저장할 데이터 구성
        val item = hashMapOf(
            "amount" to amount,
            "description" to description,
            "category" to category,
            "date" to Timestamp(transactionDate), // 선택한 날짜로 Timestamp 생성
            "userId" to auth.currentUser?.uid,
            "accountBookId" to selectedAccountBookId
        )

        // Firestore의 'items' 컬렉션에 데이터 추가
        firestore.collection("items")
            .add(item)
            .addOnSuccessListener { _ ->
                Toast.makeText(this, "내역이 추가되었습니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "내역 추가 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // 날짜 표시 업데이트 함수
    private fun updateDateDisplay() {
        val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분", Locale.KOREA)
        binding.etDate.setText(dateFormat.format(transactionDate))
    }

    // 날짜 시간 선택 다이얼로그
    private fun showDateTimePickerDialog() {
        val calendar = Calendar.getInstance().apply {
            time = transactionDate
        }

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                TimePickerDialog(
                    this,
                    { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        calendar.set(Calendar.SECOND, 0)
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

