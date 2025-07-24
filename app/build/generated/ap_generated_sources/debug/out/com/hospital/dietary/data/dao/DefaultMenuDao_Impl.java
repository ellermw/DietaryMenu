package com.hospital.dietary.data.dao;

import androidx.annotation.NonNull;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.hospital.dietary.data.entities.DefaultMenuEntity;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"unchecked", "deprecation"})
public final class DefaultMenuDao_Impl implements DefaultMenuDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<DefaultMenuEntity> __insertionAdapterOfDefaultMenuEntity;

  public DefaultMenuDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfDefaultMenuEntity = new EntityInsertionAdapter<DefaultMenuEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `default_menu` (`menu_id`,`diet_type`,`meal_type`,`day_of_week`,`item_name`,`item_category`,`is_active`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final DefaultMenuEntity entity) {
        statement.bindLong(1, entity.getMenuId());
        if (entity.dietType == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.dietType);
        }
        if (entity.mealType == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.mealType);
        }
        if (entity.dayOfWeek == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.dayOfWeek);
        }
        if (entity.itemName == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.itemName);
        }
        if (entity.itemCategory == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.itemCategory);
        }
        final int _tmp = entity.isActive ? 1 : 0;
        statement.bindLong(7, _tmp);
      }
    };
  }

  @Override
  public long insertDefaultMenuItem(final DefaultMenuEntity item) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      final long _result = __insertionAdapterOfDefaultMenuEntity.insertAndReturnId(item);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
