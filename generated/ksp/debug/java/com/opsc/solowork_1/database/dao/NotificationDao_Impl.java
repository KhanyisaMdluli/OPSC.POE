package com.opsc.solowork_1.database.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.opsc.solowork_1.database.converter.DateConverter;
import com.opsc.solowork_1.database.entity.NotificationEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.IllegalStateException;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class NotificationDao_Impl implements NotificationDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<NotificationEntity> __insertionAdapterOfNotificationEntity;

  private final DateConverter __dateConverter = new DateConverter();

  private final EntityDeletionOrUpdateAdapter<NotificationEntity> __deletionAdapterOfNotificationEntity;

  private final EntityDeletionOrUpdateAdapter<NotificationEntity> __updateAdapterOfNotificationEntity;

  private final SharedSQLiteStatement __preparedStmtOfMarkAsRead;

  private final SharedSQLiteStatement __preparedStmtOfMarkAllAsRead;

  private final SharedSQLiteStatement __preparedStmtOfDeleteNotificationById;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAllNotifications;

  private final SharedSQLiteStatement __preparedStmtOfMarkNotificationAsSynced;

  private final SharedSQLiteStatement __preparedStmtOfDeleteOldNotifications;

  public NotificationDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfNotificationEntity = new EntityInsertionAdapter<NotificationEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `notifications` (`id`,`title`,`message`,`type`,`timestamp`,`read`,`additionalData`,`userId`,`isSynced`,`lastModified`) VALUES (?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final NotificationEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getTitle());
        statement.bindString(3, entity.getMessage());
        statement.bindString(4, entity.getType());
        final Long _tmp = __dateConverter.dateToTimestamp(entity.getTimestamp());
        if (_tmp == null) {
          statement.bindNull(5);
        } else {
          statement.bindLong(5, _tmp);
        }
        final int _tmp_1 = entity.getRead() ? 1 : 0;
        statement.bindLong(6, _tmp_1);
        if (entity.getAdditionalData() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getAdditionalData());
        }
        statement.bindString(8, entity.getUserId());
        final int _tmp_2 = entity.isSynced() ? 1 : 0;
        statement.bindLong(9, _tmp_2);
        statement.bindLong(10, entity.getLastModified());
      }
    };
    this.__deletionAdapterOfNotificationEntity = new EntityDeletionOrUpdateAdapter<NotificationEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `notifications` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final NotificationEntity entity) {
        statement.bindString(1, entity.getId());
      }
    };
    this.__updateAdapterOfNotificationEntity = new EntityDeletionOrUpdateAdapter<NotificationEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `notifications` SET `id` = ?,`title` = ?,`message` = ?,`type` = ?,`timestamp` = ?,`read` = ?,`additionalData` = ?,`userId` = ?,`isSynced` = ?,`lastModified` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final NotificationEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getTitle());
        statement.bindString(3, entity.getMessage());
        statement.bindString(4, entity.getType());
        final Long _tmp = __dateConverter.dateToTimestamp(entity.getTimestamp());
        if (_tmp == null) {
          statement.bindNull(5);
        } else {
          statement.bindLong(5, _tmp);
        }
        final int _tmp_1 = entity.getRead() ? 1 : 0;
        statement.bindLong(6, _tmp_1);
        if (entity.getAdditionalData() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getAdditionalData());
        }
        statement.bindString(8, entity.getUserId());
        final int _tmp_2 = entity.isSynced() ? 1 : 0;
        statement.bindLong(9, _tmp_2);
        statement.bindLong(10, entity.getLastModified());
        statement.bindString(11, entity.getId());
      }
    };
    this.__preparedStmtOfMarkAsRead = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE notifications SET read = 1 WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfMarkAllAsRead = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE notifications SET read = 1 WHERE userId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteNotificationById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM notifications WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAllNotifications = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM notifications WHERE userId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfMarkNotificationAsSynced = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE notifications SET isSynced = 1 WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteOldNotifications = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM notifications WHERE timestamp < ? AND userId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertNotification(final NotificationEntity notification,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfNotificationEntity.insert(notification);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteNotification(final NotificationEntity notification,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfNotificationEntity.handle(notification);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateNotification(final NotificationEntity notification,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfNotificationEntity.handle(notification);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object markAsRead(final String notificationId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkAsRead.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, notificationId);
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
          __preparedStmtOfMarkAsRead.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object markAllAsRead(final String userId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkAllAsRead.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, userId);
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
          __preparedStmtOfMarkAllAsRead.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteNotificationById(final String notificationId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteNotificationById.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, notificationId);
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
          __preparedStmtOfDeleteNotificationById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAllNotifications(final String userId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAllNotifications.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, userId);
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
          __preparedStmtOfDeleteAllNotifications.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object markNotificationAsSynced(final String notificationId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkNotificationAsSynced.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, notificationId);
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
          __preparedStmtOfMarkNotificationAsSynced.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteOldNotifications(final Date timestamp, final String userId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteOldNotifications.acquire();
        int _argIndex = 1;
        final Long _tmp = __dateConverter.dateToTimestamp(timestamp);
        if (_tmp == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindLong(_argIndex, _tmp);
        }
        _argIndex = 2;
        _stmt.bindString(_argIndex, userId);
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
          __preparedStmtOfDeleteOldNotifications.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public List<NotificationEntity> getNotificationsByUser(final String userId) {
    final String _sql = "SELECT * FROM notifications WHERE userId = ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
      final int _cursorIndexOfMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "message");
      final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final int _cursorIndexOfRead = CursorUtil.getColumnIndexOrThrow(_cursor, "read");
      final int _cursorIndexOfAdditionalData = CursorUtil.getColumnIndexOrThrow(_cursor, "additionalData");
      final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
      final int _cursorIndexOfIsSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "isSynced");
      final int _cursorIndexOfLastModified = CursorUtil.getColumnIndexOrThrow(_cursor, "lastModified");
      final List<NotificationEntity> _result = new ArrayList<NotificationEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final NotificationEntity _item;
        final String _tmpId;
        _tmpId = _cursor.getString(_cursorIndexOfId);
        final String _tmpTitle;
        _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
        final String _tmpMessage;
        _tmpMessage = _cursor.getString(_cursorIndexOfMessage);
        final String _tmpType;
        _tmpType = _cursor.getString(_cursorIndexOfType);
        final Date _tmpTimestamp;
        final Long _tmp;
        if (_cursor.isNull(_cursorIndexOfTimestamp)) {
          _tmp = null;
        } else {
          _tmp = _cursor.getLong(_cursorIndexOfTimestamp);
        }
        final Date _tmp_1 = __dateConverter.fromTimestamp(_tmp);
        if (_tmp_1 == null) {
          throw new IllegalStateException("Expected NON-NULL 'java.util.Date', but it was NULL.");
        } else {
          _tmpTimestamp = _tmp_1;
        }
        final boolean _tmpRead;
        final int _tmp_2;
        _tmp_2 = _cursor.getInt(_cursorIndexOfRead);
        _tmpRead = _tmp_2 != 0;
        final String _tmpAdditionalData;
        if (_cursor.isNull(_cursorIndexOfAdditionalData)) {
          _tmpAdditionalData = null;
        } else {
          _tmpAdditionalData = _cursor.getString(_cursorIndexOfAdditionalData);
        }
        final String _tmpUserId;
        _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
        final boolean _tmpIsSynced;
        final int _tmp_3;
        _tmp_3 = _cursor.getInt(_cursorIndexOfIsSynced);
        _tmpIsSynced = _tmp_3 != 0;
        final long _tmpLastModified;
        _tmpLastModified = _cursor.getLong(_cursorIndexOfLastModified);
        _item = new NotificationEntity(_tmpId,_tmpTitle,_tmpMessage,_tmpType,_tmpTimestamp,_tmpRead,_tmpAdditionalData,_tmpUserId,_tmpIsSynced,_tmpLastModified);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public Object getNotificationById(final String notificationId,
      final Continuation<? super NotificationEntity> $completion) {
    final String _sql = "SELECT * FROM notifications WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, notificationId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<NotificationEntity>() {
      @Override
      @Nullable
      public NotificationEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "message");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfRead = CursorUtil.getColumnIndexOrThrow(_cursor, "read");
          final int _cursorIndexOfAdditionalData = CursorUtil.getColumnIndexOrThrow(_cursor, "additionalData");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfIsSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "isSynced");
          final int _cursorIndexOfLastModified = CursorUtil.getColumnIndexOrThrow(_cursor, "lastModified");
          final NotificationEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpMessage;
            _tmpMessage = _cursor.getString(_cursorIndexOfMessage);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final Date _tmpTimestamp;
            final Long _tmp;
            if (_cursor.isNull(_cursorIndexOfTimestamp)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(_cursorIndexOfTimestamp);
            }
            final Date _tmp_1 = __dateConverter.fromTimestamp(_tmp);
            if (_tmp_1 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.util.Date', but it was NULL.");
            } else {
              _tmpTimestamp = _tmp_1;
            }
            final boolean _tmpRead;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfRead);
            _tmpRead = _tmp_2 != 0;
            final String _tmpAdditionalData;
            if (_cursor.isNull(_cursorIndexOfAdditionalData)) {
              _tmpAdditionalData = null;
            } else {
              _tmpAdditionalData = _cursor.getString(_cursorIndexOfAdditionalData);
            }
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final boolean _tmpIsSynced;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsSynced);
            _tmpIsSynced = _tmp_3 != 0;
            final long _tmpLastModified;
            _tmpLastModified = _cursor.getLong(_cursorIndexOfLastModified);
            _result = new NotificationEntity(_tmpId,_tmpTitle,_tmpMessage,_tmpType,_tmpTimestamp,_tmpRead,_tmpAdditionalData,_tmpUserId,_tmpIsSynced,_tmpLastModified);
          } else {
            _result = null;
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
  public List<NotificationEntity> getUnreadNotifications(final String userId) {
    final String _sql = "SELECT * FROM notifications WHERE userId = ? AND read = 0 ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
      final int _cursorIndexOfMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "message");
      final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final int _cursorIndexOfRead = CursorUtil.getColumnIndexOrThrow(_cursor, "read");
      final int _cursorIndexOfAdditionalData = CursorUtil.getColumnIndexOrThrow(_cursor, "additionalData");
      final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
      final int _cursorIndexOfIsSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "isSynced");
      final int _cursorIndexOfLastModified = CursorUtil.getColumnIndexOrThrow(_cursor, "lastModified");
      final List<NotificationEntity> _result = new ArrayList<NotificationEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final NotificationEntity _item;
        final String _tmpId;
        _tmpId = _cursor.getString(_cursorIndexOfId);
        final String _tmpTitle;
        _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
        final String _tmpMessage;
        _tmpMessage = _cursor.getString(_cursorIndexOfMessage);
        final String _tmpType;
        _tmpType = _cursor.getString(_cursorIndexOfType);
        final Date _tmpTimestamp;
        final Long _tmp;
        if (_cursor.isNull(_cursorIndexOfTimestamp)) {
          _tmp = null;
        } else {
          _tmp = _cursor.getLong(_cursorIndexOfTimestamp);
        }
        final Date _tmp_1 = __dateConverter.fromTimestamp(_tmp);
        if (_tmp_1 == null) {
          throw new IllegalStateException("Expected NON-NULL 'java.util.Date', but it was NULL.");
        } else {
          _tmpTimestamp = _tmp_1;
        }
        final boolean _tmpRead;
        final int _tmp_2;
        _tmp_2 = _cursor.getInt(_cursorIndexOfRead);
        _tmpRead = _tmp_2 != 0;
        final String _tmpAdditionalData;
        if (_cursor.isNull(_cursorIndexOfAdditionalData)) {
          _tmpAdditionalData = null;
        } else {
          _tmpAdditionalData = _cursor.getString(_cursorIndexOfAdditionalData);
        }
        final String _tmpUserId;
        _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
        final boolean _tmpIsSynced;
        final int _tmp_3;
        _tmp_3 = _cursor.getInt(_cursorIndexOfIsSynced);
        _tmpIsSynced = _tmp_3 != 0;
        final long _tmpLastModified;
        _tmpLastModified = _cursor.getLong(_cursorIndexOfLastModified);
        _item = new NotificationEntity(_tmpId,_tmpTitle,_tmpMessage,_tmpType,_tmpTimestamp,_tmpRead,_tmpAdditionalData,_tmpUserId,_tmpIsSynced,_tmpLastModified);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<NotificationEntity> getNotificationsByType(final String userId, final String type) {
    final String _sql = "SELECT * FROM notifications WHERE userId = ? AND type = ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    _argIndex = 2;
    _statement.bindString(_argIndex, type);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
      final int _cursorIndexOfMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "message");
      final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final int _cursorIndexOfRead = CursorUtil.getColumnIndexOrThrow(_cursor, "read");
      final int _cursorIndexOfAdditionalData = CursorUtil.getColumnIndexOrThrow(_cursor, "additionalData");
      final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
      final int _cursorIndexOfIsSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "isSynced");
      final int _cursorIndexOfLastModified = CursorUtil.getColumnIndexOrThrow(_cursor, "lastModified");
      final List<NotificationEntity> _result = new ArrayList<NotificationEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final NotificationEntity _item;
        final String _tmpId;
        _tmpId = _cursor.getString(_cursorIndexOfId);
        final String _tmpTitle;
        _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
        final String _tmpMessage;
        _tmpMessage = _cursor.getString(_cursorIndexOfMessage);
        final String _tmpType;
        _tmpType = _cursor.getString(_cursorIndexOfType);
        final Date _tmpTimestamp;
        final Long _tmp;
        if (_cursor.isNull(_cursorIndexOfTimestamp)) {
          _tmp = null;
        } else {
          _tmp = _cursor.getLong(_cursorIndexOfTimestamp);
        }
        final Date _tmp_1 = __dateConverter.fromTimestamp(_tmp);
        if (_tmp_1 == null) {
          throw new IllegalStateException("Expected NON-NULL 'java.util.Date', but it was NULL.");
        } else {
          _tmpTimestamp = _tmp_1;
        }
        final boolean _tmpRead;
        final int _tmp_2;
        _tmp_2 = _cursor.getInt(_cursorIndexOfRead);
        _tmpRead = _tmp_2 != 0;
        final String _tmpAdditionalData;
        if (_cursor.isNull(_cursorIndexOfAdditionalData)) {
          _tmpAdditionalData = null;
        } else {
          _tmpAdditionalData = _cursor.getString(_cursorIndexOfAdditionalData);
        }
        final String _tmpUserId;
        _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
        final boolean _tmpIsSynced;
        final int _tmp_3;
        _tmp_3 = _cursor.getInt(_cursorIndexOfIsSynced);
        _tmpIsSynced = _tmp_3 != 0;
        final long _tmpLastModified;
        _tmpLastModified = _cursor.getLong(_cursorIndexOfLastModified);
        _item = new NotificationEntity(_tmpId,_tmpTitle,_tmpMessage,_tmpType,_tmpTimestamp,_tmpRead,_tmpAdditionalData,_tmpUserId,_tmpIsSynced,_tmpLastModified);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public Object getUnreadCount(final String userId,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM notifications WHERE userId = ? AND read = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
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
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getUnsyncedNotifications(
      final Continuation<? super List<NotificationEntity>> $completion) {
    final String _sql = "SELECT * FROM notifications WHERE isSynced = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<NotificationEntity>>() {
      @Override
      @NonNull
      public List<NotificationEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "message");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfRead = CursorUtil.getColumnIndexOrThrow(_cursor, "read");
          final int _cursorIndexOfAdditionalData = CursorUtil.getColumnIndexOrThrow(_cursor, "additionalData");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfIsSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "isSynced");
          final int _cursorIndexOfLastModified = CursorUtil.getColumnIndexOrThrow(_cursor, "lastModified");
          final List<NotificationEntity> _result = new ArrayList<NotificationEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final NotificationEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpMessage;
            _tmpMessage = _cursor.getString(_cursorIndexOfMessage);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final Date _tmpTimestamp;
            final Long _tmp;
            if (_cursor.isNull(_cursorIndexOfTimestamp)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(_cursorIndexOfTimestamp);
            }
            final Date _tmp_1 = __dateConverter.fromTimestamp(_tmp);
            if (_tmp_1 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.util.Date', but it was NULL.");
            } else {
              _tmpTimestamp = _tmp_1;
            }
            final boolean _tmpRead;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfRead);
            _tmpRead = _tmp_2 != 0;
            final String _tmpAdditionalData;
            if (_cursor.isNull(_cursorIndexOfAdditionalData)) {
              _tmpAdditionalData = null;
            } else {
              _tmpAdditionalData = _cursor.getString(_cursorIndexOfAdditionalData);
            }
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final boolean _tmpIsSynced;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsSynced);
            _tmpIsSynced = _tmp_3 != 0;
            final long _tmpLastModified;
            _tmpLastModified = _cursor.getLong(_cursorIndexOfLastModified);
            _item = new NotificationEntity(_tmpId,_tmpTitle,_tmpMessage,_tmpType,_tmpTimestamp,_tmpRead,_tmpAdditionalData,_tmpUserId,_tmpIsSynced,_tmpLastModified);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
