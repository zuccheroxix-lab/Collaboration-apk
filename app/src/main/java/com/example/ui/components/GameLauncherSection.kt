package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GameProfileEntity
import com.example.model.InstalledGame
import com.example.model.LaunchMode
import com.example.ui.theme.*
import com.example.util.GameScanner

@Composable
fun GameLauncherSection(
    launcherGames: List<InstalledGame>,
    allInstalledApps: List<InstalledGame>,
    savedProfiles: List<GameProfileEntity>,
    onLaunchGameClicked: (InstalledGame) -> Unit,
    onAddGameToLauncher: (InstalledGame) -> Unit,
    onRemoveGameFromLauncher: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddGameDialog by remember { mutableStateOf(false) }

    val filteredGames = remember(launcherGames, searchQuery) {
        if (searchQuery.isBlank()) {
            launcherGames
        } else {
            launcherGames.filter {
                it.appName.contains(searchQuery, ignoreCase = true) ||
                it.packageName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Split into Recent Games (games with configured profiles) and Other Installed Games
    val recentGames = remember(filteredGames, savedProfiles) {
        filteredGames.filter { game -> savedProfiles.any { it.packageName == game.packageName } }
    }

    val installedGamesList = remember(filteredGames, recentGames) {
        if (recentGames.isEmpty()) {
            filteredGames
        } else {
            filteredGames.filter { game -> recentGames.none { it.packageName == game.packageName } }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("game_launcher_view"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ================= HEADER =================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "GAME LAUNCHER",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Select a game to start session",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Button(
                onClick = { showAddGameDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkSurface,
                    contentColor = NeonCyan
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f)),
                modifier = Modifier.testTag("add_game_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("ADD GAME", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // ================= SEARCH BAR =================
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    text = "Search installed games...",
                    color = TextTertiary,
                    fontSize = 12.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = TextTertiary,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { searchQuery = "" },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DarkSurface,
                unfocusedContainerColor = DarkSurface,
                focusedBorderColor = NeonCyan.copy(alpha = 0.5f),
                unfocusedBorderColor = DarkBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("game_search_field")
        )

        if (filteredGames.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (searchQuery.isNotBlank()) "No games matching \"$searchQuery\"" else "No games added yet.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    if (searchQuery.isBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        TextButton(onClick = { showAddGameDialog = true }) {
                            Text("Tap here to add installed games", color = NeonCyan, fontSize = 11.sp)
                        }
                    }
                }
            }
        } else {
            // ================= RECENT GAMES SECTION =================
            if (recentGames.isNotEmpty()) {
                Text(
                    text = "RECENT GAMES",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    recentGames.forEach { game ->
                        val savedProfile = savedProfiles.firstOrNull { it.packageName == game.packageName }
                        val preferredMode = savedProfile?.preferredMode ?: LaunchMode.OPTIMIZE.name

                        GameItemCard(
                            game = game,
                            preferredMode = preferredMode,
                            onLaunch = { onLaunchGameClicked(game) },
                            onRemove = { onRemoveGameFromLauncher(game.packageName) }
                        )
                    }
                }
            }

            // ================= INSTALLED GAMES SECTION =================
            if (installedGamesList.isNotEmpty()) {
                Text(
                    text = "INSTALLED GAMES",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    installedGamesList.forEach { game ->
                        val savedProfile = savedProfiles.firstOrNull { it.packageName == game.packageName }
                        val preferredMode = savedProfile?.preferredMode ?: LaunchMode.OPTIMIZE.name

                        GameItemCard(
                            game = game,
                            preferredMode = preferredMode,
                            onLaunch = { onLaunchGameClicked(game) },
                            onRemove = { onRemoveGameFromLauncher(game.packageName) }
                        )
                    }
                }
            }
        }
    }

    if (showAddGameDialog) {
        var addAppSearch by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddGameDialog = false },
            title = {
                Text(
                    text = "Add Application to Launcher",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = addAppSearch,
                        onValueChange = { addAppSearch = it },
                        placeholder = { Text("Search installed apps...", color = TextTertiary, fontSize = 12.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkBackground,
                            unfocusedContainerColor = DarkBackground,
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = DarkBorder
                        ),
                        modifier = Modifier.fillMaxWidth().height(46.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) {
                        val available = allInstalledApps.filter { app ->
                            launcherGames.none { it.packageName == app.packageName } &&
                            (addAppSearch.isBlank() || app.appName.contains(addAppSearch, ignoreCase = true) || app.packageName.contains(addAppSearch, ignoreCase = true))
                        }
                        if (available.isEmpty()) {
                            item {
                                Text(
                                    text = "No applications found.",
                                    color = TextTertiary,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        } else {
                            items(available) { app ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onAddGameToLauncher(app)
                                            showAddGameDialog = false
                                        }
                                        .padding(vertical = 10.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = app.appName,
                                            color = TextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = app.packageName,
                                            color = TextTertiary,
                                            fontSize = 10.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    if (app.isDetectedAsGame) {
                                        Text(
                                            text = "GAME",
                                            color = NeonPurple,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                HorizontalDivider(color = DarkBorder)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddGameDialog = false }) {
                    Text("Close", color = NeonCyan)
                }
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
private fun GameItemCard(
    game: InstalledGame,
    preferredMode: String,
    onLaunch: () -> Unit,
    onRemove: () -> Unit
) {
    val context = LocalContext.current
    val appIcon = remember(game.packageName) {
        GameScanner.getAppIcon(context, game.packageName)
    }

    val modeColor = when (preferredMode.uppercase()) {
        "PERFORMANCE" -> NeonPurple
        "STABLE" -> NeonGreen
        else -> NeonCyan
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("game_item_${game.packageName}"),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // [GAME ICON]
                if (appIcon != null) {
                    Image(
                        bitmap = appIcon,
                        contentDescription = game.appName,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = game.appName.take(1).uppercase(),
                            color = NeonCyan,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = game.appName,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = game.packageName,
                        color = TextTertiary,
                        fontSize = 9.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Profile: $preferredMode",
                        color = modeColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove game",
                        tint = TextTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Button(
                    onClick = onLaunch,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyan,
                        contentColor = DarkBackground
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("launch_btn_${game.packageName}")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "LAUNCH",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
