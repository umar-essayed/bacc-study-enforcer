package com.baccalaureate.studyenforcer.service

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InstalledAppScanner(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager

    // Comprehensive signatures of distracting apps (Social, Messaging, Video, Games, Dating)
    private val distractionPackages = setOf(
        "com.whatsapp",
        "com.whatsapp.w4b",
        "org.telegram.messenger",
        "org.telegram.plus",
        "com.facebook.katana",
        "com.facebook.orca",
        "com.facebook.lite",
        "com.instagram.android",
        "com.zhiliaoapp.musically",
        "com.ss.android.ugc.trill",
        "com.google.android.youtube",
        "com.twitter.android",
        "com.twitter.android.lite",
        "com.snapchat.android",
        "com.reddit.frontpage",
        "com.discord",
        "com.pinterest",
        "com.tencent.ig",
        "com.pubg.krmobile",
        "com.dts.freefireth",
        "com.roblox.client",
        "com.supercell.brawlstars",
        "com.supercell.clashofclans",
        "com.ea.gp.fifamobile",
        "com.activision.callofduty.shooter",
        "com.king.candycrushsaga",
        "com.netflix.mediaclient",
        "net.mbc.shahid",
        "com.watchit.watchit",
        "com.spotify.music"
    )

    data class AppInfoItem(
        val packageName: String,
        val appName: String,
        val isDistraction: Boolean,
        val isBlocked: Boolean
    )

    suspend fun getInstalledDeviceApps(currentlyBlockedSet: Set<String>): List<AppInfoItem> = withContext(Dispatchers.IO) {
        val ourPackage = context.packageName
        val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

        apps
            .filter { it.packageName != ourPackage }
            .map { app ->
                val isSystem = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val label = try {
                    packageManager.getApplicationLabel(app).toString()
                } catch (e: Exception) {
                    app.packageName
                }
                val isDistraction = distractionPackages.any { app.packageName.startsWith(it) } ||
                        label.contains("WhatsApp", true) ||
                        label.contains("واتساب", true) ||
                        label.contains("Facebook", true) ||
                        label.contains("فيسبوك", true) ||
                        label.contains("Instagram", true) ||
                        label.contains("انستغرام", true) ||
                        label.contains("TikTok", true) ||
                        label.contains("تيك توك", true) ||
                        label.contains("YouTube", true) ||
                        label.contains("يوتيوب", true) ||
                        label.contains("Telegram", true) ||
                        label.contains("تليجرام", true) ||
                        label.contains("Snapchat", true) ||
                        label.contains("PUBG", true) ||
                        label.contains("Roblox", true)

                AppInfoItem(
                    packageName = app.packageName,
                    appName = label,
                    isDistraction = isDistraction,
                    isBlocked = currentlyBlockedSet.contains(app.packageName) || isDistraction
                )
            }
            // Keep user apps + any system app that is a distraction (like YouTube)
            .filter { !((packageManager.getApplicationInfo(it.packageName, 0).flags and ApplicationInfo.FLAG_SYSTEM) != 0) || it.isDistraction }
            .sortedWith(compareByDescending<AppInfoItem> { it.isDistraction }.thenBy { it.appName })
    }

    fun isPackageKnownDistraction(packageName: String): Boolean {
        return distractionPackages.any { packageName.startsWith(it) }
    }
}
