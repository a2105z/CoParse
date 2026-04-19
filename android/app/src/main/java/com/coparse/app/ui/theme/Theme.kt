package com.coparse.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LightColors = lightColorScheme(
    primary = CoParseColors.Teal700,
    onPrimary = Color.White,
    primaryContainer = CoParseColors.Teal100,
    onPrimaryContainer = CoParseColors.Teal900,
    secondary = CoParseColors.Stone600,
    onSecondary = Color.White,
    tertiary = CoParseColors.Teal600,
    onTertiary = Color.White,
    background = CoParseColors.WarmBg,
    onBackground = Color(0xFF1C1917),
    surface = CoParseColors.Surface,
    onSurface = Color(0xFF1C1917),
    surfaceVariant = Color(0xFFE7E5E4),
    onSurfaceVariant = CoParseColors.Stone600,
    outline = CoParseColors.OutlineSubtle,
    outlineVariant = Color(0xFFE7E5E4),
)

private val DarkColors = darkColorScheme(
    primary = CoParseColors.Teal300,
    onPrimary = Color(0xFF042F2E),
    primaryContainer = Color(0xFF134E4A),
    onPrimaryContainer = CoParseColors.Teal100,
    secondary = Color(0xFFD6D3D1),
    onSecondary = Color(0xFF1C1917),
    tertiary = CoParseColors.Teal300,
    background = CoParseColors.DarkBg,
    onBackground = Color(0xFFFAFAF9),
    surface = CoParseColors.DarkSurface,
    onSurface = Color(0xFFFAFAF9),
    surfaceVariant = CoParseColors.DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFD6D3D1),
    outline = Color(0xFF78716C),
    outlineVariant = Color(0xFF57534E),
)

private val CoParseShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
)

private val CoParseTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 30.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.2.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),
)

@Composable
fun CoParseTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = CoParseTypography,
        shapes = CoParseShapes,
        content = content,
    )
}
