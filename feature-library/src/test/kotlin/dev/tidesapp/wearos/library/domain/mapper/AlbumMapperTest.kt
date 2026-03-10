package dev.tidesapp.wearos.library.domain.mapper

import dev.tidesapp.wearos.library.data.dto.AlbumDataDto
import dev.tidesapp.wearos.library.data.dto.ArtistBriefDto
import dev.tidesapp.wearos.library.data.dto.PlaylistCreatorDto
import dev.tidesapp.wearos.library.data.dto.PlaylistDataDto
import dev.tidesapp.wearos.library.data.dto.TrackAlbumDto
import dev.tidesapp.wearos.library.data.dto.TrackDataDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AlbumMapperTest {

    @Test
    fun `AlbumDataDto toDomain maps all fields`() {
        val dto = AlbumDataDto(
            id = 498704813,
            title = "Lulim",
            artists = listOf(ArtistBriefDto(id = 6425648, name = "Kalki")),
            cover = "e3a885ac-49a8-4b26-b461-3d751e5961b1",
            releaseDate = "2026-02-14",
            numberOfTracks = 9,
        )

        val result = dto.toDomain()

        assertEquals("498704813", result.id)
        assertEquals("Lulim", result.title)
        assertEquals("Kalki", result.artistName)
        assertEquals(
            "https://resources.tidal.com/images/e3a885ac/49a8/4b26/b461/3d751e5961b1/320x320.jpg",
            result.imageUrl,
        )
        assertEquals("2026-02-14", result.releaseDate)
        assertEquals(9, result.numberOfTracks)
    }

    @Test
    fun `AlbumDataDto toDomain handles empty artists`() {
        val dto = AlbumDataDto(id = 1, title = "Album")

        val result = dto.toDomain()

        assertEquals("Unknown Artist", result.artistName)
    }

    @Test
    fun `AlbumDataDto toDomain handles null cover`() {
        val dto = AlbumDataDto(id = 1, title = "Album", cover = null)

        val result = dto.toDomain()

        assertNull(result.imageUrl)
    }

    @Test
    fun `TrackDataDto toDomain maps all fields`() {
        val dto = TrackDataDto(
            id = 502081569,
            title = "Nightingale Lane.",
            duration = 303,
            trackNumber = 1,
            artist = ArtistBriefDto(id = 4721588, name = "raye"),
            album = TrackAlbumDto(
                id = 502081563,
                title = "Nightingale Lane.",
                cover = "44049dc8-9fc5-4413-8f3f-14988fc9b645",
            ),
        )

        val result = dto.toDomain()

        assertEquals("502081569", result.id)
        assertEquals("Nightingale Lane.", result.title)
        assertEquals("raye", result.artistName)
        assertEquals("Nightingale Lane.", result.albumTitle)
        assertEquals(303, result.duration)
        assertEquals(1, result.trackNumber)
        assertEquals(
            "https://resources.tidal.com/images/44049dc8/9fc5/4413/8f3f/14988fc9b645/320x320.jpg",
            result.imageUrl,
        )
    }

    @Test
    fun `TrackDataDto toDomain falls back to artists list`() {
        val dto = TrackDataDto(
            id = 1,
            title = "Track",
            artist = null,
            artists = listOf(ArtistBriefDto(id = 1, name = "Fallback Artist")),
        )

        val result = dto.toDomain()

        assertEquals("Fallback Artist", result.artistName)
    }

    @Test
    fun `PlaylistDataDto toDomain maps all fields`() {
        val dto = PlaylistDataDto(
            uuid = "03cd876a-066c-49e0-8497-8aa9a9ca55b0",
            title = "Traditional Gospel Classics",
            description = "Great gospel music",
            squareImage = "d02b63f5-d9ac-40a6-895c-4ec58d7bf74d",
            numberOfTracks = 49,
            creator = PlaylistCreatorDto(id = 0, name = "TIDAL"),
        )

        val result = dto.toDomain()

        assertEquals("03cd876a-066c-49e0-8497-8aa9a9ca55b0", result.id)
        assertEquals("Traditional Gospel Classics", result.title)
        assertEquals("Great gospel music", result.description)
        assertEquals(
            "https://resources.tidal.com/images/d02b63f5/d9ac/40a6/895c/4ec58d7bf74d/320x320.jpg",
            result.imageUrl,
        )
        assertEquals(49, result.numberOfTracks)
        assertEquals("TIDAL", result.creator)
    }

    @Test
    fun `PlaylistDataDto toDomain falls back to image when squareImage is null`() {
        val dto = PlaylistDataDto(
            uuid = "p1",
            title = "Playlist",
            image = "aabbccdd-1122-3344-5566-778899001122",
            squareImage = null,
        )

        val result = dto.toDomain()

        assertEquals(
            "https://resources.tidal.com/images/aabbccdd/1122/3344/5566/778899001122/320x320.jpg",
            result.imageUrl,
        )
    }

    @Test
    fun `PlaylistDataDto toDomain handles null creator`() {
        val dto = PlaylistDataDto(uuid = "p1", title = "Playlist", creator = null)

        val result = dto.toDomain()

        assertEquals("TIDAL", result.creator)
    }

    @Test
    fun `ArtistBriefDto toDomain maps all fields`() {
        val dto = ArtistBriefDto(
            id = 4721588,
            name = "raye",
            picture = "be978241-da86-43ca-9dcc-05a0f3e69ead",
        )

        val result = dto.toDomain()

        assertEquals("4721588", result.id)
        assertEquals("raye", result.name)
        assertEquals(
            "https://resources.tidal.com/images/be978241/da86/43ca/9dcc/05a0f3e69ead/320x320.jpg",
            result.imageUrl,
        )
    }

    @Test
    fun `tidalImageUrl converts UUID dashes to slashes`() {
        val url = tidalImageUrl("44049dc8-9fc5-4413-8f3f-14988fc9b645")

        assertEquals(
            "https://resources.tidal.com/images/44049dc8/9fc5/4413/8f3f/14988fc9b645/320x320.jpg",
            url,
        )
    }

    @Test
    fun `tidalImageUrl returns null for null input`() {
        assertNull(tidalImageUrl(null))
    }
}
