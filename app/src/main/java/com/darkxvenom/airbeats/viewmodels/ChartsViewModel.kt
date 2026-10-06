package com.darkxvenom.airbeats.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.darkxvenom.airbeats.charts.AppleMusicChartsScraper
import com.darkxvenom.airbeats.charts.ChartAlbum
import com.darkxvenom.airbeats.charts.ChartArtist
import com.darkxvenom.airbeats.charts.ChartTrack
import com.darkxvenom.airbeats.innertube.YouTube
import com.darkxvenom.airbeats.innertube.models.AlbumItem
import com.darkxvenom.airbeats.innertube.models.ArtistItem
import com.darkxvenom.airbeats.innertube.models.SongItem
import com.darkxvenom.airbeats.innertube.models.WatchEndpoint
import com.darkxvenom.airbeats.models.toMediaMetadata
import com.darkxvenom.airbeats.playback.PlayerConnection
import com.darkxvenom.airbeats.playback.queues.YouTubeQueue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ChartsViewModel @Inject constructor() : ViewModel() {
    private var currentLoadedRegion: String? = null

    private val _chartTracks = MutableStateFlow<List<ChartTrack>?>(null)
    val chartTracks = _chartTracks.asStateFlow()

    private val _chartArtists = MutableStateFlow<List<ChartArtist>?>(null)
    val chartArtists = _chartArtists.asStateFlow()

    private val _chartAlbums = MutableStateFlow<List<ChartAlbum>?>(null)
    val chartAlbums = _chartAlbums.asStateFlow()

    private val _chartVideos = MutableStateFlow<List<ChartTrack>?>(null)
    val chartVideos = _chartVideos.asStateFlow()

    private val _jioSaavnTopSongs = MutableStateFlow<List<SongItem>?>(null)
    val jioSaavnTopSongs = _jioSaavnTopSongs.asStateFlow()

    private val _youtubeChartsPage = MutableStateFlow<com.darkxvenom.airbeats.innertube.pages.ChartsPage?>(null)
    val youtubeChartsPage = _youtubeChartsPage.asStateFlow()

    private val _newReleases = MutableStateFlow<List<AlbumItem>?>(null)
    val newReleases = _newReleases.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isManualLoading = MutableStateFlow(false)
    val isManualLoading = _isManualLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun loadCharts(countryCode: String = "system") {
        refresh(countryCode = countryCode, force = false)
    }

    fun retry(countryCode: String = "system") {
        refresh(countryCode = countryCode, force = true)
    }

    fun refresh(countryCode: String = "system", force: Boolean = false) {
        val resolvedCode = if (countryCode == "system") {
            Locale.getDefault().country.lowercase().ifEmpty { "us" }
        } else {
            countryCode.lowercase()
        }

        if (_isLoading.value && !force && currentLoadedRegion == resolvedCode) return

        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _error.value = null
            if (force) _isManualLoading.value = true

            if (currentLoadedRegion != resolvedCode || force) {
                _chartTracks.value = null
                _chartArtists.value = null
                _chartAlbums.value = null
                _chartVideos.value = null
            }

            try {
                coroutineScope {
                    launch {
                        try {
                            val tracks = AppleMusicChartsScraper.fetchTopSongs(resolvedCode)
                            if (tracks.isNotEmpty()) {
                                _chartTracks.value = tracks
                                _chartArtists.value = AppleMusicChartsScraper.getTrendingArtists(tracks)
                            }
                        } catch (e: Exception) {
                            Timber.tag("ChartsViewModel").e(e, "Failed to fetch top songs for %s", resolvedCode)
                        }
                    }

                    launch {
                        try {
                            val albums = AppleMusicChartsScraper.fetchTopAlbums(resolvedCode)
                            if (albums.isNotEmpty()) {
                                _chartAlbums.value = albums
                            }
                        } catch (e: Exception) {
                            Timber.tag("ChartsViewModel").e(e, "Failed to fetch top albums for %s", resolvedCode)
                        }
                    }

                    launch {
                        try {
                            val videos = AppleMusicChartsScraper.fetchTopVideos(resolvedCode)
                            if (videos.isNotEmpty()) {
                                _chartVideos.value = videos
                            }
                        } catch (e: Exception) {
                            Timber.tag("ChartsViewModel").e(e, "Failed to fetch top videos for %s", resolvedCode)
                        }
                    }

                    launch {
                        try {
                            val jioTrending = com.darkxvenom.airbeats.jiosaavn.JioSaavnApi.getTrendingSongs().getOrNull()
                            if (!jioTrending.isNullOrEmpty()) {
                                _jioSaavnTopSongs.value = jioTrending
                            }
                        } catch (e: Exception) {
                            Timber.tag("ChartsViewModel").e(e, "Failed to fetch JioSaavn trending songs")
                        }
                    }

                    launch {
                        try {
                            val ytCharts = YouTube.getChartsPage().getOrNull()
                            if (ytCharts != null) {
                                _youtubeChartsPage.value = ytCharts
                            }
                        } catch (e: Exception) {
                            Timber.tag("ChartsViewModel").e(e, "Failed to fetch YouTube charts")
                        }
                    }

                    launch {
                        try {
                            val explore = YouTube.explore().getOrNull()
                            if (explore != null && explore.newReleaseAlbums.isNotEmpty()) {
                                _newReleases.value = explore.newReleaseAlbums
                            }
                        } catch (e: Exception) {
                            Timber.tag("ChartsViewModel").e(e, "Failed to fetch new releases")
                        }
                    }
                }

                currentLoadedRegion = resolvedCode
            } catch (e: Exception) {
                Timber.tag("ChartsViewModel").e(e, "Failed to load Apple Music charts")
                _error.value = e.message ?: "Failed to load charts"
            } finally {
                _isLoading.value = false
                _isManualLoading.value = false
            }
        }
    }

    private fun artistMatches(ytArtistName: String, appleArtistName: String): Boolean {
        val ytNorm = ytArtistName.trim().lowercase()
        val apNorm = appleArtistName.trim().lowercase()
        return apNorm.contains(ytNorm) || ytNorm.contains(apNorm)
    }

    fun playTrack(track: ChartTrack, playerConnection: PlayerConnection?) {
        viewModelScope.launch(Dispatchers.IO) {
            val query = "${track.title} ${track.artist}"
            Timber.tag("ChartsViewModel").d("Searching YouTube for track: %s", query)
            YouTube.search(query, YouTube.SearchFilter.FILTER_SONG).onSuccess { searchResult ->
                val songs = searchResult.items.filterIsInstance<SongItem>()
                val bestMatch = songs.firstOrNull { s ->
                    s.title.equals(track.title, ignoreCase = true) &&
                        s.artists.any { a -> artistMatches(a.name, track.artist) }
                } ?: songs.firstOrNull { s ->
                    s.title.contains(track.title, ignoreCase = true) &&
                        s.artists.any { a -> artistMatches(a.name, track.artist) }
                } ?: songs.firstOrNull { s ->
                    s.artists.any { a -> artistMatches(a.name, track.artist) }
                } ?: songs.firstOrNull()

                if (bestMatch != null) {
                    withContext(Dispatchers.Main) {
                        playerConnection?.playQueue(
                            YouTubeQueue(
                                endpoint = WatchEndpoint(videoId = bestMatch.id),
                                preloadItem = bestMatch.toMediaMetadata()
                            )
                        )
                    }
                } else {
                    Timber.tag("ChartsViewModel").w("No YouTube match found for: %s", query)
                }
            }.onFailure { e ->
                Timber.tag("ChartsViewModel").e(e, "YouTube search failed for: %s", query)
            }
        }
    }

    fun navigateToArtist(artist: ChartArtist, navController: NavController) {
        viewModelScope.launch(Dispatchers.IO) {
            YouTube.search(artist.name, YouTube.SearchFilter.FILTER_ARTIST).onSuccess { searchResult ->
                val firstArtist = searchResult.items.filterIsInstance<ArtistItem>().firstOrNull()
                if (firstArtist != null) {
                    withContext(Dispatchers.Main) {
                        navController.navigate("artist/${firstArtist.id}")
                    }
                }
            }
        }
    }

    fun navigateToAlbum(album: ChartAlbum, navController: NavController) {
        viewModelScope.launch(Dispatchers.IO) {
            val query = "${album.title} ${album.artist}"
            YouTube.search(query, YouTube.SearchFilter.FILTER_ALBUM).onSuccess { searchResult ->
                val firstAlbum = searchResult.items.filterIsInstance<AlbumItem>().firstOrNull()
                if (firstAlbum != null) {
                    withContext(Dispatchers.Main) {
                        navController.navigate("album/${firstAlbum.id}")
                    }
                }
            }
        }
    }

    fun playVideo(video: ChartTrack, playerConnection: PlayerConnection?) {
        viewModelScope.launch(Dispatchers.IO) {
            val query = "${video.title} ${video.artist}"
            YouTube.search(query, YouTube.SearchFilter.FILTER_VIDEO).onSuccess { searchResult ->
                val songMatch = searchResult.items.filterIsInstance<SongItem>().firstOrNull()
                if (songMatch != null) {
                    withContext(Dispatchers.Main) {
                        playerConnection?.playQueue(
                            YouTubeQueue(
                                endpoint = WatchEndpoint(videoId = songMatch.id),
                                preloadItem = songMatch.toMediaMetadata()
                            )
                        )
                    }
                }
            }
        }
    }
}
