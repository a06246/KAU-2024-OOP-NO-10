package com.example.accountbooks

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.accountbooks.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SharedBudgetActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth // Firebase 인증 객체
    private lateinit var firestore: FirebaseFirestore // Firestore 객체
    private val accountBookList = mutableListOf<String>()
    private lateinit var accountBookAdapter: AccountBookAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shared_budget)

        // Firebase 인증 객체와 Firestore 인스턴스 가져오기
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // 로그인 및 회원가입 버튼 설정
        val loginButton: Button = findViewById(R.id.btnLogin)
        val registerButton: Button = findViewById(R.id.btnSignup)

        // 로그인 버튼 클릭 리스너
        loginButton.setOnClickListener {
            loginUser()
        }

        // 회원가입 버튼 클릭 리스너
        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // RecyclerView 설정
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 어댑터 설정
        accountBookAdapter = AccountBookAdapter(accountBookList) { accountBookName ->
            // 가계부 항목 클릭 시 상세 화면으로 이동 (현재 구현되지 않음)
            // val intent = Intent(this, AccountBookDetailActivity::class.java)
            // intent.putExtra("ACCOUNT_BOOK_NAME", accountBookName)
            // startActivity(intent)

            // 대신 간단히 토스트 메시지를 출력하여 클릭 이벤트가 잘 작동하는지 확인합니다.
            Toast.makeText(this, "$accountBookName 선택됨", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = accountBookAdapter

        // FloatingActionButton 클릭 리스너 (새로운 가계부 추가)
        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener {
            showAddAccountBookDialog()
        }
    }

    // 로그인 처리
    private fun loginUser() {
        val email = findViewById<EditText>(R.id.etId).text.toString()
        val password = findViewById<EditText>(R.id.etPassword).text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        // Firebase를 사용해 로그인 처리
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // 로그인 성공 시
                    Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()
                    // 로그인 후 가계부 UI 표시 및 가계부 데이터 로드
                    showAccountBookUI()
                    loadAccountBooks()
                } else {
                    // 로그인 실패 시
                    Toast.makeText(this, "로그인 실패: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // 로그인 후 가계부 UI 표시
    private fun showAccountBookUI() {
        // 로그인 후 UI 변경 작업 (로그인 화면을 숨기거나 가계부 UI를 표시하는 작업)
        findViewById<RecyclerView>(R.id.recyclerView).visibility = View.VISIBLE
        findViewById<FloatingActionButton>(R.id.fab).visibility = View.VISIBLE
    }

    // Firestore에서 가계부 목록 불러오기
    private fun loadAccountBooks() {
        val currentUserId = auth.currentUser?.uid ?: return

        firestore.collection("account_books")
            .whereEqualTo("userId", currentUserId)
            .get()
            .addOnSuccessListener { documents ->
                accountBookList.clear()
                for (document in documents) {
                    val accountBookName = document.getString("name") ?: "이름 없음"
                    accountBookList.add(accountBookName)
                }
                accountBookAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "가계부 목록 불러오기 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Firestore에 가계부 추가
    private fun addAccountBookToFirestore(accountBookName: String) {
        val currentUserId = auth.currentUser?.uid ?: return

        val accountBook = hashMapOf(
            "name" to accountBookName,
            "userId" to currentUserId // 현재 사용자의 UID 저장
        )

        firestore.collection("account_books")
            .add(accountBook)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this, "가계부 추가됨: ${documentReference.id}", Toast.LENGTH_SHORT).show()
                accountBookList.add(accountBookName)
                accountBookAdapter.notifyItemInserted(accountBookList.size - 1)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "가계부 추가 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // 가계부 추가를 위한 다이얼로그 표시
    private fun showAddAccountBookDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("새 가계부 추가")

        // 입력 필드 생성
        val input = EditText(this)
        input.hint = "가계부 이름 입력"
        builder.setView(input)

        // 확인 버튼 클릭 리스너
        builder.setPositiveButton("추가") { dialog, _ ->
            val newAccountBookName = input.text.toString()
            if (newAccountBookName.isNotEmpty()) {
                addAccountBookToFirestore(newAccountBookName)
            } else {
                Toast.makeText(this, "가계부 이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        // 취소 버튼 클릭 리스너
        builder.setNegativeButton("취소") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }
}
