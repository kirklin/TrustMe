package hk.kirk.trustme.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Immutable
data class TrustMeColors(
    val background: Color = Color(0xFF0D1117),
    val surface: Color = Color(0xFF161B22),
    val border: Color = Color(0xFF21262D),
    val divider: Color = Color(0xFF21262D),
    val accent: Color = Color(0xFF58A6FF),
    val green: Color = Color(0xFF3FB950),
    val greenBg: Color = Color(0x1A3FB950),
    val red: Color = Color(0xFFF85149),
    val redBg: Color = Color(0x1AF85149),
    val textPrimary: Color = Color(0xFFE6EDF3),
    val textSecondary: Color = Color(0xFF7D8590),
    val textTertiary: Color = Color(0xFF484F58),
    val switchOn: Color = Color(0xFF58A6FF),
    val switchOff: Color = Color(0xFF21262D),
    val switchThumbOff: Color = Color(0xFF7D8590),
)

@Immutable
data class TrustMeTypography(
    val titleLarge: TextStyle = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        letterSpacing = (-0.2).sp,
        lineHeight = 28.sp,
    ),
    val titleMedium: TextStyle = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        letterSpacing = 0.sp,
        lineHeight = 24.sp,
    ),
    val body: TextStyle = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 22.sp,
    ),
    val bodySmall: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    ),
    val label: TextStyle = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 0.5.sp,
        lineHeight = 16.sp,
    ),
    val mono: TextStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    ),
)

@Immutable
data class TrustMeShapes(
    val card: Dp = 14.dp,
    val inner: Dp = 10.dp,
    val chip: Dp = 6.dp,
)

val LocalTrustMeColors = staticCompositionLocalOf { TrustMeColors() }
val LocalTrustMeTypography = staticCompositionLocalOf { TrustMeTypography() }
val LocalTrustMeShapes = staticCompositionLocalOf { TrustMeShapes() }

object TrustMe {
    val colors: TrustMeColors @Composable get() = LocalTrustMeColors.current
    val type: TrustMeTypography @Composable get() = LocalTrustMeTypography.current
    val shapes: TrustMeShapes @Composable get() = LocalTrustMeShapes.current
}

@Composable
fun TrustMeTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalTrustMeColors provides TrustMeColors(),
        LocalTrustMeTypography provides TrustMeTypography(),
        LocalTrustMeShapes provides TrustMeShapes(),
        content = content,
    )
}
