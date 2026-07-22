package dev.kawayilab.interknot.ui.components.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.kawayilab.interknot.navigation.Home
import dev.kawayilab.interknot.navigation.InterknotRoute
import dev.kawayilab.interknot.navigation.Knock
import dev.kawayilab.interknot.navigation.Level
import dev.kawayilab.interknot.navigation.Profile
import dev.kawayilab.interknot.ui.theme.Background
import dev.kawayilab.interknot.ui.theme.Border
import dev.kawayilab.interknot.ui.theme.InterknotYellowDark
import dev.kawayilab.interknot.ui.theme.InterknotYellowLight
import dev.kawayilab.interknot.ui.theme.InterknotYellowMid
import dev.kawayilab.interknot.ui.theme.TextPrimary
import dev.kawayilab.interknot.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun InterknotBottomNav(
    currentRoute: InterknotRoute,
    onNavigate: (InterknotRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    val navPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Background,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp + navPadding)
                .drawBehind {
                    drawLine(
                        color = Border,
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = 1.dp.toPx()
                    )
                }
                .padding(bottom = navPadding),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                icon = Icons.Default.Home,
                label = "推送",
                isSelected = currentRoute == Home,
                onClick = { onNavigate(Home) }
            )
            NavItem(
                icon = Icons.Default.Email,
                label = "敲敲",
                isSelected = currentRoute == Knock,
                onClick = { onNavigate(Knock) }
            )
            Spacer(modifier = Modifier.weight(1f))
            NavItem(
                icon = Icons.Default.Star,
                label = "等级",
                isSelected = currentRoute == Level,
                onClick = { onNavigate(Level) }
            )
            NavItem(
                icon = Icons.Default.Person,
                label = "我的",
                isSelected = currentRoute == Profile,
                onClick = { onNavigate(Profile) }
            )
        }
    }
}

@Composable
private fun RowScope.NavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale = remember { Animatable(1f) }
    LaunchedEffect(isSelected) {
        if (isSelected) {
            scale.snapTo(1f)
            launch { scale.animateTo(1.2f, tween(150)) }
            launch { scale.animateTo(1f, tween(150)) }
        }
    }

    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier
                .size(24.dp)
                .scale(scale.value),
            tint = if (isSelected) TextPrimary else TextSecondary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                color = if (isSelected) TextPrimary else TextSecondary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        )
    }
}

@Composable
fun CenterCreateButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = Color.Transparent,
        contentColor = Color(0xFF14140A),
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 6.dp,
            pressedElevation = 6.dp
        )
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                InterknotYellowLight,
                                InterknotYellowLight,
                                InterknotYellowMid,
                                InterknotYellowDark
                            ),
                            center = Offset(size.width / 2, size.height * 0.22f),
                            radius = size.width * 0.7f
                        ),
                        radius = size.width / 2
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "发布委托",
                modifier = Modifier.size(26.dp),
                tint = Color(0xFF14140A)
            )
        }
    }
}
