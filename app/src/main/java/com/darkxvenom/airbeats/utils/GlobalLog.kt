package com.darkxvenom.airbeats.utils

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LogEntry(val time: Long, val level: Int, val tag: String?, val message: String)

object GlobalLog {
    private const val MAX_ENTRIES = 1000
    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs = _logs.asStateFlow()

    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    fun append(level: Int, tag: String?, message: String) {
        val entry = LogEntry(System.currentTimeMillis(), level, tag, message)
        synchronized(this) {
            val current = _logs.value
            _logs.value = (current + entry).takeLast(MAX_ENTRIES)
        }
    }

    fun clear() {
        synchronized(this) {
            _logs.value = emptyList()
        }
    }

    fun format(entry: LogEntry): String {
        val ts = timeFormat.format(Date(entry.time))
        val lvl = when (entry.level) {
            Log.VERBOSE -> "V"
            Log.DEBUG -> "D"
            Log.INFO -> "I"
            Log.WARN -> "W"
            Log.ERROR -> "E"
            else -> "?"
        }
        val tag = entry.tag ?: ""
        return "[$ts] $lvl/$tag: ${entry.message}"
    }
}

/** Timber Tree that forwards logs to GlobalLog */
class GlobalLogTree : Timber.DebugTree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        try {
            val final = if (t != null) "$message\n$t" else message
            GlobalLog.append(priority, tag, final)
        } catch (_: Exception) {
            // swallow
        }
    }
}
