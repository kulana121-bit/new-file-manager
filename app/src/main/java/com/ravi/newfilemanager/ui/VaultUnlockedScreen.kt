package com.ravi.newfilemanager.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val BgColor = Color(0xFFE8EDF3)
private val CardColor = Color(0xFFF2F5F9)
private val TextDark = Color(0xFF2B3445)
private val TextMuted = Color(0xFF8A94A6)
private val GreenTint = Color(0xFFD4EDDA)
private val GreenIcon = Color(0xFF4CAF6D)

private val PeachCard = Color(0xFFFFD9C7)
private val PurpleCard = Color(0xFFE3D4F5)
private val BlueCard = Color(0xFFCFE0F5)

@Composable
fun VaultUnlockedScreen(
    onBack: () -> Unit = {},
    onCategoryClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(CardColor)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = TextDark,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                "Locked Files",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(CardColor)
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.MoreHoriz,
                    contentDescription = "More",
                    tint = TextDark,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Unlocked status card
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(18.dp))
                        .clip(RoundedCornerShape(18.dp))
                        .background(CardColor)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(GreenTint),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.LockOpen,
                            contentDescription = "Unlocked",
                            tint = GreenIcon,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            "Unlocked",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            "Secured Storage Unlocked",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Vault Highlights card
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(18.dp))
                        .clip(RoundedCornerShape(18.dp))
                        .background(CardColor)
                        .padding(16.dp)
                ) {
                    Text(
                        "Vault Highlights",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Total Protected: 12 Files (1.2 GB)",
                        fontSize = 12.sp,
                        color = TextDark
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Locked Categories
            item {
                Text(
                    "Locked Categories",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    VaultCategoryCard("Private\nPhotos", PeachCard, onCategoryClick)
                    VaultCategoryCard("Secure\nVideo", PurpleCard, onCategoryClick)
                    VaultCategoryCard("Key\nDocuments", BlueCard, onCategoryClick)
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Locked Files header
            item {
                Text(
                    "Locked Files",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }

            items(vaultFilesSample) { file ->
                VaultFileRow(file)
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Spacer(modifier = Modifier.height(90.dp))
            }
        }
    }
}

@Composable
private fun VaultCategoryCard(
    title: String,
    bgColor: Color,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .width(100.dp)
            .height(88.dp)
            .shadow(4.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            title,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextDark,
            lineHeight = 17.sp
        )
    }
}

private data class VaultFile(
    val name: String,
    val meta: String,
    val icon: ImageVector,
    val iconTint: Color
)

private val vaultFilesSample = listOf(
    VaultFile(
        "Bank_Statement_2026.pdf",
        "1.5 MB • Oct 2026",
        Icons.Default.PictureAsPdf,
        Color(0xFFE85D5D)
    ),
    VaultFile(
        "Family_Vacation_Hidden.mp4",
        "150 MB",
        Icons.Default.VideoFile,
        Color(0xFF5B8DE8)
    ),
    VaultFile(
        "Confidential_Ideas.txt",
        "12 KB",
        Icons.Default.Description,
        Color(0xFF8A94A6)
    )
)

@Composable
private fun VaultFileRow(file: VaultFile) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // File icon with small lock badge
        Box {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(file.iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    file.icon,
                    contentDescription = null,
                    tint = file.iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
            // Lock badge
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(CardColor)
                    .align(Alignment.BottomEnd),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = TextMuted,
                    modifier = Modifier.size(11.dp)
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                file.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextDark
            )
            Text(
                file.meta,
                fontSize = 11.sp,
                color = TextMuted
            )
        }
        Icon(
            Icons.Default.Lock,
            contentDescription = "Locked",
            tint = TextMuted.copy(alpha = 0.6f),
            modifier = Modifier.size(16.dp)
        )
    }
}
