package com.example.accountbooks

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Budget : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.fragment_budget)

        // 시스템 창 삽입 설정
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Spinner와 ButtonContainer 참조
        val spinner = findViewById<Spinner>(R.id.spinner)
        val buttonContainer = findViewById<LinearLayout>(R.id.buttonContainer)

        // Spinner에서 선택된 인원 수에 따라 버튼 생성
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedPeople = position + 1 // 1인, 2인, 3인에 대응

                // 기존 버튼 제거
                buttonContainer.removeAllViews()

                // 선택된 인원 수만큼 버튼 추가
                for (i in 1..selectedPeople) {
                    val button = Button(this@Budget).apply {
                        text = "이름 $i"
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(16, 8, 16, 8)
                        }
                    }
                    buttonContainer.addView(button)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // 아무것도 선택되지 않은 경우 (필요 시 구현)
            }
        }
    }
}
