package dev.tidesapp.wearos.download.`data`.db

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class DownloadedTrackDao_Impl(
  __db: RoomDatabase,
) : DownloadedTrackDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfDownloadedTrackEntity: EntityInsertAdapter<DownloadedTrackEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfDownloadedTrackEntity = object : EntityInsertAdapter<DownloadedTrackEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `downloaded_tracks` (`trackId`,`title`,`artistName`,`albumTitle`,`imageUrl`,`duration`,`trackNumber`,`filePath`,`fileSize`,`audioQuality`,`manifestHash`,`offlineRevalidateAt`,`offlineValidUntil`,`downloadedAt`,`state`,`collectionId`,`collectionType`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: DownloadedTrackEntity) {
        statement.bindLong(1, entity.trackId)
        statement.bindText(2, entity.title)
        statement.bindText(3, entity.artistName)
        statement.bindText(4, entity.albumTitle)
        statement.bindText(5, entity.imageUrl)
        statement.bindLong(6, entity.duration.toLong())
        statement.bindLong(7, entity.trackNumber.toLong())
        statement.bindText(8, entity.filePath)
        statement.bindLong(9, entity.fileSize)
        statement.bindText(10, entity.audioQuality)
        statement.bindText(11, entity.manifestHash)
        statement.bindLong(12, entity.offlineRevalidateAt)
        statement.bindLong(13, entity.offlineValidUntil)
        statement.bindLong(14, entity.downloadedAt)
        statement.bindText(15, entity.state)
        statement.bindText(16, entity.collectionId)
        statement.bindText(17, entity.collectionType)
      }
    }
  }

  public override suspend fun insert(entity: DownloadedTrackEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfDownloadedTrackEntity.insert(_connection, entity)
  }

  public override suspend fun insertAll(entities: List<DownloadedTrackEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfDownloadedTrackEntity.insert(_connection, entities)
  }

  public override suspend fun getByTrackId(trackId: Long): DownloadedTrackEntity? {
    val _sql: String = "SELECT * FROM downloaded_tracks WHERE trackId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, trackId)
        val _columnIndexOfTrackId: Int = getColumnIndexOrThrow(_stmt, "trackId")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfArtistName: Int = getColumnIndexOrThrow(_stmt, "artistName")
        val _columnIndexOfAlbumTitle: Int = getColumnIndexOrThrow(_stmt, "albumTitle")
        val _columnIndexOfImageUrl: Int = getColumnIndexOrThrow(_stmt, "imageUrl")
        val _columnIndexOfDuration: Int = getColumnIndexOrThrow(_stmt, "duration")
        val _columnIndexOfTrackNumber: Int = getColumnIndexOrThrow(_stmt, "trackNumber")
        val _columnIndexOfFilePath: Int = getColumnIndexOrThrow(_stmt, "filePath")
        val _columnIndexOfFileSize: Int = getColumnIndexOrThrow(_stmt, "fileSize")
        val _columnIndexOfAudioQuality: Int = getColumnIndexOrThrow(_stmt, "audioQuality")
        val _columnIndexOfManifestHash: Int = getColumnIndexOrThrow(_stmt, "manifestHash")
        val _columnIndexOfOfflineRevalidateAt: Int = getColumnIndexOrThrow(_stmt, "offlineRevalidateAt")
        val _columnIndexOfOfflineValidUntil: Int = getColumnIndexOrThrow(_stmt, "offlineValidUntil")
        val _columnIndexOfDownloadedAt: Int = getColumnIndexOrThrow(_stmt, "downloadedAt")
        val _columnIndexOfState: Int = getColumnIndexOrThrow(_stmt, "state")
        val _columnIndexOfCollectionId: Int = getColumnIndexOrThrow(_stmt, "collectionId")
        val _columnIndexOfCollectionType: Int = getColumnIndexOrThrow(_stmt, "collectionType")
        val _result: DownloadedTrackEntity?
        if (_stmt.step()) {
          val _tmpTrackId: Long
          _tmpTrackId = _stmt.getLong(_columnIndexOfTrackId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpArtistName: String
          _tmpArtistName = _stmt.getText(_columnIndexOfArtistName)
          val _tmpAlbumTitle: String
          _tmpAlbumTitle = _stmt.getText(_columnIndexOfAlbumTitle)
          val _tmpImageUrl: String
          _tmpImageUrl = _stmt.getText(_columnIndexOfImageUrl)
          val _tmpDuration: Int
          _tmpDuration = _stmt.getLong(_columnIndexOfDuration).toInt()
          val _tmpTrackNumber: Int
          _tmpTrackNumber = _stmt.getLong(_columnIndexOfTrackNumber).toInt()
          val _tmpFilePath: String
          _tmpFilePath = _stmt.getText(_columnIndexOfFilePath)
          val _tmpFileSize: Long
          _tmpFileSize = _stmt.getLong(_columnIndexOfFileSize)
          val _tmpAudioQuality: String
          _tmpAudioQuality = _stmt.getText(_columnIndexOfAudioQuality)
          val _tmpManifestHash: String
          _tmpManifestHash = _stmt.getText(_columnIndexOfManifestHash)
          val _tmpOfflineRevalidateAt: Long
          _tmpOfflineRevalidateAt = _stmt.getLong(_columnIndexOfOfflineRevalidateAt)
          val _tmpOfflineValidUntil: Long
          _tmpOfflineValidUntil = _stmt.getLong(_columnIndexOfOfflineValidUntil)
          val _tmpDownloadedAt: Long
          _tmpDownloadedAt = _stmt.getLong(_columnIndexOfDownloadedAt)
          val _tmpState: String
          _tmpState = _stmt.getText(_columnIndexOfState)
          val _tmpCollectionId: String
          _tmpCollectionId = _stmt.getText(_columnIndexOfCollectionId)
          val _tmpCollectionType: String
          _tmpCollectionType = _stmt.getText(_columnIndexOfCollectionType)
          _result = DownloadedTrackEntity(_tmpTrackId,_tmpTitle,_tmpArtistName,_tmpAlbumTitle,_tmpImageUrl,_tmpDuration,_tmpTrackNumber,_tmpFilePath,_tmpFileSize,_tmpAudioQuality,_tmpManifestHash,_tmpOfflineRevalidateAt,_tmpOfflineValidUntil,_tmpDownloadedAt,_tmpState,_tmpCollectionId,_tmpCollectionType)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getByCollectionId(collectionId: String): Flow<List<DownloadedTrackEntity>> {
    val _sql: String = "SELECT * FROM downloaded_tracks WHERE collectionId = ?"
    return createFlow(__db, false, arrayOf("downloaded_tracks")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, collectionId)
        val _columnIndexOfTrackId: Int = getColumnIndexOrThrow(_stmt, "trackId")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfArtistName: Int = getColumnIndexOrThrow(_stmt, "artistName")
        val _columnIndexOfAlbumTitle: Int = getColumnIndexOrThrow(_stmt, "albumTitle")
        val _columnIndexOfImageUrl: Int = getColumnIndexOrThrow(_stmt, "imageUrl")
        val _columnIndexOfDuration: Int = getColumnIndexOrThrow(_stmt, "duration")
        val _columnIndexOfTrackNumber: Int = getColumnIndexOrThrow(_stmt, "trackNumber")
        val _columnIndexOfFilePath: Int = getColumnIndexOrThrow(_stmt, "filePath")
        val _columnIndexOfFileSize: Int = getColumnIndexOrThrow(_stmt, "fileSize")
        val _columnIndexOfAudioQuality: Int = getColumnIndexOrThrow(_stmt, "audioQuality")
        val _columnIndexOfManifestHash: Int = getColumnIndexOrThrow(_stmt, "manifestHash")
        val _columnIndexOfOfflineRevalidateAt: Int = getColumnIndexOrThrow(_stmt, "offlineRevalidateAt")
        val _columnIndexOfOfflineValidUntil: Int = getColumnIndexOrThrow(_stmt, "offlineValidUntil")
        val _columnIndexOfDownloadedAt: Int = getColumnIndexOrThrow(_stmt, "downloadedAt")
        val _columnIndexOfState: Int = getColumnIndexOrThrow(_stmt, "state")
        val _columnIndexOfCollectionId: Int = getColumnIndexOrThrow(_stmt, "collectionId")
        val _columnIndexOfCollectionType: Int = getColumnIndexOrThrow(_stmt, "collectionType")
        val _result: MutableList<DownloadedTrackEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: DownloadedTrackEntity
          val _tmpTrackId: Long
          _tmpTrackId = _stmt.getLong(_columnIndexOfTrackId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpArtistName: String
          _tmpArtistName = _stmt.getText(_columnIndexOfArtistName)
          val _tmpAlbumTitle: String
          _tmpAlbumTitle = _stmt.getText(_columnIndexOfAlbumTitle)
          val _tmpImageUrl: String
          _tmpImageUrl = _stmt.getText(_columnIndexOfImageUrl)
          val _tmpDuration: Int
          _tmpDuration = _stmt.getLong(_columnIndexOfDuration).toInt()
          val _tmpTrackNumber: Int
          _tmpTrackNumber = _stmt.getLong(_columnIndexOfTrackNumber).toInt()
          val _tmpFilePath: String
          _tmpFilePath = _stmt.getText(_columnIndexOfFilePath)
          val _tmpFileSize: Long
          _tmpFileSize = _stmt.getLong(_columnIndexOfFileSize)
          val _tmpAudioQuality: String
          _tmpAudioQuality = _stmt.getText(_columnIndexOfAudioQuality)
          val _tmpManifestHash: String
          _tmpManifestHash = _stmt.getText(_columnIndexOfManifestHash)
          val _tmpOfflineRevalidateAt: Long
          _tmpOfflineRevalidateAt = _stmt.getLong(_columnIndexOfOfflineRevalidateAt)
          val _tmpOfflineValidUntil: Long
          _tmpOfflineValidUntil = _stmt.getLong(_columnIndexOfOfflineValidUntil)
          val _tmpDownloadedAt: Long
          _tmpDownloadedAt = _stmt.getLong(_columnIndexOfDownloadedAt)
          val _tmpState: String
          _tmpState = _stmt.getText(_columnIndexOfState)
          val _tmpCollectionId: String
          _tmpCollectionId = _stmt.getText(_columnIndexOfCollectionId)
          val _tmpCollectionType: String
          _tmpCollectionType = _stmt.getText(_columnIndexOfCollectionType)
          _item = DownloadedTrackEntity(_tmpTrackId,_tmpTitle,_tmpArtistName,_tmpAlbumTitle,_tmpImageUrl,_tmpDuration,_tmpTrackNumber,_tmpFilePath,_tmpFileSize,_tmpAudioQuality,_tmpManifestHash,_tmpOfflineRevalidateAt,_tmpOfflineValidUntil,_tmpDownloadedAt,_tmpState,_tmpCollectionId,_tmpCollectionType)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getAllCompleted(): Flow<List<DownloadedTrackEntity>> {
    val _sql: String = "SELECT * FROM downloaded_tracks WHERE state = 'COMPLETED'"
    return createFlow(__db, false, arrayOf("downloaded_tracks")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfTrackId: Int = getColumnIndexOrThrow(_stmt, "trackId")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfArtistName: Int = getColumnIndexOrThrow(_stmt, "artistName")
        val _columnIndexOfAlbumTitle: Int = getColumnIndexOrThrow(_stmt, "albumTitle")
        val _columnIndexOfImageUrl: Int = getColumnIndexOrThrow(_stmt, "imageUrl")
        val _columnIndexOfDuration: Int = getColumnIndexOrThrow(_stmt, "duration")
        val _columnIndexOfTrackNumber: Int = getColumnIndexOrThrow(_stmt, "trackNumber")
        val _columnIndexOfFilePath: Int = getColumnIndexOrThrow(_stmt, "filePath")
        val _columnIndexOfFileSize: Int = getColumnIndexOrThrow(_stmt, "fileSize")
        val _columnIndexOfAudioQuality: Int = getColumnIndexOrThrow(_stmt, "audioQuality")
        val _columnIndexOfManifestHash: Int = getColumnIndexOrThrow(_stmt, "manifestHash")
        val _columnIndexOfOfflineRevalidateAt: Int = getColumnIndexOrThrow(_stmt, "offlineRevalidateAt")
        val _columnIndexOfOfflineValidUntil: Int = getColumnIndexOrThrow(_stmt, "offlineValidUntil")
        val _columnIndexOfDownloadedAt: Int = getColumnIndexOrThrow(_stmt, "downloadedAt")
        val _columnIndexOfState: Int = getColumnIndexOrThrow(_stmt, "state")
        val _columnIndexOfCollectionId: Int = getColumnIndexOrThrow(_stmt, "collectionId")
        val _columnIndexOfCollectionType: Int = getColumnIndexOrThrow(_stmt, "collectionType")
        val _result: MutableList<DownloadedTrackEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: DownloadedTrackEntity
          val _tmpTrackId: Long
          _tmpTrackId = _stmt.getLong(_columnIndexOfTrackId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpArtistName: String
          _tmpArtistName = _stmt.getText(_columnIndexOfArtistName)
          val _tmpAlbumTitle: String
          _tmpAlbumTitle = _stmt.getText(_columnIndexOfAlbumTitle)
          val _tmpImageUrl: String
          _tmpImageUrl = _stmt.getText(_columnIndexOfImageUrl)
          val _tmpDuration: Int
          _tmpDuration = _stmt.getLong(_columnIndexOfDuration).toInt()
          val _tmpTrackNumber: Int
          _tmpTrackNumber = _stmt.getLong(_columnIndexOfTrackNumber).toInt()
          val _tmpFilePath: String
          _tmpFilePath = _stmt.getText(_columnIndexOfFilePath)
          val _tmpFileSize: Long
          _tmpFileSize = _stmt.getLong(_columnIndexOfFileSize)
          val _tmpAudioQuality: String
          _tmpAudioQuality = _stmt.getText(_columnIndexOfAudioQuality)
          val _tmpManifestHash: String
          _tmpManifestHash = _stmt.getText(_columnIndexOfManifestHash)
          val _tmpOfflineRevalidateAt: Long
          _tmpOfflineRevalidateAt = _stmt.getLong(_columnIndexOfOfflineRevalidateAt)
          val _tmpOfflineValidUntil: Long
          _tmpOfflineValidUntil = _stmt.getLong(_columnIndexOfOfflineValidUntil)
          val _tmpDownloadedAt: Long
          _tmpDownloadedAt = _stmt.getLong(_columnIndexOfDownloadedAt)
          val _tmpState: String
          _tmpState = _stmt.getText(_columnIndexOfState)
          val _tmpCollectionId: String
          _tmpCollectionId = _stmt.getText(_columnIndexOfCollectionId)
          val _tmpCollectionType: String
          _tmpCollectionType = _stmt.getText(_columnIndexOfCollectionType)
          _item = DownloadedTrackEntity(_tmpTrackId,_tmpTitle,_tmpArtistName,_tmpAlbumTitle,_tmpImageUrl,_tmpDuration,_tmpTrackNumber,_tmpFilePath,_tmpFileSize,_tmpAudioQuality,_tmpManifestHash,_tmpOfflineRevalidateAt,_tmpOfflineValidUntil,_tmpDownloadedAt,_tmpState,_tmpCollectionId,_tmpCollectionType)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getByState(state: String): Flow<List<DownloadedTrackEntity>> {
    val _sql: String = "SELECT * FROM downloaded_tracks WHERE state = ?"
    return createFlow(__db, false, arrayOf("downloaded_tracks")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, state)
        val _columnIndexOfTrackId: Int = getColumnIndexOrThrow(_stmt, "trackId")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfArtistName: Int = getColumnIndexOrThrow(_stmt, "artistName")
        val _columnIndexOfAlbumTitle: Int = getColumnIndexOrThrow(_stmt, "albumTitle")
        val _columnIndexOfImageUrl: Int = getColumnIndexOrThrow(_stmt, "imageUrl")
        val _columnIndexOfDuration: Int = getColumnIndexOrThrow(_stmt, "duration")
        val _columnIndexOfTrackNumber: Int = getColumnIndexOrThrow(_stmt, "trackNumber")
        val _columnIndexOfFilePath: Int = getColumnIndexOrThrow(_stmt, "filePath")
        val _columnIndexOfFileSize: Int = getColumnIndexOrThrow(_stmt, "fileSize")
        val _columnIndexOfAudioQuality: Int = getColumnIndexOrThrow(_stmt, "audioQuality")
        val _columnIndexOfManifestHash: Int = getColumnIndexOrThrow(_stmt, "manifestHash")
        val _columnIndexOfOfflineRevalidateAt: Int = getColumnIndexOrThrow(_stmt, "offlineRevalidateAt")
        val _columnIndexOfOfflineValidUntil: Int = getColumnIndexOrThrow(_stmt, "offlineValidUntil")
        val _columnIndexOfDownloadedAt: Int = getColumnIndexOrThrow(_stmt, "downloadedAt")
        val _columnIndexOfState: Int = getColumnIndexOrThrow(_stmt, "state")
        val _columnIndexOfCollectionId: Int = getColumnIndexOrThrow(_stmt, "collectionId")
        val _columnIndexOfCollectionType: Int = getColumnIndexOrThrow(_stmt, "collectionType")
        val _result: MutableList<DownloadedTrackEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: DownloadedTrackEntity
          val _tmpTrackId: Long
          _tmpTrackId = _stmt.getLong(_columnIndexOfTrackId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpArtistName: String
          _tmpArtistName = _stmt.getText(_columnIndexOfArtistName)
          val _tmpAlbumTitle: String
          _tmpAlbumTitle = _stmt.getText(_columnIndexOfAlbumTitle)
          val _tmpImageUrl: String
          _tmpImageUrl = _stmt.getText(_columnIndexOfImageUrl)
          val _tmpDuration: Int
          _tmpDuration = _stmt.getLong(_columnIndexOfDuration).toInt()
          val _tmpTrackNumber: Int
          _tmpTrackNumber = _stmt.getLong(_columnIndexOfTrackNumber).toInt()
          val _tmpFilePath: String
          _tmpFilePath = _stmt.getText(_columnIndexOfFilePath)
          val _tmpFileSize: Long
          _tmpFileSize = _stmt.getLong(_columnIndexOfFileSize)
          val _tmpAudioQuality: String
          _tmpAudioQuality = _stmt.getText(_columnIndexOfAudioQuality)
          val _tmpManifestHash: String
          _tmpManifestHash = _stmt.getText(_columnIndexOfManifestHash)
          val _tmpOfflineRevalidateAt: Long
          _tmpOfflineRevalidateAt = _stmt.getLong(_columnIndexOfOfflineRevalidateAt)
          val _tmpOfflineValidUntil: Long
          _tmpOfflineValidUntil = _stmt.getLong(_columnIndexOfOfflineValidUntil)
          val _tmpDownloadedAt: Long
          _tmpDownloadedAt = _stmt.getLong(_columnIndexOfDownloadedAt)
          val _tmpState: String
          _tmpState = _stmt.getText(_columnIndexOfState)
          val _tmpCollectionId: String
          _tmpCollectionId = _stmt.getText(_columnIndexOfCollectionId)
          val _tmpCollectionType: String
          _tmpCollectionType = _stmt.getText(_columnIndexOfCollectionType)
          _item = DownloadedTrackEntity(_tmpTrackId,_tmpTitle,_tmpArtistName,_tmpAlbumTitle,_tmpImageUrl,_tmpDuration,_tmpTrackNumber,_tmpFilePath,_tmpFileSize,_tmpAudioQuality,_tmpManifestHash,_tmpOfflineRevalidateAt,_tmpOfflineValidUntil,_tmpDownloadedAt,_tmpState,_tmpCollectionId,_tmpCollectionType)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getTotalSizeBytes(): Flow<Long> {
    val _sql: String = "SELECT COALESCE(SUM(fileSize), 0) FROM downloaded_tracks WHERE state = 'COMPLETED'"
    return createFlow(__db, false, arrayOf("downloaded_tracks")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: Long
        if (_stmt.step()) {
          val _tmp: Long
          _tmp = _stmt.getLong(0)
          _result = _tmp
        } else {
          _result = 0L
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getTrackCount(): Flow<Int> {
    val _sql: String = "SELECT COUNT(*) FROM downloaded_tracks WHERE state = 'COMPLETED'"
    return createFlow(__db, false, arrayOf("downloaded_tracks")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun isTrackDownloaded(trackId: Long): Boolean {
    val _sql: String = "SELECT EXISTS(SELECT 1 FROM downloaded_tracks WHERE trackId = ? AND state = 'COMPLETED')"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, trackId)
        val _result: Boolean
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp != 0
        } else {
          _result = false
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getExpiredTracks(currentTime: Long): List<DownloadedTrackEntity> {
    val _sql: String = "SELECT * FROM downloaded_tracks WHERE offlineValidUntil < ? AND state = 'COMPLETED'"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, currentTime)
        val _columnIndexOfTrackId: Int = getColumnIndexOrThrow(_stmt, "trackId")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfArtistName: Int = getColumnIndexOrThrow(_stmt, "artistName")
        val _columnIndexOfAlbumTitle: Int = getColumnIndexOrThrow(_stmt, "albumTitle")
        val _columnIndexOfImageUrl: Int = getColumnIndexOrThrow(_stmt, "imageUrl")
        val _columnIndexOfDuration: Int = getColumnIndexOrThrow(_stmt, "duration")
        val _columnIndexOfTrackNumber: Int = getColumnIndexOrThrow(_stmt, "trackNumber")
        val _columnIndexOfFilePath: Int = getColumnIndexOrThrow(_stmt, "filePath")
        val _columnIndexOfFileSize: Int = getColumnIndexOrThrow(_stmt, "fileSize")
        val _columnIndexOfAudioQuality: Int = getColumnIndexOrThrow(_stmt, "audioQuality")
        val _columnIndexOfManifestHash: Int = getColumnIndexOrThrow(_stmt, "manifestHash")
        val _columnIndexOfOfflineRevalidateAt: Int = getColumnIndexOrThrow(_stmt, "offlineRevalidateAt")
        val _columnIndexOfOfflineValidUntil: Int = getColumnIndexOrThrow(_stmt, "offlineValidUntil")
        val _columnIndexOfDownloadedAt: Int = getColumnIndexOrThrow(_stmt, "downloadedAt")
        val _columnIndexOfState: Int = getColumnIndexOrThrow(_stmt, "state")
        val _columnIndexOfCollectionId: Int = getColumnIndexOrThrow(_stmt, "collectionId")
        val _columnIndexOfCollectionType: Int = getColumnIndexOrThrow(_stmt, "collectionType")
        val _result: MutableList<DownloadedTrackEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: DownloadedTrackEntity
          val _tmpTrackId: Long
          _tmpTrackId = _stmt.getLong(_columnIndexOfTrackId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpArtistName: String
          _tmpArtistName = _stmt.getText(_columnIndexOfArtistName)
          val _tmpAlbumTitle: String
          _tmpAlbumTitle = _stmt.getText(_columnIndexOfAlbumTitle)
          val _tmpImageUrl: String
          _tmpImageUrl = _stmt.getText(_columnIndexOfImageUrl)
          val _tmpDuration: Int
          _tmpDuration = _stmt.getLong(_columnIndexOfDuration).toInt()
          val _tmpTrackNumber: Int
          _tmpTrackNumber = _stmt.getLong(_columnIndexOfTrackNumber).toInt()
          val _tmpFilePath: String
          _tmpFilePath = _stmt.getText(_columnIndexOfFilePath)
          val _tmpFileSize: Long
          _tmpFileSize = _stmt.getLong(_columnIndexOfFileSize)
          val _tmpAudioQuality: String
          _tmpAudioQuality = _stmt.getText(_columnIndexOfAudioQuality)
          val _tmpManifestHash: String
          _tmpManifestHash = _stmt.getText(_columnIndexOfManifestHash)
          val _tmpOfflineRevalidateAt: Long
          _tmpOfflineRevalidateAt = _stmt.getLong(_columnIndexOfOfflineRevalidateAt)
          val _tmpOfflineValidUntil: Long
          _tmpOfflineValidUntil = _stmt.getLong(_columnIndexOfOfflineValidUntil)
          val _tmpDownloadedAt: Long
          _tmpDownloadedAt = _stmt.getLong(_columnIndexOfDownloadedAt)
          val _tmpState: String
          _tmpState = _stmt.getText(_columnIndexOfState)
          val _tmpCollectionId: String
          _tmpCollectionId = _stmt.getText(_columnIndexOfCollectionId)
          val _tmpCollectionType: String
          _tmpCollectionType = _stmt.getText(_columnIndexOfCollectionType)
          _item = DownloadedTrackEntity(_tmpTrackId,_tmpTitle,_tmpArtistName,_tmpAlbumTitle,_tmpImageUrl,_tmpDuration,_tmpTrackNumber,_tmpFilePath,_tmpFileSize,_tmpAudioQuality,_tmpManifestHash,_tmpOfflineRevalidateAt,_tmpOfflineValidUntil,_tmpDownloadedAt,_tmpState,_tmpCollectionId,_tmpCollectionType)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getTracksNeedingRevalidation(currentTime: Long): List<DownloadedTrackEntity> {
    val _sql: String = "SELECT * FROM downloaded_tracks WHERE offlineRevalidateAt < ? AND state = 'COMPLETED'"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, currentTime)
        val _columnIndexOfTrackId: Int = getColumnIndexOrThrow(_stmt, "trackId")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfArtistName: Int = getColumnIndexOrThrow(_stmt, "artistName")
        val _columnIndexOfAlbumTitle: Int = getColumnIndexOrThrow(_stmt, "albumTitle")
        val _columnIndexOfImageUrl: Int = getColumnIndexOrThrow(_stmt, "imageUrl")
        val _columnIndexOfDuration: Int = getColumnIndexOrThrow(_stmt, "duration")
        val _columnIndexOfTrackNumber: Int = getColumnIndexOrThrow(_stmt, "trackNumber")
        val _columnIndexOfFilePath: Int = getColumnIndexOrThrow(_stmt, "filePath")
        val _columnIndexOfFileSize: Int = getColumnIndexOrThrow(_stmt, "fileSize")
        val _columnIndexOfAudioQuality: Int = getColumnIndexOrThrow(_stmt, "audioQuality")
        val _columnIndexOfManifestHash: Int = getColumnIndexOrThrow(_stmt, "manifestHash")
        val _columnIndexOfOfflineRevalidateAt: Int = getColumnIndexOrThrow(_stmt, "offlineRevalidateAt")
        val _columnIndexOfOfflineValidUntil: Int = getColumnIndexOrThrow(_stmt, "offlineValidUntil")
        val _columnIndexOfDownloadedAt: Int = getColumnIndexOrThrow(_stmt, "downloadedAt")
        val _columnIndexOfState: Int = getColumnIndexOrThrow(_stmt, "state")
        val _columnIndexOfCollectionId: Int = getColumnIndexOrThrow(_stmt, "collectionId")
        val _columnIndexOfCollectionType: Int = getColumnIndexOrThrow(_stmt, "collectionType")
        val _result: MutableList<DownloadedTrackEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: DownloadedTrackEntity
          val _tmpTrackId: Long
          _tmpTrackId = _stmt.getLong(_columnIndexOfTrackId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpArtistName: String
          _tmpArtistName = _stmt.getText(_columnIndexOfArtistName)
          val _tmpAlbumTitle: String
          _tmpAlbumTitle = _stmt.getText(_columnIndexOfAlbumTitle)
          val _tmpImageUrl: String
          _tmpImageUrl = _stmt.getText(_columnIndexOfImageUrl)
          val _tmpDuration: Int
          _tmpDuration = _stmt.getLong(_columnIndexOfDuration).toInt()
          val _tmpTrackNumber: Int
          _tmpTrackNumber = _stmt.getLong(_columnIndexOfTrackNumber).toInt()
          val _tmpFilePath: String
          _tmpFilePath = _stmt.getText(_columnIndexOfFilePath)
          val _tmpFileSize: Long
          _tmpFileSize = _stmt.getLong(_columnIndexOfFileSize)
          val _tmpAudioQuality: String
          _tmpAudioQuality = _stmt.getText(_columnIndexOfAudioQuality)
          val _tmpManifestHash: String
          _tmpManifestHash = _stmt.getText(_columnIndexOfManifestHash)
          val _tmpOfflineRevalidateAt: Long
          _tmpOfflineRevalidateAt = _stmt.getLong(_columnIndexOfOfflineRevalidateAt)
          val _tmpOfflineValidUntil: Long
          _tmpOfflineValidUntil = _stmt.getLong(_columnIndexOfOfflineValidUntil)
          val _tmpDownloadedAt: Long
          _tmpDownloadedAt = _stmt.getLong(_columnIndexOfDownloadedAt)
          val _tmpState: String
          _tmpState = _stmt.getText(_columnIndexOfState)
          val _tmpCollectionId: String
          _tmpCollectionId = _stmt.getText(_columnIndexOfCollectionId)
          val _tmpCollectionType: String
          _tmpCollectionType = _stmt.getText(_columnIndexOfCollectionType)
          _item = DownloadedTrackEntity(_tmpTrackId,_tmpTitle,_tmpArtistName,_tmpAlbumTitle,_tmpImageUrl,_tmpDuration,_tmpTrackNumber,_tmpFilePath,_tmpFileSize,_tmpAudioQuality,_tmpManifestHash,_tmpOfflineRevalidateAt,_tmpOfflineValidUntil,_tmpDownloadedAt,_tmpState,_tmpCollectionId,_tmpCollectionType)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getPendingByCollectionId(collectionId: String): List<DownloadedTrackEntity> {
    val _sql: String = "SELECT * FROM downloaded_tracks WHERE collectionId = ? AND state = 'PENDING'"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, collectionId)
        val _columnIndexOfTrackId: Int = getColumnIndexOrThrow(_stmt, "trackId")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfArtistName: Int = getColumnIndexOrThrow(_stmt, "artistName")
        val _columnIndexOfAlbumTitle: Int = getColumnIndexOrThrow(_stmt, "albumTitle")
        val _columnIndexOfImageUrl: Int = getColumnIndexOrThrow(_stmt, "imageUrl")
        val _columnIndexOfDuration: Int = getColumnIndexOrThrow(_stmt, "duration")
        val _columnIndexOfTrackNumber: Int = getColumnIndexOrThrow(_stmt, "trackNumber")
        val _columnIndexOfFilePath: Int = getColumnIndexOrThrow(_stmt, "filePath")
        val _columnIndexOfFileSize: Int = getColumnIndexOrThrow(_stmt, "fileSize")
        val _columnIndexOfAudioQuality: Int = getColumnIndexOrThrow(_stmt, "audioQuality")
        val _columnIndexOfManifestHash: Int = getColumnIndexOrThrow(_stmt, "manifestHash")
        val _columnIndexOfOfflineRevalidateAt: Int = getColumnIndexOrThrow(_stmt, "offlineRevalidateAt")
        val _columnIndexOfOfflineValidUntil: Int = getColumnIndexOrThrow(_stmt, "offlineValidUntil")
        val _columnIndexOfDownloadedAt: Int = getColumnIndexOrThrow(_stmt, "downloadedAt")
        val _columnIndexOfState: Int = getColumnIndexOrThrow(_stmt, "state")
        val _columnIndexOfCollectionId: Int = getColumnIndexOrThrow(_stmt, "collectionId")
        val _columnIndexOfCollectionType: Int = getColumnIndexOrThrow(_stmt, "collectionType")
        val _result: MutableList<DownloadedTrackEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: DownloadedTrackEntity
          val _tmpTrackId: Long
          _tmpTrackId = _stmt.getLong(_columnIndexOfTrackId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpArtistName: String
          _tmpArtistName = _stmt.getText(_columnIndexOfArtistName)
          val _tmpAlbumTitle: String
          _tmpAlbumTitle = _stmt.getText(_columnIndexOfAlbumTitle)
          val _tmpImageUrl: String
          _tmpImageUrl = _stmt.getText(_columnIndexOfImageUrl)
          val _tmpDuration: Int
          _tmpDuration = _stmt.getLong(_columnIndexOfDuration).toInt()
          val _tmpTrackNumber: Int
          _tmpTrackNumber = _stmt.getLong(_columnIndexOfTrackNumber).toInt()
          val _tmpFilePath: String
          _tmpFilePath = _stmt.getText(_columnIndexOfFilePath)
          val _tmpFileSize: Long
          _tmpFileSize = _stmt.getLong(_columnIndexOfFileSize)
          val _tmpAudioQuality: String
          _tmpAudioQuality = _stmt.getText(_columnIndexOfAudioQuality)
          val _tmpManifestHash: String
          _tmpManifestHash = _stmt.getText(_columnIndexOfManifestHash)
          val _tmpOfflineRevalidateAt: Long
          _tmpOfflineRevalidateAt = _stmt.getLong(_columnIndexOfOfflineRevalidateAt)
          val _tmpOfflineValidUntil: Long
          _tmpOfflineValidUntil = _stmt.getLong(_columnIndexOfOfflineValidUntil)
          val _tmpDownloadedAt: Long
          _tmpDownloadedAt = _stmt.getLong(_columnIndexOfDownloadedAt)
          val _tmpState: String
          _tmpState = _stmt.getText(_columnIndexOfState)
          val _tmpCollectionId: String
          _tmpCollectionId = _stmt.getText(_columnIndexOfCollectionId)
          val _tmpCollectionType: String
          _tmpCollectionType = _stmt.getText(_columnIndexOfCollectionType)
          _item = DownloadedTrackEntity(_tmpTrackId,_tmpTitle,_tmpArtistName,_tmpAlbumTitle,_tmpImageUrl,_tmpDuration,_tmpTrackNumber,_tmpFilePath,_tmpFileSize,_tmpAudioQuality,_tmpManifestHash,_tmpOfflineRevalidateAt,_tmpOfflineValidUntil,_tmpDownloadedAt,_tmpState,_tmpCollectionId,_tmpCollectionType)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateState(trackId: Long, state: String) {
    val _sql: String = "UPDATE downloaded_tracks SET state = ? WHERE trackId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, state)
        _argIndex = 2
        _stmt.bindLong(_argIndex, trackId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateCompleted(
    trackId: Long,
    state: String,
    filePath: String,
    fileSize: Long,
    manifestHash: String,
    revalidateAt: Long,
    validUntil: Long,
  ) {
    val _sql: String = "UPDATE downloaded_tracks SET state = ?, filePath = ?, fileSize = ?, manifestHash = ?, offlineRevalidateAt = ?, offlineValidUntil = ? WHERE trackId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, state)
        _argIndex = 2
        _stmt.bindText(_argIndex, filePath)
        _argIndex = 3
        _stmt.bindLong(_argIndex, fileSize)
        _argIndex = 4
        _stmt.bindText(_argIndex, manifestHash)
        _argIndex = 5
        _stmt.bindLong(_argIndex, revalidateAt)
        _argIndex = 6
        _stmt.bindLong(_argIndex, validUntil)
        _argIndex = 7
        _stmt.bindLong(_argIndex, trackId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteByTrackId(trackId: Long) {
    val _sql: String = "DELETE FROM downloaded_tracks WHERE trackId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, trackId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteByCollectionId(collectionId: String) {
    val _sql: String = "DELETE FROM downloaded_tracks WHERE collectionId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, collectionId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAll() {
    val _sql: String = "DELETE FROM downloaded_tracks"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
