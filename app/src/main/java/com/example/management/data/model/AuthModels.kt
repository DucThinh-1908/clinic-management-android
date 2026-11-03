package com.example.management.data.model

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String?,
    val token: String?,
    val user: UserSummary?
)

data class UserSummary(
    val id: Long,
    val username: String,
    val fullName: String?,
    val email: String?,
    val phone: String?,
    val roles: List<String>?,
    val permissions: List<String>?
)

data class RegisterRequest(
    val username: String,
    val password: String,
    val fullName: String,
    val email: String,
    val phone: String
)

data class ChangePasswordRequest(
    val userId: Long,
    val oldPassword: String,
    val newPassword: String
)
