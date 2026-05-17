package dev.gitfudge.musicworkbench.data.db;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class LibraryDatabase_Impl extends LibraryDatabase {
  private volatile TrackDao _trackDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `tracks` (`documentUri` TEXT NOT NULL, `treeUri` TEXT NOT NULL, `displayName` TEXT NOT NULL, `parentPath` TEXT NOT NULL, `format` TEXT NOT NULL, `sizeBytes` INTEGER NOT NULL, `lastModified` INTEGER NOT NULL, `durationMs` INTEGER, `title` TEXT, `artist` TEXT, `album` TEXT, `albumArtist` TEXT, `trackNumber` INTEGER, `discNumber` INTEGER, `year` TEXT, `genre` TEXT, `albumKey` TEXT NOT NULL, `albumLabel` TEXT NOT NULL, `hasEmbeddedArt` INTEGER NOT NULL, `artWidth` INTEGER, `artHeight` INTEGER, `thumbnailPath` TEXT, `hasSidecarLrc` INTEGER NOT NULL, `sidecarLrcSynced` INTEGER NOT NULL, `coreTagsComplete` INTEGER NOT NULL, `artistUnknown` INTEGER NOT NULL, `scannedAt` INTEGER NOT NULL, PRIMARY KEY(`documentUri`))");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_tracks_documentUri` ON `tracks` (`documentUri`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_tracks_albumKey` ON `tracks` (`albumKey`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_tracks_treeUri` ON `tracks` (`treeUri`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '1cc69a76b65bdc1cd7020e1a8b753b2d')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `tracks`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsTracks = new HashMap<String, TableInfo.Column>(27);
        _columnsTracks.put("documentUri", new TableInfo.Column("documentUri", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTracks.put("treeUri", new TableInfo.Column("treeUri", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTracks.put("displayName", new TableInfo.Column("displayName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTracks.put("parentPath", new TableInfo.Column("parentPath", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTracks.put("format", new TableInfo.Column("format", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTracks.put("sizeBytes", new TableInfo.Column("sizeBytes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTracks.put("lastModified", new TableInfo.Column("lastModified", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTracks.put("durationMs", new TableInfo.Column("durationMs", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTracks.put("title", new TableInfo.Column("title", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTracks.put("artist", new TableInfo.Column("artist", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTracks.put("album", new TableInfo.Column("album", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTracks.put("albumArtist", new TableInfo.Column("albumArtist", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTracks.put("trackNumber", new TableInfo.Column("trackNumber", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTracks.put("discNumber", new TableInfo.Column("discNumber", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTracks.put("year", new TableInfo.Column("year", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTracks.put("genre", new TableInfo.Column("genre", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTracks.put("albumKey", new TableInfo.Column("albumKey", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTracks.put("albumLabel", new TableInfo.Column("albumLabel", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTracks.put("hasEmbeddedArt", new TableInfo.Column("hasEmbeddedArt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTracks.put("artWidth", new TableInfo.Column("artWidth", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTracks.put("artHeight", new TableInfo.Column("artHeight", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTracks.put("thumbnailPath", new TableInfo.Column("thumbnailPath", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTracks.put("hasSidecarLrc", new TableInfo.Column("hasSidecarLrc", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTracks.put("sidecarLrcSynced", new TableInfo.Column("sidecarLrcSynced", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTracks.put("coreTagsComplete", new TableInfo.Column("coreTagsComplete", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTracks.put("artistUnknown", new TableInfo.Column("artistUnknown", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTracks.put("scannedAt", new TableInfo.Column("scannedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTracks = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesTracks = new HashSet<TableInfo.Index>(3);
        _indicesTracks.add(new TableInfo.Index("index_tracks_documentUri", true, Arrays.asList("documentUri"), Arrays.asList("ASC")));
        _indicesTracks.add(new TableInfo.Index("index_tracks_albumKey", false, Arrays.asList("albumKey"), Arrays.asList("ASC")));
        _indicesTracks.add(new TableInfo.Index("index_tracks_treeUri", false, Arrays.asList("treeUri"), Arrays.asList("ASC")));
        final TableInfo _infoTracks = new TableInfo("tracks", _columnsTracks, _foreignKeysTracks, _indicesTracks);
        final TableInfo _existingTracks = TableInfo.read(db, "tracks");
        if (!_infoTracks.equals(_existingTracks)) {
          return new RoomOpenHelper.ValidationResult(false, "tracks(dev.gitfudge.musicworkbench.data.db.TrackEntity).\n"
                  + " Expected:\n" + _infoTracks + "\n"
                  + " Found:\n" + _existingTracks);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "1cc69a76b65bdc1cd7020e1a8b753b2d", "ebd8c8b99a8b2eb935a9669908ceb5af");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "tracks");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `tracks`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(TrackDao.class, TrackDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public TrackDao trackDao() {
    if (_trackDao != null) {
      return _trackDao;
    } else {
      synchronized(this) {
        if(_trackDao == null) {
          _trackDao = new TrackDao_Impl(this);
        }
        return _trackDao;
      }
    }
  }
}
