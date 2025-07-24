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
import com.hospital.dietary.data.entities.ItemEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
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
public final class ItemDao_Impl implements ItemDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ItemEntity> __insertionAdapterOfItemEntity;

  private final EntityDeletionOrUpdateAdapter<ItemEntity> __deletionAdapterOfItemEntity;

  private final EntityDeletionOrUpdateAdapter<ItemEntity> __updateAdapterOfItemEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteItemById;

  public ItemDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfItemEntity = new EntityInsertionAdapter<ItemEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `items` (`item_id`,`name`,`category`,`description`,`is_ada_friendly`,`created_date`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final ItemEntity entity) {
        statement.bindLong(1, entity.getItemId());
        if (entity.getName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getName());
        }
        if (entity.getCategory() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getCategory());
        }
        if (entity.getDescription() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getDescription());
        }
        final int _tmp = entity.isAdaFriendly() ? 1 : 0;
        statement.bindLong(5, _tmp);
        final Long _tmp_1 = DateConverter.dateToTimestamp(entity.getCreatedDate());
        if (_tmp_1 == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, _tmp_1);
        }
      }
    };
    this.__deletionAdapterOfItemEntity = new EntityDeletionOrUpdateAdapter<ItemEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `items` WHERE `item_id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final ItemEntity entity) {
        statement.bindLong(1, entity.getItemId());
      }
    };
    this.__updateAdapterOfItemEntity = new EntityDeletionOrUpdateAdapter<ItemEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `items` SET `item_id` = ?,`name` = ?,`category` = ?,`description` = ?,`is_ada_friendly` = ?,`created_date` = ? WHERE `item_id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final ItemEntity entity) {
        statement.bindLong(1, entity.getItemId());
        if (entity.getName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getName());
        }
        if (entity.getCategory() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getCategory());
        }
        if (entity.getDescription() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getDescription());
        }
        final int _tmp = entity.isAdaFriendly() ? 1 : 0;
        statement.bindLong(5, _tmp);
        final Long _tmp_1 = DateConverter.dateToTimestamp(entity.getCreatedDate());
        if (_tmp_1 == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, _tmp_1);
        }
        statement.bindLong(7, entity.getItemId());
      }
    };
    this.__preparedStmtOfDeleteItemById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM items WHERE item_id = ?";
        return _query;
      }
    };
  }

  @Override
  public long insertItem(final ItemEntity item) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      final long _result = __insertionAdapterOfItemEntity.insertAndReturnId(item);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public long[] insertItems(final List<ItemEntity> items) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      final long[] _result = __insertionAdapterOfItemEntity.insertAndReturnIdsArray(items);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public int deleteItem(final ItemEntity item) {
    __db.assertNotSuspendingTransaction();
    int _total = 0;
    __db.beginTransaction();
    try {
      _total += __deletionAdapterOfItemEntity.handle(item);
      __db.setTransactionSuccessful();
      return _total;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public int updateItem(final ItemEntity item) {
    __db.assertNotSuspendingTransaction();
    int _total = 0;
    __db.beginTransaction();
    try {
      _total += __updateAdapterOfItemEntity.handle(item);
      __db.setTransactionSuccessful();
      return _total;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public int deleteItemById(final long itemId) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteItemById.acquire();
    int _argIndex = 1;
    _stmt.bindLong(_argIndex, itemId);
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
      __preparedStmtOfDeleteItemById.release(_stmt);
    }
  }

  @Override
  public LiveData<List<ItemEntity>> getAllItemsLive() {
    final String _sql = "SELECT * FROM items ORDER BY category, name";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"items"}, false, new Callable<List<ItemEntity>>() {
      @Override
      @Nullable
      public List<ItemEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfItemId = CursorUtil.getColumnIndexOrThrow(_cursor, "item_id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfIsAdaFriendly = CursorUtil.getColumnIndexOrThrow(_cursor, "is_ada_friendly");
          final int _cursorIndexOfCreatedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "created_date");
          final List<ItemEntity> _result = new ArrayList<ItemEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ItemEntity _item;
            _item = new ItemEntity();
            final long _tmpItemId;
            _tmpItemId = _cursor.getLong(_cursorIndexOfItemId);
            _item.setItemId(_tmpItemId);
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            _item.setName(_tmpName);
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            _item.setCategory(_tmpCategory);
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            _item.setDescription(_tmpDescription);
            final boolean _tmpIsAdaFriendly;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsAdaFriendly);
            _tmpIsAdaFriendly = _tmp != 0;
            _item.setAdaFriendly(_tmpIsAdaFriendly);
            final Date _tmpCreatedDate;
            final Long _tmp_1;
            if (_cursor.isNull(_cursorIndexOfCreatedDate)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getLong(_cursorIndexOfCreatedDate);
            }
            _tmpCreatedDate = DateConverter.fromTimestamp(_tmp_1);
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
  public List<ItemEntity> getAllItems() {
    final String _sql = "SELECT * FROM items ORDER BY category, name";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfItemId = CursorUtil.getColumnIndexOrThrow(_cursor, "item_id");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
      final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
      final int _cursorIndexOfIsAdaFriendly = CursorUtil.getColumnIndexOrThrow(_cursor, "is_ada_friendly");
      final int _cursorIndexOfCreatedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "created_date");
      final List<ItemEntity> _result = new ArrayList<ItemEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final ItemEntity _item;
        _item = new ItemEntity();
        final long _tmpItemId;
        _tmpItemId = _cursor.getLong(_cursorIndexOfItemId);
        _item.setItemId(_tmpItemId);
        final String _tmpName;
        if (_cursor.isNull(_cursorIndexOfName)) {
          _tmpName = null;
        } else {
          _tmpName = _cursor.getString(_cursorIndexOfName);
        }
        _item.setName(_tmpName);
        final String _tmpCategory;
        if (_cursor.isNull(_cursorIndexOfCategory)) {
          _tmpCategory = null;
        } else {
          _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
        }
        _item.setCategory(_tmpCategory);
        final String _tmpDescription;
        if (_cursor.isNull(_cursorIndexOfDescription)) {
          _tmpDescription = null;
        } else {
          _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
        }
        _item.setDescription(_tmpDescription);
        final boolean _tmpIsAdaFriendly;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsAdaFriendly);
        _tmpIsAdaFriendly = _tmp != 0;
        _item.setAdaFriendly(_tmpIsAdaFriendly);
        final Date _tmpCreatedDate;
        final Long _tmp_1;
        if (_cursor.isNull(_cursorIndexOfCreatedDate)) {
          _tmp_1 = null;
        } else {
          _tmp_1 = _cursor.getLong(_cursorIndexOfCreatedDate);
        }
        _tmpCreatedDate = DateConverter.fromTimestamp(_tmp_1);
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
  public ItemEntity getItemById(final long itemId) {
    final String _sql = "SELECT * FROM items WHERE item_id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, itemId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfItemId = CursorUtil.getColumnIndexOrThrow(_cursor, "item_id");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
      final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
      final int _cursorIndexOfIsAdaFriendly = CursorUtil.getColumnIndexOrThrow(_cursor, "is_ada_friendly");
      final int _cursorIndexOfCreatedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "created_date");
      final ItemEntity _result;
      if (_cursor.moveToFirst()) {
        _result = new ItemEntity();
        final long _tmpItemId;
        _tmpItemId = _cursor.getLong(_cursorIndexOfItemId);
        _result.setItemId(_tmpItemId);
        final String _tmpName;
        if (_cursor.isNull(_cursorIndexOfName)) {
          _tmpName = null;
        } else {
          _tmpName = _cursor.getString(_cursorIndexOfName);
        }
        _result.setName(_tmpName);
        final String _tmpCategory;
        if (_cursor.isNull(_cursorIndexOfCategory)) {
          _tmpCategory = null;
        } else {
          _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
        }
        _result.setCategory(_tmpCategory);
        final String _tmpDescription;
        if (_cursor.isNull(_cursorIndexOfDescription)) {
          _tmpDescription = null;
        } else {
          _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
        }
        _result.setDescription(_tmpDescription);
        final boolean _tmpIsAdaFriendly;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsAdaFriendly);
        _tmpIsAdaFriendly = _tmp != 0;
        _result.setAdaFriendly(_tmpIsAdaFriendly);
        final Date _tmpCreatedDate;
        final Long _tmp_1;
        if (_cursor.isNull(_cursorIndexOfCreatedDate)) {
          _tmp_1 = null;
        } else {
          _tmp_1 = _cursor.getLong(_cursorIndexOfCreatedDate);
        }
        _tmpCreatedDate = DateConverter.fromTimestamp(_tmp_1);
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
  public LiveData<List<ItemEntity>> getItemsByCategoryLive(final String category) {
    final String _sql = "SELECT * FROM items WHERE category = ? ORDER BY name";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (category == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, category);
    }
    return __db.getInvalidationTracker().createLiveData(new String[] {"items"}, false, new Callable<List<ItemEntity>>() {
      @Override
      @Nullable
      public List<ItemEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfItemId = CursorUtil.getColumnIndexOrThrow(_cursor, "item_id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfIsAdaFriendly = CursorUtil.getColumnIndexOrThrow(_cursor, "is_ada_friendly");
          final int _cursorIndexOfCreatedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "created_date");
          final List<ItemEntity> _result = new ArrayList<ItemEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ItemEntity _item;
            _item = new ItemEntity();
            final long _tmpItemId;
            _tmpItemId = _cursor.getLong(_cursorIndexOfItemId);
            _item.setItemId(_tmpItemId);
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            _item.setName(_tmpName);
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            _item.setCategory(_tmpCategory);
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            _item.setDescription(_tmpDescription);
            final boolean _tmpIsAdaFriendly;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsAdaFriendly);
            _tmpIsAdaFriendly = _tmp != 0;
            _item.setAdaFriendly(_tmpIsAdaFriendly);
            final Date _tmpCreatedDate;
            final Long _tmp_1;
            if (_cursor.isNull(_cursorIndexOfCreatedDate)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getLong(_cursorIndexOfCreatedDate);
            }
            _tmpCreatedDate = DateConverter.fromTimestamp(_tmp_1);
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
  public List<ItemEntity> getItemsByCategory(final String category) {
    final String _sql = "SELECT * FROM items WHERE category = ? ORDER BY name";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (category == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, category);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfItemId = CursorUtil.getColumnIndexOrThrow(_cursor, "item_id");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
      final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
      final int _cursorIndexOfIsAdaFriendly = CursorUtil.getColumnIndexOrThrow(_cursor, "is_ada_friendly");
      final int _cursorIndexOfCreatedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "created_date");
      final List<ItemEntity> _result = new ArrayList<ItemEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final ItemEntity _item;
        _item = new ItemEntity();
        final long _tmpItemId;
        _tmpItemId = _cursor.getLong(_cursorIndexOfItemId);
        _item.setItemId(_tmpItemId);
        final String _tmpName;
        if (_cursor.isNull(_cursorIndexOfName)) {
          _tmpName = null;
        } else {
          _tmpName = _cursor.getString(_cursorIndexOfName);
        }
        _item.setName(_tmpName);
        final String _tmpCategory;
        if (_cursor.isNull(_cursorIndexOfCategory)) {
          _tmpCategory = null;
        } else {
          _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
        }
        _item.setCategory(_tmpCategory);
        final String _tmpDescription;
        if (_cursor.isNull(_cursorIndexOfDescription)) {
          _tmpDescription = null;
        } else {
          _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
        }
        _item.setDescription(_tmpDescription);
        final boolean _tmpIsAdaFriendly;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsAdaFriendly);
        _tmpIsAdaFriendly = _tmp != 0;
        _item.setAdaFriendly(_tmpIsAdaFriendly);
        final Date _tmpCreatedDate;
        final Long _tmp_1;
        if (_cursor.isNull(_cursorIndexOfCreatedDate)) {
          _tmp_1 = null;
        } else {
          _tmp_1 = _cursor.getLong(_cursorIndexOfCreatedDate);
        }
        _tmpCreatedDate = DateConverter.fromTimestamp(_tmp_1);
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
  public LiveData<List<ItemEntity>> getAdaItemsByCategoryLive(final String category) {
    final String _sql = "SELECT * FROM items WHERE category = ? AND is_ada_friendly = 1 ORDER BY name";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (category == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, category);
    }
    return __db.getInvalidationTracker().createLiveData(new String[] {"items"}, false, new Callable<List<ItemEntity>>() {
      @Override
      @Nullable
      public List<ItemEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfItemId = CursorUtil.getColumnIndexOrThrow(_cursor, "item_id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfIsAdaFriendly = CursorUtil.getColumnIndexOrThrow(_cursor, "is_ada_friendly");
          final int _cursorIndexOfCreatedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "created_date");
          final List<ItemEntity> _result = new ArrayList<ItemEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ItemEntity _item;
            _item = new ItemEntity();
            final long _tmpItemId;
            _tmpItemId = _cursor.getLong(_cursorIndexOfItemId);
            _item.setItemId(_tmpItemId);
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            _item.setName(_tmpName);
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            _item.setCategory(_tmpCategory);
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            _item.setDescription(_tmpDescription);
            final boolean _tmpIsAdaFriendly;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsAdaFriendly);
            _tmpIsAdaFriendly = _tmp != 0;
            _item.setAdaFriendly(_tmpIsAdaFriendly);
            final Date _tmpCreatedDate;
            final Long _tmp_1;
            if (_cursor.isNull(_cursorIndexOfCreatedDate)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getLong(_cursorIndexOfCreatedDate);
            }
            _tmpCreatedDate = DateConverter.fromTimestamp(_tmp_1);
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
  public List<ItemEntity> getAdaItemsByCategory(final String category) {
    final String _sql = "SELECT * FROM items WHERE category = ? AND is_ada_friendly = 1 ORDER BY name";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (category == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, category);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfItemId = CursorUtil.getColumnIndexOrThrow(_cursor, "item_id");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
      final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
      final int _cursorIndexOfIsAdaFriendly = CursorUtil.getColumnIndexOrThrow(_cursor, "is_ada_friendly");
      final int _cursorIndexOfCreatedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "created_date");
      final List<ItemEntity> _result = new ArrayList<ItemEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final ItemEntity _item;
        _item = new ItemEntity();
        final long _tmpItemId;
        _tmpItemId = _cursor.getLong(_cursorIndexOfItemId);
        _item.setItemId(_tmpItemId);
        final String _tmpName;
        if (_cursor.isNull(_cursorIndexOfName)) {
          _tmpName = null;
        } else {
          _tmpName = _cursor.getString(_cursorIndexOfName);
        }
        _item.setName(_tmpName);
        final String _tmpCategory;
        if (_cursor.isNull(_cursorIndexOfCategory)) {
          _tmpCategory = null;
        } else {
          _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
        }
        _item.setCategory(_tmpCategory);
        final String _tmpDescription;
        if (_cursor.isNull(_cursorIndexOfDescription)) {
          _tmpDescription = null;
        } else {
          _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
        }
        _item.setDescription(_tmpDescription);
        final boolean _tmpIsAdaFriendly;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsAdaFriendly);
        _tmpIsAdaFriendly = _tmp != 0;
        _item.setAdaFriendly(_tmpIsAdaFriendly);
        final Date _tmpCreatedDate;
        final Long _tmp_1;
        if (_cursor.isNull(_cursorIndexOfCreatedDate)) {
          _tmp_1 = null;
        } else {
          _tmp_1 = _cursor.getLong(_cursorIndexOfCreatedDate);
        }
        _tmpCreatedDate = DateConverter.fromTimestamp(_tmp_1);
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
  public LiveData<List<ItemEntity>> getAllAdaItemsLive() {
    final String _sql = "SELECT * FROM items WHERE is_ada_friendly = 1 ORDER BY category, name";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"items"}, false, new Callable<List<ItemEntity>>() {
      @Override
      @Nullable
      public List<ItemEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfItemId = CursorUtil.getColumnIndexOrThrow(_cursor, "item_id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfIsAdaFriendly = CursorUtil.getColumnIndexOrThrow(_cursor, "is_ada_friendly");
          final int _cursorIndexOfCreatedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "created_date");
          final List<ItemEntity> _result = new ArrayList<ItemEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ItemEntity _item;
            _item = new ItemEntity();
            final long _tmpItemId;
            _tmpItemId = _cursor.getLong(_cursorIndexOfItemId);
            _item.setItemId(_tmpItemId);
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            _item.setName(_tmpName);
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            _item.setCategory(_tmpCategory);
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            _item.setDescription(_tmpDescription);
            final boolean _tmpIsAdaFriendly;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsAdaFriendly);
            _tmpIsAdaFriendly = _tmp != 0;
            _item.setAdaFriendly(_tmpIsAdaFriendly);
            final Date _tmpCreatedDate;
            final Long _tmp_1;
            if (_cursor.isNull(_cursorIndexOfCreatedDate)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getLong(_cursorIndexOfCreatedDate);
            }
            _tmpCreatedDate = DateConverter.fromTimestamp(_tmp_1);
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
  public LiveData<List<ItemEntity>> searchItemsLive(final String searchTerm) {
    final String _sql = "SELECT * FROM items WHERE LOWER(name) LIKE LOWER(?) OR LOWER(category) LIKE LOWER(?) ORDER BY category, name";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (searchTerm == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, searchTerm);
    }
    _argIndex = 2;
    if (searchTerm == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, searchTerm);
    }
    return __db.getInvalidationTracker().createLiveData(new String[] {"items"}, false, new Callable<List<ItemEntity>>() {
      @Override
      @Nullable
      public List<ItemEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfItemId = CursorUtil.getColumnIndexOrThrow(_cursor, "item_id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfIsAdaFriendly = CursorUtil.getColumnIndexOrThrow(_cursor, "is_ada_friendly");
          final int _cursorIndexOfCreatedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "created_date");
          final List<ItemEntity> _result = new ArrayList<ItemEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ItemEntity _item;
            _item = new ItemEntity();
            final long _tmpItemId;
            _tmpItemId = _cursor.getLong(_cursorIndexOfItemId);
            _item.setItemId(_tmpItemId);
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            _item.setName(_tmpName);
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            _item.setCategory(_tmpCategory);
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            _item.setDescription(_tmpDescription);
            final boolean _tmpIsAdaFriendly;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsAdaFriendly);
            _tmpIsAdaFriendly = _tmp != 0;
            _item.setAdaFriendly(_tmpIsAdaFriendly);
            final Date _tmpCreatedDate;
            final Long _tmp_1;
            if (_cursor.isNull(_cursorIndexOfCreatedDate)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getLong(_cursorIndexOfCreatedDate);
            }
            _tmpCreatedDate = DateConverter.fromTimestamp(_tmp_1);
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
  public List<ItemEntity> searchItems(final String searchTerm) {
    final String _sql = "SELECT * FROM items WHERE LOWER(name) LIKE LOWER(?) OR LOWER(category) LIKE LOWER(?) ORDER BY category, name";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (searchTerm == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, searchTerm);
    }
    _argIndex = 2;
    if (searchTerm == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, searchTerm);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfItemId = CursorUtil.getColumnIndexOrThrow(_cursor, "item_id");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
      final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
      final int _cursorIndexOfIsAdaFriendly = CursorUtil.getColumnIndexOrThrow(_cursor, "is_ada_friendly");
      final int _cursorIndexOfCreatedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "created_date");
      final List<ItemEntity> _result = new ArrayList<ItemEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final ItemEntity _item;
        _item = new ItemEntity();
        final long _tmpItemId;
        _tmpItemId = _cursor.getLong(_cursorIndexOfItemId);
        _item.setItemId(_tmpItemId);
        final String _tmpName;
        if (_cursor.isNull(_cursorIndexOfName)) {
          _tmpName = null;
        } else {
          _tmpName = _cursor.getString(_cursorIndexOfName);
        }
        _item.setName(_tmpName);
        final String _tmpCategory;
        if (_cursor.isNull(_cursorIndexOfCategory)) {
          _tmpCategory = null;
        } else {
          _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
        }
        _item.setCategory(_tmpCategory);
        final String _tmpDescription;
        if (_cursor.isNull(_cursorIndexOfDescription)) {
          _tmpDescription = null;
        } else {
          _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
        }
        _item.setDescription(_tmpDescription);
        final boolean _tmpIsAdaFriendly;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsAdaFriendly);
        _tmpIsAdaFriendly = _tmp != 0;
        _item.setAdaFriendly(_tmpIsAdaFriendly);
        final Date _tmpCreatedDate;
        final Long _tmp_1;
        if (_cursor.isNull(_cursorIndexOfCreatedDate)) {
          _tmp_1 = null;
        } else {
          _tmp_1 = _cursor.getLong(_cursorIndexOfCreatedDate);
        }
        _tmpCreatedDate = DateConverter.fromTimestamp(_tmp_1);
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
  public LiveData<List<String>> getAllCategoriesLive() {
    final String _sql = "SELECT DISTINCT category FROM items ORDER BY category";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"items"}, false, new Callable<List<String>>() {
      @Override
      @Nullable
      public List<String> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final List<String> _result = new ArrayList<String>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final String _item;
            if (_cursor.isNull(0)) {
              _item = null;
            } else {
              _item = _cursor.getString(0);
            }
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
  public List<String> getAllCategories() {
    final String _sql = "SELECT DISTINCT category FROM items ORDER BY category";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final List<String> _result = new ArrayList<String>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final String _item;
        if (_cursor.isNull(0)) {
          _item = null;
        } else {
          _item = _cursor.getString(0);
        }
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public LiveData<Integer> getItemCountLive() {
    final String _sql = "SELECT COUNT(*) FROM items";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"items"}, false, new Callable<Integer>() {
      @Override
      @Nullable
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final Integer _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(0);
            }
            _result = _tmp;
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
  public int getItemCount() {
    final String _sql = "SELECT COUNT(*) FROM items";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
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

  @Override
  public int countByNameAndCategory(final String name, final String category) {
    final String _sql = "SELECT COUNT(*) FROM items WHERE LOWER(name) = LOWER(?) AND LOWER(category) = LOWER(?)";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (name == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, name);
    }
    _argIndex = 2;
    if (category == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, category);
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
