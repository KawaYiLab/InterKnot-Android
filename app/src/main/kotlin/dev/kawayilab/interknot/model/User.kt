package dev.kawayilab.interknot.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int,
    val username: String,
    @SerialName("interknot_level")
    val interknotLevel: Int = 1,
    @SerialName("denny")
    val denny: Long = 0
)
