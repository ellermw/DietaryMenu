package com.hospital.dietary.data.dao;

import androidx.annotation.NonNull;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.hospital.dietary.data.entities.FinalizedOrderEntity;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"unchecked", "deprecation"})
public final class FinalizedOrderDao_Impl implements FinalizedOrderDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<FinalizedOrderEntity> __insertionAdapterOfFinalizedOrderEntity;

  public FinalizedOrderDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfFinalizedOrderEntity = new EntityInsertionAdapter<FinalizedOrderEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `finalized_order` (`order_id`,`patient_name`,`wing`,`room`,`order_date`,`diet_type`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final FinalizedOrderEntity entity) {
        statement.bindLong(1, entity.getOrderId());
        if (entity.getPatientName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getPatientName());
        }
        if (entity.getWing() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getWing());
        }
        if (entity.getRoom() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getRoom());
        }
        if (entity.getOrderDate() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getOrderDate());
        }
        if (entity.getDietType() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getDietType());
        }
      }
    };
  }

  @Override
  public long insertFinalizedOrder(final FinalizedOrderEntity order) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      final long _result = __insertionAdapterOfFinalizedOrderEntity.insertAndReturnId(order);
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
