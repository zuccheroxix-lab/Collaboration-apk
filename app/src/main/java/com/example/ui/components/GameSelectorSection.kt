package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GameProfileEntity
import com.example.model.InstalledGame
import com.example.ui.theme.*

@Composable
fun GameSelectorSection(
    installedGames: List<InstalledGame>,
    selectedGame: InstalledGame?,
    savedProfiles: List<GameProfileEntity>,
    onSelectGame: (InstalledGame) -> Unit,
    onSaveProfile: () -> Unit,
    onDeleteProfile: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAllAppsDialog by remember { mutableStateOf(false) }

    val hasSavedProfile = selectedGame != null && savedProfiles.any { it.packageName == selectedGame.packageName }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("game_selector_card"),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = "Game Profiles",
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "GAME PROFILES (ROOM DB)",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                TextButton(
                    onClick = { showAllAppsDialog = true },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "View All Apps (${installedGames.size})",
                        color = NeonCyanDim,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Games horizontal scroll
            if (installedGames.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Scanning device for installed games...",
                        color = TextTertiary,
                        fontSize = 12.sp
                    )
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(installedGames.take(12)) { game ->
                        val isSelected = selectedGame?.packageName == game.packageName
                        val isSaved = savedProfiles.any { it.packageName == game.packageName }

                        val bg = if (isSelected) NeonCyan.copy(alpha = 0.15f) else DarkSurfaceVariant
                        val border = if (isSelected) NeonCyan else Color.Transparent

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(bg)
                                .border(1.dp, border, RoundedCornerShape(10.dp))
                                .clickable { onSelectGame(game) }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) NeonCyan else DarkBorder),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = game.appName.take(1).uppercase(),
                                    color = if (isSelected) DarkBackground else TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = game.appName,
                                    color = if (isSelected) TextPrimary else TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (isSaved) {
                                    Text(
                                        text = "Saved Profile",
                                        color = NeonGreen,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Active Game Info & Save / Delete bar
            selectedGame?.let { game ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkBackground)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = game.appName,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = game.packageName,
                            color = TextTertiary,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (hasSavedProfile) {
                            IconButton(
                                onClick = { onDeleteProfile(game.packageName) },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Profile",
                                    tint = NeonRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Button(
                            onClick = onSaveProfile,
                            modifier = Modifier.testTag("save_profile_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (hasSavedProfile) DarkSurfaceVariant else NeonCyan,
                                contentColor = if (hasSavedProfile) NeonGreen else DarkBackground
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = if (hasSavedProfile) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Save Game Profile",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (hasSavedProfile) "Update" else "Save Profile",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAllAppsDialog) {
        AlertDialog(
            onDismissRequest = { showAllAppsDialog = false },
            title = {
                Text(
                    text = "Select Any Installed App / Emulator",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                ) {
                    items(installedGames) { game ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectGame(game)
                                    showAllAppsDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = game.appName,
                                color = if (selectedGame?.packageName == game.packageName) NeonCyan else TextPrimary,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            if (game.isDetectedAsGame) {
                                Text(
                                    text = "GAME",
                                    color = NeonPurple,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        HorizontalDivider(color = DarkBorder)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAllAppsDialog = false }) {
                    Text("Close", color = NeonCyan)
                }
            },
            containerColor = DarkSurface
        )
    }
}
