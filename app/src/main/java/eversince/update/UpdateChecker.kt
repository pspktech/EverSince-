package eversince.update

import com.eversince.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(val version: String, val downloadUrl: String, val releaseUrl: String)

object UpdateChecker {
    private const val API = "https://api.github.com/repos/pspktech/EverSince-/releases/latest"

    suspend fun check(): Result<UpdateInfo?> = withContext(Dispatchers.IO) {
        runCatching {
            val connection = URL(API).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/vnd.github+json")
            connection.connectTimeout = 8_000
            connection.readTimeout = 8_000
            if (connection.responseCode !in 200..299) return@runCatching null
            val release = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
            val tag = release.optString("tag_name").removePrefix("v")
            val apk = release.optJSONArray("assets")?.let { assets -> (0 until assets.length()).asSequence().map { assets.getJSONObject(it) }.firstOrNull { it.optString("name").endsWith(".apk") } }
            if (tag.isBlank() || apk == null || !isNewer(tag, BuildConfig.VERSION_NAME)) null else UpdateInfo(tag, apk.getString("browser_download_url"), release.getString("html_url"))
        }
    }

    private fun isNewer(remote: String, current: String): Boolean {
        val r = remote.split('.', '-', '+').mapNotNull { it.toIntOrNull() }
        val c = current.split('.', '-', '+').mapNotNull { it.toIntOrNull() }
        val size = maxOf(r.size, c.size)
        val firstDifference = (0 until size).firstOrNull { (r.getOrNull(it) ?: 0) != (c.getOrNull(it) ?: 0) } ?: return false
        return (r.getOrNull(firstDifference) ?: 0) > (c.getOrNull(firstDifference) ?: 0)
    }
}
