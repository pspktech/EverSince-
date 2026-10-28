package eversince.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eversince.data.model.Relationship
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime

class EverSinceViewModel : ViewModel() {
    private val _relationship = MutableStateFlow(Relationship())
    val relationship: StateFlow<Relationship> = _relationship.asStateFlow()

    private val _liveTime = MutableStateFlow("0d 0h 0m 0s")
    val liveTime: StateFlow<String> = _liveTime.asStateFlow()

    init {
        startTicker()
    }

    private fun startTicker() {
        viewModelScope.launch {
            while (true) {
                val now = LocalDateTime.now()
                val start = _relationship.value.startDate.atStartOfDay()
                val duration = Duration.between(start, now)

                val days = duration.toDays()
                val hours = duration.toHours() % 24
                val minutes = duration.toMinutes() % 60
                val seconds = duration.seconds % 60

                _liveTime.value = "${days}d ${hours}h ${minutes}m ${seconds}s"
                delay(1000) 
            }
        }
    }
}
