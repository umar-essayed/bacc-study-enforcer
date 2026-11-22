package com.baccalaureate.studyenforcer.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baccalaureate.studyenforcer.ui.theme.AccentEmerald
import com.baccalaureate.studyenforcer.ui.theme.BorderHairline
import com.baccalaureate.studyenforcer.ui.theme.InkSpaceGray
import com.baccalaureate.studyenforcer.ui.theme.SurfaceWhite

@Composable
fun BottomNavBar(
    currentRoute: String,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceWhite)
            .navigationBarsPadding()
    ) {
        // 1dp Hairline Top Border
        Divider(
            color = BorderHairline,
            thickness = 1.dp
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItems.forEach { screen ->
                val isSelected = currentRoute == screen.route
                val iconColor by animateColorAsState(
                    targetValue = if (isSelected) AccentEmerald else InkSpaceGray,
                    animationSpec = tween(250),
                    label = "tab_color"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onNavigate(screen) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = screen.titleAr,
                            tint = iconColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = screen.titleAr,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = iconColor,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}
