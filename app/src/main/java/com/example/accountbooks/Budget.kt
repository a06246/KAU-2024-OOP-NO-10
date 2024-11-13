package com.example.accountbooks

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Budget : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_budget)

        // 시스템 창 삽입 설정
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Spinner와 inputContainer 참조
        val spinner = findViewById<Spinner>(R.id.spinner)
        val inputContainer = findViewById<LinearLayout>(R.id.nameContainer)
        val submitButton = findViewById<Button>(R.id.btnSubmit)

        // Spinner에서 선택된 인원 수에 따라 입력 필드 생성
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedPeople = position + 1 // 1인, 2인, 3인에 대응

                // 기존 입력 필드 제거
                inputContainer.removeAllViews()

                // 선택된 인원 수만큼 입력 필드 추가
                for (i in 1..selectedPeople) {
                    val editText = EditText(this@Budget).apply {
                        hint = "이름 $i"
                        layoutParams = LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1f
                        ).apply {
                            setMargins(16, 8, 8, 8)
                        }
                    }
                    inputContainer.addView(editText)
                }

                // 입력 완료 버튼을 보이도록 설정
                submitButton.visibility = View.VISIBLE
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // 아무것도 선택되지 않은 경우 (필요 시 구현)
            }
        }

        // 입력 완료 버튼 클릭 리스너
        submitButton.setOnClickListener {
            Toast.makeText(this, "입력이 완료되었습니다.", Toast.LENGTH_SHORT).show()
            // 입력 완료 버튼을 사라지게 설정
            submitButton.visibility = View.GONE
        }
    }
}
