package com.example.game.crash

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

data class Breadcrumb(
    val timestamp: Long = System.currentTimeMillis(),
    val category: String,
    val message: String,
    val details: Map<String, String> = emptyMap()
)

object ErrorManager {

    private const val TAG = "ErrorManager"
    private const val MAX_BREADCRUMBS = 50
    private const val CRASH_LOG_FILE = "twenty_nine_last_crash.json"

    private val breadcrumbs = ConcurrentLinkedQueue<Breadcrumb>()
    private var isInitialized = false
    private var defaultExceptionHandler: Thread.UncaughtExceptionHandler? = null
    private var appContext: Context? = null

    fun initialize(context: Context) {
        if (isInitialized) return
        appContext = context.applicationContext
        isInitialized = true

        defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            handleFatalException(thread, throwable)
            defaultExceptionHandler?.uncaughtException(thread, throwable)
        }

        recordBreadcrumb("AppLifecycle", "ErrorManager initialized")
    }

    fun recordBreadcrumb(category: String, message: String, details: Map<String, String> = emptyMap()) {
        val entry = Breadcrumb(
            category = category,
            message = message,
            details = details
        )
        breadcrumbs.add(entry)
        while (breadcrumbs.size > MAX_BREADCRUMBS) {
            breadcrumbs.poll()
        }
        Log.d(TAG, "[$category] $message ${if (details.isNotEmpty()) details else ""}")
    }

    inline fun <T> runSafely(tag: String, fallback: T, action: () -> T): T {
        return try {
            action()
        } catch (t: Throwable) {
            recordError(tag, "Recovered non-fatal error: ${t.message}", t)
            fallback
        }
    }

    fun recordError(category: String, message: String, throwable: Throwable? = null) {
        val details = mutableMapOf<String, String>()
        if (throwable != null) {
            details["exception"] = throwable.javaClass.simpleName
            details["message"] = throwable.message ?: "no message"
            details["stack"] = throwable.stackTraceToString().take(500)
        }
        recordBreadcrumb("ERROR_$category", message, details)
        Log.e(TAG, "[$category] $message", throwable)
    }

    private fun handleFatalException(thread: Thread, throwable: Throwable) {
        try {
            val context = appContext ?: return
            val crashReport = JSONObject().apply {
                put("timestamp", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()))
                put("threadName", thread.name)
                put("exception", throwable.javaClass.name)
                put("message", throwable.message ?: "Unknown fatal error")
                put("stackTrace", throwable.stackTraceToString())

                val breadcrumbArray = JSONArray()
                breadcrumbs.forEach { b ->
                    val bObj = JSONObject().apply {
                        put("time", b.timestamp)
                        put("category", b.category)
                        put("message", b.message)
                    }
                    breadcrumbArray.put(bObj)
                }
                put("breadcrumbs", breadcrumbArray)
            }

            val crashFile = File(context.filesDir, CRASH_LOG_FILE)
            crashFile.writeText(crashReport.toString(2))
            Log.e(TAG, "Fatal crash report successfully written to ${crashFile.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed writing crash report: ${e.message}")
        }
    }

    fun getLastCrashReport(): String? {
        return try {
            val file = File(appContext?.filesDir, CRASH_LOG_FILE)
            if (file.exists()) file.readText() else null
        } catch (_: Exception) {
            null
        }
    }

    fun clearLastCrashReport() {
        try {
            val file = File(appContext?.filesDir, CRASH_LOG_FILE)
            if (file.exists()) file.delete()
        } catch (_: Exception) {}
    }
}
