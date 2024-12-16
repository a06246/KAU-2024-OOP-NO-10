package com.example.accountbooks

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.accountbooks.FriendAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FriendsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private val friendList = mutableListOf<String>() //친구목록을 저장하는 리스트
    private lateinit var friendAdapter: FriendAdapter //RecyclerView의 어댑터로, 친구 목록을 화면에 표시하는 역할을 합니다.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends) //setContentView(R.layout.activity_friends)를 통해 이 액티비티에서 사용할 레이아웃을 설정합니다.

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // 뒤로가기 버튼 설정
        val backButton: Button = findViewById(R.id.btnBack)
        backButton.setOnClickListener {
            finish() // 현재 액티비티 종료 (이전 화면으로 돌아감)
        }

        val addFriendButton: Button = findViewById(R.id.btnAddFriend)
        val friendEmailEditText: EditText = findViewById(R.id.etFriendEmail)

        // 친구 추가 버튼 클릭 리스너
        addFriendButton.setOnClickListener {
            val friendEmail = friendEmailEditText.text.toString()
            if (friendEmail.isNotEmpty()) {
                checkAndAddFriend(friendEmail)
            } else {
                Toast.makeText(this, "친구 이메일을 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // 친구 목록 RecyclerView 설정
        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewFriends)
        recyclerView.layoutManager = LinearLayoutManager(this)
        friendAdapter = FriendAdapter(friendList) { friendName ->
            Toast.makeText(this, "$friendName 선택됨", Toast.LENGTH_SHORT).show()
        }

        recyclerView.adapter = friendAdapter

        loadFriends()
    }
    //친구추가전 이메일확인
    private fun checkAndAddFriend(friendEmail: String) {
        firestore.collection("users")
            .whereEqualTo("email", friendEmail)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    addFriend(friendEmail)
                } else {
                    Toast.makeText(this, "해당 회원이 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "친구 추가 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addFriend(friendEmail: String) {
        val currentUserId = auth.currentUser?.uid ?: return

        val friend = hashMapOf(
            "email" to friendEmail,
            "userId" to currentUserId
        )

        firestore.collection("friends")
            .add(friend)
            .addOnSuccessListener {
                Toast.makeText(this, "친구가 추가되었습니다.", Toast.LENGTH_SHORT).show()
                friendList.add(friendEmail)
                friendAdapter.notifyItemInserted(friendList.size - 1)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "친구 추가 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadFriends() {
        val currentUserId = auth.currentUser?.uid ?: return

        firestore.collection("friends")
            .whereEqualTo("userId", currentUserId)
            .get()
            .addOnSuccessListener { documents ->
                friendList.clear()
                for (document in documents) {
                    val friendEmail = document.getString("email") ?: "이메일 없음"
                    friendList.add(friendEmail)
                }
                friendAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "친구 목록 불러오기 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
