package com.example.accountbooks.models

import java.util.Date

data class Transaction(
    val id: String = "",
    val amount: Long = 0,
    val category: String = "",
    val description: String = "",
    val date: Date = Date(),
    val userId: String = "",
    val merchant: String = ""
) 