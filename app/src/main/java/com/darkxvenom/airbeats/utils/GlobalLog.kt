package com.darkxvenom.airbeats.utils

import android.os.Process
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

data class LogEntry(val time: Long, val level: Int, val tag: String?, val message: String)

object GlobalLog {
    private const val MAX_ENTRIES = 1000
    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs = _logs.asStateFlow()

    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    private val isStarted = AtomicBoolean(false)
    private val scope = CoroutineScope(Dispatchers.IO)

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

    fun startRealtimeLogcat() {
        if (!isStarted.compareAndSet(false, true)) return

        scope.launch {
            try {
                val pid = Process.myPid()
                val process = Runtime.getRuntime().exec(arrayOf("logcat", "-v", "time", "-b", "main", "-b", "crash", "*:V"))
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val l = line?.trim() ?: continue
                    if (l.isEmpty() || l.startsWith("--------- beginning")) continue
                    parseAndAppendLogcatLine(l, pid)
                }
            } catch (e: Exception) {
                append(Log.INFO, "GlobalLog", "Realtime logcat active (fallback mode)")
            }
        }
    }

    private fun parseAndAppendLogcatLine(line: String, myPid: Int) {
        try {
            val parts = line.split(" ", limit = 3)
            if (parts.size >= 3) {
                val header = parts[2]
                val colonIdx = header.indexOf(": ")
                if (colonIdx != -1) {
                    val tagPart = header.substring(0, colonIdx).trim()
                    val message = header.substring(colonIdx + 2)

                    val slashIdx = tagPart.indexOf('/')
                    if (slashIdx != -1) {
                        val levelChar = tagPart.substring(0, slashIdx).trim().firstOrNull() ?: 'I'
                        var tag = tagPart.substring(slashIdx + 1).trim()
                        val parenIdx = tag.indexOf('(')
                        if (parenIdx != -1) {
                            val pidStr = tag.substring(parenIdx + 1).replace(")", "").trim()
                            val linePid = pidStr.toIntOrNull()
                            if (linePid != null && linePid != myPid) {
                                return
                            }
                            tag = tag.substring(0, parenIdx).trim()
                        }

                        val level = when (levelChar) {
                            'V' -> Log.VERBOSE
                            'D' -> Log.DEBUG
                            'I' -> Log.INFO
                            'W' -> Log.WARN
                            'E' -> Log.ERROR
                            'F' -> Log.ASSERT
                            else -> Log.INFO
                        }

                        append(level, tag, message)
                        return
                    }
                }
            }
            append(Log.DEBUG, "System", line)
        } catch (_: Exception) {
            append(Log.DEBUG, "System", line)
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
class GlobalLogTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        try {
            val final = if (t != null) "$message\n$t" else message
            GlobalLog.append(priority, tag, final)
        } catch (_: Exception) {
            // swallow
        }
    }
}
