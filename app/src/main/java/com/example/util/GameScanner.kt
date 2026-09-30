package com.example.util

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import com.example.model.InstalledGame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GameScanner {

    private val GAME_KEYWORDS = listOf(
        "game", "pubg", "freefire", "cod", "genshin", "roblox", "minecraft",
        "legend", "battle", "shooter", "arena", "clash", "brawl", "speed",
        "racing", "fifa", "efootball", "fortnite", "valorant", "apex", "warzone",
        "honkai", "wildrift", "emulator", "ppsspp", "aether", "dolphin", "mobilelegends"
    )

    suspend fun getInstalledGames(context: Context): List<InstalledGame> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = try {
            pm.queryIntentActivities(mainIntent, 0)
        } catch (_: Exception) {
            emptyList()
        }

        val games = mutableListOf<InstalledGame>()
        val seenPackages = mutableSetOf<String>()

        for (resolveInfo in resolveInfos) {
            val pkg = resolveInfo.activityInfo.packageName
            if (pkg == context.packageName || seenPackages.contains(pkg)) continue
            seenPackages.add(pkg)

            val appInfo = try {
                pm.getApplicationInfo(pkg, 0)
            } catch (_: Exception) {
                null
            } ?: continue

            val label = try {
                pm.getApplicationLabel(appInfo).toString()
            } catch (_: Exception) {
                pkg
            }

            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

            var isGame = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (appInfo.category == ApplicationInfo.CATEGORY_GAME) {
                    isGame = true
                }
            }
            @Suppress("DEPRECATION")
            if ((appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0) {
                isGame = true
            }

            val lowerPkg = pkg.lowercase()
            val lowerLabel = label.lowercase()
            if (GAME_KEYWORDS.any { lowerPkg.contains(it) || lowerLabel.contains(it) }) {
                isGame = true
            }

            games.add(
                InstalledGame(
                    packageName = pkg,
                    appName = label,
                    isSystemApp = isSystem,
                    isDetectedAsGame = isGame,
                    isAddedToLauncher = isGame // Games automatically added by default, users can toggle any app
                )
            )
        }

        // Sort: detected games first, then alphabetically
        games.sortedWith(
            compareByDescending<InstalledGame> { it.isDetectedAsGame }
                .thenBy { it.appName.lowercase() }
        )
    }

    fun getAppIcon(context: Context, packageName: String): ImageBitmap? {
        return try {
            val drawable = context.packageManager.getApplicationIcon(packageName)
            val bitmap = if (drawable.intrinsicWidth > 0 && drawable.intrinsicHeight > 0) {
                drawable.toBitmap(width = 96, height = 96)
            } else {
                val b = Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(b)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                b
            }
            bitmap.asImageBitmap()
        } catch (_: Exception) {
            null
        }
    }

    fun launchApp(context: Context, packageName: String): Boolean {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }
}
