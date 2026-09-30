package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun DeveloperScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("developer_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "DEVELOPER",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Text(
                text = "Official system architecture & support directory",
                color = TextSecondary,
                fontSize = 11.sp
            )
        }

        // ================= OWNER CARD =================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("owner_card"),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(NeonCyan.copy(alpha = 0.4f)))
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(NeonCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Owner",
                            tint = NeonCyan,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "ZUCCHERO XANN",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified Owner",
                                tint = NeonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Text(
                            text = "DEVELOPER • OWNER",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp
                        )

                        Text(
                            text = "ZX-OWNER-001",
                            color = TextTertiary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Button(
                    onClick = {
                        copyToClipboard(context, "ZX-OWNER-001", "Owner ID copied")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .testTag("copy_owner_id_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkSurfaceVariant,
                        contentColor = NeonCyan
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy ID",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "COPY ID",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        // ================= CONTACT SECTION =================
        Text(
            text = "CONTACT",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(top = 4.dp)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ContactActionItem(
                title = "WHATSAPP 1",
                url = "https://wa.me/6287861072798",
                icon = Icons.AutoMirrored.Filled.Chat,
                iconColor = NeonGreen,
                onOpen = { openUrl(context, "https://wa.me/6287861072798") }
            )

            ContactActionItem(
                title = "WHATSAPP 2",
                url = "https://wa.me/6287835461585",
                icon = Icons.AutoMirrored.Filled.Chat,
                iconColor = NeonGreen,
                onOpen = { openUrl(context, "https://wa.me/6287835461585") }
            )

            ContactActionItem(
                title = "WHATSAPP CHANNEL",
                url = "https://whatsapp.com/channel/0029Vb7LHpF1iUxYUrrJ0d34",
                icon = Icons.Default.Badge,
                iconColor = NeonCyan,
                onOpen = { openUrl(context, "https://whatsapp.com/channel/0029Vb7LHpF1iUxYUrrJ0d34") }
            )

            ContactActionItem(
                title = "SUPPORT",
                url = "https://sociabuzz.com/zucchero_xann",
                icon = Icons.Default.Favorite,
                iconColor = NeonPurple,
                onOpen = { openUrl(context, "https://sociabuzz.com/zucchero_xann") }
            )
        }

        // ================= PARTNERS SECTION =================
        Text(
            text = "PARTNERS",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(top = 8.dp)
        )

        // Partner 1: LALZ
        PartnerCard(
            name = "LALZ",
            role = "PARTNER",
            id = "ZX-PARTNER-001",
            contactUrl = "https://wa.me/6285773259978",
            onContactClick = { openUrl(context, "https://wa.me/6285773259978") },
            onCopyIdClick = { copyToClipboard(context, "ZX-PARTNER-001", "Partner LALZ ID copied") }
        )

        // Partner 2: VAXXY
        PartnerCard(
            name = "VAXXY",
            role = "PARTNER",
            id = "ZX-PARTNER-002",
            contactUrl = "https://wa.me/6281959342201",
            onContactClick = { openUrl(context, "https://wa.me/6281959342201") },
            onCopyIdClick = { copyToClipboard(context, "ZX-PARTNER-002", "Partner VAXXY ID copied") }
        )
    }
}

@Composable
private fun ContactActionItem(
    title: String,
    url: String,
    icon: ImageVector,
    iconColor: Color,
    onOpen: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("contact_btn_${title.lowercase().replace(" ", "_")}"),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Text(
                        text = title,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = url.replace("https://", ""),
                        color = TextTertiary,
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                }
            }

            Button(
                onClick = onOpen,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = iconColor.copy(alpha = 0.15f),
                    contentColor = iconColor
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = "Open",
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "OPEN",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PartnerCard(
    name: String,
    role: String,
    id: String,
    contactUrl: String,
    onContactClick: () -> Unit,
    onCopyIdClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("partner_card_${name.lowercase()}"),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(NeonPurple.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Handshake,
                        contentDescription = "Partner",
                        tint = NeonPurple,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = role,
                        color = NeonPurple,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = id,
                        color = TextTertiary,
                        fontSize = 11.sp
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onContactClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = NeonGreen
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "Contact",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CONTACT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onCopyIdClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkSurfaceVariant,
                        contentColor = TextPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy ID",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "COPY ID",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String, toastMessage: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("ID", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
}

private fun openUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "Could not open browser/WhatsApp", Toast.LENGTH_SHORT).show()
    }
}
