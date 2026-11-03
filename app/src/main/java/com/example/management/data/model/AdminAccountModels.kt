package com.example.management.data.model

data class UserAccountDto(
    val id: Long,
    val username: String,
    val fullName: String?,
    val email: String?,
    val phone: String?,
    val roles: List<String> = emptyList(),
    val isActive: Boolean = true,
    val createdAt: String? = null
)

data class CreateUserAccountRequest(
    val username: String,
    val password: String,
    val fullName: String,
    val email: String? = null,
    val phone: String? = null,
    val roles: List<String> = emptyList()
)

data class ToggleAccountStatusRequest(
    val isActive: Boolean,
    val reason: String? = null
)
