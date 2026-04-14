package dev.tidesapp.wearos.library.data.repository

import com.tidal.sdk.auth.CredentialsProvider
import dev.tidesapp.wearos.library.data.api.TidesLibraryApi
import dev.tidesapp.wearos.library.data.dto.ArtistBriefDto
import dev.tidesapp.wearos.library.data.dto.TrackAlbumDto
import dev.tidesapp.wearos.library.data.dto.TrackDataDto
import dev.tidesapp.wearos.library.data.dto.V1MixItemEnvelopeDto
import dev.tidesapp.wearos.library.data.dto.V1MixItemsResponseDto
import dev.tidesapp.wearos.library.domain.repository.MixRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class MixRepositoryImplTest {

    private lateinit var api: TidesLibraryApi
    private lateinit var credentialsProvider: CredentialsProvider
    private lateinit var repository: MixRepository

    private val sampleResponse = V1MixItemsResponseDto(
        limit = 100,
        offset = 0,
        totalNumberOfItems = 2,
        items = listOf(
            V1MixItemEnvelopeDto(
                item = TrackDataDto(
                    id = 44768802,
                    title = "What Else Is There?",
                    duration = 317,
                    trackNumber = 7,
                    volumeNumber = 1,
                    artist = ArtistBriefDto(id = 9246, name = "Röyksopp"),
                    album = TrackAlbumDto(
                        id = 44768795,
                        title = "The Understanding",
                        cover = "302e67d7-72d9-483d-bd1a-4b0ef892445d",
                    ),
                ),
                type = "track",
            ),
            V1MixItemEnvelopeDto(
                item = TrackDataDto(
                    id = 19009024,
                    title = "Queen of the Underground",
                    duration = 328,
                    trackNumber = 1,
                    volumeNumber = 1,
                    artist = ArtistBriefDto(id = 21246, name = "Flunk"),
                    album = TrackAlbumDto(
                        id = 19009023,
                        title = "Lost Causes",
                        cover = "9f0f0cea-9b58-438c-9f22-6af4b331073c",
                    ),
                ),
                type = "track",
            ),
        ),
    )

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
        repository = MixRepositoryImpl(api, credentialsProvider)
    }

    @Test
    fun `getMixItems unwraps the envelope and maps each track`() = runTest {
        coEvery {
            api.getMixItems(
                token = "Bearer fake-jwt-token",
                mixId = "mix-abc",
                offset = 0,
                limit = 100,
            )
        } returns sampleResponse

        val result = repository.getMixItems("mix-abc")

        assertTrue(result.isSuccess)
        val tracks = result.getOrNull()!!
        assertEquals(2, tracks.size)
        assertEquals("44768802", tracks[0].id)
        assertEquals("What Else Is There?", tracks[0].title)
        assertEquals("Röyksopp", tracks[0].artistName)
        assertEquals("The Understanding", tracks[0].albumTitle)
        assertEquals(317, tracks[0].duration)
        assertEquals("19009024", tracks[1].id)
        coVerify(exactly = 1) {
            api.getMixItems(
                token = "Bearer fake-jwt-token",
                mixId = "mix-abc",
                offset = 0,
                limit = 100,
            )
        }
    }

    @Test
    fun `getMixItems returns failure and skips API when credentials token is null`() = runTest {
        credentialsProvider = mockk {
            coEvery { getCredentials(any()) } returns mockk {
                every { successData } returns mockk {
                    every { token } returns null
                }
            }
        }
        repository = MixRepositoryImpl(api, credentialsProvider)

        val result = repository.getMixItems("mix-abc")

        assertTrue(result.isFailure)
        val err = result.exceptionOrNull()
        assertNotNull(err)
        assertEquals("Failed to obtain credentials", err!!.message)
        coVerify(exactly = 0) { api.getMixItems(any(), any(), any(), any()) }
    }

    @Test
    fun `getMixItems wraps API exceptions as failure`() = runTest {
        coEvery {
            api.getMixItems(any(), any(), any(), any())
        } throws IOException("boom")

        val result = repository.getMixItems("mix-abc")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
    }

    @Test
    fun `getMixItems drops envelopes that have no track payload`() = runTest {
        val withNullItem = V1MixItemsResponseDto(
            items = listOf(
                V1MixItemEnvelopeDto(item = null, type = "track"),
                V1MixItemEnvelopeDto(
                    item = TrackDataDto(id = 1L, title = "Only Good One"),
                    type = "track",
                ),
            ),
        )
        coEvery { api.getMixItems(any(), any(), any(), any()) } returns withNullItem

        val result = repository.getMixItems("mix-abc")

        assertTrue(result.isSuccess)
        val tracks = result.getOrNull()!!
        assertEquals(1, tracks.size)
        assertEquals("1", tracks[0].id)
    }
}
