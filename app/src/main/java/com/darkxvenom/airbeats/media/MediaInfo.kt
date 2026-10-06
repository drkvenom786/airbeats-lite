package com.darkxvenom.airbeats.media

data class MediaInfo(
    val durationMs: Long = 0L,
    val audioTrackIndex: Int = -1,
    val mimeType: String? = null,
) {
    val hasAudio: Boolean
        get() = audioTrackIndex >= 0
    val audioMime: String?
        get() = mimeType
}
