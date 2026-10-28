package com.eversince.data.model

import java.time.LocalDate

data class Relationship(
    val person1: String = "Madhu",
    val person2: String = "Partner",
    val startDate: LocalDate = LocalDate.of(2025, 5, 27),
    val partnerPhotoUri: String? = null
)

data class SpecialMoment(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val date: LocalDate,
    val note: String
)

data class SpecialDay(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val date: LocalDate,
    val note: String,
    val reminderEnabled: Boolean = true
)
