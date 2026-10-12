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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ravi.newfilemanager.data.CloudProviders

@Composable
fun DeletePhotoBottomSheet(
    count: Int,
    onDismiss: () -> Unit,
    onRemoveFromDevice: () -> Unit,
    onDeleteFromBoth: () -> Unit,
    onToast: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(CardColor)
                .clickable(enabled = false) { }
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFD0D6DE))
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.width(40.dp))
                Text("Delete Photo", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(LineColor)
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            BottomSheetOption(
                icon = Icons.Default.DeleteForever,
                iconTint = Color(0xFFE85D5D),
                iconBg = Color(0xFFFFD4D4),
                title = "Remove from device",
                subtitle = "Delete ${if (count > 1) "$count files" else "this file"} permanently from this device and the vault.",
                onClick = { onRemoveFromDevice(); onDismiss() }
            )
            BottomSheetOption(
                icon = Icons.Default.CloudOff,
                iconTint = Color(0xFF7A9ACD),
                iconBg = Color(0xFFD4E4F7),
                title = "Remove from cloud",
                subtitle = "Delete this file from linked cloud storage, but keep on this device",
                onClick = { onToast(CloudProviders.current.comingSoonMessage) }
            )
            BottomSheetOption(
                icon = Icons.Default.Warning,
                iconTint = Color(0xFFD9975A),
                iconBg = Color(0xFFFFE4C7),
                title = "Delete from both",
                subtitle = "Permanently delete this file from both the device and your cloud",
                onClick = { onDeleteFromBoth(); onDismiss() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(CardColor)
                    .clickable { onDismiss() }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Cancel", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextDark)
            }
        }
    }
}
