package com.godzuche.bluechat.core.design_system.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * A class to model different levels of shape values.
 *
 * @param small The smallest value of [BlueChatShapes].
 * @param medium The mid-value of [BlueChatShapes].
 * @param large The largest value of [BlueChatShapes].
 * @param largeTopRounding Asymmetric large shape with only top rounding.
 */
@Immutable
data class BlueChatShapes(
    val none: Shape = RoundedCornerShape(size = 0.dp),
    val small: Shape = RoundedCornerShape(size = 0.dp),
    val medium: Shape = RoundedCornerShape(size = 0.dp),
    val large: Shape = RoundedCornerShape(size = 0.dp),
    val largeTopRounding: Shape = RoundedCornerShape(size = 0.dp),
)

/**
 * A composition local for [BlueChatShapes].
 */
val LocalBlueChatShapes = staticCompositionLocalOf { BlueChatShapes() }