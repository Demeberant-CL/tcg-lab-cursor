package com.tcglab.app.data

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AppLogger {
    private const val FILE_NAME = "tcg-lab-diagnostic.log"
    private const val MAX_BYTES = 512 * 1024L
    private val lock = Any()

    private fun file(context: Context): File =
        File(context.applicationContext.filesDir, FILE_NAME)

    fun info(context: Context, message: String) = write(context, "INFO", message)
    fun warn(context: Context, message: String) = write(context, "WARN", message)

    fun error(context: Context, message: String, throwable: Throwable? = null) {
        val details = throwable?.let { "
${it.stackTraceToString()}" }.orEmpty()
        write(context, "ERROR", message + details)
    }

    private fun write(context: Context, level: String, message: String) {
        runCatching {
            synchronized(lock) {
                val target = file(context)
                target.appendText("${timestamp()} [$level] $message
")
                if (target.length() > MAX_BYTES) {
                    val bytes = target.readBytes()
                    val start = (bytes.size - MAX_BYTES.toInt() / 2).coerceAtLeast(0)
                    target.writeBytes(bytes.copyOfRange(start, bytes.size))
                }
            }
        }
    }

    fun export(context: Context): File {
        val target = file(context)
        if (!target.exists()) {
            target.writeText("${timestamp()} [INFO] No hay errores registrados.
")
        }
        return target
    }

    private fun timestamp(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date())
}
