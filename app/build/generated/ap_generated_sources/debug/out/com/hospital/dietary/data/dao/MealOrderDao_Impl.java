package com.hospital.dietary.data.dao;

import androidx.annotation.NonNull;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.hospital.dietary.data.database.DateConverter;
import com.hospital.dietary.data.entities.MealOrderEntity;
import java.lang.Class;
import java.lang.Long;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"unchecked", "deprecation"})
public final class MealOrderDao_Impl implements MealOrderDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<MealOrderEntity> __insertionAdapterOfMealOrderEntity;

  public MealOrderDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfMealOrderEntity = new EntityInsertionAdapter<MealOrderEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `meal_orders` (`order_id`,`patient_id`,`meal`,`order_date`,`is_complete`,`created_by`,`timestamp`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final MealOrderEntity entity) {
        statement.bindLong(1, entity.getOrderId());
        statement.bindLong(2, entity.getPatientId());
        if (entity.getMeal() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getMeal());
        }
        final Long _tmp = DateConverter.dateToTimestamp(entity.getOrderDate());
        if (_tmp == null) {
          statement.bindNull(4);
        } else {
          statement.bindLong(4, _tmp);
        }
        final int _tmp_1 = entity.isComplete() ? 1 : 0;
        statement.bindLong(5, _tmp_1);
        if (entity.getCreatedBy() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getCreatedBy());
        }
        final Long _tmp_2 = DateConverter.dateToTimestamp(entity.getTimestamp());
        if (_tmp_2 == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, _tmp_2);
        }
      }
    };
  }

  @Override
  public long insertMealOrder(final MealOrderEntity order) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      final long _result = __insertionAdapterOfMealOrderEntity.insertAndReturnId(order);
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
