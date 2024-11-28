package com.example.accountbooks

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sharedbudget.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth // Firebase 인증 객체
    private lateinit var firestore: FirebaseFirestore // Firestore 객체

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Firebase 인증 및 Firestore 초기화
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // 회원가입 버튼 설정
        val registerButton: Button = findViewById(R.id.buttonRegister)
        registerButton.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        // 입력된 사용자 이름, 이메일 및 비밀번호 가져오기
        val username = findViewById<EditText>(R.id.editTextUsername).text.toString().trim()
        val email = findViewById<EditText>(R.id.editTextEmail).text.toString().trim()
        val password = findViewById<EditText>(R.id.editTextPassword).text.toString().trim()

        // 유효성 검사
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "비밀번호는 최소 6자 이상이어야 합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // Firebase를 사용해 이메일과 비밀번호로 회원가입 처리
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Firestore에 사용자 세부 정보 저장
                    saveUserData(username, email)
                    // 회원가입 성공 메시지 표시
                    Toast.makeText(this, "회원가입 성공", Toast.LENGTH_SHORT).show()
                    // 메인 액티비티로 이동
                    navigateToSharedBudgetActivity()
                } else {
                    // 회원가입 실패 시 사용자에게 메시지 표시
                    Toast.makeText(this, "회원가입 실패: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUserData(username: String, email: String) {
        // 사용자 데이터 해시맵 생성
        val user = hashMapOf(
            "username" to username,
            "email" to email
        )

        // Firestore 컬렉션에 사용자 데이터 추가
        firestore.collection("users")
            .add(user)
            .addOnSuccessListener { documentReference ->
                Log.d("RegisterActivity", "사용자 정보가 성공적으로 저장되었습니다. 문서 ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.e("RegisterActivity", "사용자 정보 저장 실패", e)
            }
    }

    private fun navigateToSharedBudgetActivity() {
        // 첫화면activity로 이동
        val intent = Intent(this, SharedBudgetActivity::class.java)
        startActivity(intent)
        finish() // 현재 액티비티 종료
    }
}
