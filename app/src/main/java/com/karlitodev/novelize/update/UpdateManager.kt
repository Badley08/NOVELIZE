package com.karlitodev.novelize.update

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Metadata descriptor for a release published on GitHub.
 */
data class GithubRelease(
    val tagName: String,
    val name: String,
    val body: String,
    val apkDownloadUrl: String,
    val htmlUrl: String,
    val publishedAt: String
)

/**
 * State representing GitHub release detection, download progress, and installation.
 */
sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data class UpToDate(val currentVersion: String) : UpdateState
    data class UpdateAvailable(val release: GithubRelease, val currentVersion: String) : UpdateState
    data class Downloading(val progressPercent: Int, val release: GithubRelease) : UpdateState
    data class ReadyToInstall(val apkFile: File) : UpdateState
    data class Error(val message: String) : UpdateState
}

/**
 * Manager handling automated update detection, background download, and APK installation
 * directly from GitHub Releases without requiring Google Play Store services.
 */
class UpdateManager(
    private val context: Context,
    private val repoOwner: String = "Badley08",
    private val repoName: String = "NOVELIZE",
    val currentVersion: String = "1.0.0"
) {

    private val _state = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val state: StateFlow<UpdateState> = _state.asStateFlow()

    /**
     * Checks GitHub Releases API for the latest published tag and APK asset.
     */
    suspend fun checkForUpdates(): UpdateState = withContext(Dispatchers.IO) {
        _state.value = UpdateState.Checking
        try {
            val endpoint = "https://api.github.com/repos/$repoOwner/$repoName/releases/latest"
            val url = URL(endpoint)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 10000
                readTimeout = 10000
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "NoveLize-Android-App")
            }

            if (connection.responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                val state = UpdateState.UpToDate(currentVersion)
                _state.value = state
                return@withContext state
            }

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                val state = UpdateState.Error("GitHub API HTTP ${connection.responseCode}")
                _state.value = state
                return@withContext state
            }

            val jsonText = connection.inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(jsonText)

            val tagName = root.optString("tag_name", "").trim()
            val releaseTitle = root.optString("name", tagName)
            val body = root.optString("body", "Bug fixes and performance improvements.")
            val htmlUrl = root.optString("html_url", "")
            val publishedAt = root.optString("published_at", "")

            var apkUrl: String? = null
            val assets = root.optJSONArray("assets")
            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val assetName = asset.optString("name", "").lowercase()
                    if (assetName.endsWith(".apk")) {
                        apkUrl = asset.optString("browser_download_url", null)
                        break
                    }
                }
            }

            if (apkUrl == null) {
                apkUrl = htmlUrl
            }

            val release = GithubRelease(
                tagName = tagName,
                name = releaseTitle,
                body = body,
                apkDownloadUrl = apkUrl,
                htmlUrl = htmlUrl,
                publishedAt = publishedAt
            )

            val hasNewVersion = isVersionGreater(tagName, currentVersion)

            val nextState = if (hasNewVersion) {
                UpdateState.UpdateAvailable(release, currentVersion)
            } else {
                UpdateState.UpToDate(currentVersion)
            }

            _state.value = nextState
            nextState
        } catch (e: Exception) {
            val errorState = UpdateState.Error("Network error: ${e.localizedMessage ?: "Unknown"}")
            _state.value = errorState
            errorState
        }
    }

    /**
     * Downloads the APK file from GitHub release assets and notifies on progress.
     */
    suspend fun downloadAndInstall(release: GithubRelease) = withContext(Dispatchers.IO) {
        if (!release.apkDownloadUrl.endsWith(".apk")) {
            // If asset is not a direct APK, open browser page
            openReleasePage(release.htmlUrl)
            return@withContext
        }

        try {
            _state.value = UpdateState.Downloading(progressPercent = 0, release = release)

            val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir
            val targetFile = File(downloadDir, "NoveLize-${release.tagName}.apk")

            if (targetFile.exists()) {
                targetFile.delete()
            }

            val url = URL(release.apkDownloadUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 15000
                readTimeout = 30000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "NoveLize-Android-App")
            }

            val fileLength = connection.contentLength
            val input = connection.inputStream
            val output = FileOutputStream(targetFile)

            val data = ByteArray(8192)
            var total: Long = 0
            var count: Int

            while (input.read(data).also { count = it } != -1) {
                total += count
                output.write(data, 0, count)
                if (fileLength > 0) {
                    val percent = ((total * 100) / fileLength).toInt()
                    _state.value = UpdateState.Downloading(progressPercent = percent, release = release)
                }
            }

            output.flush()
            output.close()
            input.close()

            _state.value = UpdateState.ReadyToInstall(targetFile)
            triggerApkInstall(targetFile)
        } catch (e: Exception) {
            _state.value = UpdateState.Error("Download failed: ${e.localizedMessage}")
        }
    }

    /**
     * Launches Android Package Installer using FileProvider.
     */
    fun triggerApkInstall(apkFile: File) {
        try {
            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                apkFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            context.startActivity(intent)
        } catch (e: Exception) {
            _state.value = UpdateState.Error("Cannot start package installer: ${e.localizedMessage}")
        }
    }

    private fun openReleasePage(urlStr: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(urlStr)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    /**
     * Semantic version comparator. Strips leading 'v' and compares numeric components.
     */
    private fun isVersionGreater(remoteTag: String, localVersion: String): Boolean {
        val cleanRemote = remoteTag.trim().removePrefix("v").removePrefix("V")
        val cleanLocal = localVersion.trim().removePrefix("v").removePrefix("V")

        val remoteParts = cleanRemote.split(".").mapNotNull { it.toIntOrNull() }
        val localParts = cleanLocal.split(".").mapNotNull { it.toIntOrNull() }

        val maxLen = maxOf(remoteParts.size, localParts.size)
        for (i in 0 until maxLen) {
            val r = remoteParts.getOrElse(i) { 0 }
            val l = localParts.getOrElse(i) { 0 }
            if (r > l) return true
            if (r < l) return false
        }
        return false
    }
}
