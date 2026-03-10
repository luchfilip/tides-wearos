package dev.tidesapp.wearos.library.domain.mapper

import dev.tidesapp.wearos.core.domain.model.ArtistItem
import dev.tidesapp.wearos.core.domain.model.PlaylistItem
import dev.tidesapp.wearos.library.data.dto.ArtistBriefDto
import dev.tidesapp.wearos.library.data.dto.PlaylistDataDto

fun PlaylistDataDto.toDomain() = PlaylistItem(
    id = uuid,
    title = title,
    description = description,
    imageUrl = tidalImageUrl(squareImage ?: image),
    numberOfTracks = numberOfTracks,
    creator = creator?.name ?: "TIDAL",
)

fun ArtistBriefDto.toDomain() = ArtistItem(
    id = id.toString(),
    name = name,
    imageUrl = tidalImageUrl(picture),
)
