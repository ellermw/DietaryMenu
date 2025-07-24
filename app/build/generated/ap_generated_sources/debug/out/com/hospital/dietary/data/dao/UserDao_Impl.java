package com.hospital.dietary.data.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.hospital.dietary.data.database.DateConverter;
import com.hospital.dietary.data.entities.UserEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

@SuppressWarnings({"unchecked", "deprecation"})
public final class UserDao_Impl implements UserDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<UserEntity> __insertionAdapterOfUserEntity;

  private final EntityDeletionOrUpdateAdapter<UserEntity> __deletionAdapterOfUserEntity;

  private final EntityDeletionOrUpdateAdapter<UserEntity> __updateAdapterOfUserEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteUserById;

  private final SharedSQLiteStatement __preparedStmtOfUpdateLastLogin;

  private final SharedSQLiteStatement __preparedStmtOfUpdatePassword;

  private final SharedSQLiteStatement __preparedStmtOfUpdatePasswordChangeRequirement;

  public UserDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfUserEntity = new EntityInsertionAdapter<UserEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `users` (`user_id`,`username`,`password`,`full_name`,`role`,`is_active`,`must_change_password`,`last_login`,`created_date`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final UserEntity entity) {
        statement.bindLong(1, entity.getUserId());
        if (entity.getUsername() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getUsername());
        }
        if (entity.getPassword() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getPassword());
        }
        if (entity.getFullName() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getFullName());
        }
        if (entity.getRole() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getRole());
        }
        final int _tmp = entity.isActive() ? 1 : 0;
        statement.bindLong(6, _tmp);
        final int _tmp_1 = entity.isMustChangePassword() ? 1 : 0;
        statement.bindLong(7, _tmp_1);
        final Long _tmp_2 = DateConverter.dateToTimestamp(entity.getLastLogin());
        if (_tmp_2 == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, _tmp_2);
        }
        final Long _tmp_3 = DateConverter.dateToTimestamp(entity.getCreatedDate());
        if (_tmp_3 == null) {
          statement.bindNull(9);
        } else {
          statement.bindLong(9, _tmp_3);
        }
      }
    };
    this.__deletionAdapterOfUserEntity = new EntityDeletionOrUpdateAdapter<UserEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `users` WHERE `user_id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final UserEntity entity) {
        statement.bindLong(1, entity.getUserId());
      }
    };
    this.__updateAdapterOfUserEntity = new EntityDeletionOrUpdateAdapter<UserEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `users` SET `user_id` = ?,`username` = ?,`password` = ?,`full_name` = ?,`role` = ?,`is_active` = ?,`must_change_password` = ?,`last_login` = ?,`created_date` = ? WHERE `user_id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final UserEntity entity) {
        statement.bindLong(1, entity.getUserId());
        if (entity.getUsername() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getUsername());
        }
        if (entity.getPassword() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getPassword());
        }
        if (entity.getFullName() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getFullName());
        }
        if (entity.getRole() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getRole());
        }
        final int _tmp = entity.isActive() ? 1 : 0;
        statement.bindLong(6, _tmp);
        final int _tmp_1 = entity.isMustChangePassword() ? 1 : 0;
        statement.bindLong(7, _tmp_1);
        final Long _tmp_2 = DateConverter.dateToTimestamp(entity.getLastLogin());
        if (_tmp_2 == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, _tmp_2);
        }
        final Long _tmp_3 = DateConverter.dateToTimestamp(entity.getCreatedDate());
        if (_tmp_3 == null) {
          statement.bindNull(9);
        } else {
          statement.bindLong(9, _tmp_3);
        }
        statement.bindLong(10, entity.getUserId());
      }
    };
    this.__preparedStmtOfDeleteUserById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM users WHERE user_id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateLastLogin = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE users SET last_login = ? WHERE user_id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdatePassword = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE users SET password = ? WHERE user_id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdatePasswordChangeRequirement = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE users SET must_change_password = ? WHERE user_id = ?";
        return _query;
      }
    };
  }

  @Override
  public long insertUser(final UserEntity user) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      final long _result = __insertionAdapterOfUserEntity.insertAndReturnId(user);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public int deleteUser(final UserEntity user) {
    __db.assertNotSuspendingTransaction();
    int _total = 0;
    __db.beginTransaction();
    try {
      _total += __deletionAdapterOfUserEntity.handle(user);
      __db.setTransactionSuccessful();
      return _total;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public int updateUser(final UserEntity user) {
    __db.assertNotSuspendingTransaction();
    int _total = 0;
    __db.beginTransaction();
    try {
      _total += __updateAdapterOfUserEntity.handle(user);
      __db.setTransactionSuccessful();
      return _total;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public int deleteUserById(final long userId) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteUserById.acquire();
    int _argIndex = 1;
    _stmt.bindLong(_argIndex, userId);
    try {
      __db.beginTransaction();
      try {
        final int _result = _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
        return _result;
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfDeleteUserById.release(_stmt);
    }
  }

  @Override
  public int updateLastLogin(final long userId, final Date lastLogin) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateLastLogin.acquire();
    int _argIndex = 1;
    final Long _tmp = DateConverter.dateToTimestamp(lastLogin);
    if (_tmp == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindLong(_argIndex, _tmp);
    }
    _argIndex = 2;
    _stmt.bindLong(_argIndex, userId);
    try {
      __db.beginTransaction();
      try {
        final int _result = _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
        return _result;
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfUpdateLastLogin.release(_stmt);
    }
  }

  @Override
  public int updatePassword(final long userId, final String password) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfUpdatePassword.acquire();
    int _argIndex = 1;
    if (password == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, password);
    }
    _argIndex = 2;
    _stmt.bindLong(_argIndex, userId);
    try {
      __db.beginTransaction();
      try {
        final int _result = _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
        return _result;
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfUpdatePassword.release(_stmt);
    }
  }

  @Override
  public int updatePasswordChangeRequirement(final long userId, final boolean mustChange) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfUpdatePasswordChangeRequirement.acquire();
    int _argIndex = 1;
    final int _tmp = mustChange ? 1 : 0;
    _stmt.bindLong(_argIndex, _tmp);
    _argIndex = 2;
    _stmt.bindLong(_argIndex, userId);
    try {
      __db.beginTransaction();
      try {
        final int _result = _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
        return _result;
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfUpdatePasswordChangeRequirement.release(_stmt);
    }
  }

  @Override
  public UserEntity getUserByUsername(final String username) {
    final String _sql = "SELECT * FROM users WHERE username = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (username == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, username);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "user_id");
      final int _cursorIndexOfUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "username");
      final int _cursorIndexOfPassword = CursorUtil.getColumnIndexOrThrow(_cursor, "password");
      final int _cursorIndexOfFullName = CursorUtil.getColumnIndexOrThrow(_cursor, "full_name");
      final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
      final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "is_active");
      final int _cursorIndexOfMustChangePassword = CursorUtil.getColumnIndexOrThrow(_cursor, "must_change_password");
      final int _cursorIndexOfLastLogin = CursorUtil.getColumnIndexOrThrow(_cursor, "last_login");
      final int _cursorIndexOfCreatedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "created_date");
      final UserEntity _result;
      if (_cursor.moveToFirst()) {
        _result = new UserEntity();
        final long _tmpUserId;
        _tmpUserId = _cursor.getLong(_cursorIndexOfUserId);
        _result.setUserId(_tmpUserId);
        final String _tmpUsername;
        if (_cursor.isNull(_cursorIndexOfUsername)) {
          _tmpUsername = null;
        } else {
          _tmpUsername = _cursor.getString(_cursorIndexOfUsername);
        }
        _result.setUsername(_tmpUsername);
        final String _tmpPassword;
        if (_cursor.isNull(_cursorIndexOfPassword)) {
          _tmpPassword = null;
        } else {
          _tmpPassword = _cursor.getString(_cursorIndexOfPassword);
        }
        _result.setPassword(_tmpPassword);
        final String _tmpFullName;
        if (_cursor.isNull(_cursorIndexOfFullName)) {
          _tmpFullName = null;
        } else {
          _tmpFullName = _cursor.getString(_cursorIndexOfFullName);
        }
        _result.setFullName(_tmpFullName);
        final String _tmpRole;
        if (_cursor.isNull(_cursorIndexOfRole)) {
          _tmpRole = null;
        } else {
          _tmpRole = _cursor.getString(_cursorIndexOfRole);
        }
        _result.setRole(_tmpRole);
        final boolean _tmpIsActive;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsActive);
        _tmpIsActive = _tmp != 0;
        _result.setActive(_tmpIsActive);
        final boolean _tmpMustChangePassword;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfMustChangePassword);
        _tmpMustChangePassword = _tmp_1 != 0;
        _result.setMustChangePassword(_tmpMustChangePassword);
        final Date _tmpLastLogin;
        final Long _tmp_2;
        if (_cursor.isNull(_cursorIndexOfLastLogin)) {
          _tmp_2 = null;
        } else {
          _tmp_2 = _cursor.getLong(_cursorIndexOfLastLogin);
        }
        _tmpLastLogin = DateConverter.fromTimestamp(_tmp_2);
        _result.setLastLogin(_tmpLastLogin);
        final Date _tmpCreatedDate;
        final Long _tmp_3;
        if (_cursor.isNull(_cursorIndexOfCreatedDate)) {
          _tmp_3 = null;
        } else {
          _tmp_3 = _cursor.getLong(_cursorIndexOfCreatedDate);
        }
        _tmpCreatedDate = DateConverter.fromTimestamp(_tmp_3);
        _result.setCreatedDate(_tmpCreatedDate);
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public UserEntity getUserById(final long userId) {
    final String _sql = "SELECT * FROM users WHERE user_id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, userId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "user_id");
      final int _cursorIndexOfUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "username");
      final int _cursorIndexOfPassword = CursorUtil.getColumnIndexOrThrow(_cursor, "password");
      final int _cursorIndexOfFullName = CursorUtil.getColumnIndexOrThrow(_cursor, "full_name");
      final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
      final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "is_active");
      final int _cursorIndexOfMustChangePassword = CursorUtil.getColumnIndexOrThrow(_cursor, "must_change_password");
      final int _cursorIndexOfLastLogin = CursorUtil.getColumnIndexOrThrow(_cursor, "last_login");
      final int _cursorIndexOfCreatedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "created_date");
      final UserEntity _result;
      if (_cursor.moveToFirst()) {
        _result = new UserEntity();
        final long _tmpUserId;
        _tmpUserId = _cursor.getLong(_cursorIndexOfUserId);
        _result.setUserId(_tmpUserId);
        final String _tmpUsername;
        if (_cursor.isNull(_cursorIndexOfUsername)) {
          _tmpUsername = null;
        } else {
          _tmpUsername = _cursor.getString(_cursorIndexOfUsername);
        }
        _result.setUsername(_tmpUsername);
        final String _tmpPassword;
        if (_cursor.isNull(_cursorIndexOfPassword)) {
          _tmpPassword = null;
        } else {
          _tmpPassword = _cursor.getString(_cursorIndexOfPassword);
        }
        _result.setPassword(_tmpPassword);
        final String _tmpFullName;
        if (_cursor.isNull(_cursorIndexOfFullName)) {
          _tmpFullName = null;
        } else {
          _tmpFullName = _cursor.getString(_cursorIndexOfFullName);
        }
        _result.setFullName(_tmpFullName);
        final String _tmpRole;
        if (_cursor.isNull(_cursorIndexOfRole)) {
          _tmpRole = null;
        } else {
          _tmpRole = _cursor.getString(_cursorIndexOfRole);
        }
        _result.setRole(_tmpRole);
        final boolean _tmpIsActive;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsActive);
        _tmpIsActive = _tmp != 0;
        _result.setActive(_tmpIsActive);
        final boolean _tmpMustChangePassword;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfMustChangePassword);
        _tmpMustChangePassword = _tmp_1 != 0;
        _result.setMustChangePassword(_tmpMustChangePassword);
        final Date _tmpLastLogin;
        final Long _tmp_2;
        if (_cursor.isNull(_cursorIndexOfLastLogin)) {
          _tmp_2 = null;
        } else {
          _tmp_2 = _cursor.getLong(_cursorIndexOfLastLogin);
        }
        _tmpLastLogin = DateConverter.fromTimestamp(_tmp_2);
        _result.setLastLogin(_tmpLastLogin);
        final Date _tmpCreatedDate;
        final Long _tmp_3;
        if (_cursor.isNull(_cursorIndexOfCreatedDate)) {
          _tmp_3 = null;
        } else {
          _tmp_3 = _cursor.getLong(_cursorIndexOfCreatedDate);
        }
        _tmpCreatedDate = DateConverter.fromTimestamp(_tmp_3);
        _result.setCreatedDate(_tmpCreatedDate);
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public LiveData<List<UserEntity>> getAllUsersLive() {
    final String _sql = "SELECT * FROM users ORDER BY username";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"users"}, false, new Callable<List<UserEntity>>() {
      @Override
      @Nullable
      public List<UserEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "user_id");
          final int _cursorIndexOfUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "username");
          final int _cursorIndexOfPassword = CursorUtil.getColumnIndexOrThrow(_cursor, "password");
          final int _cursorIndexOfFullName = CursorUtil.getColumnIndexOrThrow(_cursor, "full_name");
          final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "is_active");
          final int _cursorIndexOfMustChangePassword = CursorUtil.getColumnIndexOrThrow(_cursor, "must_change_password");
          final int _cursorIndexOfLastLogin = CursorUtil.getColumnIndexOrThrow(_cursor, "last_login");
          final int _cursorIndexOfCreatedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "created_date");
          final List<UserEntity> _result = new ArrayList<UserEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final UserEntity _item;
            _item = new UserEntity();
            final long _tmpUserId;
            _tmpUserId = _cursor.getLong(_cursorIndexOfUserId);
            _item.setUserId(_tmpUserId);
            final String _tmpUsername;
            if (_cursor.isNull(_cursorIndexOfUsername)) {
              _tmpUsername = null;
            } else {
              _tmpUsername = _cursor.getString(_cursorIndexOfUsername);
            }
            _item.setUsername(_tmpUsername);
            final String _tmpPassword;
            if (_cursor.isNull(_cursorIndexOfPassword)) {
              _tmpPassword = null;
            } else {
              _tmpPassword = _cursor.getString(_cursorIndexOfPassword);
            }
            _item.setPassword(_tmpPassword);
            final String _tmpFullName;
            if (_cursor.isNull(_cursorIndexOfFullName)) {
              _tmpFullName = null;
            } else {
              _tmpFullName = _cursor.getString(_cursorIndexOfFullName);
            }
            _item.setFullName(_tmpFullName);
            final String _tmpRole;
            if (_cursor.isNull(_cursorIndexOfRole)) {
              _tmpRole = null;
            } else {
              _tmpRole = _cursor.getString(_cursorIndexOfRole);
            }
            _item.setRole(_tmpRole);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            _item.setActive(_tmpIsActive);
            final boolean _tmpMustChangePassword;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfMustChangePassword);
            _tmpMustChangePassword = _tmp_1 != 0;
            _item.setMustChangePassword(_tmpMustChangePassword);
            final Date _tmpLastLogin;
            final Long _tmp_2;
            if (_cursor.isNull(_cursorIndexOfLastLogin)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getLong(_cursorIndexOfLastLogin);
            }
            _tmpLastLogin = DateConverter.fromTimestamp(_tmp_2);
            _item.setLastLogin(_tmpLastLogin);
            final Date _tmpCreatedDate;
            final Long _tmp_3;
            if (_cursor.isNull(_cursorIndexOfCreatedDate)) {
              _tmp_3 = null;
            } else {
              _tmp_3 = _cursor.getLong(_cursorIndexOfCreatedDate);
            }
            _tmpCreatedDate = DateConverter.fromTimestamp(_tmp_3);
            _item.setCreatedDate(_tmpCreatedDate);
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
  public List<UserEntity> getAllUsers() {
    final String _sql = "SELECT * FROM users ORDER BY username";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "user_id");
      final int _cursorIndexOfUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "username");
      final int _cursorIndexOfPassword = CursorUtil.getColumnIndexOrThrow(_cursor, "password");
      final int _cursorIndexOfFullName = CursorUtil.getColumnIndexOrThrow(_cursor, "full_name");
      final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
      final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "is_active");
      final int _cursorIndexOfMustChangePassword = CursorUtil.getColumnIndexOrThrow(_cursor, "must_change_password");
      final int _cursorIndexOfLastLogin = CursorUtil.getColumnIndexOrThrow(_cursor, "last_login");
      final int _cursorIndexOfCreatedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "created_date");
      final List<UserEntity> _result = new ArrayList<UserEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final UserEntity _item;
        _item = new UserEntity();
        final long _tmpUserId;
        _tmpUserId = _cursor.getLong(_cursorIndexOfUserId);
        _item.setUserId(_tmpUserId);
        final String _tmpUsername;
        if (_cursor.isNull(_cursorIndexOfUsername)) {
          _tmpUsername = null;
        } else {
          _tmpUsername = _cursor.getString(_cursorIndexOfUsername);
        }
        _item.setUsername(_tmpUsername);
        final String _tmpPassword;
        if (_cursor.isNull(_cursorIndexOfPassword)) {
          _tmpPassword = null;
        } else {
          _tmpPassword = _cursor.getString(_cursorIndexOfPassword);
        }
        _item.setPassword(_tmpPassword);
        final String _tmpFullName;
        if (_cursor.isNull(_cursorIndexOfFullName)) {
          _tmpFullName = null;
        } else {
          _tmpFullName = _cursor.getString(_cursorIndexOfFullName);
        }
        _item.setFullName(_tmpFullName);
        final String _tmpRole;
        if (_cursor.isNull(_cursorIndexOfRole)) {
          _tmpRole = null;
        } else {
          _tmpRole = _cursor.getString(_cursorIndexOfRole);
        }
        _item.setRole(_tmpRole);
        final boolean _tmpIsActive;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsActive);
        _tmpIsActive = _tmp != 0;
        _item.setActive(_tmpIsActive);
        final boolean _tmpMustChangePassword;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfMustChangePassword);
        _tmpMustChangePassword = _tmp_1 != 0;
        _item.setMustChangePassword(_tmpMustChangePassword);
        final Date _tmpLastLogin;
        final Long _tmp_2;
        if (_cursor.isNull(_cursorIndexOfLastLogin)) {
          _tmp_2 = null;
        } else {
          _tmp_2 = _cursor.getLong(_cursorIndexOfLastLogin);
        }
        _tmpLastLogin = DateConverter.fromTimestamp(_tmp_2);
        _item.setLastLogin(_tmpLastLogin);
        final Date _tmpCreatedDate;
        final Long _tmp_3;
        if (_cursor.isNull(_cursorIndexOfCreatedDate)) {
          _tmp_3 = null;
        } else {
          _tmp_3 = _cursor.getLong(_cursorIndexOfCreatedDate);
        }
        _tmpCreatedDate = DateConverter.fromTimestamp(_tmp_3);
        _item.setCreatedDate(_tmpCreatedDate);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public LiveData<List<UserEntity>> getActiveUsersLive() {
    final String _sql = "SELECT * FROM users WHERE is_active = 1 ORDER BY username";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"users"}, false, new Callable<List<UserEntity>>() {
      @Override
      @Nullable
      public List<UserEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "user_id");
          final int _cursorIndexOfUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "username");
          final int _cursorIndexOfPassword = CursorUtil.getColumnIndexOrThrow(_cursor, "password");
          final int _cursorIndexOfFullName = CursorUtil.getColumnIndexOrThrow(_cursor, "full_name");
          final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "is_active");
          final int _cursorIndexOfMustChangePassword = CursorUtil.getColumnIndexOrThrow(_cursor, "must_change_password");
          final int _cursorIndexOfLastLogin = CursorUtil.getColumnIndexOrThrow(_cursor, "last_login");
          final int _cursorIndexOfCreatedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "created_date");
          final List<UserEntity> _result = new ArrayList<UserEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final UserEntity _item;
            _item = new UserEntity();
            final long _tmpUserId;
            _tmpUserId = _cursor.getLong(_cursorIndexOfUserId);
            _item.setUserId(_tmpUserId);
            final String _tmpUsername;
            if (_cursor.isNull(_cursorIndexOfUsername)) {
              _tmpUsername = null;
            } else {
              _tmpUsername = _cursor.getString(_cursorIndexOfUsername);
            }
            _item.setUsername(_tmpUsername);
            final String _tmpPassword;
            if (_cursor.isNull(_cursorIndexOfPassword)) {
              _tmpPassword = null;
            } else {
              _tmpPassword = _cursor.getString(_cursorIndexOfPassword);
            }
            _item.setPassword(_tmpPassword);
            final String _tmpFullName;
            if (_cursor.isNull(_cursorIndexOfFullName)) {
              _tmpFullName = null;
            } else {
              _tmpFullName = _cursor.getString(_cursorIndexOfFullName);
            }
            _item.setFullName(_tmpFullName);
            final String _tmpRole;
            if (_cursor.isNull(_cursorIndexOfRole)) {
              _tmpRole = null;
            } else {
              _tmpRole = _cursor.getString(_cursorIndexOfRole);
            }
            _item.setRole(_tmpRole);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            _item.setActive(_tmpIsActive);
            final boolean _tmpMustChangePassword;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfMustChangePassword);
            _tmpMustChangePassword = _tmp_1 != 0;
            _item.setMustChangePassword(_tmpMustChangePassword);
            final Date _tmpLastLogin;
            final Long _tmp_2;
            if (_cursor.isNull(_cursorIndexOfLastLogin)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getLong(_cursorIndexOfLastLogin);
            }
            _tmpLastLogin = DateConverter.fromTimestamp(_tmp_2);
            _item.setLastLogin(_tmpLastLogin);
            final Date _tmpCreatedDate;
            final Long _tmp_3;
            if (_cursor.isNull(_cursorIndexOfCreatedDate)) {
              _tmp_3 = null;
            } else {
              _tmp_3 = _cursor.getLong(_cursorIndexOfCreatedDate);
            }
            _tmpCreatedDate = DateConverter.fromTimestamp(_tmp_3);
            _item.setCreatedDate(_tmpCreatedDate);
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
  public List<UserEntity> getActiveUsers() {
    final String _sql = "SELECT * FROM users WHERE is_active = 1 ORDER BY username";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "user_id");
      final int _cursorIndexOfUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "username");
      final int _cursorIndexOfPassword = CursorUtil.getColumnIndexOrThrow(_cursor, "password");
      final int _cursorIndexOfFullName = CursorUtil.getColumnIndexOrThrow(_cursor, "full_name");
      final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
      final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "is_active");
      final int _cursorIndexOfMustChangePassword = CursorUtil.getColumnIndexOrThrow(_cursor, "must_change_password");
      final int _cursorIndexOfLastLogin = CursorUtil.getColumnIndexOrThrow(_cursor, "last_login");
      final int _cursorIndexOfCreatedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "created_date");
      final List<UserEntity> _result = new ArrayList<UserEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final UserEntity _item;
        _item = new UserEntity();
        final long _tmpUserId;
        _tmpUserId = _cursor.getLong(_cursorIndexOfUserId);
        _item.setUserId(_tmpUserId);
        final String _tmpUsername;
        if (_cursor.isNull(_cursorIndexOfUsername)) {
          _tmpUsername = null;
        } else {
          _tmpUsername = _cursor.getString(_cursorIndexOfUsername);
        }
        _item.setUsername(_tmpUsername);
        final String _tmpPassword;
        if (_cursor.isNull(_cursorIndexOfPassword)) {
          _tmpPassword = null;
        } else {
          _tmpPassword = _cursor.getString(_cursorIndexOfPassword);
        }
        _item.setPassword(_tmpPassword);
        final String _tmpFullName;
        if (_cursor.isNull(_cursorIndexOfFullName)) {
          _tmpFullName = null;
        } else {
          _tmpFullName = _cursor.getString(_cursorIndexOfFullName);
        }
        _item.setFullName(_tmpFullName);
        final String _tmpRole;
        if (_cursor.isNull(_cursorIndexOfRole)) {
          _tmpRole = null;
        } else {
          _tmpRole = _cursor.getString(_cursorIndexOfRole);
        }
        _item.setRole(_tmpRole);
        final boolean _tmpIsActive;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsActive);
        _tmpIsActive = _tmp != 0;
        _item.setActive(_tmpIsActive);
        final boolean _tmpMustChangePassword;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfMustChangePassword);
        _tmpMustChangePassword = _tmp_1 != 0;
        _item.setMustChangePassword(_tmpMustChangePassword);
        final Date _tmpLastLogin;
        final Long _tmp_2;
        if (_cursor.isNull(_cursorIndexOfLastLogin)) {
          _tmp_2 = null;
        } else {
          _tmp_2 = _cursor.getLong(_cursorIndexOfLastLogin);
        }
        _tmpLastLogin = DateConverter.fromTimestamp(_tmp_2);
        _item.setLastLogin(_tmpLastLogin);
        final Date _tmpCreatedDate;
        final Long _tmp_3;
        if (_cursor.isNull(_cursorIndexOfCreatedDate)) {
          _tmp_3 = null;
        } else {
          _tmp_3 = _cursor.getLong(_cursorIndexOfCreatedDate);
        }
        _tmpCreatedDate = DateConverter.fromTimestamp(_tmp_3);
        _item.setCreatedDate(_tmpCreatedDate);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public LiveData<List<UserEntity>> getUsersByRoleLive(final String role) {
    final String _sql = "SELECT * FROM users WHERE role = ? ORDER BY username";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (role == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, role);
    }
    return __db.getInvalidationTracker().createLiveData(new String[] {"users"}, false, new Callable<List<UserEntity>>() {
      @Override
      @Nullable
      public List<UserEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "user_id");
          final int _cursorIndexOfUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "username");
          final int _cursorIndexOfPassword = CursorUtil.getColumnIndexOrThrow(_cursor, "password");
          final int _cursorIndexOfFullName = CursorUtil.getColumnIndexOrThrow(_cursor, "full_name");
          final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "is_active");
          final int _cursorIndexOfMustChangePassword = CursorUtil.getColumnIndexOrThrow(_cursor, "must_change_password");
          final int _cursorIndexOfLastLogin = CursorUtil.getColumnIndexOrThrow(_cursor, "last_login");
          final int _cursorIndexOfCreatedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "created_date");
          final List<UserEntity> _result = new ArrayList<UserEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final UserEntity _item;
            _item = new UserEntity();
            final long _tmpUserId;
            _tmpUserId = _cursor.getLong(_cursorIndexOfUserId);
            _item.setUserId(_tmpUserId);
            final String _tmpUsername;
            if (_cursor.isNull(_cursorIndexOfUsername)) {
              _tmpUsername = null;
            } else {
              _tmpUsername = _cursor.getString(_cursorIndexOfUsername);
            }
            _item.setUsername(_tmpUsername);
            final String _tmpPassword;
            if (_cursor.isNull(_cursorIndexOfPassword)) {
              _tmpPassword = null;
            } else {
              _tmpPassword = _cursor.getString(_cursorIndexOfPassword);
            }
            _item.setPassword(_tmpPassword);
            final String _tmpFullName;
            if (_cursor.isNull(_cursorIndexOfFullName)) {
              _tmpFullName = null;
            } else {
              _tmpFullName = _cursor.getString(_cursorIndexOfFullName);
            }
            _item.setFullName(_tmpFullName);
            final String _tmpRole;
            if (_cursor.isNull(_cursorIndexOfRole)) {
              _tmpRole = null;
            } else {
              _tmpRole = _cursor.getString(_cursorIndexOfRole);
            }
            _item.setRole(_tmpRole);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            _item.setActive(_tmpIsActive);
            final boolean _tmpMustChangePassword;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfMustChangePassword);
            _tmpMustChangePassword = _tmp_1 != 0;
            _item.setMustChangePassword(_tmpMustChangePassword);
            final Date _tmpLastLogin;
            final Long _tmp_2;
            if (_cursor.isNull(_cursorIndexOfLastLogin)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getLong(_cursorIndexOfLastLogin);
            }
            _tmpLastLogin = DateConverter.fromTimestamp(_tmp_2);
            _item.setLastLogin(_tmpLastLogin);
            final Date _tmpCreatedDate;
            final Long _tmp_3;
            if (_cursor.isNull(_cursorIndexOfCreatedDate)) {
              _tmp_3 = null;
            } else {
              _tmp_3 = _cursor.getLong(_cursorIndexOfCreatedDate);
            }
            _tmpCreatedDate = DateConverter.fromTimestamp(_tmp_3);
            _item.setCreatedDate(_tmpCreatedDate);
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
  public LiveData<List<UserEntity>> getUsersNeedingPasswordChangeLive() {
    final String _sql = "SELECT * FROM users WHERE must_change_password = 1 AND is_active = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"users"}, false, new Callable<List<UserEntity>>() {
      @Override
      @Nullable
      public List<UserEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "user_id");
          final int _cursorIndexOfUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "username");
          final int _cursorIndexOfPassword = CursorUtil.getColumnIndexOrThrow(_cursor, "password");
          final int _cursorIndexOfFullName = CursorUtil.getColumnIndexOrThrow(_cursor, "full_name");
          final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "is_active");
          final int _cursorIndexOfMustChangePassword = CursorUtil.getColumnIndexOrThrow(_cursor, "must_change_password");
          final int _cursorIndexOfLastLogin = CursorUtil.getColumnIndexOrThrow(_cursor, "last_login");
          final int _cursorIndexOfCreatedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "created_date");
          final List<UserEntity> _result = new ArrayList<UserEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final UserEntity _item;
            _item = new UserEntity();
            final long _tmpUserId;
            _tmpUserId = _cursor.getLong(_cursorIndexOfUserId);
            _item.setUserId(_tmpUserId);
            final String _tmpUsername;
            if (_cursor.isNull(_cursorIndexOfUsername)) {
              _tmpUsername = null;
            } else {
              _tmpUsername = _cursor.getString(_cursorIndexOfUsername);
            }
            _item.setUsername(_tmpUsername);
            final String _tmpPassword;
            if (_cursor.isNull(_cursorIndexOfPassword)) {
              _tmpPassword = null;
            } else {
              _tmpPassword = _cursor.getString(_cursorIndexOfPassword);
            }
            _item.setPassword(_tmpPassword);
            final String _tmpFullName;
            if (_cursor.isNull(_cursorIndexOfFullName)) {
              _tmpFullName = null;
            } else {
              _tmpFullName = _cursor.getString(_cursorIndexOfFullName);
            }
            _item.setFullName(_tmpFullName);
            final String _tmpRole;
            if (_cursor.isNull(_cursorIndexOfRole)) {
              _tmpRole = null;
            } else {
              _tmpRole = _cursor.getString(_cursorIndexOfRole);
            }
            _item.setRole(_tmpRole);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            _item.setActive(_tmpIsActive);
            final boolean _tmpMustChangePassword;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfMustChangePassword);
            _tmpMustChangePassword = _tmp_1 != 0;
            _item.setMustChangePassword(_tmpMustChangePassword);
            final Date _tmpLastLogin;
            final Long _tmp_2;
            if (_cursor.isNull(_cursorIndexOfLastLogin)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getLong(_cursorIndexOfLastLogin);
            }
            _tmpLastLogin = DateConverter.fromTimestamp(_tmp_2);
            _item.setLastLogin(_tmpLastLogin);
            final Date _tmpCreatedDate;
            final Long _tmp_3;
            if (_cursor.isNull(_cursorIndexOfCreatedDate)) {
              _tmp_3 = null;
            } else {
              _tmp_3 = _cursor.getLong(_cursorIndexOfCreatedDate);
            }
            _tmpCreatedDate = DateConverter.fromTimestamp(_tmp_3);
            _item.setCreatedDate(_tmpCreatedDate);
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
  public int countByUsername(final String username) {
    final String _sql = "SELECT COUNT(*) FROM users WHERE username = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (username == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, username);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _result;
      if (_cursor.moveToFirst()) {
        _result = _cursor.getInt(0);
      } else {
        _result = 0;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
