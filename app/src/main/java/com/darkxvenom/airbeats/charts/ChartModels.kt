package com.darkxvenom.airbeats.charts

data class ChartTrack(
    val rank: Int,
    val title: String,
    val artist: String,
    val thumbnailUrl: String?,
    val appleMusicUrl: String? = null,
)

data class ChartArtist(
    val rank: Int,
    val name: String,
    val thumbnailUrl: String?,
)

data class ChartAlbum(
    val rank: Int,
    val title: String,
    val artist: String,
    val thumbnailUrl: String?,
    val appleMusicUrl: String? = null,
)
