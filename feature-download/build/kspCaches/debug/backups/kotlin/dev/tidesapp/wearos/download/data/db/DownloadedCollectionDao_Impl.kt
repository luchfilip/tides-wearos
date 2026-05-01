package dev.tidesapp.wearos.download.`data`.db

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
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
public class DownloadedCollectionDao_Impl(
  __db: RoomDatabase,
) : DownloadedCollectionDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfDownloadedCollectionEntity:
      EntityInsertAdapter<DownloadedCollectionEntity>

  private val __updateAdapterOfDownloadedCollectionEntity:
      EntityDeleteOrUpdateAdapter<DownloadedCollectionEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfDownloadedCollectionEntity = object : EntityInsertAdapter<DownloadedCollectionEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `downloaded_collections` (`id`,`type`,`title`,`imageUrl`,`trackCount`,`downloadedTrackCount`,`totalSizeBytes`,`downloadedAt`,`state`) VALUES (?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: DownloadedCollectionEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.type)
        statement.bindText(3, entity.title)
        statement.bindText(4, entity.imageUrl)
        statement.bindLong(5, entity.trackCount.toLong())
        statement.bindLong(6, entity.downloadedTrackCount.toLong())
        statement.bindLong(7, entity.totalSizeBytes)
        statement.bindLong(8, entity.downloadedAt)
        statement.bindText(9, entity.state)
      }
    }
    this.__updateAdapterOfDownloadedCollectionEntity = object : EntityDeleteOrUpdateAdapter<DownloadedCollectionEntity>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `downloaded_collections` SET `id` = ?,`type` = ?,`title` = ?,`imageUrl` = ?,`trackCount` = ?,`downloadedTrackCount` = ?,`totalSizeBytes` = ?,`downloadedAt` = ?,`state` = ? WHERE `id` = ? AND `type` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: DownloadedCollectionEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.type)
        statement.bindText(3, entity.title)
        statement.bindText(4, entity.imageUrl)
        statement.bindLong(5, entity.trackCount.toLong())
        statement.bindLong(6, entity.downloadedTrackCount.toLong())
        statement.bindLong(7, entity.totalSizeBytes)
        statement.bindLong(8, entity.downloadedAt)
        statement.bindText(9, entity.state)
        statement.bindText(10, entity.id)
        statement.bindText(11, entity.type)
      }
    }
  }

  public override suspend fun insert(entity: DownloadedCollectionEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfDownloadedCollectionEntity.insert(_connection, entity)
  }

  public override suspend fun update(entity: DownloadedCollectionEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfDownloadedCollectionEntity.handle(_connection, entity)
  }

  public override fun getAll(): Flow<List<DownloadedCollectionEntity>> {
    val _sql: String = "SELECT * FROM downloaded_collections ORDER BY downloadedAt DESC"
    return createFlow(__db, false, arrayOf("downloaded_collections")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfImageUrl: Int = getColumnIndexOrThrow(_stmt, "imageUrl")
        val _columnIndexOfTrackCount: Int = getColumnIndexOrThrow(_stmt, "trackCount")
        val _columnIndexOfDownloadedTrackCount: Int = getColumnIndexOrThrow(_stmt, "downloadedTrackCount")
        val _columnIndexOfTotalSizeBytes: Int = getColumnIndexOrThrow(_stmt, "totalSizeBytes")
        val _columnIndexOfDownloadedAt: Int = getColumnIndexOrThrow(_stmt, "downloadedAt")
        val _columnIndexOfState: Int = getColumnIndexOrThrow(_stmt, "state")
        val _result: MutableList<DownloadedCollectionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: DownloadedCollectionEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpImageUrl: String
          _tmpImageUrl = _stmt.getText(_columnIndexOfImageUrl)
          val _tmpTrackCount: Int
          _tmpTrackCount = _stmt.getLong(_columnIndexOfTrackCount).toInt()
          val _tmpDownloadedTrackCount: Int
          _tmpDownloadedTrackCount = _stmt.getLong(_columnIndexOfDownloadedTrackCount).toInt()
          val _tmpTotalSizeBytes: Long
          _tmpTotalSizeBytes = _stmt.getLong(_columnIndexOfTotalSizeBytes)
          val _tmpDownloadedAt: Long
          _tmpDownloadedAt = _stmt.getLong(_columnIndexOfDownloadedAt)
          val _tmpState: String
          _tmpState = _stmt.getText(_columnIndexOfState)
          _item = DownloadedCollectionEntity(_tmpId,_tmpType,_tmpTitle,_tmpImageUrl,_tmpTrackCount,_tmpDownloadedTrackCount,_tmpTotalSizeBytes,_tmpDownloadedAt,_tmpState)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getById(id: String, type: String): DownloadedCollectionEntity? {
    val _sql: String = "SELECT * FROM downloaded_collections WHERE id = ? AND type = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        _argIndex = 2
        _stmt.bindText(_argIndex, type)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfImageUrl: Int = getColumnIndexOrThrow(_stmt, "imageUrl")
        val _columnIndexOfTrackCount: Int = getColumnIndexOrThrow(_stmt, "trackCount")
        val _columnIndexOfDownloadedTrackCount: Int = getColumnIndexOrThrow(_stmt, "downloadedTrackCount")
        val _columnIndexOfTotalSizeBytes: Int = getColumnIndexOrThrow(_stmt, "totalSizeBytes")
        val _columnIndexOfDownloadedAt: Int = getColumnIndexOrThrow(_stmt, "downloadedAt")
        val _columnIndexOfState: Int = getColumnIndexOrThrow(_stmt, "state")
        val _result: DownloadedCollectionEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpImageUrl: String
          _tmpImageUrl = _stmt.getText(_columnIndexOfImageUrl)
          val _tmpTrackCount: Int
          _tmpTrackCount = _stmt.getLong(_columnIndexOfTrackCount).toInt()
          val _tmpDownloadedTrackCount: Int
          _tmpDownloadedTrackCount = _stmt.getLong(_columnIndexOfDownloadedTrackCount).toInt()
          val _tmpTotalSizeBytes: Long
          _tmpTotalSizeBytes = _stmt.getLong(_columnIndexOfTotalSizeBytes)
          val _tmpDownloadedAt: Long
          _tmpDownloadedAt = _stmt.getLong(_columnIndexOfDownloadedAt)
          val _tmpState: String
          _tmpState = _stmt.getText(_columnIndexOfState)
          _result = DownloadedCollectionEntity(_tmpId,_tmpType,_tmpTitle,_tmpImageUrl,_tmpTrackCount,_tmpDownloadedTrackCount,_tmpTotalSizeBytes,_tmpDownloadedAt,_tmpState)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteById(id: String, type: String) {
    val _sql: String = "DELETE FROM downloaded_collections WHERE id = ? AND type = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        _argIndex = 2
        _stmt.bindText(_argIndex, type)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAll() {
    val _sql: String = "DELETE FROM downloaded_collections"
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
