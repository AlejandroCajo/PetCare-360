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
    @SerializedName("record_type") val recordType: String, // 'vacuna', 'medicina', 'visita'
    @SerializedName("title") val title: String,
    @SerializedName("date") val date: String,
    @SerializedName("status") val status: String? = null, // for vaccines: 'done' or 'pending'
    @SerializedName("description") val description: String? = null, // for meds
    @SerializedName("next_date") val nextDate: String? = null, // for meds next dose
    @SerializedName("vet_name") val vetName: String? = null, // for visits
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
    @SerializedName("user_id") val userId: String? = null,
    @SerializedName("last_seen_location") val lastSeenLocation: String? = null,
    @SerializedName("loss_location") val lossLocation: Any? = "POINT(0 0)", // Dummy geometry to satisfy constraint
    @SerializedName("photo_url") val photoUrl: String? = null,
    @SerializedName("lost_date") val lostDate: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("status") val status: String = "active", // 'active', 'resolved'
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("pets") val petDetails: SosPetDetails? = null
)

data class SosPetDetails(
    @SerializedName("name") val name: String? = null,
    @SerializedName("breed") val breed: String? = null,
    @SerializedName("photo_url") val photoUrl: String? = null
)

data class BusinessEntity(
    @SerializedName("id") val id: String? = null,
    @SerializedName("owner_id") val ownerId: String? = null,
    @SerializedName("name") val name: String,
    @SerializedName("category") val category: String, // 'vet', 'groomer', 'walker', 'daycare', 'shop'
    @SerializedName("avg_rating") val avgRating: Double = 0.0,
    @SerializedName("address") val address: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("price_info") val priceInfo: String? = null,
    @SerializedName("photo_url") val photoUrl: String? = null,
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
    @SerializedName("pet_id") val petId: String? = null,
    @SerializedName("pet_avatar") val petAvatar: String? = null,
    @SerializedName("content") val content: String,
    @SerializedName("photo_url") val photoUrl: String? = null,
    @SerializedName("likes_count") val likesCount: Int = 0,
    @SerializedName("comments_count") val commentsCount: Int = 0,
    @SerializedName("created_at") val createdAt: String? = null
)

data class BusinessServiceEntity(
    @SerializedName("id") val id: String? = null,
    @SerializedName("business_id") val businessId: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("price") val price: Double,
    @SerializedName("duration_minutes") val durationMinutes: Int? = null,
    @SerializedName("image_url") val imageUrl: String? = null
)

data class BusinessProductEntity(
    @SerializedName("id") val id: String? = null,
    @SerializedName("business_id") val businessId: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("price") val price: Double,
    @SerializedName("stock") val stock: Int = 0,
    @SerializedName("image_url") val imageUrl: String? = null
)

data class AppointmentEntity(
    @SerializedName("id") val id: String? = null,
    @SerializedName("user_id") val userId: String? = null,
    @SerializedName("pet_id") val petId: String? = null,
    @SerializedName("business_id") val businessId: String,
    @SerializedName("service_id") val serviceId: String? = null,
    @SerializedName("appointment_date") val appointmentDate: String,
    @SerializedName("status") val status: String = "pending",
    @SerializedName("created_at") val createdAt: String? = null
)

data class OrderEntity(
    @SerializedName("id") val id: String? = null,
    @SerializedName("user_id") val userId: String? = null,
    @SerializedName("business_id") val businessId: String,
    @SerializedName("total_amount") val totalAmount: Double,
    @SerializedName("status") val status: String = "pending",
    @SerializedName("created_at") val createdAt: String? = null
)

data class OrderItemEntity(
    @SerializedName("id") val id: String? = null,
    @SerializedName("order_id") val orderId: String,
    @SerializedName("product_id") val productId: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("price_at_purchase") val priceAtPurchase: Double
)
