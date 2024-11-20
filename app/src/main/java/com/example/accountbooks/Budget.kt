package com.example.accountbooks

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class Budget : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_budget)

        val nameContainer = findViewById<LinearLayout>(R.id.nameContainer)
        val submitButton = findViewById<Button>(R.id.btnSubmit)

        // 직접 입력 버튼 클릭 이벤트
        submitButton.setOnClickListener {
            // 최대 4개까지만 추가 가능
            if (nameContainer.childCount >= 4) { // 아이콘 4개까지만 허용
                Toast.makeText(this, "최대 4명까지만 추가 가능합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 이름 입력 필드 생성
            val editText = EditText(this).apply {
                hint = "이름 입력"
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(16, 8, 16, 8)
                }
            }

            // 확인 버튼 생성
            val confirmButton = Button(this).apply {
                text = "확인"
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(16, 8, 16, 8)
                }
                setOnClickListener {
                    val name = editText.text.toString()
                    if (name.isNotBlank()) {
                        // 이름이 입력되었을 경우 아이콘 추가
                        addNameBubble(name, nameContainer)
                        nameContainer.removeView(editText) // 입력 필드 제거
                        nameContainer.removeView(this)   // 확인 버튼 제거

                        // 추가 완료 후 4개가 꽉 찼으면 버튼 비활성화
                        if (nameContainer.childCount >= 4) {
                            submitButton.isEnabled = false
                            Toast.makeText(this@Budget, "모든 이름을 추가했습니다.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@Budget, "이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // 이름 입력 필드와 확인 버튼 추가
            nameContainer.addView(editText)
            nameContainer.addView(confirmButton)
        }
    }

    private fun addNameBubble(name: String, container: LinearLayout) {
        // 동그란 이름 아이콘 생성
        val bubble = TextView(this).apply {
            text = name
            setPadding(24, 12, 24, 12)
            setTextColor(Color.BLACK)
            textSize = 14f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 8, 16, 8)
            }
            background = createBubbleBackground(container.childCount) // 순서를 기반으로 색상 설정
        }

        container.addView(bubble)
    }

    // 동적으로 배경 색상을 설정하는 함수
    private fun createBubbleBackground(index: Int): GradientDrawable {
        val colors = listOf("#FFEB3B", "#FF5722", "#4CAF50", "#03A9F4") // 노랑, 주황, 초록, 파랑
        val color = Color.parseColor(colors[index % colors.size])

        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 50f // 둥근 모서리
            setColor(color) // 배경색 설정
        }
    }
}
