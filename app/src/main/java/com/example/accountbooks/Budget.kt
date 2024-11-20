package com.example.accountbooks

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class Budget : AppCompatActivity() {
    private val colors = mutableListOf("#FFEB3B", "#FF5722", "#4CAF50", "#03A9F4") // 노랑, 주황, 초록, 파랑
    private val usedColors = mutableSetOf<String>() // 사용된 색상
    private val addedNames = mutableSetOf<String>() // 추가된 이름

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_budget)

        val nameContainer = findViewById<LinearLayout>(R.id.nameContainer)
        val submitButton = findViewById<Button>(R.id.btnSubmit)
        val friendListButton = findViewById<Button>(R.id.btnFriendList)

        // 직접 입력 버튼 클릭 이벤트
        submitButton.setOnClickListener {
            if (nameContainer.childCount >= 4) { // 최대 4개까지만 추가
                Toast.makeText(this, "최대 4명까지만 추가 가능합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val editText = EditText(this).apply {
                hint = "이름 입력"
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(16, 8, 16, 8)
                }
            }

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
                    if (name.isBlank()) {
                        Toast.makeText(this@Budget, "이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
                    } else if (addedNames.contains(name)) {
                        Toast.makeText(this@Budget, "중복된 이름은 추가할 수 없습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        addNameBubble(name, nameContainer)
                        addedNames.add(name)
                        nameContainer.removeView(editText)
                        nameContainer.removeView(this)
                    }
                }
            }

            nameContainer.addView(editText)
            nameContainer.addView(confirmButton)
        }

        // 친구 목록 버튼 클릭 이벤트
        friendListButton.setOnClickListener {
            showFriendListDialog(nameContainer)
        }
    }

    private fun addNameBubble(name: String, container: LinearLayout) {
        // 색상 선택 (중복 방지)
        val availableColors = colors.filterNot { usedColors.contains(it) }
        if (availableColors.isEmpty()) {
            Toast.makeText(this, "사용 가능한 색상이 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }
        val color = availableColors.first()
        usedColors.add(color)

        // 동그란 이름 아이콘 생성
        val bubble = TextView(this).apply {
            text = name
            setPadding(24, 12, 24, 12)
            textSize = 14f
            setTextColor(Color.BLACK)
            background = createCircleBackground(color)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 8, 16, 8)
            }
        }
        container.addView(bubble)
    }

    private fun createCircleBackground(color: String): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 100f // 둥근 모서리를 만들기 위한 값
            setColor(Color.parseColor(color)) // 배경 색상
        }
    }

    private fun showFriendListDialog(container: LinearLayout) {
        val friends = arrayOf("Alice", "Bob", "Charlie", "David", "Eve")
        val selectedFriends = mutableListOf<String>()

        val builder = AlertDialog.Builder(this)
        builder.setTitle("친구 목록")
            .setMultiChoiceItems(friends, null) { _, which, isChecked ->
                val friend = friends[which]
                if (isChecked) {
                    if (!selectedFriends.contains(friend)) {
                        selectedFriends.add(friend)
                    }
                } else {
                    selectedFriends.remove(friend)
                }
            }
            .setPositiveButton("완료") { _, _ ->
                for (friend in selectedFriends) {
                    if (addedNames.contains(friend)) {
                        Toast.makeText(this, "$friend 이미 추가된 이름입니다.", Toast.LENGTH_SHORT).show()
                        continue
                    }
                    if (container.childCount >= 4) {
                        Toast.makeText(this, "최대 4명까지만 추가 가능합니다.", Toast.LENGTH_SHORT).show()
                        break
                    }
                    addNameBubble(friend, container)
                    addedNames.add(friend)
                }
            }
            .setNegativeButton("취소", null)
        builder.create().show()
    }
}
