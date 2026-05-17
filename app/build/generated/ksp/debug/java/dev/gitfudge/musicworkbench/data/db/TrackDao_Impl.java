package dev.gitfudge.musicworkbench.data.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class TrackDao_Impl implements TrackDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<TrackEntity> __insertionAdapterOfTrackEntity;

  private final SharedSQLiteStatement __preparedStmtOfClearTree;

  public TrackDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTrackEntity = new EntityInsertionAdapter<TrackEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `tracks` (`documentUri`,`treeUri`,`displayName`,`parentPath`,`format`,`sizeBytes`,`lastModified`,`durationMs`,`title`,`artist`,`album`,`albumArtist`,`trackNumber`,`discNumber`,`year`,`genre`,`albumKey`,`albumLabel`,`hasEmbeddedArt`,`artWidth`,`artHeight`,`thumbnailPath`,`hasSidecarLrc`,`sidecarLrcSynced`,`coreTagsComplete`,`artistUnknown`,`scannedAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TrackEntity entity) {
        statement.bindString(1, entity.getDocumentUri());
        statement.bindString(2, entity.getTreeUri());
        statement.bindString(3, entity.getDisplayName());
        statement.bindString(4, entity.getParentPath());
        statement.bindString(5, entity.getFormat());
        statement.bindLong(6, entity.getSizeBytes());
        statement.bindLong(7, entity.getLastModified());
        if (entity.getDurationMs() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getDurationMs());
        }
        if (entity.getTitle() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getTitle());
        }
        if (entity.getArtist() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getArtist());
        }
        if (entity.getAlbum() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getAlbum());
        }
        if (entity.getAlbumArtist() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getAlbumArtist());
        }
        if (entity.getTrackNumber() == null) {
          statement.bindNull(13);
        } else {
          statement.bindLong(13, entity.getTrackNumber());
        }
        if (entity.getDiscNumber() == null) {
          statement.bindNull(14);
        } else {
          statement.bindLong(14, entity.getDiscNumber());
        }
        if (entity.getYear() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getYear());
        }
        if (entity.getGenre() == null) {
          statement.bindNull(16);
        } else {
          statement.bindString(16, entity.getGenre());
        }
        statement.bindString(17, entity.getAlbumKey());
        statement.bindString(18, entity.getAlbumLabel());
        final int _tmp = entity.getHasEmbeddedArt() ? 1 : 0;
        statement.bindLong(19, _tmp);
        if (entity.getArtWidth() == null) {
          statement.bindNull(20);
        } else {
          statement.bindLong(20, entity.getArtWidth());
        }
        if (entity.getArtHeight() == null) {
          statement.bindNull(21);
        } else {
          statement.bindLong(21, entity.getArtHeight());
        }
        if (entity.getThumbnailPath() == null) {
          statement.bindNull(22);
        } else {
          statement.bindString(22, entity.getThumbnailPath());
        }
        final int _tmp_1 = entity.getHasSidecarLrc() ? 1 : 0;
        statement.bindLong(23, _tmp_1);
        final int _tmp_2 = entity.getSidecarLrcSynced() ? 1 : 0;
        statement.bindLong(24, _tmp_2);
        final int _tmp_3 = entity.getCoreTagsComplete() ? 1 : 0;
        statement.bindLong(25, _tmp_3);
        final int _tmp_4 = entity.getArtistUnknown() ? 1 : 0;
        statement.bindLong(26, _tmp_4);
        statement.bindLong(27, entity.getScannedAt());
      }
    };
    this.__preparedStmtOfClearTree = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM tracks WHERE treeUri = ?";
        return _query;
      }
    };
  }

  @Override
  public Object upsertAll(final List<TrackEntity> tracks,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfTrackEntity.insert(tracks);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object clearTree(final String treeUri, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearTree.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, treeUri);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearTree.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<TrackEntity> observeTrack(final String uri) {
    final String _sql = "SELECT * FROM tracks WHERE documentUri = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, uri);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"tracks"}, new Callable<TrackEntity>() {
      @Override
      @Nullable
      public TrackEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDocumentUri = CursorUtil.getColumnIndexOrThrow(_cursor, "documentUri");
          final int _cursorIndexOfTreeUri = CursorUtil.getColumnIndexOrThrow(_cursor, "treeUri");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
          final int _cursorIndexOfParentPath = CursorUtil.getColumnIndexOrThrow(_cursor, "parentPath");
          final int _cursorIndexOfFormat = CursorUtil.getColumnIndexOrThrow(_cursor, "format");
          final int _cursorIndexOfSizeBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "sizeBytes");
          final int _cursorIndexOfLastModified = CursorUtil.getColumnIndexOrThrow(_cursor, "lastModified");
          final int _cursorIndexOfDurationMs = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMs");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfArtist = CursorUtil.getColumnIndexOrThrow(_cursor, "artist");
          final int _cursorIndexOfAlbum = CursorUtil.getColumnIndexOrThrow(_cursor, "album");
          final int _cursorIndexOfAlbumArtist = CursorUtil.getColumnIndexOrThrow(_cursor, "albumArtist");
          final int _cursorIndexOfTrackNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "trackNumber");
          final int _cursorIndexOfDiscNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "discNumber");
          final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
          final int _cursorIndexOfGenre = CursorUtil.getColumnIndexOrThrow(_cursor, "genre");
          final int _cursorIndexOfAlbumKey = CursorUtil.getColumnIndexOrThrow(_cursor, "albumKey");
          final int _cursorIndexOfAlbumLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "albumLabel");
          final int _cursorIndexOfHasEmbeddedArt = CursorUtil.getColumnIndexOrThrow(_cursor, "hasEmbeddedArt");
          final int _cursorIndexOfArtWidth = CursorUtil.getColumnIndexOrThrow(_cursor, "artWidth");
          final int _cursorIndexOfArtHeight = CursorUtil.getColumnIndexOrThrow(_cursor, "artHeight");
          final int _cursorIndexOfThumbnailPath = CursorUtil.getColumnIndexOrThrow(_cursor, "thumbnailPath");
          final int _cursorIndexOfHasSidecarLrc = CursorUtil.getColumnIndexOrThrow(_cursor, "hasSidecarLrc");
          final int _cursorIndexOfSidecarLrcSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "sidecarLrcSynced");
          final int _cursorIndexOfCoreTagsComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "coreTagsComplete");
          final int _cursorIndexOfArtistUnknown = CursorUtil.getColumnIndexOrThrow(_cursor, "artistUnknown");
          final int _cursorIndexOfScannedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "scannedAt");
          final TrackEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpDocumentUri;
            _tmpDocumentUri = _cursor.getString(_cursorIndexOfDocumentUri);
            final String _tmpTreeUri;
            _tmpTreeUri = _cursor.getString(_cursorIndexOfTreeUri);
            final String _tmpDisplayName;
            _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            final String _tmpParentPath;
            _tmpParentPath = _cursor.getString(_cursorIndexOfParentPath);
            final String _tmpFormat;
            _tmpFormat = _cursor.getString(_cursorIndexOfFormat);
            final long _tmpSizeBytes;
            _tmpSizeBytes = _cursor.getLong(_cursorIndexOfSizeBytes);
            final long _tmpLastModified;
            _tmpLastModified = _cursor.getLong(_cursorIndexOfLastModified);
            final Long _tmpDurationMs;
            if (_cursor.isNull(_cursorIndexOfDurationMs)) {
              _tmpDurationMs = null;
            } else {
              _tmpDurationMs = _cursor.getLong(_cursorIndexOfDurationMs);
            }
            final String _tmpTitle;
            if (_cursor.isNull(_cursorIndexOfTitle)) {
              _tmpTitle = null;
            } else {
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            }
            final String _tmpArtist;
            if (_cursor.isNull(_cursorIndexOfArtist)) {
              _tmpArtist = null;
            } else {
              _tmpArtist = _cursor.getString(_cursorIndexOfArtist);
            }
            final String _tmpAlbum;
            if (_cursor.isNull(_cursorIndexOfAlbum)) {
              _tmpAlbum = null;
            } else {
              _tmpAlbum = _cursor.getString(_cursorIndexOfAlbum);
            }
            final String _tmpAlbumArtist;
            if (_cursor.isNull(_cursorIndexOfAlbumArtist)) {
              _tmpAlbumArtist = null;
            } else {
              _tmpAlbumArtist = _cursor.getString(_cursorIndexOfAlbumArtist);
            }
            final Integer _tmpTrackNumber;
            if (_cursor.isNull(_cursorIndexOfTrackNumber)) {
              _tmpTrackNumber = null;
            } else {
              _tmpTrackNumber = _cursor.getInt(_cursorIndexOfTrackNumber);
            }
            final Integer _tmpDiscNumber;
            if (_cursor.isNull(_cursorIndexOfDiscNumber)) {
              _tmpDiscNumber = null;
            } else {
              _tmpDiscNumber = _cursor.getInt(_cursorIndexOfDiscNumber);
            }
            final String _tmpYear;
            if (_cursor.isNull(_cursorIndexOfYear)) {
              _tmpYear = null;
            } else {
              _tmpYear = _cursor.getString(_cursorIndexOfYear);
            }
            final String _tmpGenre;
            if (_cursor.isNull(_cursorIndexOfGenre)) {
              _tmpGenre = null;
            } else {
              _tmpGenre = _cursor.getString(_cursorIndexOfGenre);
            }
            final String _tmpAlbumKey;
            _tmpAlbumKey = _cursor.getString(_cursorIndexOfAlbumKey);
            final String _tmpAlbumLabel;
            _tmpAlbumLabel = _cursor.getString(_cursorIndexOfAlbumLabel);
            final boolean _tmpHasEmbeddedArt;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfHasEmbeddedArt);
            _tmpHasEmbeddedArt = _tmp != 0;
            final Integer _tmpArtWidth;
            if (_cursor.isNull(_cursorIndexOfArtWidth)) {
              _tmpArtWidth = null;
            } else {
              _tmpArtWidth = _cursor.getInt(_cursorIndexOfArtWidth);
            }
            final Integer _tmpArtHeight;
            if (_cursor.isNull(_cursorIndexOfArtHeight)) {
              _tmpArtHeight = null;
            } else {
              _tmpArtHeight = _cursor.getInt(_cursorIndexOfArtHeight);
            }
            final String _tmpThumbnailPath;
            if (_cursor.isNull(_cursorIndexOfThumbnailPath)) {
              _tmpThumbnailPath = null;
            } else {
              _tmpThumbnailPath = _cursor.getString(_cursorIndexOfThumbnailPath);
            }
            final boolean _tmpHasSidecarLrc;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfHasSidecarLrc);
            _tmpHasSidecarLrc = _tmp_1 != 0;
            final boolean _tmpSidecarLrcSynced;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfSidecarLrcSynced);
            _tmpSidecarLrcSynced = _tmp_2 != 0;
            final boolean _tmpCoreTagsComplete;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfCoreTagsComplete);
            _tmpCoreTagsComplete = _tmp_3 != 0;
            final boolean _tmpArtistUnknown;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfArtistUnknown);
            _tmpArtistUnknown = _tmp_4 != 0;
            final long _tmpScannedAt;
            _tmpScannedAt = _cursor.getLong(_cursorIndexOfScannedAt);
            _result = new TrackEntity(_tmpDocumentUri,_tmpTreeUri,_tmpDisplayName,_tmpParentPath,_tmpFormat,_tmpSizeBytes,_tmpLastModified,_tmpDurationMs,_tmpTitle,_tmpArtist,_tmpAlbum,_tmpAlbumArtist,_tmpTrackNumber,_tmpDiscNumber,_tmpYear,_tmpGenre,_tmpAlbumKey,_tmpAlbumLabel,_tmpHasEmbeddedArt,_tmpArtWidth,_tmpArtHeight,_tmpThumbnailPath,_tmpHasSidecarLrc,_tmpSidecarLrcSynced,_tmpCoreTagsComplete,_tmpArtistUnknown,_tmpScannedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Integer> observeCount(final String treeUri) {
    final String _sql = "SELECT COUNT(*) FROM tracks WHERE treeUri = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, treeUri);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"tracks"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object signatures(final String treeUri,
      final Continuation<? super List<TrackSignature>> $completion) {
    final String _sql = "SELECT documentUri, lastModified, sizeBytes FROM tracks WHERE treeUri = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, treeUri);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<TrackSignature>>() {
      @Override
      @NonNull
      public List<TrackSignature> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDocumentUri = 0;
          final int _cursorIndexOfLastModified = 1;
          final int _cursorIndexOfSizeBytes = 2;
          final List<TrackSignature> _result = new ArrayList<TrackSignature>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TrackSignature _item;
            final String _tmpDocumentUri;
            _tmpDocumentUri = _cursor.getString(_cursorIndexOfDocumentUri);
            final long _tmpLastModified;
            _tmpLastModified = _cursor.getLong(_cursorIndexOfLastModified);
            final long _tmpSizeBytes;
            _tmpSizeBytes = _cursor.getLong(_cursorIndexOfSizeBytes);
            _item = new TrackSignature(_tmpDocumentUri,_tmpLastModified,_tmpSizeBytes);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getTracksNeedingArt(final String treeUri, final int lowResThresholdPx,
      final Continuation<? super List<TrackEntity>> $completion) {
    final String _sql = "SELECT * FROM tracks WHERE treeUri = ?\n"
            + "           AND (hasEmbeddedArt = 0\n"
            + "                OR (artWidth IS NOT NULL AND artHeight IS NOT NULL\n"
            + "                    AND MAX(artWidth, artHeight) < ?))";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, treeUri);
    _argIndex = 2;
    _statement.bindLong(_argIndex, lowResThresholdPx);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<TrackEntity>>() {
      @Override
      @NonNull
      public List<TrackEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDocumentUri = CursorUtil.getColumnIndexOrThrow(_cursor, "documentUri");
          final int _cursorIndexOfTreeUri = CursorUtil.getColumnIndexOrThrow(_cursor, "treeUri");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
          final int _cursorIndexOfParentPath = CursorUtil.getColumnIndexOrThrow(_cursor, "parentPath");
          final int _cursorIndexOfFormat = CursorUtil.getColumnIndexOrThrow(_cursor, "format");
          final int _cursorIndexOfSizeBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "sizeBytes");
          final int _cursorIndexOfLastModified = CursorUtil.getColumnIndexOrThrow(_cursor, "lastModified");
          final int _cursorIndexOfDurationMs = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMs");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfArtist = CursorUtil.getColumnIndexOrThrow(_cursor, "artist");
          final int _cursorIndexOfAlbum = CursorUtil.getColumnIndexOrThrow(_cursor, "album");
          final int _cursorIndexOfAlbumArtist = CursorUtil.getColumnIndexOrThrow(_cursor, "albumArtist");
          final int _cursorIndexOfTrackNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "trackNumber");
          final int _cursorIndexOfDiscNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "discNumber");
          final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
          final int _cursorIndexOfGenre = CursorUtil.getColumnIndexOrThrow(_cursor, "genre");
          final int _cursorIndexOfAlbumKey = CursorUtil.getColumnIndexOrThrow(_cursor, "albumKey");
          final int _cursorIndexOfAlbumLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "albumLabel");
          final int _cursorIndexOfHasEmbeddedArt = CursorUtil.getColumnIndexOrThrow(_cursor, "hasEmbeddedArt");
          final int _cursorIndexOfArtWidth = CursorUtil.getColumnIndexOrThrow(_cursor, "artWidth");
          final int _cursorIndexOfArtHeight = CursorUtil.getColumnIndexOrThrow(_cursor, "artHeight");
          final int _cursorIndexOfThumbnailPath = CursorUtil.getColumnIndexOrThrow(_cursor, "thumbnailPath");
          final int _cursorIndexOfHasSidecarLrc = CursorUtil.getColumnIndexOrThrow(_cursor, "hasSidecarLrc");
          final int _cursorIndexOfSidecarLrcSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "sidecarLrcSynced");
          final int _cursorIndexOfCoreTagsComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "coreTagsComplete");
          final int _cursorIndexOfArtistUnknown = CursorUtil.getColumnIndexOrThrow(_cursor, "artistUnknown");
          final int _cursorIndexOfScannedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "scannedAt");
          final List<TrackEntity> _result = new ArrayList<TrackEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TrackEntity _item;
            final String _tmpDocumentUri;
            _tmpDocumentUri = _cursor.getString(_cursorIndexOfDocumentUri);
            final String _tmpTreeUri;
            _tmpTreeUri = _cursor.getString(_cursorIndexOfTreeUri);
            final String _tmpDisplayName;
            _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            final String _tmpParentPath;
            _tmpParentPath = _cursor.getString(_cursorIndexOfParentPath);
            final String _tmpFormat;
            _tmpFormat = _cursor.getString(_cursorIndexOfFormat);
            final long _tmpSizeBytes;
            _tmpSizeBytes = _cursor.getLong(_cursorIndexOfSizeBytes);
            final long _tmpLastModified;
            _tmpLastModified = _cursor.getLong(_cursorIndexOfLastModified);
            final Long _tmpDurationMs;
            if (_cursor.isNull(_cursorIndexOfDurationMs)) {
              _tmpDurationMs = null;
            } else {
              _tmpDurationMs = _cursor.getLong(_cursorIndexOfDurationMs);
            }
            final String _tmpTitle;
            if (_cursor.isNull(_cursorIndexOfTitle)) {
              _tmpTitle = null;
            } else {
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            }
            final String _tmpArtist;
            if (_cursor.isNull(_cursorIndexOfArtist)) {
              _tmpArtist = null;
            } else {
              _tmpArtist = _cursor.getString(_cursorIndexOfArtist);
            }
            final String _tmpAlbum;
            if (_cursor.isNull(_cursorIndexOfAlbum)) {
              _tmpAlbum = null;
            } else {
              _tmpAlbum = _cursor.getString(_cursorIndexOfAlbum);
            }
            final String _tmpAlbumArtist;
            if (_cursor.isNull(_cursorIndexOfAlbumArtist)) {
              _tmpAlbumArtist = null;
            } else {
              _tmpAlbumArtist = _cursor.getString(_cursorIndexOfAlbumArtist);
            }
            final Integer _tmpTrackNumber;
            if (_cursor.isNull(_cursorIndexOfTrackNumber)) {
              _tmpTrackNumber = null;
            } else {
              _tmpTrackNumber = _cursor.getInt(_cursorIndexOfTrackNumber);
            }
            final Integer _tmpDiscNumber;
            if (_cursor.isNull(_cursorIndexOfDiscNumber)) {
              _tmpDiscNumber = null;
            } else {
              _tmpDiscNumber = _cursor.getInt(_cursorIndexOfDiscNumber);
            }
            final String _tmpYear;
            if (_cursor.isNull(_cursorIndexOfYear)) {
              _tmpYear = null;
            } else {
              _tmpYear = _cursor.getString(_cursorIndexOfYear);
            }
            final String _tmpGenre;
            if (_cursor.isNull(_cursorIndexOfGenre)) {
              _tmpGenre = null;
            } else {
              _tmpGenre = _cursor.getString(_cursorIndexOfGenre);
            }
            final String _tmpAlbumKey;
            _tmpAlbumKey = _cursor.getString(_cursorIndexOfAlbumKey);
            final String _tmpAlbumLabel;
            _tmpAlbumLabel = _cursor.getString(_cursorIndexOfAlbumLabel);
            final boolean _tmpHasEmbeddedArt;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfHasEmbeddedArt);
            _tmpHasEmbeddedArt = _tmp != 0;
            final Integer _tmpArtWidth;
            if (_cursor.isNull(_cursorIndexOfArtWidth)) {
              _tmpArtWidth = null;
            } else {
              _tmpArtWidth = _cursor.getInt(_cursorIndexOfArtWidth);
            }
            final Integer _tmpArtHeight;
            if (_cursor.isNull(_cursorIndexOfArtHeight)) {
              _tmpArtHeight = null;
            } else {
              _tmpArtHeight = _cursor.getInt(_cursorIndexOfArtHeight);
            }
            final String _tmpThumbnailPath;
            if (_cursor.isNull(_cursorIndexOfThumbnailPath)) {
              _tmpThumbnailPath = null;
            } else {
              _tmpThumbnailPath = _cursor.getString(_cursorIndexOfThumbnailPath);
            }
            final boolean _tmpHasSidecarLrc;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfHasSidecarLrc);
            _tmpHasSidecarLrc = _tmp_1 != 0;
            final boolean _tmpSidecarLrcSynced;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfSidecarLrcSynced);
            _tmpSidecarLrcSynced = _tmp_2 != 0;
            final boolean _tmpCoreTagsComplete;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfCoreTagsComplete);
            _tmpCoreTagsComplete = _tmp_3 != 0;
            final boolean _tmpArtistUnknown;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfArtistUnknown);
            _tmpArtistUnknown = _tmp_4 != 0;
            final long _tmpScannedAt;
            _tmpScannedAt = _cursor.getLong(_cursorIndexOfScannedAt);
            _item = new TrackEntity(_tmpDocumentUri,_tmpTreeUri,_tmpDisplayName,_tmpParentPath,_tmpFormat,_tmpSizeBytes,_tmpLastModified,_tmpDurationMs,_tmpTitle,_tmpArtist,_tmpAlbum,_tmpAlbumArtist,_tmpTrackNumber,_tmpDiscNumber,_tmpYear,_tmpGenre,_tmpAlbumKey,_tmpAlbumLabel,_tmpHasEmbeddedArt,_tmpArtWidth,_tmpArtHeight,_tmpThumbnailPath,_tmpHasSidecarLrc,_tmpSidecarLrcSynced,_tmpCoreTagsComplete,_tmpArtistUnknown,_tmpScannedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<AlbumRow>> observeAlbumRows(final String treeUri, final int lowResThresholdPx) {
    final String _sql = "SELECT\n"
            + "              albumKey                                            AS albumKey,\n"
            + "              MIN(albumLabel)                                     AS albumLabel,\n"
            + "              COUNT(*)                                            AS trackCount,\n"
            + "              COUNT(DISTINCT COALESCE(NULLIF(albumArtist, ''), NULLIF(artist, ''), '')) AS distinctArtistCount,\n"
            + "              MIN(COALESCE(NULLIF(albumArtist, ''), NULLIF(artist, ''), 'Unknown artist')) AS firstArtist,\n"
            + "              MIN(year)                                           AS year,\n"
            + "              MAX(thumbnailPath)                                  AS coverThumbnailPath,\n"
            + "\n"
            + "              SUM(CASE WHEN hasEmbeddedArt = 1 THEN 1 ELSE 0 END) AS withArt,\n"
            + "              SUM(CASE WHEN hasEmbeddedArt = 1\n"
            + "                        AND artWidth IS NOT NULL AND artHeight IS NOT NULL\n"
            + "                        AND MAX(artWidth, artHeight) < ?\n"
            + "                    THEN 1 ELSE 0 END)                            AS lowResArt,\n"
            + "\n"
            + "              SUM(CASE WHEN hasSidecarLrc = 1 THEN 1 ELSE 0 END)  AS withLyrics,\n"
            + "              SUM(CASE WHEN hasSidecarLrc = 1 AND sidecarLrcSynced = 1 THEN 1 ELSE 0 END) AS syncedLyrics,\n"
            + "\n"
            + "              SUM(CASE WHEN coreTagsComplete = 1 AND artistUnknown = 0 THEN 1 ELSE 0 END) AS okTags\n"
            + "          FROM tracks\n"
            + "          WHERE treeUri = ?\n"
            + "            AND album IS NOT NULL AND album <> ''\n"
            + "          GROUP BY albumKey\n"
            + "          ORDER BY MIN(albumLabel) COLLATE NOCASE ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, lowResThresholdPx);
    _argIndex = 2;
    _statement.bindString(_argIndex, treeUri);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"tracks"}, new Callable<List<AlbumRow>>() {
      @Override
      @NonNull
      public List<AlbumRow> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfAlbumKey = 0;
          final int _cursorIndexOfAlbumLabel = 1;
          final int _cursorIndexOfTrackCount = 2;
          final int _cursorIndexOfDistinctArtistCount = 3;
          final int _cursorIndexOfFirstArtist = 4;
          final int _cursorIndexOfYear = 5;
          final int _cursorIndexOfCoverThumbnailPath = 6;
          final int _cursorIndexOfWithArt = 7;
          final int _cursorIndexOfLowResArt = 8;
          final int _cursorIndexOfWithLyrics = 9;
          final int _cursorIndexOfSyncedLyrics = 10;
          final int _cursorIndexOfOkTags = 11;
          final List<AlbumRow> _result = new ArrayList<AlbumRow>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AlbumRow _item;
            final String _tmpAlbumKey;
            _tmpAlbumKey = _cursor.getString(_cursorIndexOfAlbumKey);
            final String _tmpAlbumLabel;
            _tmpAlbumLabel = _cursor.getString(_cursorIndexOfAlbumLabel);
            final int _tmpTrackCount;
            _tmpTrackCount = _cursor.getInt(_cursorIndexOfTrackCount);
            final int _tmpDistinctArtistCount;
            _tmpDistinctArtistCount = _cursor.getInt(_cursorIndexOfDistinctArtistCount);
            final String _tmpFirstArtist;
            _tmpFirstArtist = _cursor.getString(_cursorIndexOfFirstArtist);
            final String _tmpYear;
            if (_cursor.isNull(_cursorIndexOfYear)) {
              _tmpYear = null;
            } else {
              _tmpYear = _cursor.getString(_cursorIndexOfYear);
            }
            final String _tmpCoverThumbnailPath;
            if (_cursor.isNull(_cursorIndexOfCoverThumbnailPath)) {
              _tmpCoverThumbnailPath = null;
            } else {
              _tmpCoverThumbnailPath = _cursor.getString(_cursorIndexOfCoverThumbnailPath);
            }
            final int _tmpWithArt;
            _tmpWithArt = _cursor.getInt(_cursorIndexOfWithArt);
            final int _tmpLowResArt;
            _tmpLowResArt = _cursor.getInt(_cursorIndexOfLowResArt);
            final int _tmpWithLyrics;
            _tmpWithLyrics = _cursor.getInt(_cursorIndexOfWithLyrics);
            final int _tmpSyncedLyrics;
            _tmpSyncedLyrics = _cursor.getInt(_cursorIndexOfSyncedLyrics);
            final int _tmpOkTags;
            _tmpOkTags = _cursor.getInt(_cursorIndexOfOkTags);
            _item = new AlbumRow(_tmpAlbumKey,_tmpAlbumLabel,_tmpTrackCount,_tmpDistinctArtistCount,_tmpFirstArtist,_tmpYear,_tmpCoverThumbnailPath,_tmpWithArt,_tmpLowResArt,_tmpWithLyrics,_tmpSyncedLyrics,_tmpOkTags);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<TrackEntity>> observeTracksInAlbum(final String treeUri, final String albumKey) {
    final String _sql = "SELECT * FROM tracks\n"
            + "           WHERE treeUri = ? AND albumKey = ?\n"
            + "           ORDER BY discNumber ASC, trackNumber ASC, displayName ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, treeUri);
    _argIndex = 2;
    _statement.bindString(_argIndex, albumKey);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"tracks"}, new Callable<List<TrackEntity>>() {
      @Override
      @NonNull
      public List<TrackEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDocumentUri = CursorUtil.getColumnIndexOrThrow(_cursor, "documentUri");
          final int _cursorIndexOfTreeUri = CursorUtil.getColumnIndexOrThrow(_cursor, "treeUri");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
          final int _cursorIndexOfParentPath = CursorUtil.getColumnIndexOrThrow(_cursor, "parentPath");
          final int _cursorIndexOfFormat = CursorUtil.getColumnIndexOrThrow(_cursor, "format");
          final int _cursorIndexOfSizeBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "sizeBytes");
          final int _cursorIndexOfLastModified = CursorUtil.getColumnIndexOrThrow(_cursor, "lastModified");
          final int _cursorIndexOfDurationMs = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMs");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfArtist = CursorUtil.getColumnIndexOrThrow(_cursor, "artist");
          final int _cursorIndexOfAlbum = CursorUtil.getColumnIndexOrThrow(_cursor, "album");
          final int _cursorIndexOfAlbumArtist = CursorUtil.getColumnIndexOrThrow(_cursor, "albumArtist");
          final int _cursorIndexOfTrackNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "trackNumber");
          final int _cursorIndexOfDiscNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "discNumber");
          final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
          final int _cursorIndexOfGenre = CursorUtil.getColumnIndexOrThrow(_cursor, "genre");
          final int _cursorIndexOfAlbumKey = CursorUtil.getColumnIndexOrThrow(_cursor, "albumKey");
          final int _cursorIndexOfAlbumLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "albumLabel");
          final int _cursorIndexOfHasEmbeddedArt = CursorUtil.getColumnIndexOrThrow(_cursor, "hasEmbeddedArt");
          final int _cursorIndexOfArtWidth = CursorUtil.getColumnIndexOrThrow(_cursor, "artWidth");
          final int _cursorIndexOfArtHeight = CursorUtil.getColumnIndexOrThrow(_cursor, "artHeight");
          final int _cursorIndexOfThumbnailPath = CursorUtil.getColumnIndexOrThrow(_cursor, "thumbnailPath");
          final int _cursorIndexOfHasSidecarLrc = CursorUtil.getColumnIndexOrThrow(_cursor, "hasSidecarLrc");
          final int _cursorIndexOfSidecarLrcSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "sidecarLrcSynced");
          final int _cursorIndexOfCoreTagsComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "coreTagsComplete");
          final int _cursorIndexOfArtistUnknown = CursorUtil.getColumnIndexOrThrow(_cursor, "artistUnknown");
          final int _cursorIndexOfScannedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "scannedAt");
          final List<TrackEntity> _result = new ArrayList<TrackEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TrackEntity _item;
            final String _tmpDocumentUri;
            _tmpDocumentUri = _cursor.getString(_cursorIndexOfDocumentUri);
            final String _tmpTreeUri;
            _tmpTreeUri = _cursor.getString(_cursorIndexOfTreeUri);
            final String _tmpDisplayName;
            _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            final String _tmpParentPath;
            _tmpParentPath = _cursor.getString(_cursorIndexOfParentPath);
            final String _tmpFormat;
            _tmpFormat = _cursor.getString(_cursorIndexOfFormat);
            final long _tmpSizeBytes;
            _tmpSizeBytes = _cursor.getLong(_cursorIndexOfSizeBytes);
            final long _tmpLastModified;
            _tmpLastModified = _cursor.getLong(_cursorIndexOfLastModified);
            final Long _tmpDurationMs;
            if (_cursor.isNull(_cursorIndexOfDurationMs)) {
              _tmpDurationMs = null;
            } else {
              _tmpDurationMs = _cursor.getLong(_cursorIndexOfDurationMs);
            }
            final String _tmpTitle;
            if (_cursor.isNull(_cursorIndexOfTitle)) {
              _tmpTitle = null;
            } else {
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            }
            final String _tmpArtist;
            if (_cursor.isNull(_cursorIndexOfArtist)) {
              _tmpArtist = null;
            } else {
              _tmpArtist = _cursor.getString(_cursorIndexOfArtist);
            }
            final String _tmpAlbum;
            if (_cursor.isNull(_cursorIndexOfAlbum)) {
              _tmpAlbum = null;
            } else {
              _tmpAlbum = _cursor.getString(_cursorIndexOfAlbum);
            }
            final String _tmpAlbumArtist;
            if (_cursor.isNull(_cursorIndexOfAlbumArtist)) {
              _tmpAlbumArtist = null;
            } else {
              _tmpAlbumArtist = _cursor.getString(_cursorIndexOfAlbumArtist);
            }
            final Integer _tmpTrackNumber;
            if (_cursor.isNull(_cursorIndexOfTrackNumber)) {
              _tmpTrackNumber = null;
            } else {
              _tmpTrackNumber = _cursor.getInt(_cursorIndexOfTrackNumber);
            }
            final Integer _tmpDiscNumber;
            if (_cursor.isNull(_cursorIndexOfDiscNumber)) {
              _tmpDiscNumber = null;
            } else {
              _tmpDiscNumber = _cursor.getInt(_cursorIndexOfDiscNumber);
            }
            final String _tmpYear;
            if (_cursor.isNull(_cursorIndexOfYear)) {
              _tmpYear = null;
            } else {
              _tmpYear = _cursor.getString(_cursorIndexOfYear);
            }
            final String _tmpGenre;
            if (_cursor.isNull(_cursorIndexOfGenre)) {
              _tmpGenre = null;
            } else {
              _tmpGenre = _cursor.getString(_cursorIndexOfGenre);
            }
            final String _tmpAlbumKey;
            _tmpAlbumKey = _cursor.getString(_cursorIndexOfAlbumKey);
            final String _tmpAlbumLabel;
            _tmpAlbumLabel = _cursor.getString(_cursorIndexOfAlbumLabel);
            final boolean _tmpHasEmbeddedArt;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfHasEmbeddedArt);
            _tmpHasEmbeddedArt = _tmp != 0;
            final Integer _tmpArtWidth;
            if (_cursor.isNull(_cursorIndexOfArtWidth)) {
              _tmpArtWidth = null;
            } else {
              _tmpArtWidth = _cursor.getInt(_cursorIndexOfArtWidth);
            }
            final Integer _tmpArtHeight;
            if (_cursor.isNull(_cursorIndexOfArtHeight)) {
              _tmpArtHeight = null;
            } else {
              _tmpArtHeight = _cursor.getInt(_cursorIndexOfArtHeight);
            }
            final String _tmpThumbnailPath;
            if (_cursor.isNull(_cursorIndexOfThumbnailPath)) {
              _tmpThumbnailPath = null;
            } else {
              _tmpThumbnailPath = _cursor.getString(_cursorIndexOfThumbnailPath);
            }
            final boolean _tmpHasSidecarLrc;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfHasSidecarLrc);
            _tmpHasSidecarLrc = _tmp_1 != 0;
            final boolean _tmpSidecarLrcSynced;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfSidecarLrcSynced);
            _tmpSidecarLrcSynced = _tmp_2 != 0;
            final boolean _tmpCoreTagsComplete;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfCoreTagsComplete);
            _tmpCoreTagsComplete = _tmp_3 != 0;
            final boolean _tmpArtistUnknown;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfArtistUnknown);
            _tmpArtistUnknown = _tmp_4 != 0;
            final long _tmpScannedAt;
            _tmpScannedAt = _cursor.getLong(_cursorIndexOfScannedAt);
            _item = new TrackEntity(_tmpDocumentUri,_tmpTreeUri,_tmpDisplayName,_tmpParentPath,_tmpFormat,_tmpSizeBytes,_tmpLastModified,_tmpDurationMs,_tmpTitle,_tmpArtist,_tmpAlbum,_tmpAlbumArtist,_tmpTrackNumber,_tmpDiscNumber,_tmpYear,_tmpGenre,_tmpAlbumKey,_tmpAlbumLabel,_tmpHasEmbeddedArt,_tmpArtWidth,_tmpArtHeight,_tmpThumbnailPath,_tmpHasSidecarLrc,_tmpSidecarLrcSynced,_tmpCoreTagsComplete,_tmpArtistUnknown,_tmpScannedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<TrackEntity>> observeUnfiledTracks(final String treeUri) {
    final String _sql = "SELECT * FROM tracks\n"
            + "           WHERE treeUri = ? AND (album IS NULL OR album = '')\n"
            + "           ORDER BY COALESCE(artist, '') COLLATE NOCASE ASC,\n"
            + "                    COALESCE(title, displayName) COLLATE NOCASE ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, treeUri);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"tracks"}, new Callable<List<TrackEntity>>() {
      @Override
      @NonNull
      public List<TrackEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDocumentUri = CursorUtil.getColumnIndexOrThrow(_cursor, "documentUri");
          final int _cursorIndexOfTreeUri = CursorUtil.getColumnIndexOrThrow(_cursor, "treeUri");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
          final int _cursorIndexOfParentPath = CursorUtil.getColumnIndexOrThrow(_cursor, "parentPath");
          final int _cursorIndexOfFormat = CursorUtil.getColumnIndexOrThrow(_cursor, "format");
          final int _cursorIndexOfSizeBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "sizeBytes");
          final int _cursorIndexOfLastModified = CursorUtil.getColumnIndexOrThrow(_cursor, "lastModified");
          final int _cursorIndexOfDurationMs = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMs");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfArtist = CursorUtil.getColumnIndexOrThrow(_cursor, "artist");
          final int _cursorIndexOfAlbum = CursorUtil.getColumnIndexOrThrow(_cursor, "album");
          final int _cursorIndexOfAlbumArtist = CursorUtil.getColumnIndexOrThrow(_cursor, "albumArtist");
          final int _cursorIndexOfTrackNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "trackNumber");
          final int _cursorIndexOfDiscNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "discNumber");
          final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
          final int _cursorIndexOfGenre = CursorUtil.getColumnIndexOrThrow(_cursor, "genre");
          final int _cursorIndexOfAlbumKey = CursorUtil.getColumnIndexOrThrow(_cursor, "albumKey");
          final int _cursorIndexOfAlbumLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "albumLabel");
          final int _cursorIndexOfHasEmbeddedArt = CursorUtil.getColumnIndexOrThrow(_cursor, "hasEmbeddedArt");
          final int _cursorIndexOfArtWidth = CursorUtil.getColumnIndexOrThrow(_cursor, "artWidth");
          final int _cursorIndexOfArtHeight = CursorUtil.getColumnIndexOrThrow(_cursor, "artHeight");
          final int _cursorIndexOfThumbnailPath = CursorUtil.getColumnIndexOrThrow(_cursor, "thumbnailPath");
          final int _cursorIndexOfHasSidecarLrc = CursorUtil.getColumnIndexOrThrow(_cursor, "hasSidecarLrc");
          final int _cursorIndexOfSidecarLrcSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "sidecarLrcSynced");
          final int _cursorIndexOfCoreTagsComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "coreTagsComplete");
          final int _cursorIndexOfArtistUnknown = CursorUtil.getColumnIndexOrThrow(_cursor, "artistUnknown");
          final int _cursorIndexOfScannedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "scannedAt");
          final List<TrackEntity> _result = new ArrayList<TrackEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TrackEntity _item;
            final String _tmpDocumentUri;
            _tmpDocumentUri = _cursor.getString(_cursorIndexOfDocumentUri);
            final String _tmpTreeUri;
            _tmpTreeUri = _cursor.getString(_cursorIndexOfTreeUri);
            final String _tmpDisplayName;
            _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            final String _tmpParentPath;
            _tmpParentPath = _cursor.getString(_cursorIndexOfParentPath);
            final String _tmpFormat;
            _tmpFormat = _cursor.getString(_cursorIndexOfFormat);
            final long _tmpSizeBytes;
            _tmpSizeBytes = _cursor.getLong(_cursorIndexOfSizeBytes);
            final long _tmpLastModified;
            _tmpLastModified = _cursor.getLong(_cursorIndexOfLastModified);
            final Long _tmpDurationMs;
            if (_cursor.isNull(_cursorIndexOfDurationMs)) {
              _tmpDurationMs = null;
            } else {
              _tmpDurationMs = _cursor.getLong(_cursorIndexOfDurationMs);
            }
            final String _tmpTitle;
            if (_cursor.isNull(_cursorIndexOfTitle)) {
              _tmpTitle = null;
            } else {
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            }
            final String _tmpArtist;
            if (_cursor.isNull(_cursorIndexOfArtist)) {
              _tmpArtist = null;
            } else {
              _tmpArtist = _cursor.getString(_cursorIndexOfArtist);
            }
            final String _tmpAlbum;
            if (_cursor.isNull(_cursorIndexOfAlbum)) {
              _tmpAlbum = null;
            } else {
              _tmpAlbum = _cursor.getString(_cursorIndexOfAlbum);
            }
            final String _tmpAlbumArtist;
            if (_cursor.isNull(_cursorIndexOfAlbumArtist)) {
              _tmpAlbumArtist = null;
            } else {
              _tmpAlbumArtist = _cursor.getString(_cursorIndexOfAlbumArtist);
            }
            final Integer _tmpTrackNumber;
            if (_cursor.isNull(_cursorIndexOfTrackNumber)) {
              _tmpTrackNumber = null;
            } else {
              _tmpTrackNumber = _cursor.getInt(_cursorIndexOfTrackNumber);
            }
            final Integer _tmpDiscNumber;
            if (_cursor.isNull(_cursorIndexOfDiscNumber)) {
              _tmpDiscNumber = null;
            } else {
              _tmpDiscNumber = _cursor.getInt(_cursorIndexOfDiscNumber);
            }
            final String _tmpYear;
            if (_cursor.isNull(_cursorIndexOfYear)) {
              _tmpYear = null;
            } else {
              _tmpYear = _cursor.getString(_cursorIndexOfYear);
            }
            final String _tmpGenre;
            if (_cursor.isNull(_cursorIndexOfGenre)) {
              _tmpGenre = null;
            } else {
              _tmpGenre = _cursor.getString(_cursorIndexOfGenre);
            }
            final String _tmpAlbumKey;
            _tmpAlbumKey = _cursor.getString(_cursorIndexOfAlbumKey);
            final String _tmpAlbumLabel;
            _tmpAlbumLabel = _cursor.getString(_cursorIndexOfAlbumLabel);
            final boolean _tmpHasEmbeddedArt;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfHasEmbeddedArt);
            _tmpHasEmbeddedArt = _tmp != 0;
            final Integer _tmpArtWidth;
            if (_cursor.isNull(_cursorIndexOfArtWidth)) {
              _tmpArtWidth = null;
            } else {
              _tmpArtWidth = _cursor.getInt(_cursorIndexOfArtWidth);
            }
            final Integer _tmpArtHeight;
            if (_cursor.isNull(_cursorIndexOfArtHeight)) {
              _tmpArtHeight = null;
            } else {
              _tmpArtHeight = _cursor.getInt(_cursorIndexOfArtHeight);
            }
            final String _tmpThumbnailPath;
            if (_cursor.isNull(_cursorIndexOfThumbnailPath)) {
              _tmpThumbnailPath = null;
            } else {
              _tmpThumbnailPath = _cursor.getString(_cursorIndexOfThumbnailPath);
            }
            final boolean _tmpHasSidecarLrc;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfHasSidecarLrc);
            _tmpHasSidecarLrc = _tmp_1 != 0;
            final boolean _tmpSidecarLrcSynced;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfSidecarLrcSynced);
            _tmpSidecarLrcSynced = _tmp_2 != 0;
            final boolean _tmpCoreTagsComplete;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfCoreTagsComplete);
            _tmpCoreTagsComplete = _tmp_3 != 0;
            final boolean _tmpArtistUnknown;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfArtistUnknown);
            _tmpArtistUnknown = _tmp_4 != 0;
            final long _tmpScannedAt;
            _tmpScannedAt = _cursor.getLong(_cursorIndexOfScannedAt);
            _item = new TrackEntity(_tmpDocumentUri,_tmpTreeUri,_tmpDisplayName,_tmpParentPath,_tmpFormat,_tmpSizeBytes,_tmpLastModified,_tmpDurationMs,_tmpTitle,_tmpArtist,_tmpAlbum,_tmpAlbumArtist,_tmpTrackNumber,_tmpDiscNumber,_tmpYear,_tmpGenre,_tmpAlbumKey,_tmpAlbumLabel,_tmpHasEmbeddedArt,_tmpArtWidth,_tmpArtHeight,_tmpThumbnailPath,_tmpHasSidecarLrc,_tmpSidecarLrcSynced,_tmpCoreTagsComplete,_tmpArtistUnknown,_tmpScannedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Integer> observeUnfiledCount(final String treeUri) {
    final String _sql = "SELECT COUNT(*) FROM tracks\n"
            + "           WHERE treeUri = ? AND (album IS NULL OR album = '')";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, treeUri);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"tracks"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object deleteByUris(final List<String> uris,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("DELETE FROM tracks WHERE documentUri IN (");
        final int _inputSize = uris.size();
        StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
        _stringBuilder.append(")");
        final String _sql = _stringBuilder.toString();
        final SupportSQLiteStatement _stmt = __db.compileStatement(_sql);
        int _argIndex = 1;
        for (String _item : uris) {
          _stmt.bindString(_argIndex, _item);
          _argIndex++;
        }
        __db.beginTransaction();
        try {
          _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<TrackEntity>> observeFiltered(final SupportSQLiteQuery query) {
    return CoroutinesRoom.createFlow(__db, false, new String[] {"tracks"}, new Callable<List<TrackEntity>>() {
      @Override
      @NonNull
      public List<TrackEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, query, false, null);
        try {
          final List<TrackEntity> _result = new ArrayList<TrackEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TrackEntity _item;
            _item = __entityCursorConverter_devGitfudgeMusicworkbenchDataDbTrackEntity(_cursor);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }

  private TrackEntity __entityCursorConverter_devGitfudgeMusicworkbenchDataDbTrackEntity(
      @NonNull final Cursor cursor) {
    final TrackEntity _entity;
    final int _cursorIndexOfDocumentUri = CursorUtil.getColumnIndex(cursor, "documentUri");
    final int _cursorIndexOfTreeUri = CursorUtil.getColumnIndex(cursor, "treeUri");
    final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndex(cursor, "displayName");
    final int _cursorIndexOfParentPath = CursorUtil.getColumnIndex(cursor, "parentPath");
    final int _cursorIndexOfFormat = CursorUtil.getColumnIndex(cursor, "format");
    final int _cursorIndexOfSizeBytes = CursorUtil.getColumnIndex(cursor, "sizeBytes");
    final int _cursorIndexOfLastModified = CursorUtil.getColumnIndex(cursor, "lastModified");
    final int _cursorIndexOfDurationMs = CursorUtil.getColumnIndex(cursor, "durationMs");
    final int _cursorIndexOfTitle = CursorUtil.getColumnIndex(cursor, "title");
    final int _cursorIndexOfArtist = CursorUtil.getColumnIndex(cursor, "artist");
    final int _cursorIndexOfAlbum = CursorUtil.getColumnIndex(cursor, "album");
    final int _cursorIndexOfAlbumArtist = CursorUtil.getColumnIndex(cursor, "albumArtist");
    final int _cursorIndexOfTrackNumber = CursorUtil.getColumnIndex(cursor, "trackNumber");
    final int _cursorIndexOfDiscNumber = CursorUtil.getColumnIndex(cursor, "discNumber");
    final int _cursorIndexOfYear = CursorUtil.getColumnIndex(cursor, "year");
    final int _cursorIndexOfGenre = CursorUtil.getColumnIndex(cursor, "genre");
    final int _cursorIndexOfAlbumKey = CursorUtil.getColumnIndex(cursor, "albumKey");
    final int _cursorIndexOfAlbumLabel = CursorUtil.getColumnIndex(cursor, "albumLabel");
    final int _cursorIndexOfHasEmbeddedArt = CursorUtil.getColumnIndex(cursor, "hasEmbeddedArt");
    final int _cursorIndexOfArtWidth = CursorUtil.getColumnIndex(cursor, "artWidth");
    final int _cursorIndexOfArtHeight = CursorUtil.getColumnIndex(cursor, "artHeight");
    final int _cursorIndexOfThumbnailPath = CursorUtil.getColumnIndex(cursor, "thumbnailPath");
    final int _cursorIndexOfHasSidecarLrc = CursorUtil.getColumnIndex(cursor, "hasSidecarLrc");
    final int _cursorIndexOfSidecarLrcSynced = CursorUtil.getColumnIndex(cursor, "sidecarLrcSynced");
    final int _cursorIndexOfCoreTagsComplete = CursorUtil.getColumnIndex(cursor, "coreTagsComplete");
    final int _cursorIndexOfArtistUnknown = CursorUtil.getColumnIndex(cursor, "artistUnknown");
    final int _cursorIndexOfScannedAt = CursorUtil.getColumnIndex(cursor, "scannedAt");
    final String _tmpDocumentUri;
    if (_cursorIndexOfDocumentUri == -1) {
      _tmpDocumentUri = null;
    } else {
      _tmpDocumentUri = cursor.getString(_cursorIndexOfDocumentUri);
    }
    final String _tmpTreeUri;
    if (_cursorIndexOfTreeUri == -1) {
      _tmpTreeUri = null;
    } else {
      _tmpTreeUri = cursor.getString(_cursorIndexOfTreeUri);
    }
    final String _tmpDisplayName;
    if (_cursorIndexOfDisplayName == -1) {
      _tmpDisplayName = null;
    } else {
      _tmpDisplayName = cursor.getString(_cursorIndexOfDisplayName);
    }
    final String _tmpParentPath;
    if (_cursorIndexOfParentPath == -1) {
      _tmpParentPath = null;
    } else {
      _tmpParentPath = cursor.getString(_cursorIndexOfParentPath);
    }
    final String _tmpFormat;
    if (_cursorIndexOfFormat == -1) {
      _tmpFormat = null;
    } else {
      _tmpFormat = cursor.getString(_cursorIndexOfFormat);
    }
    final long _tmpSizeBytes;
    if (_cursorIndexOfSizeBytes == -1) {
      _tmpSizeBytes = 0;
    } else {
      _tmpSizeBytes = cursor.getLong(_cursorIndexOfSizeBytes);
    }
    final long _tmpLastModified;
    if (_cursorIndexOfLastModified == -1) {
      _tmpLastModified = 0;
    } else {
      _tmpLastModified = cursor.getLong(_cursorIndexOfLastModified);
    }
    final Long _tmpDurationMs;
    if (_cursorIndexOfDurationMs == -1) {
      _tmpDurationMs = null;
    } else {
      if (cursor.isNull(_cursorIndexOfDurationMs)) {
        _tmpDurationMs = null;
      } else {
        _tmpDurationMs = cursor.getLong(_cursorIndexOfDurationMs);
      }
    }
    final String _tmpTitle;
    if (_cursorIndexOfTitle == -1) {
      _tmpTitle = null;
    } else {
      if (cursor.isNull(_cursorIndexOfTitle)) {
        _tmpTitle = null;
      } else {
        _tmpTitle = cursor.getString(_cursorIndexOfTitle);
      }
    }
    final String _tmpArtist;
    if (_cursorIndexOfArtist == -1) {
      _tmpArtist = null;
    } else {
      if (cursor.isNull(_cursorIndexOfArtist)) {
        _tmpArtist = null;
      } else {
        _tmpArtist = cursor.getString(_cursorIndexOfArtist);
      }
    }
    final String _tmpAlbum;
    if (_cursorIndexOfAlbum == -1) {
      _tmpAlbum = null;
    } else {
      if (cursor.isNull(_cursorIndexOfAlbum)) {
        _tmpAlbum = null;
      } else {
        _tmpAlbum = cursor.getString(_cursorIndexOfAlbum);
      }
    }
    final String _tmpAlbumArtist;
    if (_cursorIndexOfAlbumArtist == -1) {
      _tmpAlbumArtist = null;
    } else {
      if (cursor.isNull(_cursorIndexOfAlbumArtist)) {
        _tmpAlbumArtist = null;
      } else {
        _tmpAlbumArtist = cursor.getString(_cursorIndexOfAlbumArtist);
      }
    }
    final Integer _tmpTrackNumber;
    if (_cursorIndexOfTrackNumber == -1) {
      _tmpTrackNumber = null;
    } else {
      if (cursor.isNull(_cursorIndexOfTrackNumber)) {
        _tmpTrackNumber = null;
      } else {
        _tmpTrackNumber = cursor.getInt(_cursorIndexOfTrackNumber);
      }
    }
    final Integer _tmpDiscNumber;
    if (_cursorIndexOfDiscNumber == -1) {
      _tmpDiscNumber = null;
    } else {
      if (cursor.isNull(_cursorIndexOfDiscNumber)) {
        _tmpDiscNumber = null;
      } else {
        _tmpDiscNumber = cursor.getInt(_cursorIndexOfDiscNumber);
      }
    }
    final String _tmpYear;
    if (_cursorIndexOfYear == -1) {
      _tmpYear = null;
    } else {
      if (cursor.isNull(_cursorIndexOfYear)) {
        _tmpYear = null;
      } else {
        _tmpYear = cursor.getString(_cursorIndexOfYear);
      }
    }
    final String _tmpGenre;
    if (_cursorIndexOfGenre == -1) {
      _tmpGenre = null;
    } else {
      if (cursor.isNull(_cursorIndexOfGenre)) {
        _tmpGenre = null;
      } else {
        _tmpGenre = cursor.getString(_cursorIndexOfGenre);
      }
    }
    final String _tmpAlbumKey;
    if (_cursorIndexOfAlbumKey == -1) {
      _tmpAlbumKey = null;
    } else {
      _tmpAlbumKey = cursor.getString(_cursorIndexOfAlbumKey);
    }
    final String _tmpAlbumLabel;
    if (_cursorIndexOfAlbumLabel == -1) {
      _tmpAlbumLabel = null;
    } else {
      _tmpAlbumLabel = cursor.getString(_cursorIndexOfAlbumLabel);
    }
    final boolean _tmpHasEmbeddedArt;
    if (_cursorIndexOfHasEmbeddedArt == -1) {
      _tmpHasEmbeddedArt = false;
    } else {
      final int _tmp;
      _tmp = cursor.getInt(_cursorIndexOfHasEmbeddedArt);
      _tmpHasEmbeddedArt = _tmp != 0;
    }
    final Integer _tmpArtWidth;
    if (_cursorIndexOfArtWidth == -1) {
      _tmpArtWidth = null;
    } else {
      if (cursor.isNull(_cursorIndexOfArtWidth)) {
        _tmpArtWidth = null;
      } else {
        _tmpArtWidth = cursor.getInt(_cursorIndexOfArtWidth);
      }
    }
    final Integer _tmpArtHeight;
    if (_cursorIndexOfArtHeight == -1) {
      _tmpArtHeight = null;
    } else {
      if (cursor.isNull(_cursorIndexOfArtHeight)) {
        _tmpArtHeight = null;
      } else {
        _tmpArtHeight = cursor.getInt(_cursorIndexOfArtHeight);
      }
    }
    final String _tmpThumbnailPath;
    if (_cursorIndexOfThumbnailPath == -1) {
      _tmpThumbnailPath = null;
    } else {
      if (cursor.isNull(_cursorIndexOfThumbnailPath)) {
        _tmpThumbnailPath = null;
      } else {
        _tmpThumbnailPath = cursor.getString(_cursorIndexOfThumbnailPath);
      }
    }
    final boolean _tmpHasSidecarLrc;
    if (_cursorIndexOfHasSidecarLrc == -1) {
      _tmpHasSidecarLrc = false;
    } else {
      final int _tmp_1;
      _tmp_1 = cursor.getInt(_cursorIndexOfHasSidecarLrc);
      _tmpHasSidecarLrc = _tmp_1 != 0;
    }
    final boolean _tmpSidecarLrcSynced;
    if (_cursorIndexOfSidecarLrcSynced == -1) {
      _tmpSidecarLrcSynced = false;
    } else {
      final int _tmp_2;
      _tmp_2 = cursor.getInt(_cursorIndexOfSidecarLrcSynced);
      _tmpSidecarLrcSynced = _tmp_2 != 0;
    }
    final boolean _tmpCoreTagsComplete;
    if (_cursorIndexOfCoreTagsComplete == -1) {
      _tmpCoreTagsComplete = false;
    } else {
      final int _tmp_3;
      _tmp_3 = cursor.getInt(_cursorIndexOfCoreTagsComplete);
      _tmpCoreTagsComplete = _tmp_3 != 0;
    }
    final boolean _tmpArtistUnknown;
    if (_cursorIndexOfArtistUnknown == -1) {
      _tmpArtistUnknown = false;
    } else {
      final int _tmp_4;
      _tmp_4 = cursor.getInt(_cursorIndexOfArtistUnknown);
      _tmpArtistUnknown = _tmp_4 != 0;
    }
    final long _tmpScannedAt;
    if (_cursorIndexOfScannedAt == -1) {
      _tmpScannedAt = 0;
    } else {
      _tmpScannedAt = cursor.getLong(_cursorIndexOfScannedAt);
    }
    _entity = new TrackEntity(_tmpDocumentUri,_tmpTreeUri,_tmpDisplayName,_tmpParentPath,_tmpFormat,_tmpSizeBytes,_tmpLastModified,_tmpDurationMs,_tmpTitle,_tmpArtist,_tmpAlbum,_tmpAlbumArtist,_tmpTrackNumber,_tmpDiscNumber,_tmpYear,_tmpGenre,_tmpAlbumKey,_tmpAlbumLabel,_tmpHasEmbeddedArt,_tmpArtWidth,_tmpArtHeight,_tmpThumbnailPath,_tmpHasSidecarLrc,_tmpSidecarLrcSynced,_tmpCoreTagsComplete,_tmpArtistUnknown,_tmpScannedAt);
    return _entity;
  }
}
