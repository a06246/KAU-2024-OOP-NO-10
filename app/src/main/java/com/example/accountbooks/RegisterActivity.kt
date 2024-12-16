package com.example.accountbooks

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
//import com.example.accountbooks.R
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth // Firebase Authentication 객체

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Firebase Authentication 초기화
        auth = FirebaseAuth.getInstance()

        // 회원가입 버튼 설정
        val registerButton: Button = findViewById(R.id.buttonRegister)
        registerButton.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val email = findViewById<EditText>(R.id.editTextEmail).text.toString().trim()
        val password = findViewById<EditText>(R.id.editTextPassword).text.toString().trim()

        // 유효성 검사
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "비밀번호는 최소 6자 이상이어야 합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // Firebase Authentication으로 회원가입 처리
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "회원가입 성공", Toast.LENGTH_SHORT).show()
                    navigateToSharedBudgetActivity()
                } else {
                    Toast.makeText(this, "회원가입 실패: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun navigateToSharedBudgetActivity() {
        val intent = Intent(this, SharedBudgetActivity::class.java)
        startActivity(intent)
        finish() // 현재 액티비티 종료
    }
}
