package com.ravi.newfilemanager.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---------- theme-aware palette (soft pastel, light + dark) ----------
val LocalDarkMode = compositionLocalOf { false }

val BgColor: Color @Composable get() =
    if (LocalDarkMode.current) Color(0xFF131A28) else Color(0xFFE8EDF3)
val CardColor: Color @Composable get() =
    if (LocalDarkMode.current) Color(0xFF1E2942) else Color(0xFFF2F5F9)
val TextDark: Color @Composable get() =
    if (LocalDarkMode.current) Color(0xFFE9EEF7) else Color(0xFF2B3445)
val TextMuted: Color @Composable get() =
    if (LocalDarkMode.current) Color(0xFF8B98B0) else Color(0xFF8A94A6)
val LineColor: Color @Composable get() =
    if (LocalDarkMode.current) Color(0xFF2A3650) else Color(0xFFE2E8F0)

// pastel accents (work on both themes)
val PeachCard = Color(0xFFFFD9C7); val PeachText = Color(0xFF8A4A2B)
val PurpleCard = Color(0xFFE3D4F5); val PurpleText = Color(0xFF5B3E8A)
val BlueCard = Color(0xFFCFE0F5); val BlueText = Color(0xFF2E4E7A)
val GreenCard = Color(0xFFCDEAC0); val GreenText = Color(0xFF2E6B2E)
val PinkCard = Color(0xFFF9D3DE); val PinkText = Color(0xFF8A3E54)
val CreamCard = Color(0xFFF7E8C4); val CreamText = Color(0xFF7A5A24)
val Accent = Color(0xFF7A8FE8)

// frosted liquid-glass surface (translucent, never solid)
val GlassColor: Color @Composable get() =
    if (LocalDarkMode.current) Color(0xFF1E2942).copy(alpha = 0.62f) else Color.White.copy(alpha = 0.55f)
val GlassBorder: Color @Composable get() =
    Color.White.copy(alpha = if (LocalDarkMode.current) 0.18f else 0.6f)

// ---------- small bottom dock (normal size, per user feedback) ----------
@Composable
fun BottomDock(
    selected: String,
    onHome: () -> Unit,
    onBrowse: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .shadow(10.dp, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .background(GlassColor)
                .border(1.dp, GlassBorder, RoundedCornerShape(28.dp))
                .padding(horizontal = 28.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DockIcon(Icons.Default.Home, "Home", selected == "home", onHome)
            DockIcon(Icons.Default.GridView, "Browse", selected == "browse", onBrowse)
            DockIcon(Icons.Default.Settings, "Settings", selected == "settings", onSettings)
        }
    }
}

@Composable
private fun DockIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    desc: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) LineColor else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon, contentDescription = desc,
            tint = if (isSelected) TextDark else TextMuted,
            modifier = Modifier.size(21.dp)
        )
    }
}

// ---------- screen header ----------
@Composable
fun ScreenHeader(
    title: String,
    onBack: (() -> Unit)? = null,
    actionIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(CardColor)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextDark, modifier = Modifier.size(18.dp))
            }
        } else {
            Spacer(modifier = Modifier.width(38.dp))
        }
        Text(
            title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark,
            modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        if (actionIcon != null && onAction != null) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(CardColor)
                    .clickable { onAction() },
                contentAlignment = Alignment.Center
            ) {
                Icon(actionIcon, contentDescription = null, tint = TextDark, modifier = Modifier.size(18.dp))
            }
        } else {
            Spacer(modifier = Modifier.width(38.dp))
        }
    }
}

// ---------- search pill ----------
@Composable
fun SearchPill(hint: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(CardColor)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
        Text(hint, fontSize = 14.sp, color = TextMuted)
    }
}

// ---------- section title ----------
@Composable
fun SectionTitle(text: String) {
    Text(
        text, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark,
        modifier = Modifier.padding(top = 18.dp, bottom = 10.dp)
    )
}

// ---------- generic file row ----------
@Composable
fun FileRowItem(
    iconBg: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    trailing: @Composable (() -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
        }
        androidx.compose.foundation.layout.Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextDark, maxLines = 1)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 11.5.sp, color = TextMuted, maxLines = 1)
        }
        if (trailing != null) trailing()
        else Icon(Icons.Default.MoreHoriz, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
    }
}

// ---------- centered title (for tab roots like Browse/Settings) ----------
@Composable
fun TopTitle(text: String) {
    Text(
        text, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = TextDark,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )
}
