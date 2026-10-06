package com.darkxvenom.airbeats.charts

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import timber.log.Timber
import java.util.concurrent.TimeUnit

object AppleMusicChartsScraper {
    private const val TAG = "AppleMusicChartsScraper"

    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }

    private fun executeGet(url: String): String? {
        return try {
            val request = Request.Builder()
                .url(url)
                .header(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
                )
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Timber.tag(TAG).w("GET failed: HTTP %d for %s", response.code, url)
                    return null
                }
                response.body?.string()
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error executing GET request for %s", url)
            null
        }
    }

    fun fetchTopSongs(countryCode: String = "us"): List<ChartTrack> {
        val tracks = mutableListOf<ChartTrack>()
        try {
            val url = "https://rss.marketingtools.apple.com/api/v2/$countryCode/music/most-played/100/songs.json"
            val response = executeGet(url) ?: return tracks

            val json = JSONObject(response)
            val results = json.getJSONObject("feed").getJSONArray("results")

            for (i in 0 until results.length()) {
                val item = results.getJSONObject(i)
                val rank = i + 1
                val title = item.getString("name")
                val artist = item.getString("artistName")
                val artwork = item.getString("artworkUrl100").replace(Regex("(\\d+)x(\\d+)"), "1400x1400")
                val appleUrl = item.optString("url", null)

                tracks.add(ChartTrack(rank, title, artist, artwork, appleUrl))
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error fetching Apple Music Top Songs for %s", countryCode)
        }
        return tracks
    }

    fun fetchTopAlbums(countryCode: String = "us"): List<ChartAlbum> {
        val albums = mutableListOf<ChartAlbum>()
        try {
            val url = "https://rss.marketingtools.apple.com/api/v2/$countryCode/music/most-played/20/albums.json"
            val response = executeGet(url) ?: return albums

            val json = JSONObject(response)
            val results = json.getJSONObject("feed").getJSONArray("results")

            for (i in 0 until results.length()) {
                val item = results.getJSONObject(i)
                val rank = i + 1
                val title = item.getString("name")
                val artist = item.getString("artistName")
                val artwork = item.getString("artworkUrl100").replace(Regex("(\\d+)x(\\d+)"), "1400x1400")
                val appleUrl = item.optString("url", null)

                albums.add(ChartAlbum(rank, title, artist, artwork, appleUrl))
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error fetching Apple Music Top Albums for %s", countryCode)
        }
        return albums
    }

    fun fetchTopVideos(countryCode: String = "us"): List<ChartTrack> {
        val videos = mutableListOf<ChartTrack>()
        try {
            val url = "https://rss.marketingtools.apple.com/api/v2/$countryCode/music/most-played/20/music-videos.json"
            val response = executeGet(url) ?: return videos

            val json = JSONObject(response)
            val results = json.getJSONObject("feed").getJSONArray("results")

            val videoMap = mutableMapOf<String, ChartTrack>()

            for (i in 0 until results.length()) {
                val item = results.getJSONObject(i)
                val id = item.optString("id", i.toString())
                val rank = i + 1
                val title = item.getString("name")
                val artist = item.getString("artistName")
                val artwork = item.getString("artworkUrl100").replace(Regex("(\\d+)x(\\d+)"), "1920x1080")
                val appleUrl = item.optString("url", null)

                videoMap[id] = ChartTrack(rank, title, artist, artwork, appleUrl)
            }

            videos.addAll(videoMap.values.sortedBy { it.rank })
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error fetching Apple Music Top Videos for %s", countryCode)
        }
        return videos
    }

    fun getTrendingArtists(tracks: List<ChartTrack>): List<ChartArtist> {
        val artistCounts = mutableMapOf<String, Int>()
        val artistImages = mutableMapOf<String, String?>()

        tracks.forEach { track ->
            val mainArtist = track.artist.split(",", "&", "feat.", "ft.").first().trim()
            if (mainArtist.isNotEmpty()) {
                artistCounts[mainArtist] = (artistCounts[mainArtist] ?: 0) + 1
                if (artistImages[mainArtist] == null) {
                    artistImages[mainArtist] = track.thumbnailUrl?.replace("1400x1400", "500x500")
                }
            }
        }

        return artistCounts
            .toList()
            .sortedByDescending { it.second }
            .take(15)
            .mapIndexed { index, pair ->
                ChartArtist(index + 1, pair.first, artistImages[pair.first])
            }
    }
}
