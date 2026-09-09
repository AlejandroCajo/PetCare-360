package com.example.petcare360.data.remote

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object CloudinaryConfig {
    const val CLOUD_NAME = "lucocn0h" // del URL cloudinary://764794979748868:VFTnt-fK1wfankNf6TETQQNq-Mw@lucocn0h
    const val API_KEY = "764794979748868"
    const val API_SECRET = "VFTnt-fK1wfankNf6TETQQNq-Mw"
}

class CloudinaryManager(private val context: Context) {

    init {
        try {
            val config = mapOf(
                "cloud_name" to CloudinaryConfig.CLOUD_NAME,
                "api_key" to CloudinaryConfig.API_KEY,
                "api_secret" to CloudinaryConfig.API_SECRET,
                "secure" to true
            )
            MediaManager.init(context.applicationContext, config)
        } catch (_: IllegalStateException) {
            // MediaManager ya fue inicializado previamente
        }
    }

    suspend fun uploadImage(imageUri: Uri): Result<String> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            kotlinx.coroutines.withTimeout(30000L) {
                suspendCancellableCoroutine { continuation ->
                    try {
                        val inputStream = context.contentResolver.openInputStream(imageUri)
                        val tempFile = java.io.File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")

                        if (inputStream != null) {
                            tempFile.outputStream().use { outputStream ->
                                inputStream.copyTo(outputStream)
                            }
                            inputStream.close()
                        }

                        val fileToUpload = if (tempFile.exists() && tempFile.length() > 0) tempFile.absolutePath else imageUri.toString()

                        MediaManager.get().upload(fileToUpload)
                            .option("folder", "petcare360")
                            .callback(object : UploadCallback {
                                override fun onStart(requestId: String) {}

                                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                                    try { if (tempFile.exists()) tempFile.delete() } catch (_: Exception) {}

                                    val secureUrl = resultData["secure_url"] as? String
                                        ?: resultData["url"] as? String
                                        ?: ""
                                    if (continuation.isActive) {
                                        continuation.resume(Result.success(secureUrl))
                                    }
                                }

                                override fun onError(requestId: String, error: ErrorInfo) {
                                    try { if (tempFile.exists()) tempFile.delete() } catch (_: Exception) {}
                                    if (continuation.isActive) {
                                        continuation.resume(Result.failure(Exception("Cloudinary: ${error.description}")))
                                    }
                                }

                                override fun onReschedule(requestId: String, error: ErrorInfo) {
                                    try { if (tempFile.exists()) tempFile.delete() } catch (_: Exception) {}
                                    if (continuation.isActive) {
                                        continuation.resume(Result.failure(Exception("Subida reprogramada: ${error.description}")))
                                    }
                                }
                            })
                            .dispatch()
                    } catch (e: Exception) {
                        if (continuation.isActive) {
                            continuation.resume(Result.failure(e))
                        }
                    }
                }
            }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Result.failure(Exception("Tiempo de espera agotado al subir imagen a Cloudinary (30s)"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
