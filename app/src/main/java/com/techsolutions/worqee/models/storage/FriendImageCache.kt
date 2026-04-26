package com.techsolutions.worqee.models.storage

import android.graphics.Bitmap
import android.util.LruCache

object FriendImageCache {

    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 8  // 1/8 de la RAM disponible

    private val lruCache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024  // tamaño en KB
        }
    }

    fun put(userId: String, bitmap: Bitmap) {
        lruCache.put(userId, bitmap)
    }

    fun get(userId: String): Bitmap? {
        return lruCache.get(userId)
    }

    fun contains(userId: String): Boolean {
        return lruCache.get(userId) != null
    }

    fun clear() {
        lruCache.evictAll()
    }
}