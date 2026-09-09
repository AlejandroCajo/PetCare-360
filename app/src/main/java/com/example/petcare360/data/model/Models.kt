package com.example.petcare360.data.model

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("access_token") val accessToken: String? = null,
    @SerializedName("token_type") val tokenType: String? = null,
    @SerializedName("expires_in") val expiresIn: Long? = null,
    @SerializedName("refresh_token") val refreshToken: String? = null,
    @SerializedName("user") val user: UserDto? = null
)

data class UserDto(
    @SerializedName("id") val id: String,
    @SerializedName("email") val email: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("user_metadata") val userMetadata: Map<String, Any>? = null
)

data class UserProfile(
    @SerializedName("id") val id: String,
    @SerializedName("email") val email: String,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("full_name") val fullName: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

data class PetEntity(
    @SerializedName("id") val id: String? = null,
    @SerializedName("owner_id") val ownerId: String? = null,
    @SerializedName("name") val name: String,
    @SerializedName("species") val species: String,
    @SerializedName("breed") val breed: String? = null,
    @SerializedName("birth_date") val birthDate: String? = null,
    @SerializedName("photo_url") val photoUrl: String? = null,
    @SerializedName("qr_code_id") val qrCodeId: String? = null,
    @SerializedName("show_owner_phone") val showOwnerPhone: Boolean = false,
    @SerializedName("show_owner_name") val showOwnerName: Boolean = true,
    @SerializedName("show_address") val showAddress: Boolean = false,
    @SerializedName("created_at") val createdAt: String? = null
)

data class MedicalRecordEntity(
    @SerializedName("id") val id: String? = null,
    @SerializedName("pet_id") val petId: String,
    @SerializedName("record_type") val recordType: String, // 'vacuna', 'cirugia', 'receta'
    @SerializedName("title") val title: String,
    @SerializedName("date") val date: String,
    @SerializedName("document_url") val documentUrl: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

data class WeightLogEntity(
    @SerializedName("id") val id: String? = null,
    @SerializedName("pet_id") val petId: String,
    @SerializedName("weight_kg") val weightKg: Double,
    @SerializedName("recorded_at") val recordedAt: String? = null
)

data class SosAlertEntity(
    @SerializedName("id") val id: String? = null,
    @SerializedName("pet_id") val petId: String,
    @SerializedName("status") val status: String = "active", // 'active', 'resolved'
    @SerializedName("created_at") val createdAt: String? = null
)

data class BusinessEntity(
    @SerializedName("id") val id: String? = null,
    @SerializedName("owner_id") val ownerId: String? = null,
    @SerializedName("name") val name: String,
    @SerializedName("category") val category: String, // 'vet', 'park', 'groomer'
    @SerializedName("avg_rating") val avgRating: Double = 0.0,
    @SerializedName("created_at") val createdAt: String? = null
)

data class ReviewEntity(
    @SerializedName("id") val id: String? = null,
    @SerializedName("user_id") val userId: String,
    @SerializedName("business_id") val businessId: String,
    @SerializedName("rating") val rating: Int,
    @SerializedName("comment") val comment: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

data class PostEntity(
    @SerializedName("id") val id: String? = null,
    @SerializedName("user_id") val userId: String? = null,
    @SerializedName("user_name") val userName: String? = null,
    @SerializedName("user_avatar") val userAvatar: String? = null,
    @SerializedName("pet_name") val petName: String? = null,
    @SerializedName("content") val content: String,
    @SerializedName("photo_url") val photoUrl: String? = null,
    @SerializedName("likes_count") val likesCount: Int = 0,
    @SerializedName("comments_count") val commentsCount: Int = 0,
    @SerializedName("created_at") val createdAt: String? = null
)
