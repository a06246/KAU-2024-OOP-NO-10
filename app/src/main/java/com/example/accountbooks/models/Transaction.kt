package com.example.accountbooks.models

import java.util.Date

data class Transaction(
    val id: String = "",
    val amount: Long = 0,
    val category: String = "",
    val date: Date = Date(),
    val memo: String = "",
    val userId: String = "",
    val merchant: String = "",
    val isExpense: Boolean = false
) 