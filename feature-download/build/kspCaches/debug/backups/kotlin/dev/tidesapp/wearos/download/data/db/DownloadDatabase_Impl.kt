package dev.tidesapp.wearos.download.`data`.db

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class DownloadDatabase_Impl : DownloadDatabase() {
  private val _downloadedTrackDao: Lazy<DownloadedTrackDao> = lazy {
    DownloadedTrackDao_Impl(this)
  }

  private val _downloadedCollectionDao: Lazy<DownloadedCollectionDao> = lazy {
    DownloadedCollectionDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(2, "a73de55bd123d46435f29b6e7e2c598e", "cefb347ddce702595d020e18d054488b") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `downloaded_tracks` (`trackId` INTEGER NOT NULL, `title` TEXT NOT NULL, `artistName` TEXT NOT NULL, `albumTitle` TEXT NOT NULL, `imageUrl` TEXT NOT NULL, `duration` INTEGER NOT NULL, `trackNumber` INTEGER NOT NULL, `filePath` TEXT NOT NULL, `fileSize` INTEGER NOT NULL, `audioQuality` TEXT NOT NULL, `manifestHash` TEXT NOT NULL, `offlineRevalidateAt` INTEGER NOT NULL, `offlineValidUntil` INTEGER NOT NULL, `downloadedAt` INTEGER NOT NULL, `state` TEXT NOT NULL, `collectionId` TEXT NOT NULL, `collectionType` TEXT NOT NULL, PRIMARY KEY(`trackId`))")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_downloaded_tracks_collectionId` ON `downloaded_tracks` (`collectionId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_downloaded_tracks_state` ON `downloaded_tracks` (`state`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `downloaded_collections` (`id` TEXT NOT NULL, `type` TEXT NOT NULL, `title` TEXT NOT NULL, `imageUrl` TEXT NOT NULL, `trackCount` INTEGER NOT NULL, `downloadedTrackCount` INTEGER NOT NULL, `totalSizeBytes` INTEGER NOT NULL, `downloadedAt` INTEGER NOT NULL, `state` TEXT NOT NULL, PRIMARY KEY(`id`, `type`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'a73de55bd123d46435f29b6e7e2c598e')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `downloaded_tracks`")
        connection.execSQL("DROP TABLE IF EXISTS `downloaded_collections`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection): RoomOpenDelegate.ValidationResult {
        val _columnsDownloadedTracks: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsDownloadedTracks.put("trackId", TableInfo.Column("trackId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDownloadedTracks.put("title", TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDownloadedTracks.put("artistName", TableInfo.Column("artistName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDownloadedTracks.put("albumTitle", TableInfo.Column("albumTitle", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDownloadedTracks.put("imageUrl", TableInfo.Column("imageUrl", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDownloadedTracks.put("duration", TableInfo.Column("duration", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDownloadedTracks.put("trackNumber", TableInfo.Column("trackNumber", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDownloadedTracks.put("filePath", TableInfo.Column("filePath", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDownloadedTracks.put("fileSize", TableInfo.Column("fileSize", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDownloadedTracks.put("audioQuality", TableInfo.Column("audioQuality", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDownloadedTracks.put("manifestHash", TableInfo.Column("manifestHash", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDownloadedTracks.put("offlineRevalidateAt", TableInfo.Column("offlineRevalidateAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDownloadedTracks.put("offlineValidUntil", TableInfo.Column("offlineValidUntil", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDownloadedTracks.put("downloadedAt", TableInfo.Column("downloadedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDownloadedTracks.put("state", TableInfo.Column("state", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDownloadedTracks.put("collectionId", TableInfo.Column("collectionId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDownloadedTracks.put("collectionType", TableInfo.Column("collectionType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysDownloadedTracks: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesDownloadedTracks: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesDownloadedTracks.add(TableInfo.Index("index_downloaded_tracks_collectionId", false, listOf("collectionId"), listOf("ASC")))
        _indicesDownloadedTracks.add(TableInfo.Index("index_downloaded_tracks_state", false, listOf("state"), listOf("ASC")))
        val _infoDownloadedTracks: TableInfo = TableInfo("downloaded_tracks", _columnsDownloadedTracks, _foreignKeysDownloadedTracks, _indicesDownloadedTracks)
        val _existingDownloadedTracks: TableInfo = read(connection, "downloaded_tracks")
        if (!_infoDownloadedTracks.equals(_existingDownloadedTracks)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |downloaded_tracks(dev.tidesapp.wearos.download.data.db.DownloadedTrackEntity).
              | Expected:
              |""".trimMargin() + _infoDownloadedTracks + """
              |
              | Found:
              |""".trimMargin() + _existingDownloadedTracks)
        }
        val _columnsDownloadedCollections: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsDownloadedCollections.put("id", TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDownloadedCollections.put("type", TableInfo.Column("type", "TEXT", true, 2, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDownloadedCollections.put("title", TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDownloadedCollections.put("imageUrl", TableInfo.Column("imageUrl", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDownloadedCollections.put("trackCount", TableInfo.Column("trackCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDownloadedCollections.put("downloadedTrackCount", TableInfo.Column("downloadedTrackCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDownloadedCollections.put("totalSizeBytes", TableInfo.Column("totalSizeBytes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDownloadedCollections.put("downloadedAt", TableInfo.Column("downloadedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDownloadedCollections.put("state", TableInfo.Column("state", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysDownloadedCollections: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesDownloadedCollections: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoDownloadedCollections: TableInfo = TableInfo("downloaded_collections", _columnsDownloadedCollections, _foreignKeysDownloadedCollections, _indicesDownloadedCollections)
        val _existingDownloadedCollections: TableInfo = read(connection, "downloaded_collections")
        if (!_infoDownloadedCollections.equals(_existingDownloadedCollections)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |downloaded_collections(dev.tidesapp.wearos.download.data.db.DownloadedCollectionEntity).
              | Expected:
              |""".trimMargin() + _infoDownloadedCollections + """
              |
              | Found:
              |""".trimMargin() + _existingDownloadedCollections)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "downloaded_tracks", "downloaded_collections")
  }

  public override fun clearAllTables() {
    super.performClear(false, "downloaded_tracks", "downloaded_collections")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(DownloadedTrackDao::class, DownloadedTrackDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(DownloadedCollectionDao::class, DownloadedCollectionDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>): List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun downloadedTrackDao(): DownloadedTrackDao = _downloadedTrackDao.value

  public override fun downloadedCollectionDao(): DownloadedCollectionDao = _downloadedCollectionDao.value
}
