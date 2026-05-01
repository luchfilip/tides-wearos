package dev.tidesapp.wearos.download.data.db

import dev.tidesapp.wearos.download.domain.model.CollectionType
import dev.tidesapp.wearos.download.domain.model.DownloadState
import dev.tidesapp.wearos.download.domain.model.DownloadedCollection
import dev.tidesapp.wearos.download.domain.model.DownloadedTrack

fun DownloadedTrackEntity.toDomain(): DownloadedTrack = DownloadedTrack(
    trackId = trackId,
    title = title,
    artistName = artistName,
    albumTitle = albumTitle,
    imageUrl = imageUrl,
    duration = duration,
    trackNumber = trackNumber,
    filePath = filePath,
    fileSize = fileSize,
    audioQuality = audioQuality,
    manifestHash = manifestHash,
    offlineRevalidateAt = offlineRevalidateAt,
    offlineValidUntil = offlineValidUntil,
    downloadedAt = downloadedAt,
    state = DownloadState.valueOf(state),
    collectionId = collectionId,
    collectionType = CollectionType.valueOf(collectionType),
)

fun DownloadedTrack.toEntity(): DownloadedTrackEntity = DownloadedTrackEntity(
    trackId = trackId,
    title = title,
    artistName = artistName,
    albumTitle = albumTitle,
    imageUrl = imageUrl,
    duration = duration,
    trackNumber = trackNumber,
    filePath = filePath,
    fileSize = fileSize,
    audioQuality = audioQuality,
    manifestHash = manifestHash,
    offlineRevalidateAt = offlineRevalidateAt,
    offlineValidUntil = offlineValidUntil,
    downloadedAt = downloadedAt,
    state = state.name,
    collectionId = collectionId,
    collectionType = collectionType.name,
)

fun DownloadedCollectionEntity.toDomain(): DownloadedCollection = DownloadedCollection(
    id = id,
    type = CollectionType.valueOf(type),
    title = title,
    imageUrl = imageUrl,
    trackCount = trackCount,
    downloadedTrackCount = downloadedTrackCount,
    totalSizeBytes = totalSizeBytes,
    downloadedAt = downloadedAt,
    state = DownloadState.valueOf(state),
)

fun DownloadedCollection.toEntity(): DownloadedCollectionEntity = DownloadedCollectionEntity(
    id = id,
    type = type.name,
    title = title,
    imageUrl = imageUrl,
    trackCount = trackCount,
    downloadedTrackCount = downloadedTrackCount,
    totalSizeBytes = totalSizeBytes,
    downloadedAt = downloadedAt,
    state = state.name,
)
