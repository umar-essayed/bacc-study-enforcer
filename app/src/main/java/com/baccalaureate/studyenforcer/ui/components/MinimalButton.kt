package com.baccalaureate.studyenforcer.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baccalaureate.studyenforcer.ui.theme.AccentEmerald
import com.baccalaureate.studyenforcer.ui.theme.AlertCrimson
import com.baccalaureate.studyenforcer.ui.theme.BorderHairline
import com.baccalaureate.studyenforcer.ui.theme.InkTitanium
import com.baccalaureate.studyenforcer.ui.theme.SurfaceWhite

enum class ButtonStyleVariant {
    PRIMARY,
    SECONDARY,
    DESTRUCTIVE;

    companion object {
        val Primary = PRIMARY
        val Secondary = SECONDARY
        val Destructive = DESTRUCTIVE
    }
}

@Composable
fun MinimalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = 48.dp,
    variant: ButtonStyleVariant = ButtonStyleVariant.PRIMARY,
    style: ButtonStyleVariant = variant
) {
    val effectiveVariant = if (style != ButtonStyleVariant.PRIMARY) style else variant

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        label = "button_scale"
    )

    val (containerColor, contentColor, border) = when (effectiveVariant) {
        ButtonStyleVariant.PRIMARY -> Triple(
            AccentEmerald,
            SurfaceWhite,
            null
        )
        ButtonStyleVariant.SECONDARY -> Triple(
            SurfaceWhite,
            InkTitanium,
            BorderStroke(1.dp, BorderHairline)
        )
        ButtonStyleVariant.DESTRUCTIVE -> Triple(
            AlertCrimson,
            SurfaceWhite,
            null
        )
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .height(height)
            .scale(scale),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.5f),
            disabledContentColor = contentColor.copy(alpha = 0.5f)
        ),
        border = border,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp
        ),
        contentPadding = PaddingValues(horizontal = 20.dp),
        interactionSource = interactionSource
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = contentColor
        )
    }
}
