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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class SharedBudgetActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth // Firebase Authentication 객체
    private lateinit var firestore: FirebaseFirestore // Firestore 객체
    private val accountBookList = mutableListOf<String>()
    private lateinit var accountBookAdapter: AccountBookAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shared_budget)

        // Firebase 초기화
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // UI 요소 초기화
        val loginButton: Button = findViewById(R.id.btnLogin)
        val registerButton: Button = findViewById(R.id.btnSignup)
        val friendManagementButton: Button = findViewById(R.id.btnFriendManagement)
        val fab: FloatingActionButton = findViewById(R.id.fab)

        // 개인 가계부, 내역추가, 자산 버튼 설정
        val expenseHistoryButton: Button = findViewById(R.id.btnExpenseHistory)
        val addTransactionButton: Button = findViewById(R.id.btnAddTransaction)
        val assetsButton: Button = findViewById(R.id.btnAssets)
        val sharedAccountButton: Button = findViewById(R.id.btnSharedAccount)

        expenseHistoryButton.visibility = View.GONE
        addTransactionButton.visibility = View.GONE
        assetsButton.visibility = View.GONE
        sharedAccountButton.visibility = View.GONE

        // 로그인 버튼 리스너
        loginButton.setOnClickListener { loginUser() }

        // 회원가입 버튼 리스너
        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // 친구 관리 화면 이동
        friendManagementButton.setOnClickListener {
            val intent = Intent(this, FriendsActivity::class.java)
            startActivity(intent)
        }

        // RecyclerView 설정
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        accountBookAdapter = AccountBookAdapter(accountBookList) { accountBookName ->
            Toast.makeText(this, "$accountBookName 선택됨", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = accountBookAdapter

        // FloatingActionButton 클릭 리스너 (새로운 가계부 추가)
        fab.setOnClickListener { showAddAccountBookDialog() }

        // 개인 가계부 버튼 클릭
        expenseHistoryButton.setOnClickListener {
            Toast.makeText(this, "개인 가계부 버튼 클릭됨", Toast.LENGTH_SHORT).show()
            // TODO: 개인 가계부 화면으로 이동 코드 추가
            val intent = Intent(this, CalendarActivity::class.java)
            startActivity(intent)
        }

        // 내역추가 버튼 클릭
        addTransactionButton.setOnClickListener {
            Toast.makeText(this, "내역추가 버튼 클릭됨", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, AddItemActivity::class.java)
            startActivity(intent)
        }

        // 자산 버튼 클릭
        assetsButton.setOnClickListener {
            Toast.makeText(this, "자산 버튼 클릭됨", Toast.LENGTH_SHORT).show()
            // TODO: 자산 화면으로 이동 코드 추가
        }

        //공유가계부 버튼 클릭
        sharedAccountButton.setOnClickListener {
            Toast.makeText(this, "공유 가계부 버튼 클릭됨", Toast.LENGTH_SHORT).show()

            // TODO: 공유 가계부 화면으로 이동 (SharedAccountActivity가 아직 구현되지 않았습니다)
        }
    }

    // 로그인 처리 (Firebase Authentication)
    private fun loginUser() {
        val email = findViewById<EditText>(R.id.etId).text.toString().trim()
        val password = findViewById<EditText>(R.id.etPassword).text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()
                    showAccountBookUI()
                    loadAccountBooks() // Firestore에서 가계부 목록 불러오기
                    loadFriends()      // Firestore에서 친구 목록 불러오기
                } else {
                    Toast.makeText(this, "로그인 실패: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // 로그인 후 UI 표시
    private fun showAccountBookUI() {
        findViewById<RecyclerView>(R.id.recyclerView).visibility = View.VISIBLE
        findViewById<FloatingActionButton>(R.id.fab).visibility = View.VISIBLE

        findViewById<Button>(R.id.btnLogin).visibility = View.GONE
        findViewById<Button>(R.id.btnSignup).visibility = View.GONE
        findViewById<EditText>(R.id.etId).visibility = View.GONE
        findViewById<EditText>(R.id.etPassword).visibility = View.GONE
        findViewById<Button>(R.id.btnFriendManagement).visibility = View.VISIBLE
        findViewById<Button>(R.id.btnExpenseHistory).visibility = View.VISIBLE
        findViewById<Button>(R.id.btnAddTransaction).visibility = View.VISIBLE
        findViewById<Button>(R.id.btnAssets).visibility = View.VISIBLE
        findViewById<Button>(R.id.btnSharedAccount).visibility = View.VISIBLE
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
                    if (accountBookName != "myAccountBook") { // 기본 가계부는 제외
                        accountBookList.add(accountBookName)
                    }
                }
                accountBookAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "가계부 목록 불러오기 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Firestore에서 친구 목록 불러오기
    private fun loadFriends() {
        val currentUserId = auth.currentUser?.uid ?: return

        firestore.collection("friends")
            .whereEqualTo("userId", currentUserId)
            .get()
            .addOnSuccessListener { documents ->
                val friendList = mutableListOf<String>()
                for (document in documents) {
                    val friendEmail = document.getString("email") ?: "이메일 없음"
                    friendList.add(friendEmail)
                }
                Toast.makeText(this, "친구 목록 불러오기 성공: ${friendList.size}명", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "친구 목록 불러오기 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // 가계부 추가 다이얼로그
    private fun showAddAccountBookDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("새 가계부 추가")

        val input = EditText(this)
        input.hint = "가계부 이름 입력"
        builder.setView(input)

        builder.setPositiveButton("추가") { _, _ ->
            val accountBookName = input.text.toString()
            if (accountBookName.isNotEmpty()) {
                addAccountBookToFirestore(accountBookName)
            } else {
                Toast.makeText(this, "가계부 이름을 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("취소") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun addAccountBookToFirestore(accountBookName: String) {
        val currentUserId = auth.currentUser?.uid ?: return

        firestore.collection("account_books")
            .add(mapOf("name" to accountBookName, "userId" to currentUserId))
            .addOnSuccessListener {
                accountBookList.add(accountBookName)
                accountBookAdapter.notifyItemInserted(accountBookList.size - 1)
                Toast.makeText(this, "가계부 추가 완료", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "가계부 추가 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}