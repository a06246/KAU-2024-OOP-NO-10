package com.example.accountbooks

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FriendsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private val friendList = mutableListOf<String>() // 친구목록을 저장하는 리스트
    private lateinit var friendAdapter: FriendAdapter // RecyclerView의 어댑터

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // 뒤로가기 버튼
        val backButton: Button = findViewById(R.id.btnBack)
        backButton.setOnClickListener { finish() }

        val addFriendButton: Button = findViewById(R.id.btnAddFriend)
        val friendEmailEditText: EditText = findViewById(R.id.etFriendEmail)

        // 친구 추가 버튼 클릭 리스너
        addFriendButton.setOnClickListener {
            val friendEmail = friendEmailEditText.text.toString().trim()
            if (friendEmail.isNotEmpty()) {
                checkAndAddFriendFromAuth(friendEmail) // Authentication에서 확인
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

    // Firebase Authentication에서 이메일 확인 후 친구 추가
    private fun checkAndAddFriendFromAuth(friendEmail: String) {
        auth.fetchSignInMethodsForEmail(friendEmail)
            .addOnSuccessListener { result ->
                val signInMethods = result.signInMethods
                if (signInMethods != null && signInMethods.isNotEmpty()) {
                    // 이메일이 Authentication에 존재하면 친구 추가
                    addFriend(friendEmail)
                } else {
                    Toast.makeText(this, "해당 회원이 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "이메일 확인 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Firestore에 친구 정보 추가
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

    // Firestore에서 친구 목록 불러오기
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
