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
    val background: Color = Color.Black,
    val surface: Color = Color(0xFF111111),
    val border: Color = Color(0xFF2F3336),
    val divider: Color = Color(0xFF2F3336),
    val accent: Color = Color(0xFF1D9BF0),
    val green: Color = Color(0xFF00BA7C),
    val greenBg: Color = Color(0x1A00BA7C),
    val red: Color = Color(0xFFF4212E),
    val redBg: Color = Color(0x1AF4212E),
    val textPrimary: Color = Color(0xFFE7E9EA),
    val textSecondary: Color = Color(0xFF71767B),
    val switchOn: Color = Color(0xFF1D9BF0),
    val switchOff: Color = Color(0xFF333639),
)

@Immutable
data class TrustMeTypography(
    val titleLarge: TextStyle = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        letterSpacing = (-0.3).sp,
    ),
    val titleMedium: TextStyle = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
    ),
    val body: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
    ),
    val bodySmall: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
    ),
    val label: TextStyle = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
    ),
    val mono: TextStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
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
