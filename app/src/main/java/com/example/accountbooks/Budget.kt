package com.example.accountbooks

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore


class Budget : AppCompatActivity() {

    // Firestore 객체 및 뷰 초기화
    private lateinit var firestore: FirebaseFirestore
    private lateinit var memberContainer: LinearLayout
    private lateinit var historyContainer: LinearLayout

    // 가계부와 관련된 변수
    private val accountBookNames = mutableListOf<String>()
    private val accountBookIds = mutableListOf<String>()
    private var currentAccountBookId: String? = null

    // 멤버 이름과 색상 매핑
    private val memberColorMap = mutableMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_budget)

        // Firestore 초기화
        firestore = FirebaseFirestore.getInstance()

        // UI 초기화
        val accountBookButton = findViewById<Button>(R.id.btnAccountBookList)
        val friendAddButton = findViewById<Button>(R.id.btnFriendAdd)
        memberContainer = findViewById(R.id.memberContainer)
        historyContainer = findViewById(R.id.historyContainer)

        // 버튼 이벤트 리스너
        accountBookButton.setOnClickListener { fetchAccountBookNames() }
        friendAddButton.setOnClickListener { fetchFriendList() }
    }


    // Firestore에서 모든 가계부 이름 가져오기
    private fun fetchAccountBookNames() {
        firestore.collection("account_books")
            .get()
            .addOnSuccessListener { documents ->
                accountBookNames.clear()
                accountBookIds.clear()
                for (document in documents) {
                    val name = document.getString("name") ?: "이름 없음"
                    accountBookNames.add(name)
                    accountBookIds.add(document.id)
                }
                showAccountBookListDialog()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "가계부 목록 불러오기 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // 가계부 목록을 선택하는 다이얼로그 표시
    private fun fetchFriendList() {
        val friendNames = mutableListOf<String>()

        // Firebase friends 컬렉션에서 모든 name 필드 가져오기
        firestore.collection("friends")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val name = document.getString("name") ?: "이름 없음"
                    friendNames.add(name)
                }
                showFriendListDialog(friendNames)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "친구 목록 불러오기 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // 친구 목록 다이얼로그 표시
    private fun showFriendListDialog(friendNames: List<String>) {
        val selectedFriends = mutableListOf<String>()

        // 다이얼로그 레이아웃
        val dialogLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }

        // 친구 목록 ListView
        val listView = ListView(this)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, friendNames)
        listView.adapter = adapter
        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE

        dialogLayout.addView(listView)

        // 직접 추가 버튼
        val addButton = Button(this).apply {
            text = "직접 추가"
            setOnClickListener {
                showDirectAddDialog() // 직접 추가 다이얼로그 표시
            }
        }
        dialogLayout.addView(addButton)

        // 다이얼로그 표시
        AlertDialog.Builder(this)
            .setTitle("친구 목록")
            .setView(dialogLayout)
            .setPositiveButton("확인") { _, _ ->
                for (i in 0 until listView.count) {
                    if (listView.isItemChecked(i)) {
                        selectedFriends.add(friendNames[i])
                    }
                }
                displaySelectedFriends(selectedFriends)
            }
            .setNegativeButton("닫기", null)
            .show()
    }


    private fun showDirectAddDialog() {
        val editText = EditText(this).apply {
            hint = "이름을 입력하세요"
        }

        AlertDialog.Builder(this)
            .setTitle("친구 추가")
            .setView(editText)
            .setPositiveButton("추가") { _, _ ->
                val name = editText.text.toString().trim()
                if (name.isNotEmpty()) {
                    addFriendToFirebase(name)
                } else {
                    Toast.makeText(this, "이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }


    private fun addFriendToFirebase(name: String) {
        // friends 컬렉션에 친구 추가
        val newFriend = hashMapOf("name" to name)

        firestore.collection("friends")
            .add(newFriend)
            .addOnSuccessListener {
                Toast.makeText(this, "친구가 추가되었습니다: $name", Toast.LENGTH_SHORT).show()
                updateAccountBookMembers(name) // 가계부에 멤버 추가
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "친구 추가 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateAccountBookMembers(newMember: String) {
        currentAccountBookId?.let { accountBookId ->
            firestore.collection("account_books")
                .document(accountBookId)
                .get()
                .addOnSuccessListener { document ->
                    val existingMembers = document.getString("member") ?: ""

                    // 기존 멤버 리스트와 새 멤버 병합
                    val memberList = existingMembers.split(",").map { it.trim() }.toMutableList()
                    if (!memberList.contains(newMember)) {
                        memberList.add(newMember)
                    }

                    // 업데이트된 멤버 리스트를 Firestore에 저장
                    val updatedMembers = memberList.joinToString(", ")
                    firestore.collection("account_books")
                        .document(accountBookId)
                        .update("member", updatedMembers)
                        .addOnSuccessListener {
                            Toast.makeText(this, "$newMember 가 가계부에 추가되었습니다.", Toast.LENGTH_SHORT).show()
                            reloadMemberIcons(accountBookId) // 멤버 아이콘 갱신
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "멤버 추가 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "기존 멤버 불러오기 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } ?: Toast.makeText(this, "가계부가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show()
    }



    private fun reloadMemberIcons(accountBookId: String) {
        firestore.collection("account_books")
            .document(accountBookId)
            .get()
            .addOnSuccessListener { document ->
                val updatedMembers = document.getString("member") ?: ""
                displayMemberIcons(updatedMembers) // 최신 멤버 아이콘 표시
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "멤버 아이콘 업데이트 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }





    private fun displaySelectedFriends(selectedFriends: List<String>) {
        memberContainer.removeAllViews()

        val colors = listOf("#FF5733", "#33FF57", "#3357FF", "#FF33A1") // 색상 목록

        selectedFriends.forEachIndexed { index, friend ->
            val color = colors[index % colors.size]
            val icon = createMemberIcon(friend, color)
            memberContainer.addView(icon)
        }
    }


    // 가계부 목록 다이얼로그 표시
    private fun showAccountBookListDialog() {
        AlertDialog.Builder(this)
            .setTitle("가계부 목록")
            .setItems(accountBookNames.toTypedArray()) { _, which ->
                currentAccountBookId = accountBookIds[which] // 선택한 가계부 ID 저장
                val selectedAccountBookName = accountBookNames[which] // 선택한 가계부 이름 저장
                updateAccountBookTitle(selectedAccountBookName) // 제목 업데이트
                loadMemberTransactions(currentAccountBookId!!) // 거래 내역 불러오기
            }
            .setNegativeButton("닫기", null)
            .show()
    }

    private fun updateAccountBookTitle(accountBookName: String) {
        val tvTitle = findViewById<TextView>(R.id.tvAccountBookTitle)
        tvTitle.text = "공유 가계부 ($accountBookName)"
    }


    // 선택된 가계부의 멤버와 해당 멤버의 거래 내역 불러오기
    private fun loadMemberTransactions(accountBookId: String) {
        firestore.collection("account_books")
            .document(accountBookId)
            .get()
            .addOnSuccessListener { document ->
                val members = document.getString("member") ?: ""
                val memberList = members.split(",").map { it.trim() }
                displayMemberIcons(members)
                loadTransactionsForMembers(memberList)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "멤버 정보 불러오기 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // 멤버 아이콘 표시
    private fun displayMemberIcons(members: String) {
        memberContainer.removeAllViews() // 기존 아이콘 초기화

        val memberList = members.split(",").map { it.trim() }
        val colors = listOf("#FF5733", "#33FF57", "#3357FF", "#FF33A1") // 색상 목록

        memberList.forEachIndexed { index, member ->
            val color = colors[index % colors.size]
            memberColorMap[member] = color // 멤버별 색상 매핑 저장
            val icon = createMemberIcon(member, color)
            memberContainer.addView(icon)
        }
    }




    // 멤버 아이콘 생성 시 색상 매핑 저장
    private fun createMemberIcon(name: String, color: String): TextView {
        memberColorMap[name] = color // 이름과 색상 저장
        return TextView(this).apply {
            text = name
            textSize = 14f
            setTextColor(Color.WHITE)
            setPadding(24, 12, 24, 12)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 50f
                setColor(Color.parseColor(color))
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(8, 0, 8, 0) }
        }
    }


    // items 컬렉션에서 멤버와 관련된 거래 내역 가져오기
    private fun loadTransactionsForMembers(memberList: List<String>) {
        firestore.collection("items")
            .whereIn("name", memberList)
            .get()
            .addOnSuccessListener { documents ->
                historyContainer.removeAllViews()
                for (document in documents) {
                    val itemId = document.id
                    val name = document.getString("name") ?: "알 수 없음"
                    val amount = document.getLong("amount") ?: 0L
                    val category = document.getString("category") ?: "카테고리 없음"
                    val timestamp = document.getTimestamp("date")
                    val date = timestamp?.toDate()?.toString() ?: "날짜 없음"
                    val description = document.getString("description") ?: "설명 없음"

                    val transactionText =
                        "금액: $amount 원\n카테고리: $category\n날짜: $date\n설명: $description"

                    // 멤버별 색상 가져오기
                    val color = memberColorMap[name] ?: "#FF33A1" // 없으면 기본 색상
                    addTransactionToHistory(itemId, transactionText, name, color)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "거래 내역 불러오기 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }



    // 거래 내역을 historyContainer에 추가
    private fun addTransactionToHistory(itemId: String, transaction: String, name: String, color: String) {
        val transactionLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(8, 8, 8, 8) }
            setPadding(16, 16, 16, 16)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 10f
                setColor(Color.parseColor("#F0F0F0"))
            }
        }

        // 수평 레이아웃에 멤버 이름과 원 추가
        val nameWithIconLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // 작은 원 생성 (멤버 색상 사용)
        val smallCircle = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(20, 20).apply {
                setMargins(0, 0, 8, 0) // 원과 이름 사이 여백
            }
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor(color)) // 멤버 색상 적용
            }
        }

        // 거래 이름 표시
        val nameTextView = TextView(this).apply {
            text = "$name"
            textSize = 14f
            setTextColor(Color.BLACK)
        }

        // 작은 원과 이름 추가
        nameWithIconLayout.addView(smallCircle)
        nameWithIconLayout.addView(nameTextView)

        val transactionTextView = TextView(this).apply {
            text = transaction
            textSize = 14f
            setTextColor(Color.DKGRAY)
        }

        val memoButton = Button(this).apply {
            text = "메모"
            setOnClickListener {
                showMemoPopup(itemId)
            }
        }

        val photoButton = Button(this).apply {
            text = "사진 추가"
            setOnClickListener {
                openImagePicker()
            }
        }

        val buttonLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(memoButton)
            addView(photoButton)
        }

        // 전체 레이아웃 구성
        transactionLayout.addView(nameWithIconLayout)  // 이름과 원
        transactionLayout.addView(transactionTextView) // 거래 내역
        transactionLayout.addView(buttonLayout)        // 메모와 사진 버튼

        historyContainer.addView(transactionLayout)   // 메인 컨테이너에 추가
    }








    private fun showMemoPopup(itemId: String) {
        val memoList = mutableListOf<String>() // 메모 목록

        // 다이얼로그에 표시될 레이아웃
        val dialogLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }

        // 메모 목록을 표시하는 ListView
        val memoListView = ListView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                400 // 높이 제한
            )
        }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, memoList)
        memoListView.adapter = adapter

        // Firebase에서 메모 가져오기
        firestore.collection("items").document(itemId)
            .get()
            .addOnSuccessListener { document ->
                val existingMemo = document.get("memo") as? List<String> ?: listOf()
                memoList.addAll(existingMemo)
                adapter.notifyDataSetChanged()
            }

        // 메모 추가 버튼
        val addMemoButton = Button(this).apply {
            text = "메모 추가"
            setOnClickListener {
                showMemoInputDialog { newMemo ->
                    memoList.add(newMemo)
                    adapter.notifyDataSetChanged()
                    updateMemoInFirebase(itemId, memoList) // Firebase에 메모 업데이트
                }
            }
        }

        // 다이얼로그 레이아웃에 ListView와 버튼 추가
        dialogLayout.addView(memoListView)
        dialogLayout.addView(addMemoButton)

        // 다이얼로그 표시
        AlertDialog.Builder(this)
            .setTitle("메모 내역")
            .setView(dialogLayout)
            .setPositiveButton("닫기", null)
            .show()
    }

    private fun updateMemoInFirebase(itemId: String, memoList: List<String>) {
        firestore.collection("items").document(itemId)
            .update("memo", memoList)
            .addOnSuccessListener {
                Toast.makeText(this, "메모가 저장되었습니다.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "메모 저장 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun showMemoInputDialog(onMemoAdded: (String) -> Unit) {
        val editText = EditText(this).apply {
            hint = "메모를 입력하세요"
        }

        AlertDialog.Builder(this)
            .setTitle("메모 추가")
            .setView(editText)
            .setPositiveButton("추가") { _, _ ->
                val memo = editText.text.toString().trim()
                if (memo.isNotEmpty()) {
                    onMemoAdded(memo)
                } else {
                    Toast.makeText(this, "메모를 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }



    private fun addMemoAddButton(memoContainer: LinearLayout) {
        // 메모 추가 버튼
        val addButton = Button(this).apply {
            text = "메모 추가"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener {
                showMemoDialog(memoContainer) // 메모 입력창 표시
            }
        }
        memoContainer.addView(addButton)
    }

    private fun showMemoDialog(memoContainer: LinearLayout) {
        val editText = EditText(this).apply {
            hint = "메모를 입력하세요"
        }

        AlertDialog.Builder(this)
            .setTitle("메모 추가")
            .setView(editText)
            .setPositiveButton("저장") { _, _ ->
                val memo = editText.text.toString().trim()
                if (memo.isNotEmpty()) {
                    addMemoToContainer(memo, memoContainer) // 메모 내역에 추가
                } else {
                    Toast.makeText(this, "메모가 비어 있습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun addMemoToContainer(memo: String, memoContainer: LinearLayout) {
        val memoTextView = TextView(this).apply {
            text = memo
            textSize = 12f
            setTextColor(Color.DKGRAY)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 8, 0, 8) }
        }
        memoContainer.addView(memoTextView, 0) // 메모를 가장 위에 추가
    }


    // 메모 추가 다이얼로그
    private fun showMemoDialog(transaction: String) {
        val editText = EditText(this).apply {
            hint = "메모를 입력하세요"
        }

        AlertDialog.Builder(this)
            .setTitle("메모 추가")
            .setView(editText)
            .setPositiveButton("저장") { _, _ ->
                val memo = editText.text.toString().trim()
                if (memo.isNotEmpty()) {
                    Toast.makeText(this, "메모 저장됨: $memo", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "메모가 비어 있습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }


    // 사진 추가 기능
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        startActivityForResult(intent, IMAGE_PICK_REQUEST)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_REQUEST && resultCode == RESULT_OK) {
            val imageUri: Uri? = data?.data
            if (imageUri != null) {
                Toast.makeText(this, "이미지 선택됨: $imageUri", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "이미지 선택 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val IMAGE_PICK_REQUEST = 100
    }
}