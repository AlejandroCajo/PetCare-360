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
        if (!token.isNullOrEmpty()) {
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

    fun getCurrentUserId(): String? {
        return sessionManager.getUserId()
    }

    suspend fun updateUserAvatar(avatarUrl: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = sessionManager.getUserId() ?: throw Exception("No user ID found")
            val payload = mapOf("avatar_url" to avatarUrl)
            val body = gson.toJson(payload).toRequestBody(jsonMediaType)

            val requestBuilder = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/users?id=eq.$userId")
                .patch(body)

            buildHeaders(requiresAuth = true).forEach { (k, v) -> requestBuilder.addHeader(k, v) }

            val response = httpClient.newCall(requestBuilder.build()).execute()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val err = response.body?.string()
                Result.failure(Exception("Error al actualizar avatar: $err"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val userId = sessionManager.getUserId() ?: throw Exception("No user ID found")
            val requestBuilder = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/users?id=eq.$userId")
                .get()

            buildHeaders(requiresAuth = true).forEach { (k, v) -> requestBuilder.addHeader(k, v) }

            val response = httpClient.newCall(requestBuilder.build()).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val typeToken = object : TypeToken<List<UserProfile>>() {}.type
                val profiles: List<UserProfile> = gson.fromJson(responseBody, typeToken)
                if (profiles.isNotEmpty()) {
                    Result.success(profiles.first())
                } else {
                    Result.failure(Exception("Usuario no encontrado en public.users"))
                }
            } else {
                Result.failure(Exception("Error al obtener perfil: $responseBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // MASCOTAS (pets)
    // ==========================================

    suspend fun getPets(): Result<List<PetEntity>> = withContext(Dispatchers.IO) {
        try {
            val userId = sessionManager.getUserId() ?: return@withContext Result.failure(Exception("No user logged in"))
            val requestBuilder = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/pets?owner_id=eq.$userId&select=*&order=created_at.desc")
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
                .url("${SupabaseConfig.REST_URL}/sos_alerts?select=*,pets(name,breed,photo_url)&order=created_at.desc")
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
    suspend fun createSosAlert(alert: SosAlertEntity): Result<SosAlertEntity> = withContext(Dispatchers.IO) {
        try {
            val alertWithUser = alert.copy(userId = sessionManager.getUserId())
            val body = gson.toJson(alertWithUser).toRequestBody(jsonMediaType)
            val requestBuilder = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/sos_alerts")
                .post(body)
            buildHeaders(requiresAuth = true).forEach { (k, v) -> requestBuilder.addHeader(k, v) }
            val response = httpClient.newCall(requestBuilder.build()).execute()
            val responseBody = response.body?.string() ?: ""
            if (response.isSuccessful) {
                val listType = object : TypeToken<List<SosAlertEntity>>() {}.type
                val inserted: List<SosAlertEntity> = gson.fromJson(responseBody, listType)
                if (inserted.isNotEmpty()) Result.success(inserted[0]) else Result.success(alertWithUser)
            } else {
                Result.failure(Exception("Error al crear alerta: $responseBody"))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun updateSosAlertStatus(id: String, status: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val payload = mapOf("status" to status)
            val body = gson.toJson(payload).toRequestBody(jsonMediaType)
            val requestBuilder = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/sos_alerts?id=eq.$id")
                .patch(body)
            buildHeaders(requiresAuth = true).forEach { (k, v) -> requestBuilder.addHeader(k, v) }
            val response = httpClient.newCall(requestBuilder.build()).execute()
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Error al actualizar alerta: ${response.body?.string()}"))
        } catch (e: Exception) { Result.failure(e) }
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

    suspend fun getBusinessServices(businessId: String): Result<List<com.example.petcare360.data.model.BusinessServiceEntity>> = withContext(Dispatchers.IO) {
        try {
            val url = "${SupabaseConfig.REST_URL}/business_services?business_id=eq.$businessId&select=*&order=created_at.desc"
            val requestBuilder = Request.Builder().url(url).get()
            buildHeaders(requiresAuth = false).forEach { (k, v) -> requestBuilder.addHeader(k, v) }

            val response = httpClient.newCall(requestBuilder.build()).execute()
            val body = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val listType = object : TypeToken<List<com.example.petcare360.data.model.BusinessServiceEntity>>() {}.type
                val services: List<com.example.petcare360.data.model.BusinessServiceEntity> = gson.fromJson(body, listType)
                Result.success(services)
            } else {
                Result.failure(Exception("Error al obtener servicios: $body"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBusinessProducts(businessId: String): Result<List<com.example.petcare360.data.model.BusinessProductEntity>> = withContext(Dispatchers.IO) {
        try {
            val url = "${SupabaseConfig.REST_URL}/business_products?business_id=eq.$businessId&select=*&order=created_at.desc"
            val requestBuilder = Request.Builder().url(url).get()
            buildHeaders(requiresAuth = false).forEach { (k, v) -> requestBuilder.addHeader(k, v) }

            val response = httpClient.newCall(requestBuilder.build()).execute()
            val body = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val listType = object : TypeToken<List<com.example.petcare360.data.model.BusinessProductEntity>>() {}.type
                val products: List<com.example.petcare360.data.model.BusinessProductEntity> = gson.fromJson(body, listType)
                Result.success(products)
            } else {
                Result.failure(Exception("Error al obtener productos: $body"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // COMUNIDAD (posts) - Paginación de 10 en 10
    // ==========================================

    suspend fun getPosts(limit: Int = 10, offset: Int = 0): Result<List<com.example.petcare360.data.model.PostEntity>> = withContext(Dispatchers.IO) {
        try {
            val url = "${SupabaseConfig.REST_URL}/vw_posts_with_avatars?select=*&order=created_at.desc&limit=$limit&offset=$offset"
            val requestBuilder = Request.Builder()
                .url(url)
                .get()

            buildHeaders(requiresAuth = false).forEach { (k, v) -> requestBuilder.addHeader(k, v) }

            val response = httpClient.newCall(requestBuilder.build()).execute()
            val body = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val listType = object : TypeToken<List<com.example.petcare360.data.model.PostEntity>>() {}.type
                val posts: List<com.example.petcare360.data.model.PostEntity> = gson.fromJson(body, listType)
                Result.success(posts)
            } else {
                Result.failure(Exception("Error al obtener posts: $body"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createPost(post: com.example.petcare360.data.model.PostEntity): Result<com.example.petcare360.data.model.PostEntity> = withContext(Dispatchers.IO) {
        try {
            val postData = mutableMapOf<String, Any?>()
            postData["user_id"] = sessionManager.getUserId()
            postData["user_name"] = sessionManager.getUserName() ?: post.userName ?: "Usuario"
            postData["user_avatar"] = post.userAvatar
            postData["pet_name"] = post.petName
            postData["pet_id"] = post.petId
            postData["content"] = post.content
            postData["photo_url"] = post.photoUrl
            
            val body = gson.toJson(postData).toRequestBody(jsonMediaType)

            val requestBuilder = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/posts")
                .post(body)

            buildHeaders(requiresAuth = true).forEach { (k, v) -> requestBuilder.addHeader(k, v) }

            val response = httpClient.newCall(requestBuilder.build()).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val listType = object : TypeToken<List<com.example.petcare360.data.model.PostEntity>>() {}.type
                val inserted: List<com.example.petcare360.data.model.PostEntity> = gson.fromJson(responseBody, listType)
                if (inserted.isNotEmpty()) {
                    Result.success(inserted[0])
                } else {
                    Result.success(post)
                }
            } else {
                Result.failure(Exception("Error al crear post: $responseBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // HISTORIAL MÉDICO Y CONSULTAS (medical_records)
    // ==========================================

    suspend fun getMedicalRecords(petId: String? = null): Result<List<com.example.petcare360.data.model.MedicalRecordEntity>> = withContext(Dispatchers.IO) {
        try {
            val url = if (petId != null) {
                "${SupabaseConfig.REST_URL}/medical_records?pet_id=eq.$petId&select=*&order=date.desc"
            } else {
                "${SupabaseConfig.REST_URL}/medical_records?select=*&order=date.desc"
            }

            val requestBuilder = Request.Builder()
                .url(url)
                .get()

            buildHeaders(requiresAuth = true).forEach { (k, v) -> requestBuilder.addHeader(k, v) }

            val response = httpClient.newCall(requestBuilder.build()).execute()
            val body = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val listType = object : TypeToken<List<com.example.petcare360.data.model.MedicalRecordEntity>>() {}.type
                val records: List<com.example.petcare360.data.model.MedicalRecordEntity> = gson.fromJson(body, listType)
                Result.success(records)
            } else {
                Result.failure(Exception("Error al obtener registros médicos: $body"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createMedicalRecord(record: com.example.petcare360.data.model.MedicalRecordEntity): Result<com.example.petcare360.data.model.MedicalRecordEntity> = withContext(Dispatchers.IO) {
        try {
            val body = gson.toJson(record).toRequestBody(jsonMediaType)
            val requestBuilder = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/medical_records")
                .post(body)

            buildHeaders(requiresAuth = true).forEach { (k, v) -> requestBuilder.addHeader(k, v) }

            val response = httpClient.newCall(requestBuilder.build()).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val listType = object : TypeToken<List<com.example.petcare360.data.model.MedicalRecordEntity>>() {}.type
                val inserted: List<com.example.petcare360.data.model.MedicalRecordEntity> = gson.fromJson(responseBody, listType)
                if (inserted.isNotEmpty()) {
                    Result.success(inserted[0])
                } else {
                    Result.success(record)
                }
            } else {
                Result.failure(Exception("Error al crear registro médico: $responseBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // CITAS Y ORDENES (appointments & orders)
    // ==========================================

    suspend fun getAppointments(): Result<List<com.example.petcare360.data.model.AppointmentEntity>> = withContext(Dispatchers.IO) {
        try {
            val userId = sessionManager.getUserId() ?: return@withContext Result.failure(Exception("No user logged in"))
            val requestBuilder = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/appointments?user_id=eq.$userId&order=appointment_date.desc")
                .get()

            buildHeaders(requiresAuth = true).forEach { (k, v) -> requestBuilder.addHeader(k, v) }
            val response = httpClient.newCall(requestBuilder.build()).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val type = object : TypeToken<List<com.example.petcare360.data.model.AppointmentEntity>>() {}.type
                val items: List<com.example.petcare360.data.model.AppointmentEntity> = gson.fromJson(responseBody, type)
                Result.success(items)
            } else {
                Result.failure(Exception("Error fetching appointments"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOrders(): Result<List<com.example.petcare360.data.model.OrderEntity>> = withContext(Dispatchers.IO) {
        try {
            val userId = sessionManager.getUserId() ?: return@withContext Result.failure(Exception("No user logged in"))
            val requestBuilder = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/orders?user_id=eq.$userId&order=created_at.desc")
                .get()

            buildHeaders(requiresAuth = true).forEach { (k, v) -> requestBuilder.addHeader(k, v) }
            val response = httpClient.newCall(requestBuilder.build()).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val type = object : TypeToken<List<com.example.petcare360.data.model.OrderEntity>>() {}.type
                val items: List<com.example.petcare360.data.model.OrderEntity> = gson.fromJson(responseBody, type)
                Result.success(items)
            } else {
                Result.failure(Exception("Error fetching orders"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createAppointment(appointment: com.example.petcare360.data.model.AppointmentEntity): Result<com.example.petcare360.data.model.AppointmentEntity> = withContext(Dispatchers.IO) {
        try {
            val appointmentWithUser = appointment.copy(userId = sessionManager.getUserId())
            val body = gson.toJson(appointmentWithUser).toRequestBody(jsonMediaType)

            val requestBuilder = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/appointments")
                .post(body)

            buildHeaders(requiresAuth = true).forEach { (k, v) -> requestBuilder.addHeader(k, v) }

            val response = httpClient.newCall(requestBuilder.build()).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val listType = object : TypeToken<List<com.example.petcare360.data.model.AppointmentEntity>>() {}.type
                val inserted: List<com.example.petcare360.data.model.AppointmentEntity> = gson.fromJson(responseBody, listType)
                if (inserted.isNotEmpty()) {
                    Result.success(inserted[0])
                } else {
                    Result.success(appointmentWithUser)
                }
            } else {
                Result.failure(Exception("Error al crear cita: $responseBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createOrder(order: com.example.petcare360.data.model.OrderEntity): Result<com.example.petcare360.data.model.OrderEntity> = withContext(Dispatchers.IO) {
        try {
            val orderWithUser = order.copy(userId = sessionManager.getUserId())
            val body = gson.toJson(orderWithUser).toRequestBody(jsonMediaType)

            val requestBuilder = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/orders")
                .post(body)

            buildHeaders(requiresAuth = true).forEach { (k, v) -> requestBuilder.addHeader(k, v) }

            val response = httpClient.newCall(requestBuilder.build()).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val listType = object : TypeToken<List<com.example.petcare360.data.model.OrderEntity>>() {}.type
                val inserted: List<com.example.petcare360.data.model.OrderEntity> = gson.fromJson(responseBody, listType)
                if (inserted.isNotEmpty()) {
                    Result.success(inserted[0])
                } else {
                    Result.success(orderWithUser)
                }
            } else {
                Result.failure(Exception("Error al crear orden: $responseBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createOrderItem(item: com.example.petcare360.data.model.OrderItemEntity): Result<com.example.petcare360.data.model.OrderItemEntity> = withContext(Dispatchers.IO) {
        try {
            val body = gson.toJson(item).toRequestBody(jsonMediaType)

            val requestBuilder = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/order_items")
                .post(body)

            buildHeaders(requiresAuth = true).forEach { (k, v) -> requestBuilder.addHeader(k, v) }

            val response = httpClient.newCall(requestBuilder.build()).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val listType = object : TypeToken<List<com.example.petcare360.data.model.OrderItemEntity>>() {}.type
                val inserted: List<com.example.petcare360.data.model.OrderItemEntity> = gson.fromJson(responseBody, listType)
                if (inserted.isNotEmpty()) {
                    Result.success(inserted[0])
                } else {
                    Result.success(item)
                }
            } else {
                Result.failure(Exception("Error al añadir item a la orden: $responseBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePost(postId: String, newContent: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val payload = mapOf("content" to newContent)
            val body = gson.toJson(payload).toRequestBody(jsonMediaType)
            val requestBuilder = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/posts?id=eq.$postId")
                .patch(body)
            buildHeaders(requiresAuth = true).forEach { (k, v) -> requestBuilder.addHeader(k, v) }
            val response = httpClient.newCall(requestBuilder.build()).execute()
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Error al actualizar post: ${response.body?.string()}"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun deletePost(postId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val requestBuilder = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/posts?id=eq.$postId")
                .delete()
            buildHeaders(requiresAuth = true).forEach { (k, v) -> requestBuilder.addHeader(k, v) }
            val response = httpClient.newCall(requestBuilder.build()).execute()
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Error al eliminar post: ${response.body?.string()}"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun deletePet(petId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val requestBuilder = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/pets?id=eq.$petId")
                .delete()
            buildHeaders(requiresAuth = true).forEach { (k, v) -> requestBuilder.addHeader(k, v) }
            val response = httpClient.newCall(requestBuilder.build()).execute()
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Error al eliminar mascota: ${response.body?.string()}"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun deleteMedicalRecord(recordId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val requestBuilder = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/medical_records?id=eq.$recordId")
                .delete()
            buildHeaders(requiresAuth = true).forEach { (k, v) -> requestBuilder.addHeader(k, v) }
            val response = httpClient.newCall(requestBuilder.build()).execute()
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Error al eliminar historial: ${response.body?.string()}"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun createSosSighting(alertId: String, location: String, notes: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val sightingMap = mapOf(
                "sos_alert_id" to alertId,
                "location" to location,
                "notes" to notes,
                "user_id" to getCurrentUserId()
            )
            val json = gson.toJson(sightingMap)
            val requestBody = json.toRequestBody("application/json".toMediaType())
            
            val requestBuilder = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/sos_sightings")
                .post(requestBody)

            buildHeaders(requiresAuth = true).forEach { (k, v) -> requestBuilder.addHeader(k, v) }

            val response = httpClient.newCall(requestBuilder.build()).execute()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to create SOS sighting: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
