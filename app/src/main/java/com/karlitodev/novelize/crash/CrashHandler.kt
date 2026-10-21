package com.karlitodev.novelize.crash

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Global uncaught exception handler that intercepts fatal crashes,
 * writes detailed diagnostic information to crash.txt, and facilitates
 * direct sharing with developers via WhatsApp.
 */
object CrashHandler {

    private const val CRASH_FILE_NAME = "crash.txt"
    private var defaultHandler: Thread.UncaughtExceptionHandler? = null

    /**
     * Installs the uncaught exception handler.
     */
    fun init(context: Context) {
        if (defaultHandler != null) return
        val appContext = context.applicationContext
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                writeCrashReport(appContext, thread, throwable)
            } catch (_: Exception) {}

            // Delegate to system default handler to ensure proper termination
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    /**
     * Returns the File reference to crash.txt.
     */
    fun getCrashFile(context: Context): File {
        return File(context.filesDir, CRASH_FILE_NAME)
    }

    /**
     * Checks if a pending crash.txt report exists.
     */
    fun hasCrashReport(context: Context): Boolean {
        val file = getCrashFile(context)
        return file.exists() && file.length() > 0
    }

    /**
     * Reads the crash report content.
     */
    fun readCrashReport(context: Context): String? {
        val file = getCrashFile(context)
        return if (file.exists() && file.length() > 0) {
            file.readText()
        } else null
    }

    /**
     * Clears the crash.txt file after review or dispatch.
     */
    fun clearCrashReport(context: Context) {
        val file = getCrashFile(context)
        if (file.exists()) {
            file.delete()
        }
    }

    /**
     * Writes full diagnostic telemetry to crash.txt.
     */
    private fun writeCrashReport(context: Context, thread: Thread, throwable: Throwable) {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        val stackTrace = sw.toString()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val dateString = dateFormat.format(Date())

        val packageInfo = try {
            context.packageManager.getPackageInfo(context.packageName, 0)
        } catch (_: Exception) {
            null
        }
        val versionName = packageInfo?.versionName ?: "1.2"
        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo?.longVersionCode ?: 12
        } else {
            @Suppress("DEPRECATION")
            packageInfo?.versionCode ?: 12
        }

        val report = buildString {
            appendLine("=== NOVELIZE CRASH REPORT ===")
            appendLine("Timestamp: $dateString")
            appendLine("App Version: $versionName (code $versionCode)")
            appendLine("Android OS: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL} (${Build.PRODUCT})")
            appendLine("Hardware: ${Build.HARDWARE}")
            appendLine("Thread: ${thread.name} (id: ${thread.id})")
            appendLine("Exception: ${throwable.javaClass.name}")
            appendLine("Message: ${throwable.message ?: "No message"}")
            appendLine()
            appendLine("--- STACK TRACE ---")
            appendLine(stackTrace)
            appendLine("=== END OF REPORT ===")
        }

        val file = getCrashFile(context)
        file.writeText(report)
    }

    /**
     * Dispatches the crash.txt report to WhatsApp.
     */
    fun sendCrashReportViaWhatsApp(context: Context, crashLog: String) {
        val cleanSummary = if (crashLog.length > 3500) {
            crashLog.take(3500) + "\n...[TRUNCATED]"
        } else crashLog

        val message = "NoveLize Crash Report:\n\n$cleanSummary"

        // Attempt 1: Direct WhatsApp application intent
        try {
            val waIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                setPackage("com.whatsapp")
                putExtra(Intent.EXTRA_TEXT, message)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(waIntent)
            return
        } catch (_: Exception) {}

        // Attempt 2: WhatsApp Web link
        try {
            val webIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://api.whatsapp.com/send?text=" + Uri.encode(message))
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(webIntent)
            return
        } catch (_: Exception) {}

        // Attempt 3: Generic system share chooser
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "NoveLize Crash Report")
                putExtra(Intent.EXTRA_TEXT, crashLog)
            }
            val chooser = Intent.createChooser(shareIntent, "Envoyer le rapport de crash").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(chooser)
        } catch (_: Exception) {}
    }
}
