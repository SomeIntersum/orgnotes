package com.example.orgnotes.data

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Note(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val dateTimeIso: String,  // ISO-8601
    val content: String
)
