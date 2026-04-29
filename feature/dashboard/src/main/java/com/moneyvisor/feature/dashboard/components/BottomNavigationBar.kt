package com.moneyvisor.feature.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneyvisor.core.designsystem.icons.MoneyVisorIcons
import com.moneyvisor.core.designsystem.theme.BrandBlue

@Composable
fun BottomNavigationBar(
    selectedIndex: Int = 0,
    onTabSelected: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(32.dp),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                icon = MoneyVisorIcons.Home,
                label = "Home",
                isSelected = selectedIndex == 0,
                onClick = { onTabSelected(0) }
            )
            BottomNavItem(
                icon = MoneyVisorIcons.Timeline,
                label = "Timeline",
                isSelected = selectedIndex == 1,
                onClick = { onTabSelected(1) }
            )
            BottomNavItem(
                icon = MoneyVisorIcons.Status,
                label = "Status",
                isSelected = selectedIndex == 2,
                onClick = { onTabSelected(2) }
            )
            BottomNavItem(
                icon = MoneyVisorIcons.Settings,
                label = "Settings",
                isSelected = selectedIndex == 3,
                onClick = { onTabSelected(3) }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isSelected) BrandBlue.copy(alpha = 0.1f) else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) BrandBlue else Color(0xFFBDBDBD),
                modifier = Modifier.size(26.dp)
            )
        }
        if (isSelected) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = BrandBlue,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        }
    }
}
