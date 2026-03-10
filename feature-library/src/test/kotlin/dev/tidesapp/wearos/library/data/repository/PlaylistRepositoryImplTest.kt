package dev.tidesapp.wearos.library.data.repository

import com.tidal.sdk.auth.CredentialsProvider
import dev.tidesapp.wearos.library.data.api.TidesLibraryApi
import dev.tidesapp.wearos.library.data.dto.ArtistBriefDto
import dev.tidesapp.wearos.library.data.dto.CollectionPlaylistsResponseDto
import dev.tidesapp.wearos.library.data.dto.PlaylistCreatorDto
import dev.tidesapp.wearos.library.data.dto.PlaylistDataDto
import dev.tidesapp.wearos.library.data.dto.TrackDataDto
import dev.tidesapp.wearos.library.data.dto.V1TrackListResponseDto
import dev.tidesapp.wearos.library.domain.repository.PlaylistRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PlaylistRepositoryImplTest {

    private lateinit var api: TidesLibraryApi
    private lateinit var credentialsProvider: CredentialsProvider
    private lateinit var repository: PlaylistRepository

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
        repository = PlaylistRepositoryImpl(api, credentialsProvider)
    }

    @Test
    fun `getUserPlaylists fetches on first API call`() = runTest {
        val response = createPlaylistsResponse(2)
        coEvery { api.getUserPlaylists(any()) } returns response

        val result = repository.getUserPlaylists()

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        assertEquals("Playlist 0", result.getOrNull()?.first()?.title)
        coVerify(exactly = 1) { api.getUserPlaylists(any()) }
    }

    @Test
    fun `getUserPlaylists returns cached data on second call`() = runTest {
        val response = createPlaylistsResponse(3)
        coEvery { api.getUserPlaylists(any()) } returns response

        repository.getUserPlaylists()
        val result = repository.getUserPlaylists()

        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrNull()?.size)
        coVerify(exactly = 1) { api.getUserPlaylists(any()) }
    }

    @Test
    fun `getUserPlaylists refreshes with forceRefresh`() = runTest {
        val response = createPlaylistsResponse(1)
        coEvery { api.getUserPlaylists(any()) } returns response

        repository.getUserPlaylists()
        val result = repository.getUserPlaylists(forceRefresh = true)

        assertTrue(result.isSuccess)
        coVerify(exactly = 2) { api.getUserPlaylists(any()) }
    }

    @Test
    fun `getUserPlaylists returns failure when API throws exception`() = runTest {
        val exception = RuntimeException("Network error")
        coEvery { api.getUserPlaylists(any()) } throws exception

        val result = repository.getUserPlaylists()

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `getUserPlaylists makes a single API request on concurrent calls`() = runTest {
        val response = createPlaylistsResponse(2)
        coEvery { api.getUserPlaylists(any()) } returns response

        val results = List(10) {
            async { repository.getUserPlaylists() }
        }.awaitAll()

        assertEquals(10, results.size)
        results.forEach { assertTrue(it.isSuccess) }
        coVerify(exactly = 1) { api.getUserPlaylists(any()) }
    }

    @Test
    fun `getPlaylistTracks returns tracks successfully`() = runTest {
        val response = V1TrackListResponseDto(
            limit = 50,
            offset = 0,
            totalNumberOfItems = 1,
            items = listOf(
                TrackDataDto(
                    id = 1,
                    title = "Track 1",
                    duration = 180,
                    trackNumber = 1,
                    artist = ArtistBriefDto(id = 1, name = "Artist"),
                ),
            ),
        )
        coEvery { api.getPlaylistTracks(any(), any()) } returns response

        val result = repository.getPlaylistTracks("playlist-1")

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("Track 1", result.getOrNull()?.first()?.title)
    }

    private fun createPlaylistsResponse(count: Int) = CollectionPlaylistsResponseDto(
        items = List(count) { index ->
            PlaylistDataDto(
                uuid = "playlist-$index",
                title = "Playlist $index",
                description = "Description $index",
                squareImage = "aabbccdd-1122-3344-5566-77889900${index.toString().padStart(4, '0')}",
                numberOfTracks = 15,
                creator = PlaylistCreatorDto(name = "User"),
            )
        },
        totalNumberOfItems = count,
    )
}
