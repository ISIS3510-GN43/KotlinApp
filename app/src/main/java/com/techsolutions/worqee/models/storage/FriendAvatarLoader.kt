package com.techsolutions.worqee.models.storage


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object FriendAvatarLoader {

    // Tamaño reducido para guardar en caché
    private const val TARGET_SIZE = 88

    suspend fun loadAvatar(context: Context, userId: String, avatarUrl: String): Bitmap? {
        // 1. Está en LruCache → retorna directo
        FriendImageCache.get(userId)?.let { return it }

        // 2. No está en caché → intentar descargar
        if (avatarUrl.isBlank()) return null

        return withContext(Dispatchers.IO) {
            try {
                val connection = URL(avatarUrl).openConnection() as HttpURLConnection
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.connect()

                val original = BitmapFactory.decodeStream(connection.inputStream)
                connection.disconnect()

                if (original != null) {
                    // Redimensionar antes de guardar en caché
                    val scaled = Bitmap.createScaledBitmap(original, TARGET_SIZE, TARGET_SIZE, true)
                    if (original != scaled) original.recycle()

                    FriendImageCache.put(userId, scaled)
                    scaled
                } else null

            } catch (e: Exception) {
                // Sin red o error → null, la UI mostrará el fallback
                null
            }
        }
    }

    fun getDefaultAvatar(context: Context): Bitmap? {
        return BitmapFactory.decodeResource(context.resources, android.R.drawable.ic_menu_myplaces)
    }
}