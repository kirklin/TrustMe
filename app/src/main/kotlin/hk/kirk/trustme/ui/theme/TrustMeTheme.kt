package hk.kirk.trustme.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Immutable
data class TrustMeColors(
    val background: Color = Color.Black,
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
    val body: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
    ),
    val bodySmall: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
    ),
    val sectionHeader: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
    ),
    val mono: TextStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
    ),
)

val LocalTrustMeColors = staticCompositionLocalOf { TrustMeColors() }
val LocalTrustMeTypography = staticCompositionLocalOf { TrustMeTypography() }

object TrustMe {
    val colors: TrustMeColors @Composable get() = LocalTrustMeColors.current
    val type: TrustMeTypography @Composable get() = LocalTrustMeTypography.current
}

@Composable
fun TrustMeTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalTrustMeColors provides TrustMeColors(),
        LocalTrustMeTypography provides TrustMeTypography(),
        content = content,
    )
}
