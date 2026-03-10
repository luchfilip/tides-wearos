package dev.tidesapp.wearos.library.domain.mapper

import dev.tidesapp.wearos.core.domain.model.AlbumItem
import dev.tidesapp.wearos.core.domain.model.TrackItem
import dev.tidesapp.wearos.library.data.dto.AlbumDataDto
import dev.tidesapp.wearos.library.data.dto.TrackDataDto

fun AlbumDataDto.toDomain() = AlbumItem(
    id = id.toString(),
    title = title,
    artistName = artists.firstOrNull()?.name ?: "Unknown Artist",
    imageUrl = tidalImageUrl(cover),
    releaseDate = releaseDate,
    numberOfTracks = numberOfTracks,
)

fun TrackDataDto.toDomain() = TrackItem(
    id = id.toString(),
    title = title,
    artistName = artist?.name ?: artists.firstOrNull()?.name ?: "Unknown Artist",
    albumTitle = album?.title ?: "",
    duration = duration,
    trackNumber = trackNumber,
    imageUrl = tidalImageUrl(album?.cover),
)

internal fun tidalImageUrl(uuid: String?, size: Int = 320): String? {
    if (uuid == null) return null
    return "https://resources.tidal.com/images/${uuid.replace("-", "/")}/${size}x${size}.jpg"
}
