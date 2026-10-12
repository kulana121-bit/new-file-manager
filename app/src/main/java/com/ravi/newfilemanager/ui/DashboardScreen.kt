package com.ravi.newfilemanager.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Soft background matching reference
private val BgColor = Color(0xFFE8EDF3)
private val CardColor = Color(0xFFF2F5F9)
private val TextDark = Color(0xFF2B3445)
private val TextMuted = Color(0xFF8A94A6)

// Pastel quick access colors
private val PeachCard = Color(0xFFFFD9C7)
private val PeachText = Color(0xFF8A4A2B)
private val PurpleCard = Color(0xFFE3D4F5)
private val PurpleText = Color(0xFF5B3E8A)
private val BlueCard = Color(0xFFCFE0F5)
private val BlueText = Color(0xFF2E4E7A)
private val GreenCard = Color(0xFFCDEAC0)
private val GreenText = Color(0xFF2E6B2E)

@Composable
fun DashboardScreen(
    onNavigate: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Top bar: Search + Bell
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search bar
            Row(
                modifier = Modifier
                    .weight(1f)
                    .shadow(4.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(CardColor)
                    .clickable { }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    "Search files...",
                    fontSize = 13.sp,
                    color = TextMuted
                )
            }
            // Bell
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(CardColor)
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.NotificationsNone,
                    contentDescription = "Notifications",
                    tint = TextDark,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Storage section
            item {
                Text(
                    "Storage",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                // Storage card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(CardColor)
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Circular progress
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = 0.5f,
                            modifier = Modifier.size(72.dp),
                            color = Color(0xFF7AA8E8),
                            trackColor = Color(0xFFE0E6EE),
                            strokeWidth = 8.dp,
                            strokeCap = StrokeCap.Round
                        )
                        Text(
                            "64 GB",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Storage Used",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "64 GB / 128 GB",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = 0.5f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFF7AA8E8),
                            trackColor = Color(0xFFE0E6EE)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Free space: 64 GB",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Quick Access section
            item {
                Text(
                    "Quick Access",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                // 2x2 grid
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickAccessCard(
                            title = "Images",
                            bgColor = PeachCard,
                            textColor = PeachText,
                            modifier = Modifier.weight(1f)
                        )
                        QuickAccessCard(
                            title = "Videos",
                            bgColor = PurpleCard,
                            textColor = PurpleText,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickAccessCard(
                            title = "Documents",
                            bgColor = BlueCard,
                            textColor = BlueText,
                            modifier = Modifier.weight(1f)
                        )
                        QuickAccessCard(
                            title = "Music",
                            bgColor = GreenCard,
                            textColor = GreenText,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Recent Files section
            item {
                Text(
                    "Recent Files",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }

            items(recentFilesSample) { file ->
                RecentFileRow(file)
                Spacer(modifier = Modifier.height(4.dp))
            }

            item {
                Spacer(modifier = Modifier.height(90.dp))
            }
        }
    }

    // Bottom floating dock
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .padding(bottom = 24.dp)
                .shadow(8.dp, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .background(CardColor)
                .padding(horizontal = 32.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(36.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Home,
                contentDescription = "Home",
                tint = TextDark,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onNavigate("dashboard") }
            )
            Icon(
                Icons.Default.GridView,
                contentDescription = "Files",
                tint = TextMuted,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onNavigate("browser") }
            )
            Icon(
                Icons.Default.Settings,
                contentDescription = "Settings",
                tint = TextMuted,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onNavigate("settings") }
            )
        }
    }
}

@Composable
private fun QuickAccessCard(
    title: String,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(bgColor)
            .clickable { }
            .padding(vertical = 22.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

private data class RecentFile(val name: String, val iconTint: Color)

private val recentFilesSample = listOf(
    RecentFile("Project_Proposal.pdf", Color(0xFFE85D5D)),
    RecentFile("Vacation_Video.mp4", Color(0xFF5B8DE8)),
    RecentFile("Invoice_Oct.xlsx", Color(0xFF5BB85B))
)

@Composable
private fun RecentFileRow(file: RecentFile) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(file.iconTint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            val icon = when {
                file.name.endsWith(".pdf") -> Icons.Default.PictureAsPdf
                file.name.endsWith(".mp4") -> Icons.Default.VideoFile
                else -> Icons.Default.TableChart
            }
            Icon(
                icon,
                contentDescription = null,
                tint = file.iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            file.name,
            fontSize = 13.sp,
            color = TextDark,
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = { },
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                Icons.Default.MoreHoriz,
                contentDescription = "More",
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
