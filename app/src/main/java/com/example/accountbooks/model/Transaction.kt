package com.example.accountbooks.model

import java.util.Date

data class Transaction(
    val id: String = "",
    val amount: Int = 0,
    val category: String = "",
    val date: Date = Date(),
    val description: String = "",
    val userId: String = ""
) 