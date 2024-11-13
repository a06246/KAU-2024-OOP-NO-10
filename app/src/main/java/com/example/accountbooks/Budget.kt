package com.example.accountbooks

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
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

        // Spinner에서 선택된 인원 수에 따라 EditText 생성
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedPeople = position + 1 // 1인, 2인, 3인에 대응

                // 기존 EditText 및 버튼 제거
                buttonContainer.removeAllViews()

                // 선택된 인원 수만큼 EditText 추가
                for (i in 1..selectedPeople) {
                    val editText = EditText(this@Budget).apply {
                        hint = "이름 $i 입력"
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(16, 8, 16, 8)
                        }
                    }
                    buttonContainer.addView(editText)
                }

                // 입력 완료 버튼 추가
                val completeButton = Button(this@Budget).apply {
                    text = "입력 완료"
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(16, 8, 16, 8)
                    }
                    setOnClickListener {
                        // 이름을 입력 완료했을 때 처리 로직
                        val names = mutableListOf<String>()
                        for (i in 0 until buttonContainer.childCount - 1) { // 마지막 버튼 제외
                            val editText = buttonContainer.getChildAt(i) as? EditText
                            val name = editText?.text.toString()
                            names.add(name)
                        }
                        Toast.makeText(this@Budget, "입력된 이름: ${names.joinToString(", ")}", Toast.LENGTH_SHORT).show()

                        // 입력 완료 버튼을 사라지게 설정
                        buttonContainer.removeView(this)
                    }
                }
                buttonContainer.addView(completeButton)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // 아무것도 선택되지 않은 경우 (필요 시 구현)
            }
        }
    }
}
