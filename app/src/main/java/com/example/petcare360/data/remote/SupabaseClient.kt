package com.example.petcare360.data.remote

import com.example.petcare360.data.model.AuthResponse
import com.example.petcare360.data.model.BusinessEntity
import com.example.petcare360.data.model.MedicalRecordEntity
import com.example.petcare360.data.model.PetEntity
import com.example.petcare360.data.model.SosAlertEntity
import com.example.petcare360.data.model.UserProfile
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class SupabaseClient(private val sessionManager: SessionManager) {

    private val gson = Gson()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private fun buildHeaders(requiresAuth: Boolean = false): Map<String, String> {
        val headers = mutableMapOf(
            "apikey" to SupabaseConfig.ANON_KEY,
            "Content-Type" to "application/json",
            "Prefer" to "return=representation"
        )
        val token = sessionManager.getAccessToken()
        if (requiresAuth && !token.isNullOrEmpty()) {
            headers["Authorization"] = "Bearer $token"
        } else {
            headers["Authorization"] = "Bearer ${SupabaseConfig.ANON_KEY}"
        }
        return headers
    }

    // ==========================================
    // AUTENTICACIÓN
    // ==========================================

    suspend fun signUp(
        email: String,
        pass: String,
        fullName: String,
        phone: String
    ): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val payload = mapOf(
                "email" to email,
                "password" to pass,
                "data" to mapOf(
                    "full_name" to fullName,
                    "phone" to phone
                )
            )
            val body = gson.toJson(payload).toRequestBody(jsonMediaType)

            val requestBuilder = Request.Builder()
                .url("${SupabaseConfig.AUTH_URL}/signup")
                .post(body)

            buildHeaders().forEach { (k, v) -> requestBuilder.addHeader(k, v) }

            val response = httpClient.newCall(requestBuilder.build()).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val authResponse = gson.fromJson(responseBody, AuthResponse::class.java)
                val userId = authResponse.user?.id
                sessionManager.saveSession(
                    token = authResponse.accessToken,
                    userId = userId,
                    email = email,
                    fullName = fullName
                )
                Result.success(authResponse)
            } else {
                Result.failure(Exception("Error al registrarse: $responseBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signIn(
        email: String,
        pass: String
    ): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val payload = mapOf(
                "email" to email,
                "password" to pass
            )
            val body = gson.toJson(payload).toRequestBody(jsonMediaType)

            val requestBuilder = Request.Builder()
                .url("${SupabaseConfig.AUTH_URL}/token?grant_type=password")
                .post(body)

            buildHeaders().forEach { (k, v) -> requestBuilder.addHeader(k, v) }

            val response = httpClient.newCall(requestBuilder.build()).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val authResponse = gson.fromJson(responseBody, AuthResponse::class.java)
                val metadata = authResponse.user?.userMetadata
                val fullName = metadata?.get("full_name") as? String ?: email.substringBefore("@")
                
                sessionManager.saveSession(
                    token = authResponse.accessToken,
                    userId = authResponse.user?.id,
                    email = email,
                    fullName = fullName
                )
                Result.success(authResponse)
            } else {
                Result.failure(Exception("Credenciales incorrectas o error: $responseBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        sessionManager.clearSession()
    }

    // ==========================================
    // MASCOTAS (pets)
    // ==========================================

    suspend fun getPets(): Result<List<PetEntity>> = withContext(Dispatchers.IO) {
        try {
            val requestBuilder = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/pets?select=*&order=created_at.desc")
                .get()

            buildHeaders(requiresAuth = true).forEach { (k, v) -> requestBuilder.addHeader(k, v) }

            val response = httpClient.newCall(requestBuilder.build()).execute()
            val body = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val listType = object : TypeToken<List<PetEntity>>() {}.type
                val pets: List<PetEntity> = gson.fromJson(body, listType)
                Result.success(pets)
            } else {
                Result.failure(Exception("Error al obtener mascotas: $body"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createPet(pet: PetEntity): Result<PetEntity> = withContext(Dispatchers.IO) {
        try {
            val petWithUser = pet.copy(ownerId = sessionManager.getUserId())
            val body = gson.toJson(petWithUser).toRequestBody(jsonMediaType)

            val requestBuilder = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/pets")
                .post(body)

            buildHeaders(requiresAuth = true).forEach { (k, v) -> requestBuilder.addHeader(k, v) }

            val response = httpClient.newCall(requestBuilder.build()).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val listType = object : TypeToken<List<PetEntity>>() {}.type
                val inserted: List<PetEntity> = gson.fromJson(responseBody, listType)
                if (inserted.isNotEmpty()) {
                    Result.success(inserted[0])
                } else {
                    Result.success(pet)
                }
            } else {
                Result.failure(Exception("Error al crear mascota: $responseBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // SOS ALERTS (Mascotas Perdidas)
    // ==========================================

    suspend fun getSosAlerts(): Result<List<SosAlertEntity>> = withContext(Dispatchers.IO) {
        try {
            val requestBuilder = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/sos_alerts?select=*&order=created_at.desc")
                .get()

            buildHeaders(requiresAuth = false).forEach { (k, v) -> requestBuilder.addHeader(k, v) }

            val response = httpClient.newCall(requestBuilder.build()).execute()
            val body = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val listType = object : TypeToken<List<SosAlertEntity>>() {}.type
                val alerts: List<SosAlertEntity> = gson.fromJson(body, listType)
                Result.success(alerts)
            } else {
                Result.failure(Exception("Error al obtener alertas: $body"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // SERVICIOS Y NEGOCIOS (businesses)
    // ==========================================

    suspend fun getBusinesses(category: String? = null): Result<List<BusinessEntity>> = withContext(Dispatchers.IO) {
        try {
            val url = if (category.isNullOrEmpty()) {
                "${SupabaseConfig.REST_URL}/businesses?select=*&order=avg_rating.desc"
            } else {
                "${SupabaseConfig.REST_URL}/businesses?category=eq.$category&select=*&order=avg_rating.desc"
            }

            val requestBuilder = Request.Builder()
                .url(url)
                .get()

            buildHeaders(requiresAuth = false).forEach { (k, v) -> requestBuilder.addHeader(k, v) }

            val response = httpClient.newCall(requestBuilder.build()).execute()
            val body = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val listType = object : TypeToken<List<BusinessEntity>>() {}.type
                val businesses: List<BusinessEntity> = gson.fromJson(body, listType)
                Result.success(businesses)
            } else {
                Result.failure(Exception("Error al obtener negocios: $body"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
