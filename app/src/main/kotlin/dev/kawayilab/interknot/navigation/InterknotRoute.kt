package dev.kawayilab.interknot.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed class InterknotRoute(val requiresLogin: Boolean = false) : NavKey

@Serializable
data object Home : InterknotRoute()

@Serializable
data object Explore : InterknotRoute()

@Serializable
data object Profile : InterknotRoute(requiresLogin = true)

@Serializable
data class PostDetail(val postId: String) : InterknotRoute()

@Serializable
data class Login(val redirectToKey: InterknotRoute? = null) : InterknotRoute()
