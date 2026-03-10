package dev.tidesapp.wearos.library.data.repository

import com.tidal.sdk.auth.CredentialsProvider
import dev.tidesapp.wearos.library.data.api.TidesLibraryApi
import dev.tidesapp.wearos.library.data.dto.AlbumDataDto
import dev.tidesapp.wearos.library.data.dto.ArtistBriefDto
import dev.tidesapp.wearos.library.data.dto.SearchAlbumsSection
import dev.tidesapp.wearos.library.data.dto.SearchArtistsSection
import dev.tidesapp.wearos.library.data.dto.SearchResponseDto
import dev.tidesapp.wearos.library.data.dto.SearchTracksSection
import dev.tidesapp.wearos.library.data.dto.TrackDataDto
import dev.tidesapp.wearos.library.domain.repository.SearchRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SearchRepositoryImplTest {

    private lateinit var api: TidesLibraryApi
    private lateinit var credentialsProvider: CredentialsProvider
    private lateinit var repository: SearchRepository

    @Before
    fun setup() {
        api = mockk()
        credentialsProvider = mockk {
            coEvery { getCredentials(any()) } returns mockk {
                every { successData } returns mockk {
                    every { token } returns "fake-jwt-token"
                }
            }
        }
        repository = SearchRepositoryImpl(api, credentialsProvider)
    }

    @Test
    fun `search returns grouped results`() = runTest {
        val response = SearchResponseDto(
            albums = SearchAlbumsSection(
                items = listOf(
                    AlbumDataDto(id = 1, title = "Found Album", artists = listOf(ArtistBriefDto(name = "Artist")))
                )
            ),
            tracks = SearchTracksSection(
                items = listOf(
                    TrackDataDto(id = 1, title = "Found Track", artist = ArtistBriefDto(name = "Artist"))
                )
            ),
            artists = SearchArtistsSection(
                items = listOf(
                    ArtistBriefDto(id = 1, name = "Found Artist")
                )
            ),
        )
        coEvery { api.search(any(), any()) } returns response

        val result = repository.search("test query")

        assertTrue(result.isSuccess)
        val searchResult = result.getOrNull()!!
        assertEquals(1, searchResult.albums.size)
        assertEquals("Found Album", searchResult.albums.first().title)
        assertEquals(1, searchResult.tracks.size)
        assertEquals("Found Track", searchResult.tracks.first().title)
        assertEquals(1, searchResult.artists.size)
        assertEquals("Found Artist", searchResult.artists.first().name)
        assertEquals(0, searchResult.playlists.size)
    }

    @Test
    fun `search returns failure when API throws exception`() = runTest {
        val exception = RuntimeException("Search failed")
        coEvery { api.search(any(), any()) } throws exception

        val result = repository.search("query")

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `search handles empty results`() = runTest {
        val response = SearchResponseDto()
        coEvery { api.search(any(), any()) } returns response

        val result = repository.search("nothing")

        assertTrue(result.isSuccess)
        val searchResult = result.getOrNull()!!
        assertEquals(0, searchResult.albums.size)
        assertEquals(0, searchResult.tracks.size)
        assertEquals(0, searchResult.playlists.size)
        assertEquals(0, searchResult.artists.size)
    }
}
