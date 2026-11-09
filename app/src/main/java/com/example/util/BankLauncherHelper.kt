package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object BankLauncherHelper {

    fun isAppInstalled(context: Context, packageName: String): Boolean {
        if (packageName.isBlank()) return false
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            intent != null
        } catch (_: Exception) {
            false
        }
    }

    fun openBankAppOrPortal(
        context: Context,
        packageName: String,
        webPortalUrl: String,
        institutionName: String = "Bank"
    ) {
        if (packageName.isNotBlank()) {
            try {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    Toast.makeText(context, "Opening $institutionName app...", Toast.LENGTH_SHORT).show()
                    return
                }
            } catch (e: Exception) {
                // fall through to portal
            }
        }

        // Fallback to web portal
        openWebPortal(context, webPortalUrl, fallbackMessage = "Opening official $institutionName online banking portal")
    }

    fun openWebPortal(
        context: Context,
        rawUrl: String,
        fallbackMessage: String = "Opening secure banking portal"
    ) {
        val targetUrl = when {
            rawUrl.isBlank() -> "https://www.chase.com"
            !rawUrl.startsWith("http://") && !rawUrl.startsWith("https://") -> "https://$rawUrl"
            else -> rawUrl
        }

        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            Toast.makeText(context, fallbackMessage, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open browser: ${e.localizedMessage ?: "Unknown error"}", Toast.LENGTH_SHORT).show()
        }
    }

    fun openPlayStore(context: Context, packageName: String) {
        if (packageName.isBlank()) return
        try {
            val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
            marketIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(marketIntent)
        } catch (_: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
            webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(webIntent)
        }
    }
}
