package eversince.update

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment

object ApkInstaller {
    fun download(context: Context, info: UpdateInfo) {
        val request = DownloadManager.Request(Uri.parse(info.downloadUrl))
            .setTitle("EverSince ${info.version}")
            .setDescription("Downloading update")
            .setMimeType("application/vnd.android.package-archive")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "EverSince-${info.version}.apk")
        context.getSystemService(DownloadManager::class.java).enqueue(request)
    }
}
