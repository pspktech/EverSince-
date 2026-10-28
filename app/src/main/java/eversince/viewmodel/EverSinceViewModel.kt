package com.eversince.viewmodel

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.eversince.data.model.Relationship
import com.eversince.data.model.SpecialDay
import com.eversince.data.model.SpecialMoment
import com.eversince.reminders.ReminderReceiver
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class EverSinceViewModel(application: android.app.Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("ever_since", Context.MODE_PRIVATE)
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val _relationship = MutableStateFlow(loadRelationship())
    private val _onboarded = MutableStateFlow(prefs.getBoolean("onboarded", false))
    val onboarded: StateFlow<Boolean> = _onboarded.asStateFlow()
    val relationship: StateFlow<Relationship> = _relationship.asStateFlow()
    private val _moments = MutableStateFlow(loadMoments())
    val moments: StateFlow<List<SpecialMoment>> = _moments.asStateFlow()
    private val _specialDays = MutableStateFlow(loadSpecialDays())
    val specialDays: StateFlow<List<SpecialDay>> = _specialDays.asStateFlow()
    private val _liveTime = MutableStateFlow("0 days 0 hours 0 minutes")
    val liveTime: StateFlow<String> = _liveTime.asStateFlow()

    init { startTicker() }

    fun completeOnboarding(person1: String, person2: String, startDate: String) {
        saveProfile(person1, person2, startDate)
        prefs.edit().putBoolean("onboarded", true).apply()
        _onboarded.value = true
    }

    fun resetOnboarding() {
        prefs.edit().putBoolean("onboarded", false).apply()
        _onboarded.value = false
    }

    fun saveProfile(person1: String, person2: String, startDate: String) {
        val parsed = runCatching { LocalDate.parse(startDate.trim(), dateFormatter) }.getOrNull() ?: return
        _relationship.value = _relationship.value.copy(person1 = person1.trim().ifBlank { "You" }, person2 = person2.trim().ifBlank { "Partner" }, startDate = parsed)
        persistRelationship()
    }

    fun savePartnerPhoto(uri: String) { _relationship.value = _relationship.value.copy(partnerPhotoUri = uri); persistRelationship() }

    fun addMoment(title: String, date: String, note: String) {
        val parsed = runCatching { LocalDate.parse(date.trim(), dateFormatter) }.getOrNull() ?: return
        _moments.value = listOf(SpecialMoment(title = title.trim(), date = parsed, note = note.trim())) + _moments.value
        persistMoments()
    }

    fun addSpecialDay(title: String, date: String, note: String, reminder: Boolean) {
        val parsed = runCatching { LocalDate.parse(date.trim(), dateFormatter) }.getOrNull() ?: return
        val day = SpecialDay(title = title.trim(), date = parsed, note = note.trim(), reminderEnabled = reminder)
        _specialDays.value = listOf(day) + _specialDays.value
        persistSpecialDays()
        if (reminder) scheduleReminder(day)
    }

    fun toggleReminder(day: SpecialDay) {
        val updated = day.copy(reminderEnabled = !day.reminderEnabled)
        _specialDays.value = _specialDays.value.map { if (it.id == day.id) updated else it }
        persistSpecialDays()
        if (updated.reminderEnabled) scheduleReminder(updated) else cancelReminder(updated)
    }

    private fun startTicker() = viewModelScope.launch {
        while (true) {
            val duration = java.time.Duration.between(_relationship.value.startDate.atStartOfDay(), LocalDateTime.now())
            val totalSeconds = duration.seconds.coerceAtLeast(0)
            _liveTime.value = "${totalSeconds / 86_400} days  ${(totalSeconds / 3_600) % 24} hours  ${(totalSeconds / 60) % 60} minutes  ${totalSeconds % 60} seconds"
            delay(1000)
        }
    }

    private fun loadRelationship(): Relationship = runCatching { Relationship(prefs.getString("person1", "Madhu") ?: "Madhu", prefs.getString("person2", "Partner") ?: "Partner", LocalDate.parse(prefs.getString("startDate", "2025-05-27"), dateFormatter), prefs.getString("photo", null)) }.getOrDefault(Relationship())
    private fun persistRelationship() = prefs.edit().putString("person1", _relationship.value.person1).putString("person2", _relationship.value.person2).putString("startDate", _relationship.value.startDate.toString()).putString("photo", _relationship.value.partnerPhotoUri).apply()

    private fun loadMoments(): List<SpecialMoment> = runCatching { val a = JSONArray(prefs.getString("moments", "[]")); List(a.length()) { i -> a.getJSONObject(i).let { SpecialMoment(it.getLong("id"), it.getString("title"), LocalDate.parse(it.getString("date")), it.optString("note")) } } }.getOrDefault(emptyList())
    private fun persistMoments() = prefs.edit().putString("moments", JSONArray().apply { _moments.value.forEach { put(JSONObject().put("id", it.id).put("title", it.title).put("date", it.date.toString()).put("note", it.note)) } }.toString()).apply()

    private fun loadSpecialDays(): List<SpecialDay> = runCatching { val a = JSONArray(prefs.getString("specialDays", "[]")); List(a.length()) { i -> a.getJSONObject(i).let { SpecialDay(it.getLong("id"), it.getString("title"), LocalDate.parse(it.getString("date")), it.optString("note"), it.optBoolean("reminder", true)) } } }.getOrDefault(emptyList())
    private fun persistSpecialDays() = prefs.edit().putString("specialDays", JSONArray().apply { _specialDays.value.forEach { put(JSONObject().put("id", it.id).put("title", it.title).put("date", it.date.toString()).put("note", it.note).put("reminder", it.reminderEnabled)) } }.toString()).apply()

    private fun scheduleReminder(day: SpecialDay) {
        val alarmManager = getApplication<android.app.Application>().getSystemService(AlarmManager::class.java)
        val date = day.date.withYear(if (day.date.isBefore(LocalDate.now())) day.date.year + 1 else day.date.year)
        val millis = date.atTime(LocalTime.of(9, 0)).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val intent = Intent(getApplication(), ReminderReceiver::class.java).putExtra("title", day.title).putExtra("note", day.note)
        val pending = PendingIntent.getBroadcast(getApplication(), day.id.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pending)
    }

    private fun cancelReminder(day: SpecialDay) {
        val intent = Intent(getApplication(), ReminderReceiver::class.java)
        val pending = PendingIntent.getBroadcast(getApplication(), day.id.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        getApplication<android.app.Application>().getSystemService(AlarmManager::class.java).cancel(pending)
    }
}
