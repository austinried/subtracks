package com.subtracks.data.model

data class SubsonicConfig(
    val id: Long,
    val name: String,
    val address: String,
    val username: String,
    val password: String,
    val useTokenAuth: Boolean,
)
