package dev.tidesapp.wearos.download.data.api

import dev.tidesapp.wearos.download.data.api.dto.OfflineClientsResponse
import dev.tidesapp.wearos.download.data.api.dto.OfflinePlaybackInfoResponse
import dev.tidesapp.wearos.download.data.api.dto.SessionResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TidesOfflineApi {

    @GET("v1/sessions")
    suspend fun getSession(
        @Header("Authorization") token: String,
    ): SessionResponse

    @GET("v1/sessions/{sessionId}")
    suspend fun getSessionById(
        @Header("Authorization") token: String,
        @Path("sessionId") sessionId: String,
    ): SessionResponse

    @GET("v1/users/{userId}/clients")
    suspend fun getOfflineClients(
        @Header("Authorization") token: String,
        @Path("userId") userId: Long,
        @Query("filter") filter: String = "HAS_OFFLINE_CONTENT",
        @Query("limit") limit: Int = 9999,
    ): OfflineClientsResponse

    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("v1/users/{userId}/clients/offline")
    suspend fun registerForOffline(
        @Header("Authorization") token: String,
        @Path("userId") userId: Long,
    ): Response<Unit>

    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("v1/users/{userId}/clients/{clientId}/offline/playlists")
    suspend fun registerPlaylistOffline(
        @Header("Authorization") token: String,
        @Path("userId") userId: Long,
        @Path("clientId") clientId: Long,
    ): Response<Unit>

    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("v1/users/{userId}/clients/{clientId}/offline/albums")
    suspend fun registerAlbumOffline(
        @Header("Authorization") token: String,
        @Path("userId") userId: Long,
        @Path("clientId") clientId: Long,
    ): Response<Unit>

    @GET("v1/tracks/{trackId}/playbackinfopostpaywall")
    suspend fun getOfflinePlaybackInfo(
        @Header("Authorization") token: String,
        @Path("trackId") trackId: String,
        @Query("playbackmode") playbackMode: String = "OFFLINE",
        @Query("assetpresentation") assetPresentation: String = "FULL",
        @Query("audioquality") audioQuality: String = "HIGH",
        @Query("immersiveaudio") immersiveAudio: Boolean = false,
        @Query("streamingsessionid") streamingSessionId: String,
    ): OfflinePlaybackInfoResponse
}
