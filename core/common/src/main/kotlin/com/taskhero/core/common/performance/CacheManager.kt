package com.taskhero.core.common.performance

import android.util.Log
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory LRU cache manager for frequently accessed data
 *
 * This class provides caching capabilities to improve performance by storing:
 * - Urgency calculations for tasks
 * - Parsed dates
 * - Other frequently accessed computed values
 *
 * Uses Least Recently Used (LRU) eviction policy when cache size limit is reached.
 */
@Singleton
class CacheManager @Inject constructor() {

    companion object {
        private const val TAG = "CacheManager"

        // Default cache sizes for different types of data
        private const val DEFAULT_URGENCY_CACHE_SIZE = 100
        private const val DEFAULT_DATE_CACHE_SIZE = 50
        private const val DEFAULT_GENERIC_CACHE_SIZE = 50

        // Enable debug logging
        private var isDebugMode = true // Set to BuildConfig.DEBUG in production
    }

    // Individual caches for different types of data
    private val urgencyCache = LRUCache<String, Double>(DEFAULT_URGENCY_CACHE_SIZE)
    private val dateCache = LRUCache<String, LocalDateTime>(DEFAULT_DATE_CACHE_SIZE)
    private val genericCache = LRUCache<String, Any>(DEFAULT_GENERIC_CACHE_SIZE)

    // Cache statistics
    private var urgencyCacheHits = 0
    private var urgencyCacheMisses = 0
    private var dateCacheHits = 0
    private var dateCacheMisses = 0
    private var genericCacheHits = 0
    private var genericCacheMisses = 0

    /**
     * Cache an urgency calculation result
     *
     * @param taskId The task ID
     * @param urgency The calculated urgency value
     */
    fun cacheUrgency(taskId: String, urgency: Double) {
        urgencyCache.put(taskId, urgency)
        logDebug("Cached urgency for task: $taskId = $urgency")
    }

    /**
     * Get cached urgency for a task
     *
     * @param taskId The task ID
     * @return Cached urgency value or null if not found
     */
    fun getUrgency(taskId: String): Double? {
        val result = urgencyCache.get(taskId)
        if (result != null) {
            urgencyCacheHits++
            logDebug("Urgency cache hit for task: $taskId")
        } else {
            urgencyCacheMisses++
            logDebug("Urgency cache miss for task: $taskId")
        }
        return result
    }

    /**
     * Invalidate cached urgency for a specific task
     *
     * @param taskId The task ID to invalidate
     */
    fun invalidateUrgency(taskId: String) {
        urgencyCache.remove(taskId)
        logDebug("Invalidated urgency cache for task: $taskId")
    }

    /**
     * Cache a parsed date
     *
     * @param dateString The original date string
     * @param parsedDate The parsed LocalDateTime
     */
    fun cacheDate(dateString: String, parsedDate: LocalDateTime) {
        dateCache.put(dateString, parsedDate)
        logDebug("Cached parsed date: $dateString")
    }

    /**
     * Get cached parsed date
     *
     * @param dateString The date string to look up
     * @return Cached LocalDateTime or null if not found
     */
    fun getDate(dateString: String): LocalDateTime? {
        val result = dateCache.get(dateString)
        if (result != null) {
            dateCacheHits++
            logDebug("Date cache hit for: $dateString")
        } else {
            dateCacheMisses++
            logDebug("Date cache miss for: $dateString")
        }
        return result
    }

    /**
     * Parse and cache a date string
     *
     * @param dateString The date string to parse
     * @param formatter The DateTimeFormatter to use
     * @return Parsed LocalDateTime
     */
    fun parseAndCacheDate(
        dateString: String,
        formatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    ): LocalDateTime {
        // Check cache first
        getDate(dateString)?.let { return it }

        // Parse and cache
        val parsed = LocalDateTime.parse(dateString, formatter)
        cacheDate(dateString, parsed)
        return parsed
    }

    /**
     * Cache a generic value
     *
     * @param key The cache key
     * @param value The value to cache
     */
    fun <T : Any> cacheValue(key: String, value: T) {
        genericCache.put(key, value)
        logDebug("Cached value for key: $key")
    }

    /**
     * Get cached generic value
     *
     * @param key The cache key
     * @return Cached value or null if not found
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getValue(key: String): T? {
        val result = genericCache.get(key)
        if (result != null) {
            genericCacheHits++
            logDebug("Generic cache hit for key: $key")
        } else {
            genericCacheMisses++
            logDebug("Generic cache miss for key: $key")
        }
        return result as? T
    }

    /**
     * Get or compute a value, caching the result
     *
     * @param key The cache key
     * @param compute Function to compute the value if not cached
     * @return The cached or computed value
     */
    suspend fun <T : Any> getOrCompute(key: String, compute: suspend () -> T): T {
        // Check cache first
        getValue<T>(key)?.let { return it }

        // Compute and cache
        val computed = compute()
        cacheValue(key, computed)
        return computed
    }

    /**
     * Invalidate a specific cache entry
     *
     * @param key The cache key to invalidate
     */
    fun invalidate(key: String) {
        genericCache.remove(key)
        logDebug("Invalidated cache for key: $key")
    }

    /**
     * Invalidate all caches
     */
    fun invalidateAll() {
        urgencyCache.clear()
        dateCache.clear()
        genericCache.clear()
        resetStatistics()
        logDebug("All caches invalidated")
    }

    /**
     * Get cache statistics
     */
    fun getStatistics(): CacheStatistics {
        val urgencyHitRate = calculateHitRate(urgencyCacheHits, urgencyCacheMisses)
        val dateHitRate = calculateHitRate(dateCacheHits, dateCacheMisses)
        val genericHitRate = calculateHitRate(genericCacheHits, genericCacheMisses)

        return CacheStatistics(
            urgencyCacheSize = urgencyCache.size(),
            urgencyCacheHits = urgencyCacheHits,
            urgencyCacheMisses = urgencyCacheMisses,
            urgencyHitRate = urgencyHitRate,
            dateCacheSize = dateCache.size(),
            dateCacheHits = dateCacheHits,
            dateCacheMisses = dateCacheMisses,
            dateHitRate = dateHitRate,
            genericCacheSize = genericCache.size(),
            genericCacheHits = genericCacheHits,
            genericCacheMisses = genericCacheMisses,
            genericHitRate = genericHitRate
        )
    }

    /**
     * Log cache statistics
     */
    fun logStatistics() {
        val stats = getStatistics()
        Log.i(TAG, """
            === Cache Statistics ===
            Urgency Cache:
              Size: ${stats.urgencyCacheSize}
              Hits: ${stats.urgencyCacheHits}
              Misses: ${stats.urgencyCacheMisses}
              Hit Rate: ${stats.urgencyHitRate}%

            Date Cache:
              Size: ${stats.dateCacheSize}
              Hits: ${stats.dateCacheHits}
              Misses: ${stats.dateCacheMisses}
              Hit Rate: ${stats.dateHitRate}%

            Generic Cache:
              Size: ${stats.genericCacheSize}
              Hits: ${stats.genericCacheHits}
              Misses: ${stats.genericCacheMisses}
              Hit Rate: ${stats.genericHitRate}%
            ========================
        """.trimIndent())
    }

    /**
     * Reset cache statistics
     */
    private fun resetStatistics() {
        urgencyCacheHits = 0
        urgencyCacheMisses = 0
        dateCacheHits = 0
        dateCacheMisses = 0
        genericCacheHits = 0
        genericCacheMisses = 0
    }

    private fun calculateHitRate(hits: Int, misses: Int): Double {
        val total = hits + misses
        return if (total > 0) (hits.toDouble() / total * 100) else 0.0
    }

    private fun logDebug(message: String) {
        if (isDebugMode) {
            Log.d(TAG, message)
        }
    }

    /**
     * Set debug mode
     */
    fun setDebugMode(enabled: Boolean) {
        isDebugMode = enabled
    }
}

/**
 * Generic LRU (Least Recently Used) cache implementation
 *
 * @param maxSize Maximum number of entries in the cache
 */
private class LRUCache<K, V>(private val maxSize: Int) {

    private val cache = object : LinkedHashMap<K, V>(maxSize, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean {
            return size > maxSize
        }
    }

    // Use ConcurrentHashMap wrapper for thread safety
    private val threadSafeCache = ConcurrentHashMap<K, V>()

    init {
        require(maxSize > 0) { "Max size must be greater than 0" }
    }

    @Synchronized
    fun get(key: K): V? {
        // Use regular LinkedHashMap for proper LRU behavior with access order
        return cache[key]?.also {
            // Update in thread-safe map
            threadSafeCache[key] = it
        }
    }

    @Synchronized
    fun put(key: K, value: V) {
        cache[key] = value
        threadSafeCache[key] = value

        // Clean up thread-safe map if cache evicted entries
        if (threadSafeCache.size > maxSize) {
            val keysToRemove = threadSafeCache.keys - cache.keys
            keysToRemove.forEach { threadSafeCache.remove(it) }
        }
    }

    @Synchronized
    fun remove(key: K): V? {
        threadSafeCache.remove(key)
        return cache.remove(key)
    }

    @Synchronized
    fun clear() {
        cache.clear()
        threadSafeCache.clear()
    }

    @Synchronized
    fun size(): Int = cache.size

    @Synchronized
    fun containsKey(key: K): Boolean = cache.containsKey(key)
}

/**
 * Data class containing cache statistics
 */
data class CacheStatistics(
    val urgencyCacheSize: Int,
    val urgencyCacheHits: Int,
    val urgencyCacheMisses: Int,
    val urgencyHitRate: Double,
    val dateCacheSize: Int,
    val dateCacheHits: Int,
    val dateCacheMisses: Int,
    val dateHitRate: Double,
    val genericCacheSize: Int,
    val genericCacheHits: Int,
    val genericCacheMisses: Int,
    val genericHitRate: Double
)
