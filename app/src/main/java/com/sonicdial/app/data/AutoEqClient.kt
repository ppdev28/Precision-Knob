package com.sonicdial.app.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/** One headphone result in the public AutoEq repository (https://github.com/jaakkopasanen/AutoEq). */
data class AutoEqEntry(
    val name: String,
    val source: String,
    val rig: String,
    /** URL-encoded path under `results/`, e.g. `oratory1990/over-ear/Sennheiser%20HD%20600`. */
    val path: String,
) {
    val parametricUrl: String
        get() = "$RawBase/results/$path/${path.substringAfterLast('/')}%20ParametricEQ.txt"

    companion object {
        const val RawBase = "https://raw.githubusercontent.com/jaakkopasanen/AutoEq/master"
        private val lineRegex = Regex("""^- \[(.*)]\(\./(.*)\) by (.*?)(?: on (.*))?$""")

        fun parseIndex(text: String): List<AutoEqEntry> = text.lineSequence().mapNotNull { line ->
            lineRegex.find(line)?.let { m ->
                AutoEqEntry(m.groupValues[1], m.groupValues[3], m.groupValues[4], m.groupValues[2])
            }
        }.toList()
    }
}

/** Downloads the AutoEq index and individual ParametricEQ files. Network is only used from here. */
class AutoEqClient(context: Context) {
    private val cache = File(context.filesDir, "autoeq_index.md")

    suspend fun loadIndex(forceRefresh: Boolean = false): List<AutoEqEntry> = withContext(Dispatchers.IO) {
        if (!forceRefresh && cache.exists()) {
            AutoEqEntry.parseIndex(cache.readText()).takeIf { it.isNotEmpty() }?.let { return@withContext it }
        }
        val text = download("${AutoEqEntry.RawBase}/results/INDEX.md")
        val entries = AutoEqEntry.parseIndex(text)
        check(entries.isNotEmpty()) { "Unexpected AutoEq index format" }
        cache.writeText(text)
        entries
    }

    suspend fun downloadProfile(entry: AutoEqEntry): String = withContext(Dispatchers.IO) { download(entry.parametricUrl) }

    private fun download(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = 15_000
            connection.readTimeout = 30_000
            check(connection.responseCode == 200) { "HTTP ${connection.responseCode}" }
            return connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }
}
