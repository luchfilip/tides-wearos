package dev.tidesapp.wearos.library.domain.mapper

import dev.tidesapp.wearos.core.domain.model.HomeFeedItem
import dev.tidesapp.wearos.core.domain.model.HomeFeedSection
import dev.tidesapp.wearos.library.data.dto.HomePageItemDto
import dev.tidesapp.wearos.library.data.dto.HomePageModuleDto
import kotlinx.collections.immutable.toImmutableList

private const val MAX_ITEMS_PER_SECTION = 5

fun HomePageModuleDto.toDomain(): HomeFeedSection? {
    if (title.isBlank()) return null
    val pagedItems = pagedList?.items ?: return null
    val feedItems = pagedItems
        .take(MAX_ITEMS_PER_SECTION)
        .mapNotNull { it.toDomain(type) }
    if (feedItems.isEmpty()) return null
    return HomeFeedSection(
        title = title,
        items = feedItems.toImmutableList(),
    )
}

fun HomePageItemDto.toDomain(moduleType: String): HomeFeedItem? {
    return when (moduleType) {
        "ALBUM_LIST" -> HomeFeedItem.Album(
            id = id.toString(),
            title = title,
            imageUrl = tidalImageUrl(cover),
            artistName = artists?.firstOrNull()?.name ?: "Unknown Artist",
        )
        "PLAYLIST_LIST" -> {
            val playlistId = uuid ?: return null
            HomeFeedItem.Playlist(
                id = playlistId,
                title = title,
                imageUrl = tidalImageUrl(squareImage ?: image),
                creator = promotedArtists?.firstOrNull()?.name ?: "TIDAL",
            )
        }
        "MIX_LIST" -> {
            val imageUrl = images?.SMALL?.url ?: images?.MEDIUM?.url
            HomeFeedItem.Mix(
                id = id.toString(),
                title = title,
                imageUrl = imageUrl,
                subTitle = subTitle,
            )
        }
        else -> null
    }
}
