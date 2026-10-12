package com.ravi.newfilemanager.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ravi.newfilemanager.data.VaultManager

private val LockBlue = Color(0xFF7AA8E8)
private val LockBlueLight = Color(0xFFB8D4F5)

@Composable
fun LockedFilesScreen(
    onBack: () -> Unit = {},
    onUnlock: (pin: String) -> Unit = {}
) {
    val ctx = LocalContext.current
    val vault = remember { VaultManager(ctx) }
    val setupMode = remember { !vault.hasPin() }

    var pin by remember { mutableStateOf("") }
    var firstEntry by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    fun submit() {
        if (setupMode) {
            if (firstEntry == null) {
                firstEntry = pin
                pin = ""
                error = null
            } else {
                if (pin == firstEntry) {
                    try {
                        vault.setPin(pin)
                        onUnlock(pin)
                    } catch (e: Exception) {
                        error = "Could not save passcode"
                        firstEntry = null; pin = ""
                    }
                } else {
                    error = "Passcodes don't match, try again"
                    firstEntry = null; pin = ""
                }
            }
        } else {
            try {
                if (vault.verifyPin(pin)) onUnlock(pin)
                else { error = "Wrong passcode"; pin = "" }
            } catch (e: Exception) {
                error = "Something went wrong"; pin = ""
            }
        }
    }

    fun press(d: String) {
        if (pin.length < 4) {
            pin += d
            if (pin.length == 4) submit()
        }
    }

    val title = if (setupMode) {
        if (firstEntry == null) "Set a 4-digit\npasscode" else "Confirm your\npasscode"
    } else "Your Secured Storage\nis Locked"
    val subtitle = when {
        error != null -> null
        setupMode && firstEntry == null -> "Choose a 4-digit passcode"
        setupMode -> "Enter it once more"
        else -> "Enter your 4-digit passcode to unlock"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
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
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextDark, modifier = Modifier.size(18.dp))
            }
            Text("Locked Files", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(CardColor)
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextDark, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Main card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(CardColor)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .shadow(12.dp, RoundedCornerShape(24.dp), spotColor = LockBlue.copy(alpha = 0.4f))
                    .clip(RoundedCornerShape(24.dp))
                    .background(LockBlueLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Lock, contentDescription = "Locked", tint = LockBlue, modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark,
                textAlign = TextAlign.Center, lineHeight = 24.sp)

            Spacer(modifier = Modifier.height(8.dp))

            if (error != null) {
                Text(error!!, fontSize = 12.sp, color = Color(0xFFE5484D), textAlign = TextAlign.Center)
            } else if (subtitle != null) {
                Text(subtitle, fontSize = 12.sp, color = TextMuted, textAlign = TextAlign.Center)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // PIN dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 4) {
                    val filled = i < pin.length
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(if (filled) TextMuted else Color.Transparent)
                            .border(1.5.dp, TextMuted.copy(alpha = 0.4f), CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Numeric keypad
            val keys = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9")
            )
            keys.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    row.forEach { digit ->
                        KeypadButton(text = digit, onClick = { press(digit) })
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { pin = "" }, modifier = Modifier.size(64.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(20.dp))
                }
                KeypadButton(text = "0", onClick = { press("0") })
                IconButton(
                    onClick = { if (pin.isNotEmpty()) pin = pin.dropLast(1) },
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(Icons.Default.Backspace, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(Color.White)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 20.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2B3445))
    }
}
