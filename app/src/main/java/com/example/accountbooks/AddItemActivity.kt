package com.example.accountbooks

import android.content.Intent
import android.os.Bundle // 액티비티 간에 데이터 전달
import androidx.appcompat.app.AppCompatActivity

// 디스플레이
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.widget.Button        // 버튼
import android.widget.EditText      // 텍스트 입력
import android.widget.Spinner       // 드롭 다운 선택 메뉴(스피터)
import android.widget.ArrayAdapter  // 스피너에 데이터 바인딩
import android.widget.Toast         // 팝업

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import java.util.Date // 날짜 처리
import java.text.SimpleDateFormat
import java.util.Locale
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.DatePicker
import android.widget.TimePicker
import java.util.Calendar

class AddItemActivity : AppCompatActivity() {
    // Firebase 인증과 Firestore 인스턴스를 저장할 변수 선언
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var accountBookSpinner: Spinner
    private val accountBookList = mutableListOf<String>()
    private val accountBookIds = mutableListOf<String>() // 문서 ID 저장용
    private lateinit var etDate: EditText
    private lateinit var transactionDate: Date

    // 액티비티가 생성될 때 호출되는 onCreate 메서드
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_add_item) // 레이아웃 설정

        // Firebase 인스턴스 초기화
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        accountBookSpinner = findViewById(R.id.accountBookSpinner)
        setupAccountBookSpinner()
        loadAccountBooks()

        // 스피너 설정
        val categorySpinner: Spinner = findViewById(R.id.spinnerCategory)
        // 스피너에 카테고리 목록을 설정하는 어댑터 생성
        ArrayAdapter.createFromResource(
            this,
            R.array.categories, // strings.xml에 정의된 카테고리 배열
            android.R.layout.simple_spinner_item // 기본 스피너 아이템 레이아웃
        ).also { adapter ->
            // 드롭다운 시 보여질 레이아웃 설정
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // 스피너에 어댑터 설정
            categorySpinner.adapter = adapter
        }
        // 저장 버튼 클릭 이벤트
        findViewById<Button>(R.id.btnSave).setOnClickListener {
            saveItem()
        }

        // 뒤로가기 버튼 클릭 이벤트
        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish() // 현재 액티비티 종료
        }

        etDate = findViewById(R.id.etDate)
        transactionDate = Date()
        updateDateDisplay()
    }

    private fun setupAccountBookSpinner() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            accountBookList
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        accountBookSpinner.adapter = adapter
    }

    private fun loadAccountBooks() {
        val currentUserId = auth.currentUser?.uid ?: return

        firestore.collection("account_books")
            .whereEqualTo("userId", currentUserId)
            .get()
            .addOnSuccessListener { documents ->
                accountBookList.clear()
                accountBookIds.clear()
                for (document in documents) {
                    val accountBookName = document.getString("name") ?: "이름 없음"
                    accountBookList.add(accountBookName)
                    accountBookIds.add(document.id)
                }
                (accountBookSpinner.adapter as ArrayAdapter<*>).notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "가계부 목록 불러오기 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveItem() {
        // 입력된 금액, 메모, 선택된 카테고리 가져오기
        val amount = findViewById<EditText>(R.id.etAmount).text.toString().toIntOrNull()
        val description = findViewById<EditText>(R.id.etDescription).text.toString()
        val category = findViewById<Spinner>(R.id.spinnerCategory).selectedItem.toString()

        // 선택된 가계부 ID 가져오기
        val selectedPosition = accountBookSpinner.selectedItemPosition
        if (selectedPosition == -1 || accountBookIds.isEmpty()) {
            Toast.makeText(this, "가계부를 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }
        val selectedAccountBookId = accountBookIds[selectedPosition]

        // 필수 입력값 검증
        if (amount == null) {
            Toast.makeText(this, "모든 내용를 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        // Firestore에 저장할 데이터 구성
        val item = hashMapOf(
            "amount" to amount,
            "description" to description,
            "category" to category,
            "date" to transactionDate,
            "userId" to auth.currentUser?.uid,
            "accountBookId" to selectedAccountBookId
        )

        // Firestore의 'items' 컬렉션에 데이터 추가
        firestore.collection("items")
            .add(item)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this, "내역이 추가되었습니다.", Toast.LENGTH_SHORT).show()
                finish() // 현재 액티비티만 종료
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "내역 추가 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // 날짜 표시 업데이트 함수
    private fun updateDateDisplay() {
        val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분", Locale.KOREA)
        etDate.setText(dateFormat.format(transactionDate))
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

