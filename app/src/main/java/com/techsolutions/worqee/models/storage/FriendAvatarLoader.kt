package com.techsolutions.worqee.models.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object FriendAvatarLoader {

    private const val TARGET_SIZE = 88

    suspend fun loadAvatar(context: Context, userId: String, avatarUrl: String): Bitmap? {
        FriendImageCache.get(userId)?.let { return it }

        if (avatarUrl.isBlank()) return null

        return withContext(Dispatchers.IO) {
            try {
                val connection = URL(avatarUrl).openConnection() as HttpURLConnection
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.connect()

                val originalBitmap = BitmapFactory.decodeStream(connection.inputStream)
                connection.disconnect()

                if (originalBitmap != null) {
                    val scaledBitmap = Bitmap.createScaledBitmap(
                        originalBitmap,
                        TARGET_SIZE,
                        TARGET_SIZE,
                        true
                    )

                    if (originalBitmap != scaledBitmap) {
                        originalBitmap.recycle()
                    }

                    FriendImageCache.put(userId, scaledBitmap)
                    scaledBitmap
                } else {
                    null
                }

            } catch (e: Exception) {
                null
            }
        }
    }

    fun getDefaultAvatar(context: Context): Bitmap? {
        return BitmapFactory.decodeResource(
            context.resources,
            android.R.drawable.ic_menu_myplaces
        )
    }
}