package dev.tidesapp.wearos.library.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class HomePageResponseDto(
    val title: String = "",
    val rows: List<HomePageRowDto> = emptyList(),
)

@Serializable
data class HomePageRowDto(
    val modules: List<HomePageModuleDto> = emptyList(),
)

@Serializable
data class HomePageModuleDto(
    val type: String = "",
    val title: String = "",
    val pagedList: HomePagedListDto? = null,
)

@Serializable
data class HomePagedListDto(
    val items: List<HomePageItemDto> = emptyList(),
    val totalNumberOfItems: Int = 0,
)

@Serializable
data class HomePageItemDto(
    // Playlist fields
    val uuid: String? = null,
    val squareImage: String? = null,
    val image: String? = null,
    val description: String? = null,
    val promotedArtists: List<ArtistBriefDto>? = null,
    // Album fields
    val id: Long = 0,
    val title: String = "",
    val artists: List<ArtistBriefDto>? = null,
    val cover: String? = null,
    val numberOfTracks: Int = 0,
    val releaseDate: String? = null,
    // Track fields
    val duration: Int = 0,
    val album: HomePageItemAlbumDto? = null,
    // Mix fields
    val subTitle: String? = null,
    val images: HomePageMixImagesDto? = null,
)

@Serializable
data class HomePageItemAlbumDto(
    val title: String = "",
    val cover: String? = null,
)

@Serializable
data class HomePageMixImagesDto(
    val SMALL: HomePageMixImageDto? = null,
    val MEDIUM: HomePageMixImageDto? = null,
    val LARGE: HomePageMixImageDto? = null,
)

@Serializable
data class HomePageMixImageDto(
    val url: String = "",
    val width: Int = 0,
    val height: Int = 0,
)
