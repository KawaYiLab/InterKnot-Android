package dev.kawayilab.interknot.ui.components.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import dev.kawayilab.interknot.navigation.Create
import dev.kawayilab.interknot.navigation.Home
import dev.kawayilab.interknot.navigation.InterknotRoute
import dev.kawayilab.interknot.navigation.Knock
import dev.kawayilab.interknot.navigation.Level
import dev.kawayilab.interknot.navigation.Profile

private data class NavConfig(
    val route: InterknotRoute,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val isCreate: Boolean = false
)

private val navItems = listOf(
    NavConfig(Home, "推送", Icons.Filled.Home, Icons.Outlined.Home),
    NavConfig(Knock, "敲敲", Icons.Filled.Email, Icons.Outlined.Email),
    NavConfig(Create, "发布", Icons.Filled.Add, Icons.Outlined.Add, isCreate = true),
    NavConfig(Level, "等级", Icons.Filled.Star, Icons.Outlined.Star),
    NavConfig(Profile, "我的", Icons.Filled.Person, Icons.Outlined.Person)
)

@Composable
fun InterknotBottomNav(
    currentRoute: InterknotRoute,
    onNavigate: (InterknotRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
    ) {
        navItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = {
                    if (item.isCreate) {
                        CreateNavIcon(selected = selected)
                    } else {
                        Icon(
                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label
                        )
                    }
                },
                label = { Text(item.label) },
                alwaysShowLabel = true,
                colors = if (item.isCreate) {
                    createItemColors(selected)
                } else {
                    NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
        }
    }
}

@Composable
private fun CreateNavIcon(selected: Boolean) {
    Surface(
        modifier = Modifier.size(36.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primary
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "发布",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun createItemColors(selected: Boolean) = NavigationBarItemDefaults.colors(
    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
    selectedTextColor = MaterialTheme.colorScheme.primary,
    indicatorColor = Color.Transparent,
    unselectedIconColor = MaterialTheme.colorScheme.onPrimary,
    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
)
