# Tides — Offline Download Feature Implementation Plan

## Overview

Add offline download and playback support to Tides (Wear OS). Users can download playlists, albums, and individual tracks for offline listening. Downloads are stored locally with configurable storage limits and quality settings.

**New module**: `feature-download` — follows the same Clean Architecture pattern as existing feature modules.

**Key API flow** (from Charles capture of official Tidal app v2.187.0):
```
POST /v1/users/{uid}/clients/offline              → register device (once)
POST /v1/users/{uid}/clients/{cid}/offline/playlists → register playlist
POST /v1/users/{uid}/clients/{cid}/offline/albums    → register album
GET  /v1/tracks/{id}/playbackinfopostpaywall?playbackmode=OFFLINE → get manifest
GET  CDN /mediatracks/{hash}/0.mp4 ... N.mp4       → download segments
```

No DRM on FLAC/AAC offline content (confirmed via Charles — no `keyId`, no `<ContentProtection>`).

---

## Architecture Decisions

| Decision | Choice | Rationale |
|---|---|---|
| New module | `feature-download` | Isolates download logic from player/library; follows existing pattern |
| Database | Room in `feature-download` | Only module that needs persistent storage; keeps DB local |
| Background work | WorkManager | Already in deps (2.10.0); WiFi constraints, retry, survives app kill |
| Segment storage | Concatenate into single `.mp4` file per track | Simpler file management; ExoPlayer plays local MP4 natively |
| DASH parsing | Lightweight XML parser (no ExoPlayer dependency) | Testable, no Android framework dependency |
| DI | Hilt throughout | Consistent with project; no `init {}` blocks |
| Testing | TDD with MockK + Turbine + Flint E2E | Unit tests for all non-UI; Flint for screen verification |
| File size | Max 500 lines per file | Extract into focused classes when approaching limit |

---

## Module Structure

```
feature-download/
  build.gradle.kts
  src/main/kotlin/dev/tidesapp/wearos/download/
    di/
      DownloadModule.kt              — Hilt bindings + Room provider
    domain/
      model/
        DownloadState.kt             — PENDING, DOWNLOADING, COMPLETED, FAILED, EXPIRED
        DownloadedTrack.kt           — Domain model for a downloaded track
        DownloadedCollection.kt      — Domain model for a downloaded album/playlist
        OfflineRegistration.kt       — Device offline auth state
        StorageInfo.kt               — Used/limit/available bytes
      repository/
        DownloadRepository.kt        — Interface: query/manage downloaded content
        OfflineRegistrationRepository.kt — Interface: device offline registration
        DownloadStorageRepository.kt — Interface: storage management
    data/
      api/
        TidesOfflineApi.kt           — Retrofit: offline registration + playbackinfopostpaywall
      db/
        DownloadDatabase.kt          — Room database
        DownloadedTrackEntity.kt     — Room entity
        DownloadedCollectionEntity.kt — Room entity
        DownloadedTrackDao.kt        — Room DAO
        DownloadedCollectionDao.kt   — Room DAO
      repository/
        DownloadRepositoryImpl.kt
        OfflineRegistrationRepositoryImpl.kt
        DownloadStorageRepositoryImpl.kt
      manifest/
        DashManifestParser.kt        — Pure Kotlin XML→segment URL extraction
        SegmentInfo.kt               — Data class: initUrl, mediaUrlTemplate, segmentCount
      download/
        TrackDownloader.kt           — Downloads segments → single .mp4 file
        SegmentDownloader.kt         — HTTP GET for individual segments
        DownloadProgressTracker.kt   — Aggregates segment progress into track %
      worker/
        DownloadWorker.kt            — WorkManager worker: orchestrates download of a collection
        DownloadWorkScheduler.kt     — Enqueue/cancel WorkManager jobs
    ui/
      downloads/
        DownloadsScreen.kt           — List of downloaded collections + storage bar
        DownloadsViewModel.kt
      downloadmanager/
        DownloadManagerScreen.kt     — Active downloads progress
        DownloadManagerViewModel.kt
```

Changes to existing modules:
```
core/
  domain/model/SharedModels.kt      — Add DownloadQualityPreference enum

feature-player/
  service/TrackResolver.kt          — Check local DB before calling API
  data/api/TidesPlaybackApi.kt      — Add playbackinfopostpaywall endpoint

feature-library/
  ui/albumdetail/AlbumDetailScreen.kt    — Add download button
  ui/playlistdetail/PlaylistDetailScreen.kt — Add download button

feature-settings/
  domain/repository/SettingsRepository.kt — Add download quality + storage limit prefs
  data/repository/SettingsRepositoryImpl.kt
  ui/settings/SettingsScreen.kt          — Add download settings section

app/
  nav/TidesNavGraph.kt              — Add download routes
  src/main/AndroidManifest.xml      — No new permissions needed (already has INTERNET, FOREGROUND_SERVICE_DATA_SYNC)
  build.gradle.kts                  — Add feature-download dependency + Flint SDK
```

---

## Jira Epics

---

### EPIC 1: Room Database & Domain Models

**Goal**: Establish the persistence layer and domain types that all other epics depend on.

#### TIDE-D01: Domain Models

Create all domain model classes in `feature-download/domain/model/`.

**Files**:
- `DownloadState.kt` — enum: `PENDING`, `DOWNLOADING`, `COMPLETED`, `FAILED`, `EXPIRED`
- `DownloadedTrack.kt` — data class: `trackId: Long`, `title: String`, `artistName: String`, `albumTitle: String`, `imageUrl: String`, `filePath: String`, `fileSize: Long`, `audioQuality: String`, `manifestHash: String`, `offlineRevalidateAt: Long`, `offlineValidUntil: Long`, `downloadedAt: Long`, `state: DownloadState`
- `DownloadedCollection.kt` — data class: `id: String`, `type: CollectionType` (ALBUM, PLAYLIST), `title: String`, `imageUrl: String`, `trackCount: Int`, `downloadedTrackCount: Int`, `totalSizeBytes: Long`, `downloadedAt: Long`, `state: DownloadState`
- `OfflineRegistration.kt` — data class: `clientId: Long`, `authorizedForOffline: Boolean`, `authorizedAt: Long?`
- `StorageInfo.kt` — data class: `usedBytes: Long`, `limitBytes: Long`, `trackCount: Int`
- `CollectionType.kt` — enum: `ALBUM`, `PLAYLIST`

**Acceptance Criteria**:
- [ ] All domain models are data classes with no default mutable state
- [ ] `DownloadState` covers the full lifecycle: pending → downloading → completed/failed, plus expired for revalidation
- [ ] `DownloadedCollection` tracks both total and downloaded track counts for progress
- [ ] All models are in the domain layer — no Room annotations, no serialization annotations
- [ ] Unit tests verify equality, copy, and toString for each model
- [ ] No file exceeds 500 lines

**TDD sequence**:
1. Write tests asserting enum values exist and model construction works
2. Implement the models

---

#### TIDE-D02: Room Entities & DAOs

Create the Room persistence layer.

**Files**:
- `DownloadedTrackEntity.kt` — Room `@Entity(tableName = "downloaded_tracks")`, primary key `trackId: Long`, columns mirror domain model fields, plus `collectionId: String`, `collectionType: String`
- `DownloadedCollectionEntity.kt` — Room `@Entity(tableName = "downloaded_collections")`, composite key `(id, type)`
- `DownloadedTrackDao.kt`:
  - `@Query getByTrackId(trackId: Long): DownloadedTrackEntity?`
  - `@Query getByCollectionId(collectionId: String): Flow<List<DownloadedTrackEntity>>`
  - `@Query getAllCompleted(): Flow<List<DownloadedTrackEntity>>`
  - `@Query getByState(state: String): Flow<List<DownloadedTrackEntity>>`
  - `@Query getTotalSizeBytes(): Flow<Long>`
  - `@Query getTrackCount(): Flow<Int>`
  - `@Query isTrackDownloaded(trackId: Long): Boolean`
  - `@Insert(onConflict = REPLACE) insert(entity: DownloadedTrackEntity)`
  - `@Insert(onConflict = REPLACE) insertAll(entities: List<DownloadedTrackEntity>)`
  - `@Update updateState(trackId: Long, state: String)`
  - `@Delete deleteByTrackId(trackId: Long)`
  - `@Delete deleteByCollectionId(collectionId: String)`
- `DownloadedCollectionDao.kt`:
  - `@Query getAll(): Flow<List<DownloadedCollectionEntity>>`
  - `@Query getById(id: String, type: String): DownloadedCollectionEntity?`
  - `@Insert(onConflict = REPLACE) insert(entity: DownloadedCollectionEntity)`
  - `@Update update(entity: DownloadedCollectionEntity)`
  - `@Delete deleteById(id: String, type: String)`
- `DownloadDatabase.kt` — `@Database(entities = [DownloadedTrackEntity, DownloadedCollectionEntity], version = 1)`
- `EntityMappers.kt` — extension functions: `DownloadedTrackEntity.toDomain()`, `DownloadedTrack.toEntity(collectionId, collectionType)`, same for collections

**Acceptance Criteria**:
- [ ] Room entities have proper indices on `collectionId` and `state` columns for query performance
- [ ] DAOs return `Flow` for observable queries (UI reacts to changes)
- [ ] `getByTrackId` returns nullable (used by TrackResolver to check local-first)
- [ ] `getTotalSizeBytes()` returns `Flow<Long>` for live storage bar updates
- [ ] Entity mappers are pure functions, tested independently
- [ ] Instrumented tests verify all DAO operations using in-memory Room database
- [ ] All `@Query` SQL is compile-time validated by Room
- [ ] No file exceeds 500 lines

**TDD sequence**:
1. Write DAO tests with in-memory database (androidTest)
2. Write entity mapper unit tests
3. Implement entities, DAOs, mappers, database class

---

#### TIDE-D03: Hilt DI Module for Download

Wire up Room database and all repositories via Hilt.

**Files**:
- `DownloadModule.kt`:
  ```kotlin
  @Module
  @InstallIn(SingletonComponent::class)
  abstract class DownloadBindingsModule {
      @Binds abstract fun bindDownloadRepository(impl: DownloadRepositoryImpl): DownloadRepository
      @Binds abstract fun bindOfflineRegistrationRepository(impl: OfflineRegistrationRepositoryImpl): OfflineRegistrationRepository
      @Binds abstract fun bindDownloadStorageRepository(impl: DownloadStorageRepositoryImpl): DownloadStorageRepository
  }

  @Module
  @InstallIn(SingletonComponent::class)
  object DownloadProvidesModule {
      @Provides @Singleton
      fun provideDownloadDatabase(@ApplicationContext context: Context): DownloadDatabase
      @Provides fun provideDownloadedTrackDao(db: DownloadDatabase): DownloadedTrackDao
      @Provides fun provideDownloadedCollectionDao(db: DownloadDatabase): DownloadedCollectionDao
      @Provides @Singleton
      fun provideTidesOfflineApi(retrofit: Retrofit): TidesOfflineApi
  }
  ```

**Acceptance Criteria**:
- [ ] All dependencies are constructor-injected — zero `init {}` blocks, zero `lateinit var`
- [ ] Database is singleton-scoped; DAOs are unscoped (cheap to create)
- [ ] `TidesOfflineApi` is created from the same Retrofit instance as other API interfaces
- [ ] Module follows same pattern as existing `PlayerModule.kt` (separate bindings + provides)
- [ ] Hilt graph compiles without errors when integrated into app module

**TDD sequence**:
1. Write a test that verifies the module provides non-null instances (using Hilt test rules)
2. Implement the module

---

### EPIC 2: Offline API Client

**Goal**: Implement the Retrofit interface and supporting DTOs for all offline-specific API calls.

#### TIDE-D04: TidesOfflineApi Retrofit Interface

**Files**:
- `TidesOfflineApi.kt`:
  ```kotlin
  interface TidesOfflineApi {
      @GET("v1/sessions")
      suspend fun getSession(@Header("Authorization") token: String): SessionResponse

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

      @FormUrlEncoded // empty body needs this or use @Body with empty
      @POST("v1/users/{userId}/clients/offline")
      suspend fun registerForOffline(
          @Header("Authorization") token: String,
          @Path("userId") userId: Long,
      ): Response<Unit>

      @POST("v1/users/{userId}/clients/{clientId}/offline/playlists")
      suspend fun registerPlaylistOffline(
          @Header("Authorization") token: String,
          @Path("userId") userId: Long,
          @Path("clientId") clientId: Long,
      ): Response<Unit>

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
  ```

- `OfflineDtos.kt`:
  ```kotlin
  @Serializable
  data class SessionResponse(
      val sessionId: String = "",
      val userId: Long = 0,
      val countryCode: String = "",
      val client: SessionClientInfo = SessionClientInfo(),
  )

  @Serializable
  data class SessionClientInfo(
      val id: Long = 0,
      val name: String = "",
      val authorizedForOffline: Boolean = false,
      val authorizedForOfflineDate: String? = null,
  )

  @Serializable
  data class OfflineClientsResponse(
      val limit: Int = 0,
      val offset: Int = 0,
      val totalNumberOfItems: Int = 0,
      val items: List<SessionClientInfo> = emptyList(),
  )

  @Serializable
  data class OfflinePlaybackInfoResponse(
      val trackId: Long = 0,
      val assetPresentation: String = "",
      val audioMode: String = "",
      val audioQuality: String = "",
      val streamingSessionId: String = "",
      val manifestMimeType: String = "",
      val manifestHash: String = "",
      val manifest: String = "",
      val albumReplayGain: Double = 0.0,
      val albumPeakAmplitude: Double = 0.0,
      val trackReplayGain: Double = 0.0,
      val trackPeakAmplitude: Double = 0.0,
      val offlineRevalidateAt: Long = 0,
      val offlineValidUntil: Long = 0,
      val bitDepth: Int = 0,
      val sampleRate: Int = 0,
  )
  ```

**Acceptance Criteria**:
- [ ] All endpoints match the Charles capture exactly (paths, query params, headers)
- [ ] `registerForOffline` returns `Response<Unit>` to handle 204 No Content
- [ ] `getOfflinePlaybackInfo` includes `streamingsessionid` as required UUID param
- [ ] DTOs use Kotlinx Serialization with sensible defaults (consistent with existing `PlaybackDtos.kt`)
- [ ] `OfflinePlaybackInfoResponse` includes `manifestHash`, `offlineRevalidateAt`, `offlineValidUntil` (not present in streaming `PlaybackInfoResponse`)
- [ ] Unit tests verify DTO deserialization from sample JSON payloads (copied from Charles capture)
- [ ] No file exceeds 500 lines (split DTOs from API interface if needed)

**TDD sequence**:
1. Write deserialization tests with JSON samples from Charles logs
2. Implement DTOs
3. Write API interface (compile-time verified by Retrofit)

---

#### TIDE-D05: Offline Registration Repository

Manages device registration for offline playback. One-time setup per device.

**Files**:
- `OfflineRegistrationRepository.kt` (interface in domain):
  ```kotlin
  interface OfflineRegistrationRepository {
      suspend fun getSessionInfo(): Result<OfflineRegistration>
      suspend fun registerDeviceForOffline(): Result<OfflineRegistration>
      suspend fun ensureOfflineAuthorized(): Result<OfflineRegistration>
      suspend fun registerCollectionOffline(collectionId: String, type: CollectionType): Result<Unit>
  }
  ```
- `OfflineRegistrationRepositoryImpl.kt`:
  - Injects: `TidesOfflineApi`, `CredentialsProvider`, `DataStore<Preferences>`
  - `getSessionInfo()`: calls `/v1/sessions`, extracts `client.id` + `authorizedForOffline`
  - `registerDeviceForOffline()`: calls `POST /clients/offline`, then confirms via `/v1/sessions/{sid}`
  - `ensureOfflineAuthorized()`: checks cached state → if not authorized, calls register → returns result
  - `registerCollectionOffline()`: calls the playlist or album registration endpoint based on type
  - Caches `clientId` and `authorizedForOffline` in DataStore to avoid repeated API calls

**Acceptance Criteria**:
- [ ] `ensureOfflineAuthorized()` is idempotent — safe to call multiple times
- [ ] `clientId` is persisted in DataStore after first successful session call
- [ ] Registration endpoints handle 204 responses correctly
- [ ] Errors from API are wrapped in `Result.failure` with descriptive messages
- [ ] Unit tests cover: first-time registration, already-registered shortcut, API error handling, playlist vs album registration
- [ ] All dependencies constructor-injected via Hilt

**TDD sequence**:
1. Test `ensureOfflineAuthorized` returns cached state when already authorized
2. Test `ensureOfflineAuthorized` calls API and persists when not authorized
3. Test `registerCollectionOffline` dispatches to correct endpoint by type
4. Test error wrapping
5. Implement

---

### EPIC 3: DASH Manifest Parser & Segment Downloader

**Goal**: Parse DASH MPD XML to extract segment URLs and download audio segments into a single local file. Entirely pure Kotlin — fully unit-testable with no Android dependencies.

#### TIDE-D06: DASH Manifest Parser

Parse the base64-encoded DASH MPD manifest from `playbackinfopostpaywall` into segment download instructions.

**Files**:
- `SegmentInfo.kt`:
  ```kotlin
  data class SegmentInfo(
      val initUrl: String,
      val mediaUrls: List<String>,
      val codec: String,         // "flac" or "mp4a.40.2"
      val bandwidth: Long,       // bps
      val sampleRate: Int,       // 44100, 48000, etc
      val durationSeconds: Double,
  )
  ```
- `DashManifestParser.kt`:
  ```kotlin
  class DashManifestParser @Inject constructor() {
      fun parse(base64Manifest: String): SegmentInfo
      fun parseXml(xml: String): SegmentInfo  // exposed for testing
  }
  ```
  Implementation:
  - Base64 decode the manifest string
  - Parse XML using `XmlPullParser` (available in pure Kotlin/JVM via `javax.xml.parsers`)
  - Extract from `<SegmentTemplate>`: `initialization` URL, `media` URL template
  - Extract from `<SegmentTimeline>`: `<S d="..." r="..."/>` elements
  - Calculate total segment count: sum of `(r + 1)` for each `<S>` element
  - Generate media URLs by substituting `$Number$` with 1..N in the template
  - Extract codec, bandwidth, sampleRate from `<Representation>`
  - Calculate duration from `mediaPresentationDuration` attribute (ISO 8601 duration)

**Acceptance Criteria**:
- [ ] Correctly parses the exact MPD format from Charles capture (FLAC example in docs)
- [ ] Handles both single-`<S>` and multi-`<S>` SegmentTimeline entries
- [ ] `$Number$` substitution generates correct URLs for all segments (1-based)
- [ ] Duration parsing handles `PT4M56.978S` format correctly
- [ ] Throws clear exception on malformed XML (not a generic NPE)
- [ ] Zero Android framework dependencies — uses `javax.xml` or Kotlin XML parsing
- [ ] Unit tests with real MPD samples extracted from Charles capture
- [ ] Unit tests with edge cases: single segment, 200+ segments, missing attributes
- [ ] File stays under 500 lines (parser logic is focused)

**TDD sequence**:
1. Write test with real base64 manifest from Charles → assert segment count, URLs, codec
2. Write test with hand-crafted MPD with multiple `<S>` entries
3. Write test for duration parsing
4. Write test for malformed XML error handling
5. Implement parser

---

#### TIDE-D07: Segment Downloader

Downloads individual HTTP segments from CDN URLs.

**Files**:
- `SegmentDownloader.kt`:
  ```kotlin
  class SegmentDownloader @Inject constructor(
      private val okHttpClient: OkHttpClient,
  ) {
      suspend fun downloadSegment(url: String): ByteArray
      suspend fun downloadSegmentToFile(url: String, outputFile: File, append: Boolean)
  }
  ```
  - Uses OkHttp directly (not Retrofit — binary data, not JSON)
  - `downloadSegmentToFile` writes to disk with `append = true` for concatenation
  - Throws `IOException` on HTTP errors (4xx/5xx)

**Acceptance Criteria**:
- [ ] Downloads binary data from CloudFront signed URLs
- [ ] `downloadSegmentToFile` with `append = true` appends to existing file (for segment concatenation)
- [ ] Throws descriptive exception on HTTP 403 (URL expired), 404, 5xx
- [ ] Does not hold entire file in memory — streams to disk
- [ ] Unit tests with MockWebServer verify correct HTTP requests and file writing
- [ ] Injected OkHttpClient — same instance from NetworkModule (has correct timeouts/interceptors)

**TDD sequence**:
1. Test successful download writes correct bytes to file
2. Test append mode concatenates correctly
3. Test HTTP error responses throw appropriate exceptions
4. Implement

---

#### TIDE-D08: Track Downloader

Orchestrates downloading a complete track: get playback info → parse manifest → download all segments → produce single .mp4 file.

**Files**:
- `TrackDownloader.kt`:
  ```kotlin
  class TrackDownloader @Inject constructor(
      private val offlineApi: TidesOfflineApi,
      private val manifestParser: DashManifestParser,
      private val segmentDownloader: SegmentDownloader,
      private val credentialsProvider: CredentialsProvider,
  ) {
      suspend fun downloadTrack(
          trackId: Long,
          audioQuality: String,
          outputDir: File,
          onProgress: (downloaded: Int, total: Int) -> Unit,
      ): TrackDownloadResult
  }

  data class TrackDownloadResult(
      val filePath: String,
      val fileSize: Long,
      val manifestHash: String,
      val offlineRevalidateAt: Long,
      val offlineValidUntil: Long,
      val audioQuality: String,
      val bitDepth: Int,
      val sampleRate: Int,
  )
  ```
  Flow:
  1. Get bearer token from `CredentialsProvider`
  2. Generate UUID for `streamingsessionid`
  3. Call `getOfflinePlaybackInfo(trackId, audioQuality, sessionId)`
  4. Pass `manifest` to `DashManifestParser.parse()`
  5. Create output file: `{outputDir}/{trackId}.mp4`
  6. Download init segment → write to file (append=false)
  7. Loop through media segments → write to file (append=true), call `onProgress` after each
  8. Return `TrackDownloadResult` with metadata from API response

**Acceptance Criteria**:
- [ ] Produces a single `.mp4` file that ExoPlayer can play locally
- [ ] Progress callback fires after each segment with (segmentIndex, totalSegments)
- [ ] On failure mid-download, deletes the partial file (no corrupt files on disk)
- [ ] Uses `streamingsessionid` as a fresh UUID per track (matches official app behavior)
- [ ] Returns all offline metadata (`manifestHash`, `offlineRevalidateAt`, `offlineValidUntil`) for DB storage
- [ ] Unit tests with mocked API + mocked SegmentDownloader verify full orchestration
- [ ] Test: partial failure deletes output file
- [ ] Test: progress callback fires correct number of times
- [ ] All dependencies injected — no `init {}`, no static calls

**TDD sequence**:
1. Test happy path: mock API returns manifest, mock parser returns segments, verify file operations
2. Test failure cleanup: mock segment download throws on segment 3, verify file deleted
3. Test progress tracking: verify callback count matches segment count
4. Implement

---

### EPIC 4: Download Repository & WorkManager

**Goal**: High-level download management — queue tracks, track progress, manage collections, run downloads in background.

#### TIDE-D09: Download Repository

Central repository for all download state queries and mutations.

**Files**:
- `DownloadRepository.kt` (interface):
  ```kotlin
  interface DownloadRepository {
      fun getDownloadedCollections(): Flow<List<DownloadedCollection>>
      fun getDownloadedTracksForCollection(collectionId: String): Flow<List<DownloadedTrack>>
      fun getStorageInfo(): Flow<StorageInfo>
      suspend fun isTrackDownloaded(trackId: Long): Boolean
      suspend fun getDownloadedTrack(trackId: Long): DownloadedTrack?
      suspend fun queueCollectionDownload(
          collectionId: String, type: CollectionType, title: String, imageUrl: String,
          tracks: List<TrackItem>,
      )
      suspend fun markTrackCompleted(trackId: Long, result: TrackDownloadResult)
      suspend fun markTrackFailed(trackId: Long, error: String)
      suspend fun deleteCollection(collectionId: String, type: CollectionType)
      suspend fun deleteTrack(trackId: Long)
      suspend fun getExpiredTracks(currentTimeSeconds: Long): List<DownloadedTrack>
      suspend fun getTracksNeedingRevalidation(currentTimeSeconds: Long): List<DownloadedTrack>
  }
  ```

- `DownloadRepositoryImpl.kt`:
  - Injects: `DownloadedTrackDao`, `DownloadedCollectionDao`, `@IoDispatcher CoroutineDispatcher`
  - `queueCollectionDownload()`: inserts collection entity + track entities (state = PENDING)
  - `markTrackCompleted()`: updates track state + file path, recalculates collection progress
  - `deleteCollection()`: deletes DB entries + physical files from disk
  - `deleteTrack()`: deletes single track entry + file
  - Storage info aggregated from `getTotalSizeBytes()` + settings limit

**Acceptance Criteria**:
- [ ] `getDownloadedCollections()` returns `Flow` — UI updates reactively when downloads complete
- [ ] `queueCollectionDownload()` creates both collection and track entities in a single transaction
- [ ] `deleteCollection()` deletes physical audio files from disk (not just DB rows)
- [ ] `isTrackDownloaded()` checks state = COMPLETED (not just row exists)
- [ ] `getExpiredTracks()` compares `offlineValidUntil` against provided timestamp
- [ ] All Flow queries run on IO dispatcher
- [ ] Unit tests cover: queue → complete lifecycle, deletion cleanup, expiry filtering
- [ ] Tests use in-memory Room database

**TDD sequence**:
1. Test `queueCollectionDownload` creates correct entities
2. Test `markTrackCompleted` updates state and recalculates collection
3. Test `deleteCollection` removes files and entities
4. Test `getExpiredTracks` filters correctly
5. Implement

---

#### TIDE-D10: Download Storage Repository

Manages storage limits and provides disk usage info.

**Files**:
- `DownloadStorageRepository.kt` (interface):
  ```kotlin
  interface DownloadStorageRepository {
      fun getStorageInfo(): Flow<StorageInfo>
      suspend fun getDownloadDirectory(): File
      suspend fun hasSpaceForTrack(estimatedBytes: Long): Boolean
      suspend fun clearAllDownloads()
  }
  ```
- `DownloadStorageRepositoryImpl.kt`:
  - Injects: `@ApplicationContext Context`, `DownloadedTrackDao`, `SettingsRepository`
  - Download directory: `context.filesDir/offline_tracks/`
  - `hasSpaceForTrack()`: compares (current usage + estimated) against user's configured limit
  - `clearAllDownloads()`: deletes all files in download directory + clears DB

**Acceptance Criteria**:
- [ ] Download directory is internal app storage (not SD card — Wear OS rarely has one)
- [ ] `hasSpaceForTrack()` respects user's configured storage limit from settings
- [ ] `clearAllDownloads()` is atomic — clears files then DB in transaction
- [ ] `getStorageInfo()` returns reactive Flow (updates as downloads complete)
- [ ] Unit tests with temp directories verify file operations

**TDD sequence**:
1. Test `hasSpaceForTrack` against various limit/usage scenarios
2. Test `clearAllDownloads` removes files and resets DB
3. Implement

---

#### TIDE-D11: WorkManager Download Worker

Background worker that downloads all tracks in a collection.

**Files**:
- `DownloadWorker.kt`:
  ```kotlin
  @HiltWorker
  class DownloadWorker @AssistedInject constructor(
      @Assisted context: Context,
      @Assisted params: WorkerParameters,
      private val downloadRepository: DownloadRepository,
      private val offlineRegistration: OfflineRegistrationRepository,
      private val trackDownloader: TrackDownloader,
      private val storageRepository: DownloadStorageRepository,
  ) : CoroutineWorker(context, params) {
      override suspend fun doWork(): Result
      override suspend fun getForegroundInfo(): ForegroundInfo
  }
  ```
  Flow:
  1. Extract `collectionId` and `collectionType` from input data
  2. `offlineRegistration.ensureOfflineAuthorized()`
  3. `offlineRegistration.registerCollectionOffline(collectionId, type)`
  4. Get pending tracks from `downloadRepository`
  5. For each track:
     a. Check `storageRepository.hasSpaceForTrack()`
     b. Call `trackDownloader.downloadTrack()`
     c. On success: `downloadRepository.markTrackCompleted()`
     d. On failure: `downloadRepository.markTrackFailed()`, continue to next
  6. Return `Result.success()` or `Result.retry()` on transient errors

- `DownloadWorkScheduler.kt`:
  ```kotlin
  class DownloadWorkScheduler @Inject constructor(
      private val workManager: WorkManager,
  ) {
      fun enqueueCollectionDownload(collectionId: String, type: CollectionType)
      fun cancelCollectionDownload(collectionId: String)
      fun cancelAll()
      fun getWorkStatus(collectionId: String): Flow<WorkInfo?>
  }
  ```
  - Constraints: `NetworkType.CONNECTED` (or `UNMETERED` if wifi-only setting is on)
  - Uses `OneTimeWorkRequest` with unique work name per collection
  - `ExistingWorkPolicy.KEEP` — don't restart if already running

**Acceptance Criteria**:
- [ ] Worker runs as foreground service with notification showing download progress
- [ ] Worker respects WiFi-only setting via WorkManager network constraints
- [ ] Individual track failures don't abort the entire collection download
- [ ] Worker is idempotent — re-running skips already-completed tracks
- [ ] Storage limit is checked before each track download (not just at start)
- [ ] `cancelCollectionDownload()` cancels the WorkManager job (in-flight download stops)
- [ ] Worker uses `@HiltWorker` + `@AssistedInject` (Hilt pattern for WorkManager)
- [ ] Unit tests with mocked dependencies verify: full success flow, partial failure, storage exhaustion, cancellation
- [ ] No file exceeds 500 lines — worker orchestration is separate from download logic

**TDD sequence**:
1. Test full success flow: all tracks download and are marked completed
2. Test partial failure: track 2 fails, tracks 1 and 3 succeed
3. Test storage exhaustion: worker stops gracefully when limit reached
4. Test idempotency: re-running skips completed tracks
5. Implement worker
6. Implement scheduler (thin wrapper, fewer tests needed)

---

### EPIC 5: Offline Playback Integration

**Goal**: Modify the player to check for local files before streaming. Seamless — user shouldn't notice the difference.

#### TIDE-D12: TrackResolver Local-First Check

Modify `TrackResolver` in `feature-player` to check Room DB for downloaded tracks before calling the streaming API.

**Changes to**:
- `feature-player/service/WearMusicService.kt` (or `TrackResolvingCallback.kt`)
- Inject `DownloadRepository` via Hilt EntryPoint (since WearMusicService uses EntryPoint pattern)

**Logic**:
```
For each track in queue:
  1. Check downloadRepository.getDownloadedTrack(trackId)
  2. If found AND state == COMPLETED AND file exists on disk:
     → Build MediaItem with Uri.fromFile(localPath)
  3. Else:
     → Existing streaming resolution (call playbackinfo API)
```

**Acceptance Criteria**:
- [ ] Downloaded tracks play from local file — no network request made
- [ ] If local file is missing (deleted externally), falls back to streaming transparently
- [ ] If track is downloaded but expired (`offlineValidUntil` < now), falls back to streaming
- [ ] Mixed queues work: some tracks local, some streamed, in any order
- [ ] No change to player UI — NowPlayingScreen works identically
- [ ] Unit tests verify: local-first resolution, fallback on missing file, fallback on expired, mixed queue
- [ ] `DownloadRepository` injected via Hilt EntryPoint (consistent with existing service DI pattern)

**TDD sequence**:
1. Test: track is downloaded → resolver returns local URI
2. Test: track is downloaded but file missing → resolver calls API
3. Test: track is expired → resolver calls API
4. Test: mixed queue of local + remote tracks → all resolve correctly
5. Implement changes to `TrackResolvingCallback`

---

#### TIDE-D13: Offline Revalidation

Background job to revalidate expiring offline content.

**Files**:
- `RevalidationWorker.kt`:
  ```kotlin
  @HiltWorker
  class RevalidationWorker @AssistedInject constructor(...) : CoroutineWorker(...) {
      override suspend fun doWork(): Result
  }
  ```
  Flow:
  1. Query `downloadRepository.getTracksNeedingRevalidation(now)`
  2. For each track: call `playbackinfopostpaywall` with `OFFLINE` mode
  3. Compare `manifestHash` with stored hash
  4. If same: update `offlineRevalidateAt` and `offlineValidUntil` timestamps
  5. If different: mark track for re-download (state = PENDING, schedule download)
  6. Mark tracks past `offlineValidUntil` as EXPIRED

- Add to `DownloadWorkScheduler.kt`:
  - `schedulePeriodicRevalidation()` — `PeriodicWorkRequest` every 24 hours
  - Constraint: `NetworkType.CONNECTED`

**Acceptance Criteria**:
- [ ] Runs daily via periodic WorkManager job
- [ ] Only processes tracks where `offlineRevalidateAt < now`
- [ ] Hash-match tracks get updated timestamps without re-download
- [ ] Hash-mismatch tracks trigger re-download via `DownloadWorker`
- [ ] Tracks past `offlineValidUntil` are marked EXPIRED and won't play offline
- [ ] Expired tracks are still playable via streaming (graceful degradation)
- [ ] Unit tests verify: hash match update, hash mismatch re-download trigger, expiry marking

**TDD sequence**:
1. Test hash match → timestamps updated
2. Test hash mismatch → track state set to PENDING
3. Test expired track → state set to EXPIRED
4. Implement

---

### EPIC 6: Settings Extensions

**Goal**: Add download quality and storage limit preferences.

#### TIDE-D14: Download Settings

Extend `SettingsRepository` with download-specific preferences.

**Changes to**:
- `feature-settings/domain/repository/SettingsRepository.kt` — add:
  ```kotlin
  fun getDownloadQuality(): Flow<AudioQualityPreference>
  suspend fun setDownloadQuality(quality: AudioQualityPreference)
  fun getStorageLimitBytes(): Flow<Long>
  suspend fun setStorageLimitBytes(limitBytes: Long)
  ```
- `feature-settings/data/repository/SettingsRepositoryImpl.kt` — implement with DataStore keys:
  - `DOWNLOAD_QUALITY` (string, default = "HIGH")
  - `STORAGE_LIMIT_BYTES` (long, default = 1GB = 1_073_741_824)

- `feature-settings/ui/settings/SettingsScreen.kt` — add new section:
  - Download quality chip (LOW / HIGH / LOSSLESS)
  - Storage limit chip (500 MB / 1 GB / 2 GB / 4 GB)
  - Storage usage bar (used / limit) — reads from `DownloadStorageRepository`
  - "Clear all downloads" button with confirmation

**Acceptance Criteria**:
- [ ] Download quality defaults to HIGH (AAC 320kbps — practical for Wear OS)
- [ ] Storage limit defaults to 1 GB
- [ ] Storage usage bar updates reactively as downloads complete
- [ ] "Clear all downloads" shows confirmation dialog before deleting
- [ ] Settings persist across app restarts (DataStore)
- [ ] Unit tests for repository: default values, set/get round-trip
- [ ] Existing settings tests still pass (no regression)

**TDD sequence**:
1. Test default download quality is HIGH
2. Test set/get round-trip for quality and storage limit
3. Implement repository changes
4. Implement UI (Flint-tested in EPIC 8)

---

### EPIC 7: UI — Download Screens & Library Integration

**Goal**: Add download triggers to album/playlist screens, a downloads list screen, and an active download manager screen.

#### TIDE-D15: Download Button on Album/Playlist Detail

Add a download icon button to `AlbumDetailScreen` and `PlaylistDetailScreen`.

**Changes to**:
- `feature-library/ui/albumdetail/AlbumDetailViewModel.kt`:
  - Inject `DownloadRepository`, `DownloadWorkScheduler`
  - Add `isDownloaded: Boolean` and `isDownloading: Boolean` to UI state
  - Add event: `DownloadAlbum`, `RemoveDownload`
- `feature-library/ui/albumdetail/AlbumDetailScreen.kt`:
  - Download icon in header area (download arrow icon / checkmark if downloaded)
  - Long-press or tap downloaded icon → "Remove download" option

- Same pattern for `PlaylistDetailScreen` / `PlaylistDetailViewModel`

**Acceptance Criteria**:
- [ ] Download button shows correct state: not downloaded (arrow down) / downloading (progress) / downloaded (checkmark)
- [ ] Tapping download triggers `DownloadWorkScheduler.enqueueCollectionDownload()`
- [ ] Tapping when already downloaded shows remove option
- [ ] Download state persists across navigation (reads from Room Flow)
- [ ] Button is disabled when offline and content not downloaded
- [ ] ViewModels inject download dependencies — no `init {}` blocks

---

#### TIDE-D16: Downloads Screen

New screen listing all downloaded collections with storage info.

**Files**:
- `feature-download/ui/downloads/DownloadsScreen.kt`:
  - ScalingLazyColumn with:
    - Storage usage bar at top (used / limit)
    - List of downloaded collections (albums/playlists) with cover art, title, track count
    - Each item shows download state (complete / partial / expired)
    - Tap → navigate to collection detail (existing album/playlist detail screens)
    - Swipe to delete
  - Empty state when no downloads

- `feature-download/ui/downloads/DownloadsViewModel.kt`:
  - Injects: `DownloadRepository`, `DownloadStorageRepository`
  - UI State: `collections: List<DownloadedCollection>`, `storageInfo: StorageInfo`
  - Events: `DeleteCollection(id, type)`, `NavigateToCollection(id, type)`

**Acceptance Criteria**:
- [ ] Collections sorted by download date (newest first)
- [ ] Storage bar shows visual fill proportional to usage
- [ ] Expired collections show visual indicator (warning icon or different color)
- [ ] Delete removes both files and DB entries
- [ ] Empty state is friendly ("No downloads yet")
- [ ] Screen accessible from library hub navigation

---

#### TIDE-D17: Download Manager Screen (Active Downloads)

Shows in-progress downloads with per-track progress.

**Files**:
- `feature-download/ui/downloadmanager/DownloadManagerScreen.kt`:
  - Header: collection title + overall progress (X of Y tracks)
  - List of tracks with individual progress indicators
  - Cancel button to abort download
  - Auto-navigates to Downloads screen when complete

- `feature-download/ui/downloadmanager/DownloadManagerViewModel.kt`:
  - Observes WorkManager status via `DownloadWorkScheduler.getWorkStatus()`
  - Observes track states from `DownloadRepository.getDownloadedTracksForCollection()`
  - Events: `CancelDownload`

**Acceptance Criteria**:
- [ ] Shows real-time progress of active download
- [ ] Individual track states: pending (queued icon), downloading (spinner), completed (check), failed (error)
- [ ] Cancel stops the WorkManager job and marks remaining tracks as PENDING (resumable)
- [ ] Screen handles configuration changes (rotation) without losing state
- [ ] Navigating away and back shows correct current state

---

#### TIDE-D18: Navigation Integration

Wire up new screens into the nav graph.

**Changes to**:
- `app/nav/TidesNavGraph.kt`:
  - Add route: `DOWNLOADS = "downloads"`
  - Add route: `DOWNLOAD_MANAGER = "download_manager/{collectionId}"`
  - Add composable destinations
- `feature-library/ui/library/LibraryHubScreen.kt`:
  - Add "Downloads" entry point (download icon chip)

**Acceptance Criteria**:
- [ ] Downloads screen accessible from Library Hub
- [ ] Download Manager screen accessible after triggering a download
- [ ] Back navigation works correctly from all new screens
- [ ] Deep linking to download manager via collection ID

---

### EPIC 8: Flint E2E Testing

**Goal**: Integrate Flint SDK for full end-to-end testing of all download UI flows via ADB.

#### TIDE-D19: Flint SDK Integration

Add Flint SDK to the project and instrument all download-related screens.

**Changes to**:
- `settings.gradle.kts` — add `mavenLocal()` to both repository blocks
- `app/build.gradle.kts` — add `implementation("com.flintsdk:runtime:1.3.0")` + `ksp("com.flintsdk:compiler:1.3.0")`
- `app/src/main/kotlin/dev/tidesapp/wearos/App.kt` — add `Flint.init(this, adbMode = BuildConfig.DEBUG, networkMode = false)`

**Screen Instrumentation**:

**Downloads Screen**:
```kotlin
Flint.screen("Downloads")
Flint.tools {
    tool("delete_collection", "Delete a downloaded collection") {
        param("id", "string", "Collection ID")
        action { params ->
            val id = params["id"]?.toString() ?: return@action null
            viewModel.onEvent(DeleteCollection(id))
            null
        }
    }
    tool("get_storage_info", "Get current storage usage") {
        action {
            mapOf(
                "used_bytes" to uiState.storageInfo.usedBytes,
                "limit_bytes" to uiState.storageInfo.limitBytes,
                "track_count" to uiState.storageInfo.trackCount,
            )
        }
    }
}
// flintContent for each collection item
Box(Modifier.flintContent("collection_count").semantics {
    text = AnnotatedString(uiState.collections.size.toString())
})
```

**Download Manager Screen**:
```kotlin
Flint.screen("DownloadManager")
Flint.tools {
    tool("cancel_download", "Cancel active download") {
        action { viewModel.onEvent(CancelDownload); null }
    }
    tool("get_progress", "Get download progress") {
        action {
            mapOf(
                "completed" to uiState.completedCount,
                "total" to uiState.totalCount,
                "state" to uiState.overallState.name,
            )
        }
    }
}
```

**Album Detail Screen** (extend existing):
```kotlin
Flint.tools {
    // ... existing tools ...
    tool("download_album", "Download this album for offline") {
        action { viewModel.onEvent(DownloadAlbum); null }
    }
    tool("remove_download", "Remove offline download") {
        action { viewModel.onEvent(RemoveDownload); null }
    }
}
Box(Modifier.flintContent("download_state").semantics {
    text = AnnotatedString(uiState.downloadState.name) // NOT_DOWNLOADED, DOWNLOADING, DOWNLOADED
})
```

**Playlist Detail Screen** — same pattern as Album Detail.

**Settings Screen** (extend existing):
```kotlin
Flint.tools {
    // ... existing tools ...
    tool("set_download_quality", "Set download quality") {
        param("quality", "string", "LOW, HIGH, or LOSSLESS")
        action { params ->
            val q = AudioQualityPreference.valueOf(params["quality"].toString())
            viewModel.onEvent(ChangeDownloadQuality(q))
            null
        }
    }
    tool("set_storage_limit", "Set storage limit in bytes") {
        param("limit", "string", "Limit in bytes")
        action { params ->
            val limit = params["limit"]?.toString()?.toLongOrNull() ?: return@action null
            viewModel.onEvent(ChangeStorageLimit(limit))
            null
        }
    }
    tool("clear_all_downloads", "Delete all downloaded content") {
        action { viewModel.onEvent(ClearAllDownloads); null }
    }
}
Box(Modifier.flintContent("download_quality").semantics {
    text = AnnotatedString(uiState.downloadQuality.name)
})
Box(Modifier.flintContent("storage_used").semantics {
    text = AnnotatedString(uiState.storageUsedBytes.toString())
})
Box(Modifier.flintContent("storage_limit").semantics {
    text = AnnotatedString(uiState.storageLimitBytes.toString())
})
```

**Acceptance Criteria**:
- [ ] `Flint.init()` called in `App.onCreate()` with `adbMode = BuildConfig.DEBUG`
- [ ] All download screens registered with `Flint.screen()`
- [ ] Every user action has a corresponding Flint tool (no action requires touch coordinates)
- [ ] Every verifiable state has a `flintContent()` annotation
- [ ] All tools are testable via ADB: `adb shell "content read --uri 'content://dev.tidesapp.wearos.flint/call_tool?_tool=download_album'"`
- [ ] `read_screen` returns complete state for every screen
- [ ] Flint is debug-only — zero overhead in release builds

---

#### TIDE-D20: E2E Test Scripts

ADB-based test scripts that exercise the full download flow.

**Files**:
- `scripts/e2e/test_download_album.sh`:
  ```bash
  # 1. Navigate to album detail
  # 2. read_screen → verify download_state = NOT_DOWNLOADED
  # 3. call_tool download_album
  # 4. read_screen → verify download_state = DOWNLOADING
  # 5. Poll until download_state = DOWNLOADED (or timeout)
  # 6. Navigate to Downloads screen
  # 7. read_screen → verify collection_count > 0
  # 8. get_storage_info → verify used_bytes > 0
  ```

- `scripts/e2e/test_offline_playback.sh`:
  ```bash
  # 1. Download an album
  # 2. Enable airplane mode (adb shell cmd connectivity airplane-mode enable)
  # 3. Navigate to album, play track
  # 4. Verify playback starts (NowPlaying screen, track playing)
  # 5. Disable airplane mode
  ```

- `scripts/e2e/test_delete_download.sh`:
  ```bash
  # 1. Verify downloaded content exists
  # 2. call_tool delete_collection
  # 3. Verify collection_count decreased
  # 4. Verify storage_used decreased
  ```

- `scripts/e2e/test_download_settings.sh`:
  ```bash
  # 1. Navigate to Settings
  # 2. call_tool set_download_quality with quality=LOSSLESS
  # 3. read_screen → verify download_quality = LOSSLESS
  # 4. call_tool set_storage_limit with limit=2147483648
  # 5. read_screen → verify storage_limit = 2147483648
  ```

**Acceptance Criteria**:
- [ ] All E2E tests executable via single ADB connection to Wear OS device/emulator
- [ ] Tests are self-contained — setup and teardown included
- [ ] Tests verify state changes via `read_screen` (not screenshots)
- [ ] Offline playback test toggles airplane mode and verifies playback works
- [ ] Tests can be run in CI with an emulator
- [ ] Each test has clear pass/fail output

---

## Dependency Graph

```
TIDE-D01 (Domain Models)
  └→ TIDE-D02 (Room Entities & DAOs)
       └→ TIDE-D03 (DI Module)
            └→ TIDE-D09 (Download Repository)
            └→ TIDE-D10 (Storage Repository)

TIDE-D04 (Offline API)
  └→ TIDE-D05 (Registration Repository)
       └→ TIDE-D11 (WorkManager Worker)

TIDE-D06 (DASH Parser)
  └→ TIDE-D07 (Segment Downloader)
       └→ TIDE-D08 (Track Downloader)
            └→ TIDE-D11 (WorkManager Worker)

TIDE-D09 + TIDE-D11
  └→ TIDE-D12 (Offline Playback)
  └→ TIDE-D13 (Revalidation)

TIDE-D14 (Settings)
  └→ TIDE-D15 (Download Buttons)
  └→ TIDE-D16 (Downloads Screen)
  └→ TIDE-D17 (Download Manager)
       └→ TIDE-D18 (Navigation)

TIDE-D18
  └→ TIDE-D19 (Flint Integration)
       └→ TIDE-D20 (E2E Tests)
```

## Parallelizable Tracks

Three work streams can proceed concurrently after TIDE-D01:

| Track A (Data) | Track B (Download Engine) | Track C (Settings) |
|---|---|---|
| D02: Room Entities | D04: Offline API | D14: Settings Extensions |
| D03: DI Module | D05: Registration Repo | |
| D09: Download Repo | D06: DASH Parser | |
| D10: Storage Repo | D07: Segment Downloader | |
| | D08: Track Downloader | |

**Convergence point**: TIDE-D11 (WorkManager Worker) requires Track A + Track B complete.

After D11: D12 (Offline Playback) + D13 (Revalidation) + D15-D18 (UI) can proceed.

Final: D19 (Flint) + D20 (E2E) after all UI is complete.

---

## Estimated Scope

| Epic | Tickets | New Files | Modified Files |
|---|---|---|---|
| 1: Database & Models | 3 | 10 | 0 |
| 2: Offline API | 2 | 4 | 0 |
| 3: Parser & Downloader | 3 | 5 | 0 |
| 4: Repository & Worker | 3 | 6 | 0 |
| 5: Offline Playback | 2 | 1 | 2 |
| 6: Settings | 1 | 0 | 3 |
| 7: UI Screens | 4 | 8 | 4 |
| 8: Flint E2E | 2 | 4 | 6 |
| **Total** | **20** | **38** | **15** |
