package dev.kawayilab.interknot.navigation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList

@Stable
class InterknotBackStack(initialRoute: InterknotRoute = Home) {
    val topLevelRoutes: List<InterknotRoute> = listOf(Home, Knock, Create, Level, Profile)
    private val _backStack: SnapshotStateList<InterknotRoute> = mutableStateListOf(initialRoute)
    val backStack: List<InterknotRoute> get() = _backStack

    var isLoggedIn by mutableStateOf(false)
        private set

    val currentRoute: InterknotRoute? get() = _backStack.lastOrNull()
    val currentTopLevel: InterknotRoute
        get() = _backStack.findLast { it in topLevelRoutes } ?: Home
    val isTopLevel: Boolean get() = currentRoute in topLevelRoutes

    fun navigate(route: InterknotRoute) {
        if (route == _backStack.lastOrNull()) return
        if (route.requiresLogin && !isLoggedIn) {
            _backStack.add(Login(route))
            return
        }
        _backStack.add(route)
    }

    fun goBack() {
        if (_backStack.size > 1) _backStack.removeAt(_backStack.lastIndex)
    }

    fun login() {
        isLoggedIn = true
        val current = _backStack.lastOrNull()
        if (current is Login) {
            _backStack.removeAt(_backStack.lastIndex)
            val redirect = current.redirectToKey
            if (redirect != null) _backStack.add(redirect) else if (_backStack.isEmpty()) _backStack.add(Home)
        }
    }

    fun logout() {
        isLoggedIn = false
        val filtered = _backStack.filter { !it.requiresLogin }
        _backStack.clear()
        if (filtered.isNotEmpty()) _backStack.addAll(filtered) else _backStack.add(Home)
    }
}
