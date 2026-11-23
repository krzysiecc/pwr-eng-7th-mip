package pl.dn.morsik.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val MorsikGreen = Color(0xFF76C84A)
val MorsikPink = Color(0xFFFDE2DE)
val MorsikRed = Color(0xFFFF2A0F)
val MorsikInk = Color(0xFF1B1411)

private val MorsikColors = lightColorScheme(
    primary = MorsikGreen,
    onPrimary = MorsikInk,
    secondary = MorsikInk,
    onSecondary = Color.White,
    surface = Color.White,
    onSurface = MorsikInk,
    surfaceVariant = MorsikPink,
    onSurfaceVariant = MorsikInk,
    background = Color.White,
    onBackground = MorsikInk,
    error = MorsikRed,
    onError = Color.White,
)

private val MorsikTypography = Typography(
    headlineMedium = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
    titleLarge = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Medium),
    labelLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
    labelSmall = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
)

private val MorsikShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
)

@Composable
fun MorsikTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MorsikColors,
        typography = MorsikTypography,
        shapes = MorsikShapes,
        content = content,
    )
}
