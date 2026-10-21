package com.karlitodev.novelize.sfx

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.json.JSONArray
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Progress states emitted during an SFX file download.
 */
sealed interface SfxDownloadState {
    data object Idle : SfxDownloadState
    data class Downloading(val fileName: String, val progressPercent: Int) : SfxDownloadState
    data class Done(val fileName: String, val file: File) : SfxDownloadState
    data class Failed(val fileName: String, val reason: String) : SfxDownloadState
    data class CatalogLoaded(val assets: List<GitHubAsset>) : SfxDownloadState
}

/**
 * Metadata for a single file asset from a GitHub release.
 */
data class GitHubAsset(
    val name: String,
    val downloadUrl: String,
    val sizeBytes: Long
)

/**
 * Fetches the SFX file list from the latest GitHub release and allows
 * downloading individual files into internal app storage.
 *
 * The user triggers downloads manually from the Settings screen.
 * Files are never bundled in the APK; they are fetched on demand.
 */
class SfxDownloadManager(private val context: Context) {

    private val sfxDir: File
        get() = File(context.filesDir, "sfx").also { it.mkdirs() }

    /**
     * Provides the list of SFX assets directly from SfxCatalog using raw GitHub content URLs.
     * 100% Kotlin with no external index.json required.
     */
    fun fetchAvailableSfx(): Flow<SfxDownloadState> = flow {
        try {
            val assets = SfxCatalog.items.map { item ->
                GitHubAsset(
                    name = item.fileName,
                    downloadUrl = "https://raw.githubusercontent.com/Badley08/NOVELIZE/main/SFX/${item.fileName}",
                    sizeBytes = 250_000L
                )
            }
            emit(SfxDownloadState.CatalogLoaded(assets))
        } catch (e: Exception) {
            emit(SfxDownloadState.Failed("catalog", e.localizedMessage ?: "Unknown error"))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Downloads a single SFX asset to internal storage, emitting progress updates.
     * If the file already exists and is non-empty, skips the download.
     */
    fun downloadSfx(asset: GitHubAsset): Flow<SfxDownloadState> = flow {
        val targetFile = File(sfxDir, asset.name)

        if (targetFile.exists() && targetFile.length() > 0L) {
            emit(SfxDownloadState.Done(asset.name, targetFile))
            return@flow
        }

        emit(SfxDownloadState.Downloading(asset.name, 0))

        try {
            val url = URL(asset.downloadUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 15_000
                readTimeout = 30_000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "Novelize-Android-App")
            }

            val contentLength = connection.contentLength
            val input = connection.inputStream
            val output = targetFile.outputStream()
            val buffer = ByteArray(8192)
            var totalRead = 0L
            var bytesRead: Int

            while (input.read(buffer).also { bytesRead = it } != -1) {
                output.write(buffer, 0, bytesRead)
                totalRead += bytesRead
                if (contentLength > 0) {
                    val percent = ((totalRead * 100) / contentLength).toInt()
                    emit(SfxDownloadState.Downloading(asset.name, percent))
                }
            }

            output.flush()
            output.close()
            input.close()

            emit(SfxDownloadState.Done(asset.name, targetFile))
        } catch (e: Exception) {
            if (targetFile.exists()) targetFile.delete()
            emit(SfxDownloadState.Failed(asset.name, e.localizedMessage ?: "Download failed"))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Returns true if the given SFX file has already been downloaded to internal storage.
     */
    fun isDownloaded(fileName: String): Boolean {
        val file = File(sfxDir, fileName)
        return file.exists() && file.length() > 0L
    }

    /**
     * Checks all catalog items against local storage.
     * Never re-downloads already downloaded files.
     * Fetches missing audio files from GitHub raw URL.
     * Returns the count of newly downloaded sound files.
     */
    suspend fun syncMissingSfx(): Int = kotlinx.coroutines.withContext(Dispatchers.IO) {
        var newCount = 0
        for (item in SfxCatalog.items) {
            val targetFile = File(sfxDir, item.fileName)
            // NEVER download the same file twice
            if (targetFile.exists() && targetFile.length() > 0L) {
                continue
            }

            try {
                val rawUrl = "https://raw.githubusercontent.com/Badley08/NOVELIZE/main/SFX/${item.fileName}"
                val connection = (URL(rawUrl).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 8_000
                    readTimeout = 12_000
                    instanceFollowRedirects = true
                    setRequestProperty("User-Agent", "Novelize-Android-App")
                }

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val tempFile = File(sfxDir, "${item.fileName}.tmp")
                    connection.inputStream.use { input ->
                        tempFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    if (tempFile.length() > 0L) {
                        if (targetFile.exists()) targetFile.delete()
                        tempFile.renameTo(targetFile)
                        newCount++
                    } else {
                        tempFile.delete()
                    }
                }
            } catch (_: Exception) {}
        }
        newCount
    }
}
