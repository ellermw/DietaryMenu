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
import com.hospital.dietary.data.entities.PatientEntity;
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
public final class PatientDao_Impl implements PatientDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<PatientEntity> __insertionAdapterOfPatientEntity;

  private final EntityDeletionOrUpdateAdapter<PatientEntity> __deletionAdapterOfPatientEntity;

  private final EntityDeletionOrUpdateAdapter<PatientEntity> __updateAdapterOfPatientEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdateBreakfastComplete;

  private final SharedSQLiteStatement __preparedStmtOfUpdateLunchComplete;

  private final SharedSQLiteStatement __preparedStmtOfUpdateDinnerComplete;

  private final SharedSQLiteStatement __preparedStmtOfUpdateModifiedDate;

  private final SharedSQLiteStatement __preparedStmtOfDeletePatientById;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAllPatients;

  private final SharedSQLiteStatement __preparedStmtOfUpdateMealCompletion;

  private final SharedSQLiteStatement __preparedStmtOfUpdateBreakfastItems;

  private final SharedSQLiteStatement __preparedStmtOfUpdateLunchItems;

  private final SharedSQLiteStatement __preparedStmtOfUpdateDinnerItems;

  public PatientDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPatientEntity = new EntityInsertionAdapter<PatientEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `patient_info` (`patient_id`,`patient_first_name`,`patient_last_name`,`wing`,`room_number`,`diet_type`,`diet`,`ada_diet`,`fluid_restriction`,`texture_modifications`,`mechanical_chopped`,`mechanical_ground`,`bite_size`,`bread_ok`,`nectar_thick`,`pudding_thick`,`honey_thick`,`extra_gravy`,`meats_only`,`breakfast_complete`,`lunch_complete`,`dinner_complete`,`breakfast_npo`,`lunch_npo`,`dinner_npo`,`breakfast_items`,`lunch_items`,`dinner_items`,`breakfast_juices`,`lunch_juices`,`dinner_juices`,`breakfast_drinks`,`lunch_drinks`,`dinner_drinks`,`created_date`,`modified_date`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final PatientEntity entity) {
        statement.bindLong(1, entity.getPatientId());
        if (entity.getPatientFirstName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getPatientFirstName());
        }
        if (entity.getPatientLastName() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getPatientLastName());
        }
        if (entity.getWing() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getWing());
        }
        if (entity.getRoomNumber() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getRoomNumber());
        }
        if (entity.getDietType() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getDietType());
        }
        if (entity.getDiet() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getDiet());
        }
        final int _tmp = entity.isAdaDiet() ? 1 : 0;
        statement.bindLong(8, _tmp);
        if (entity.getFluidRestriction() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getFluidRestriction());
        }
        if (entity.getTextureModifications() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getTextureModifications());
        }
        final int _tmp_1 = entity.isMechanicalChopped() ? 1 : 0;
        statement.bindLong(11, _tmp_1);
        final int _tmp_2 = entity.isMechanicalGround() ? 1 : 0;
        statement.bindLong(12, _tmp_2);
        final int _tmp_3 = entity.isBiteSize() ? 1 : 0;
        statement.bindLong(13, _tmp_3);
        final int _tmp_4 = entity.isBreadOK() ? 1 : 0;
        statement.bindLong(14, _tmp_4);
        final int _tmp_5 = entity.isNectarThick() ? 1 : 0;
        statement.bindLong(15, _tmp_5);
        final int _tmp_6 = entity.isPuddingThick() ? 1 : 0;
        statement.bindLong(16, _tmp_6);
        final int _tmp_7 = entity.isHoneyThick() ? 1 : 0;
        statement.bindLong(17, _tmp_7);
        final int _tmp_8 = entity.isExtraGravy() ? 1 : 0;
        statement.bindLong(18, _tmp_8);
        final int _tmp_9 = entity.isMeatsOnly() ? 1 : 0;
        statement.bindLong(19, _tmp_9);
        final int _tmp_10 = entity.isBreakfastComplete() ? 1 : 0;
        statement.bindLong(20, _tmp_10);
        final int _tmp_11 = entity.isLunchComplete() ? 1 : 0;
        statement.bindLong(21, _tmp_11);
        final int _tmp_12 = entity.isDinnerComplete() ? 1 : 0;
        statement.bindLong(22, _tmp_12);
        final int _tmp_13 = entity.isBreakfastNPO() ? 1 : 0;
        statement.bindLong(23, _tmp_13);
        final int _tmp_14 = entity.isLunchNPO() ? 1 : 0;
        statement.bindLong(24, _tmp_14);
        final int _tmp_15 = entity.isDinnerNPO() ? 1 : 0;
        statement.bindLong(25, _tmp_15);
        if (entity.getBreakfastItems() == null) {
          statement.bindNull(26);
        } else {
          statement.bindString(26, entity.getBreakfastItems());
        }
        if (entity.getLunchItems() == null) {
          statement.bindNull(27);
        } else {
          statement.bindString(27, entity.getLunchItems());
        }
        if (entity.getDinnerItems() == null) {
          statement.bindNull(28);
        } else {
          statement.bindString(28, entity.getDinnerItems());
        }
        if (entity.getBreakfastJuices() == null) {
          statement.bindNull(29);
        } else {
          statement.bindString(29, entity.getBreakfastJuices());
        }
        if (entity.getLunchJuices() == null) {
          statement.bindNull(30);
        } else {
          statement.bindString(30, entity.getLunchJuices());
        }
        if (entity.getDinnerJuices() == null) {
          statement.bindNull(31);
        } else {
          statement.bindString(31, entity.getDinnerJuices());
        }
        if (entity.getBreakfastDrinks() == null) {
          statement.bindNull(32);
        } else {
          statement.bindString(32, entity.getBreakfastDrinks());
        }
        if (entity.getLunchDrinks() == null) {
          statement.bindNull(33);
        } else {
          statement.bindString(33, entity.getLunchDrinks());
        }
        if (entity.getDinnerDrinks() == null) {
          statement.bindNull(34);
        } else {
          statement.bindString(34, entity.getDinnerDrinks());
        }
        final Long _tmp_16 = DateConverter.dateToTimestamp(entity.getCreatedDate());
        if (_tmp_16 == null) {
          statement.bindNull(35);
        } else {
          statement.bindLong(35, _tmp_16);
        }
        final Long _tmp_17 = DateConverter.dateToTimestamp(entity.getModifiedDate());
        if (_tmp_17 == null) {
          statement.bindNull(36);
        } else {
          statement.bindLong(36, _tmp_17);
        }
      }
    };
    this.__deletionAdapterOfPatientEntity = new EntityDeletionOrUpdateAdapter<PatientEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `patient_info` WHERE `patient_id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final PatientEntity entity) {
        statement.bindLong(1, entity.getPatientId());
      }
    };
    this.__updateAdapterOfPatientEntity = new EntityDeletionOrUpdateAdapter<PatientEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `patient_info` SET `patient_id` = ?,`patient_first_name` = ?,`patient_last_name` = ?,`wing` = ?,`room_number` = ?,`diet_type` = ?,`diet` = ?,`ada_diet` = ?,`fluid_restriction` = ?,`texture_modifications` = ?,`mechanical_chopped` = ?,`mechanical_ground` = ?,`bite_size` = ?,`bread_ok` = ?,`nectar_thick` = ?,`pudding_thick` = ?,`honey_thick` = ?,`extra_gravy` = ?,`meats_only` = ?,`breakfast_complete` = ?,`lunch_complete` = ?,`dinner_complete` = ?,`breakfast_npo` = ?,`lunch_npo` = ?,`dinner_npo` = ?,`breakfast_items` = ?,`lunch_items` = ?,`dinner_items` = ?,`breakfast_juices` = ?,`lunch_juices` = ?,`dinner_juices` = ?,`breakfast_drinks` = ?,`lunch_drinks` = ?,`dinner_drinks` = ?,`created_date` = ?,`modified_date` = ? WHERE `patient_id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final PatientEntity entity) {
        statement.bindLong(1, entity.getPatientId());
        if (entity.getPatientFirstName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getPatientFirstName());
        }
        if (entity.getPatientLastName() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getPatientLastName());
        }
        if (entity.getWing() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getWing());
        }
        if (entity.getRoomNumber() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getRoomNumber());
        }
        if (entity.getDietType() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getDietType());
        }
        if (entity.getDiet() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getDiet());
        }
        final int _tmp = entity.isAdaDiet() ? 1 : 0;
        statement.bindLong(8, _tmp);
        if (entity.getFluidRestriction() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getFluidRestriction());
        }
        if (entity.getTextureModifications() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getTextureModifications());
        }
        final int _tmp_1 = entity.isMechanicalChopped() ? 1 : 0;
        statement.bindLong(11, _tmp_1);
        final int _tmp_2 = entity.isMechanicalGround() ? 1 : 0;
        statement.bindLong(12, _tmp_2);
        final int _tmp_3 = entity.isBiteSize() ? 1 : 0;
        statement.bindLong(13, _tmp_3);
        final int _tmp_4 = entity.isBreadOK() ? 1 : 0;
        statement.bindLong(14, _tmp_4);
        final int _tmp_5 = entity.isNectarThick() ? 1 : 0;
        statement.bindLong(15, _tmp_5);
        final int _tmp_6 = entity.isPuddingThick() ? 1 : 0;
        statement.bindLong(16, _tmp_6);
        final int _tmp_7 = entity.isHoneyThick() ? 1 : 0;
        statement.bindLong(17, _tmp_7);
        final int _tmp_8 = entity.isExtraGravy() ? 1 : 0;
        statement.bindLong(18, _tmp_8);
        final int _tmp_9 = entity.isMeatsOnly() ? 1 : 0;
        statement.bindLong(19, _tmp_9);
        final int _tmp_10 = entity.isBreakfastComplete() ? 1 : 0;
        statement.bindLong(20, _tmp_10);
        final int _tmp_11 = entity.isLunchComplete() ? 1 : 0;
        statement.bindLong(21, _tmp_11);
        final int _tmp_12 = entity.isDinnerComplete() ? 1 : 0;
        statement.bindLong(22, _tmp_12);
        final int _tmp_13 = entity.isBreakfastNPO() ? 1 : 0;
        statement.bindLong(23, _tmp_13);
        final int _tmp_14 = entity.isLunchNPO() ? 1 : 0;
        statement.bindLong(24, _tmp_14);
        final int _tmp_15 = entity.isDinnerNPO() ? 1 : 0;
        statement.bindLong(25, _tmp_15);
        if (entity.getBreakfastItems() == null) {
          statement.bindNull(26);
        } else {
          statement.bindString(26, entity.getBreakfastItems());
        }
        if (entity.getLunchItems() == null) {
          statement.bindNull(27);
        } else {
          statement.bindString(27, entity.getLunchItems());
        }
        if (entity.getDinnerItems() == null) {
          statement.bindNull(28);
        } else {
          statement.bindString(28, entity.getDinnerItems());
        }
        if (entity.getBreakfastJuices() == null) {
          statement.bindNull(29);
        } else {
          statement.bindString(29, entity.getBreakfastJuices());
        }
        if (entity.getLunchJuices() == null) {
          statement.bindNull(30);
        } else {
          statement.bindString(30, entity.getLunchJuices());
        }
        if (entity.getDinnerJuices() == null) {
          statement.bindNull(31);
        } else {
          statement.bindString(31, entity.getDinnerJuices());
        }
        if (entity.getBreakfastDrinks() == null) {
          statement.bindNull(32);
        } else {
          statement.bindString(32, entity.getBreakfastDrinks());
        }
        if (entity.getLunchDrinks() == null) {
          statement.bindNull(33);
        } else {
          statement.bindString(33, entity.getLunchDrinks());
        }
        if (entity.getDinnerDrinks() == null) {
          statement.bindNull(34);
        } else {
          statement.bindString(34, entity.getDinnerDrinks());
        }
        final Long _tmp_16 = DateConverter.dateToTimestamp(entity.getCreatedDate());
        if (_tmp_16 == null) {
          statement.bindNull(35);
        } else {
          statement.bindLong(35, _tmp_16);
        }
        final Long _tmp_17 = DateConverter.dateToTimestamp(entity.getModifiedDate());
        if (_tmp_17 == null) {
          statement.bindNull(36);
        } else {
          statement.bindLong(36, _tmp_17);
        }
        statement.bindLong(37, entity.getPatientId());
      }
    };
    this.__preparedStmtOfUpdateBreakfastComplete = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE patient_info SET breakfast_complete = ? WHERE patient_id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateLunchComplete = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE patient_info SET lunch_complete = ? WHERE patient_id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateDinnerComplete = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE patient_info SET dinner_complete = ? WHERE patient_id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateModifiedDate = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE patient_info SET modified_date = CURRENT_TIMESTAMP WHERE patient_id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeletePatientById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM patient_info WHERE patient_id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAllPatients = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM patient_info";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateMealCompletion = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE patient_info SET breakfast_complete = ?, lunch_complete = ?, dinner_complete = ?, modified_date = CURRENT_TIMESTAMP WHERE patient_id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateBreakfastItems = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE patient_info SET breakfast_items = ?, breakfast_juices = ?, breakfast_drinks = ?, modified_date = CURRENT_TIMESTAMP WHERE patient_id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateLunchItems = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE patient_info SET lunch_items = ?, lunch_juices = ?, lunch_drinks = ?, modified_date = CURRENT_TIMESTAMP WHERE patient_id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateDinnerItems = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE patient_info SET dinner_items = ?, dinner_juices = ?, dinner_drinks = ?, modified_date = CURRENT_TIMESTAMP WHERE patient_id = ?";
        return _query;
      }
    };
  }

  @Override
  public long insertPatient(final PatientEntity patient) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      final long _result = __insertionAdapterOfPatientEntity.insertAndReturnId(patient);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public long[] insertPatients(final List<PatientEntity> patients) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      final long[] _result = __insertionAdapterOfPatientEntity.insertAndReturnIdsArray(patients);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public int deletePatient(final PatientEntity patient) {
    __db.assertNotSuspendingTransaction();
    int _total = 0;
    __db.beginTransaction();
    try {
      _total += __deletionAdapterOfPatientEntity.handle(patient);
      __db.setTransactionSuccessful();
      return _total;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public int updatePatient(final PatientEntity patient) {
    __db.assertNotSuspendingTransaction();
    int _total = 0;
    __db.beginTransaction();
    try {
      _total += __updateAdapterOfPatientEntity.handle(patient);
      __db.setTransactionSuccessful();
      return _total;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public int updateBreakfastComplete(final long patientId, final boolean complete) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateBreakfastComplete.acquire();
    int _argIndex = 1;
    final int _tmp = complete ? 1 : 0;
    _stmt.bindLong(_argIndex, _tmp);
    _argIndex = 2;
    _stmt.bindLong(_argIndex, patientId);
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
      __preparedStmtOfUpdateBreakfastComplete.release(_stmt);
    }
  }

  @Override
  public int updateLunchComplete(final long patientId, final boolean complete) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateLunchComplete.acquire();
    int _argIndex = 1;
    final int _tmp = complete ? 1 : 0;
    _stmt.bindLong(_argIndex, _tmp);
    _argIndex = 2;
    _stmt.bindLong(_argIndex, patientId);
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
      __preparedStmtOfUpdateLunchComplete.release(_stmt);
    }
  }

  @Override
  public int updateDinnerComplete(final long patientId, final boolean complete) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateDinnerComplete.acquire();
    int _argIndex = 1;
    final int _tmp = complete ? 1 : 0;
    _stmt.bindLong(_argIndex, _tmp);
    _argIndex = 2;
    _stmt.bindLong(_argIndex, patientId);
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
      __preparedStmtOfUpdateDinnerComplete.release(_stmt);
    }
  }

  @Override
  public void updateModifiedDate(final long patientId) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateModifiedDate.acquire();
    int _argIndex = 1;
    _stmt.bindLong(_argIndex, patientId);
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfUpdateModifiedDate.release(_stmt);
    }
  }

  @Override
  public int deletePatientById(final long patientId) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeletePatientById.acquire();
    int _argIndex = 1;
    _stmt.bindLong(_argIndex, patientId);
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
      __preparedStmtOfDeletePatientById.release(_stmt);
    }
  }

  @Override
  public void deleteAllPatients() {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAllPatients.acquire();
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfDeleteAllPatients.release(_stmt);
    }
  }

  @Override
  public int updateMealCompletion(final long patientId, final boolean breakfastComplete,
      final boolean lunchComplete, final boolean dinnerComplete) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateMealCompletion.acquire();
    int _argIndex = 1;
    final int _tmp = breakfastComplete ? 1 : 0;
    _stmt.bindLong(_argIndex, _tmp);
    _argIndex = 2;
    final int _tmp_1 = lunchComplete ? 1 : 0;
    _stmt.bindLong(_argIndex, _tmp_1);
    _argIndex = 3;
    final int _tmp_2 = dinnerComplete ? 1 : 0;
    _stmt.bindLong(_argIndex, _tmp_2);
    _argIndex = 4;
    _stmt.bindLong(_argIndex, patientId);
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
      __preparedStmtOfUpdateMealCompletion.release(_stmt);
    }
  }

  @Override
  public int updateBreakfastItems(final long patientId, final String items, final String juices,
      final String drinks) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateBreakfastItems.acquire();
    int _argIndex = 1;
    if (items == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, items);
    }
    _argIndex = 2;
    if (juices == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, juices);
    }
    _argIndex = 3;
    if (drinks == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, drinks);
    }
    _argIndex = 4;
    _stmt.bindLong(_argIndex, patientId);
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
      __preparedStmtOfUpdateBreakfastItems.release(_stmt);
    }
  }

  @Override
  public int updateLunchItems(final long patientId, final String items, final String juices,
      final String drinks) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateLunchItems.acquire();
    int _argIndex = 1;
    if (items == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, items);
    }
    _argIndex = 2;
    if (juices == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, juices);
    }
    _argIndex = 3;
    if (drinks == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, drinks);
    }
    _argIndex = 4;
    _stmt.bindLong(_argIndex, patientId);
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
      __preparedStmtOfUpdateLunchItems.release(_stmt);
    }
  }

  @Override
  public int updateDinnerItems(final long patientId, final String items, final String juices,
      final String drinks) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateDinnerItems.acquire();
    int _argIndex = 1;
    if (items == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, items);
    }
    _argIndex = 2;
    if (juices == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, juices);
    }
    _argIndex = 3;
    if (drinks == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, drinks);
    }
    _argIndex = 4;
    _stmt.bindLong(_argIndex, patientId);
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
      __preparedStmtOfUpdateDinnerItems.release(_stmt);
    }
  }

  @Override
  public LiveData<List<PatientEntity>> getAllPatientsLive() {
    final String _sql = "SELECT * FROM patient_info ORDER BY wing, CAST(room_number AS INTEGER)";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"patient_info"}, false, new Callable<List<PatientEntity>>() {
      @Override
      @Nullable
      public List<PatientEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfPatientId = CursorUtil.getColumnIndexOrThrow(_cursor, "patient_id");
          final int _cursorIndexOfPatientFirstName = CursorUtil.getColumnIndexOrThrow(_cursor, "patient_first_name");
          final int _cursorIndexOfPatientLastName = CursorUtil.getColumnIndexOrThrow(_cursor, "patient_last_name");
          final int _cursorIndexOfWing = CursorUtil.getColumnIndexOrThrow(_cursor, "wing");
          final int _cursorIndexOfRoomNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "room_number");
          final int _cursorIndexOfDietType = CursorUtil.getColumnIndexOrThrow(_cursor, "diet_type");
          final int _cursorIndexOfDiet = CursorUtil.getColumnIndexOrThrow(_cursor, "diet");
          final int _cursorIndexOfAdaDiet = CursorUtil.getColumnIndexOrThrow(_cursor, "ada_diet");
          final int _cursorIndexOfFluidRestriction = CursorUtil.getColumnIndexOrThrow(_cursor, "fluid_restriction");
          final int _cursorIndexOfTextureModifications = CursorUtil.getColumnIndexOrThrow(_cursor, "texture_modifications");
          final int _cursorIndexOfMechanicalChopped = CursorUtil.getColumnIndexOrThrow(_cursor, "mechanical_chopped");
          final int _cursorIndexOfMechanicalGround = CursorUtil.getColumnIndexOrThrow(_cursor, "mechanical_ground");
          final int _cursorIndexOfBiteSize = CursorUtil.getColumnIndexOrThrow(_cursor, "bite_size");
          final int _cursorIndexOfBreadOK = CursorUtil.getColumnIndexOrThrow(_cursor, "bread_ok");
          final int _cursorIndexOfNectarThick = CursorUtil.getColumnIndexOrThrow(_cursor, "nectar_thick");
          final int _cursorIndexOfPuddingThick = CursorUtil.getColumnIndexOrThrow(_cursor, "pudding_thick");
          final int _cursorIndexOfHoneyThick = CursorUtil.getColumnIndexOrThrow(_cursor, "honey_thick");
          final int _cursorIndexOfExtraGravy = CursorUtil.getColumnIndexOrThrow(_cursor, "extra_gravy");
          final int _cursorIndexOfMeatsOnly = CursorUtil.getColumnIndexOrThrow(_cursor, "meats_only");
          final int _cursorIndexOfBreakfastComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_complete");
          final int _cursorIndexOfLunchComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_complete");
          final int _cursorIndexOfDinnerComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_complete");
          final int _cursorIndexOfBreakfastNPO = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_npo");
          final int _cursorIndexOfLunchNPO = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_npo");
          final int _cursorIndexOfDinnerNPO = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_npo");
          final int _cursorIndexOfBreakfastItems = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_items");
          final int _cursorIndexOfLunchItems = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_items");
          final int _cursorIndexOfDinnerItems = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_items");
          final int _cursorIndexOfBreakfastJuices = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_juices");
          final int _cursorIndexOfLunchJuices = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_juices");
          final int _cursorIndexOfDinnerJuices = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_juices");
          final int _cursorIndexOfBreakfastDrinks = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_drinks");
          final int _cursorIndexOfLunchDrinks = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_drinks");
          final int _cursorIndexOfDinnerDrinks = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_drinks");
          final int _cursorIndexOfCreatedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "created_date");
          final int _cursorIndexOfModifiedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "modified_date");
          final List<PatientEntity> _result = new ArrayList<PatientEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PatientEntity _item;
            _item = new PatientEntity();
            final long _tmpPatientId;
            _tmpPatientId = _cursor.getLong(_cursorIndexOfPatientId);
            _item.setPatientId(_tmpPatientId);
            final String _tmpPatientFirstName;
            if (_cursor.isNull(_cursorIndexOfPatientFirstName)) {
              _tmpPatientFirstName = null;
            } else {
              _tmpPatientFirstName = _cursor.getString(_cursorIndexOfPatientFirstName);
            }
            _item.setPatientFirstName(_tmpPatientFirstName);
            final String _tmpPatientLastName;
            if (_cursor.isNull(_cursorIndexOfPatientLastName)) {
              _tmpPatientLastName = null;
            } else {
              _tmpPatientLastName = _cursor.getString(_cursorIndexOfPatientLastName);
            }
            _item.setPatientLastName(_tmpPatientLastName);
            final String _tmpWing;
            if (_cursor.isNull(_cursorIndexOfWing)) {
              _tmpWing = null;
            } else {
              _tmpWing = _cursor.getString(_cursorIndexOfWing);
            }
            _item.setWing(_tmpWing);
            final String _tmpRoomNumber;
            if (_cursor.isNull(_cursorIndexOfRoomNumber)) {
              _tmpRoomNumber = null;
            } else {
              _tmpRoomNumber = _cursor.getString(_cursorIndexOfRoomNumber);
            }
            _item.setRoomNumber(_tmpRoomNumber);
            final String _tmpDietType;
            if (_cursor.isNull(_cursorIndexOfDietType)) {
              _tmpDietType = null;
            } else {
              _tmpDietType = _cursor.getString(_cursorIndexOfDietType);
            }
            _item.setDietType(_tmpDietType);
            final String _tmpDiet;
            if (_cursor.isNull(_cursorIndexOfDiet)) {
              _tmpDiet = null;
            } else {
              _tmpDiet = _cursor.getString(_cursorIndexOfDiet);
            }
            _item.setDiet(_tmpDiet);
            final boolean _tmpAdaDiet;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfAdaDiet);
            _tmpAdaDiet = _tmp != 0;
            _item.setAdaDiet(_tmpAdaDiet);
            final String _tmpFluidRestriction;
            if (_cursor.isNull(_cursorIndexOfFluidRestriction)) {
              _tmpFluidRestriction = null;
            } else {
              _tmpFluidRestriction = _cursor.getString(_cursorIndexOfFluidRestriction);
            }
            _item.setFluidRestriction(_tmpFluidRestriction);
            final String _tmpTextureModifications;
            if (_cursor.isNull(_cursorIndexOfTextureModifications)) {
              _tmpTextureModifications = null;
            } else {
              _tmpTextureModifications = _cursor.getString(_cursorIndexOfTextureModifications);
            }
            _item.setTextureModifications(_tmpTextureModifications);
            final boolean _tmpMechanicalChopped;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfMechanicalChopped);
            _tmpMechanicalChopped = _tmp_1 != 0;
            _item.setMechanicalChopped(_tmpMechanicalChopped);
            final boolean _tmpMechanicalGround;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfMechanicalGround);
            _tmpMechanicalGround = _tmp_2 != 0;
            _item.setMechanicalGround(_tmpMechanicalGround);
            final boolean _tmpBiteSize;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfBiteSize);
            _tmpBiteSize = _tmp_3 != 0;
            _item.setBiteSize(_tmpBiteSize);
            final boolean _tmpBreadOK;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfBreadOK);
            _tmpBreadOK = _tmp_4 != 0;
            _item.setBreadOK(_tmpBreadOK);
            final boolean _tmpNectarThick;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfNectarThick);
            _tmpNectarThick = _tmp_5 != 0;
            _item.setNectarThick(_tmpNectarThick);
            final boolean _tmpPuddingThick;
            final int _tmp_6;
            _tmp_6 = _cursor.getInt(_cursorIndexOfPuddingThick);
            _tmpPuddingThick = _tmp_6 != 0;
            _item.setPuddingThick(_tmpPuddingThick);
            final boolean _tmpHoneyThick;
            final int _tmp_7;
            _tmp_7 = _cursor.getInt(_cursorIndexOfHoneyThick);
            _tmpHoneyThick = _tmp_7 != 0;
            _item.setHoneyThick(_tmpHoneyThick);
            final boolean _tmpExtraGravy;
            final int _tmp_8;
            _tmp_8 = _cursor.getInt(_cursorIndexOfExtraGravy);
            _tmpExtraGravy = _tmp_8 != 0;
            _item.setExtraGravy(_tmpExtraGravy);
            final boolean _tmpMeatsOnly;
            final int _tmp_9;
            _tmp_9 = _cursor.getInt(_cursorIndexOfMeatsOnly);
            _tmpMeatsOnly = _tmp_9 != 0;
            _item.setMeatsOnly(_tmpMeatsOnly);
            final boolean _tmpBreakfastComplete;
            final int _tmp_10;
            _tmp_10 = _cursor.getInt(_cursorIndexOfBreakfastComplete);
            _tmpBreakfastComplete = _tmp_10 != 0;
            _item.setBreakfastComplete(_tmpBreakfastComplete);
            final boolean _tmpLunchComplete;
            final int _tmp_11;
            _tmp_11 = _cursor.getInt(_cursorIndexOfLunchComplete);
            _tmpLunchComplete = _tmp_11 != 0;
            _item.setLunchComplete(_tmpLunchComplete);
            final boolean _tmpDinnerComplete;
            final int _tmp_12;
            _tmp_12 = _cursor.getInt(_cursorIndexOfDinnerComplete);
            _tmpDinnerComplete = _tmp_12 != 0;
            _item.setDinnerComplete(_tmpDinnerComplete);
            final boolean _tmpBreakfastNPO;
            final int _tmp_13;
            _tmp_13 = _cursor.getInt(_cursorIndexOfBreakfastNPO);
            _tmpBreakfastNPO = _tmp_13 != 0;
            _item.setBreakfastNPO(_tmpBreakfastNPO);
            final boolean _tmpLunchNPO;
            final int _tmp_14;
            _tmp_14 = _cursor.getInt(_cursorIndexOfLunchNPO);
            _tmpLunchNPO = _tmp_14 != 0;
            _item.setLunchNPO(_tmpLunchNPO);
            final boolean _tmpDinnerNPO;
            final int _tmp_15;
            _tmp_15 = _cursor.getInt(_cursorIndexOfDinnerNPO);
            _tmpDinnerNPO = _tmp_15 != 0;
            _item.setDinnerNPO(_tmpDinnerNPO);
            final String _tmpBreakfastItems;
            if (_cursor.isNull(_cursorIndexOfBreakfastItems)) {
              _tmpBreakfastItems = null;
            } else {
              _tmpBreakfastItems = _cursor.getString(_cursorIndexOfBreakfastItems);
            }
            _item.setBreakfastItems(_tmpBreakfastItems);
            final String _tmpLunchItems;
            if (_cursor.isNull(_cursorIndexOfLunchItems)) {
              _tmpLunchItems = null;
            } else {
              _tmpLunchItems = _cursor.getString(_cursorIndexOfLunchItems);
            }
            _item.setLunchItems(_tmpLunchItems);
            final String _tmpDinnerItems;
            if (_cursor.isNull(_cursorIndexOfDinnerItems)) {
              _tmpDinnerItems = null;
            } else {
              _tmpDinnerItems = _cursor.getString(_cursorIndexOfDinnerItems);
            }
            _item.setDinnerItems(_tmpDinnerItems);
            final String _tmpBreakfastJuices;
            if (_cursor.isNull(_cursorIndexOfBreakfastJuices)) {
              _tmpBreakfastJuices = null;
            } else {
              _tmpBreakfastJuices = _cursor.getString(_cursorIndexOfBreakfastJuices);
            }
            _item.setBreakfastJuices(_tmpBreakfastJuices);
            final String _tmpLunchJuices;
            if (_cursor.isNull(_cursorIndexOfLunchJuices)) {
              _tmpLunchJuices = null;
            } else {
              _tmpLunchJuices = _cursor.getString(_cursorIndexOfLunchJuices);
            }
            _item.setLunchJuices(_tmpLunchJuices);
            final String _tmpDinnerJuices;
            if (_cursor.isNull(_cursorIndexOfDinnerJuices)) {
              _tmpDinnerJuices = null;
            } else {
              _tmpDinnerJuices = _cursor.getString(_cursorIndexOfDinnerJuices);
            }
            _item.setDinnerJuices(_tmpDinnerJuices);
            final String _tmpBreakfastDrinks;
            if (_cursor.isNull(_cursorIndexOfBreakfastDrinks)) {
              _tmpBreakfastDrinks = null;
            } else {
              _tmpBreakfastDrinks = _cursor.getString(_cursorIndexOfBreakfastDrinks);
            }
            _item.setBreakfastDrinks(_tmpBreakfastDrinks);
            final String _tmpLunchDrinks;
            if (_cursor.isNull(_cursorIndexOfLunchDrinks)) {
              _tmpLunchDrinks = null;
            } else {
              _tmpLunchDrinks = _cursor.getString(_cursorIndexOfLunchDrinks);
            }
            _item.setLunchDrinks(_tmpLunchDrinks);
            final String _tmpDinnerDrinks;
            if (_cursor.isNull(_cursorIndexOfDinnerDrinks)) {
              _tmpDinnerDrinks = null;
            } else {
              _tmpDinnerDrinks = _cursor.getString(_cursorIndexOfDinnerDrinks);
            }
            _item.setDinnerDrinks(_tmpDinnerDrinks);
            final Date _tmpCreatedDate;
            final Long _tmp_16;
            if (_cursor.isNull(_cursorIndexOfCreatedDate)) {
              _tmp_16 = null;
            } else {
              _tmp_16 = _cursor.getLong(_cursorIndexOfCreatedDate);
            }
            _tmpCreatedDate = DateConverter.fromTimestamp(_tmp_16);
            _item.setCreatedDate(_tmpCreatedDate);
            final Date _tmpModifiedDate;
            final Long _tmp_17;
            if (_cursor.isNull(_cursorIndexOfModifiedDate)) {
              _tmp_17 = null;
            } else {
              _tmp_17 = _cursor.getLong(_cursorIndexOfModifiedDate);
            }
            _tmpModifiedDate = DateConverter.fromTimestamp(_tmp_17);
            _item.setModifiedDate(_tmpModifiedDate);
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
  public List<PatientEntity> getAllPatients() {
    final String _sql = "SELECT * FROM patient_info ORDER BY wing, CAST(room_number AS INTEGER)";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfPatientId = CursorUtil.getColumnIndexOrThrow(_cursor, "patient_id");
      final int _cursorIndexOfPatientFirstName = CursorUtil.getColumnIndexOrThrow(_cursor, "patient_first_name");
      final int _cursorIndexOfPatientLastName = CursorUtil.getColumnIndexOrThrow(_cursor, "patient_last_name");
      final int _cursorIndexOfWing = CursorUtil.getColumnIndexOrThrow(_cursor, "wing");
      final int _cursorIndexOfRoomNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "room_number");
      final int _cursorIndexOfDietType = CursorUtil.getColumnIndexOrThrow(_cursor, "diet_type");
      final int _cursorIndexOfDiet = CursorUtil.getColumnIndexOrThrow(_cursor, "diet");
      final int _cursorIndexOfAdaDiet = CursorUtil.getColumnIndexOrThrow(_cursor, "ada_diet");
      final int _cursorIndexOfFluidRestriction = CursorUtil.getColumnIndexOrThrow(_cursor, "fluid_restriction");
      final int _cursorIndexOfTextureModifications = CursorUtil.getColumnIndexOrThrow(_cursor, "texture_modifications");
      final int _cursorIndexOfMechanicalChopped = CursorUtil.getColumnIndexOrThrow(_cursor, "mechanical_chopped");
      final int _cursorIndexOfMechanicalGround = CursorUtil.getColumnIndexOrThrow(_cursor, "mechanical_ground");
      final int _cursorIndexOfBiteSize = CursorUtil.getColumnIndexOrThrow(_cursor, "bite_size");
      final int _cursorIndexOfBreadOK = CursorUtil.getColumnIndexOrThrow(_cursor, "bread_ok");
      final int _cursorIndexOfNectarThick = CursorUtil.getColumnIndexOrThrow(_cursor, "nectar_thick");
      final int _cursorIndexOfPuddingThick = CursorUtil.getColumnIndexOrThrow(_cursor, "pudding_thick");
      final int _cursorIndexOfHoneyThick = CursorUtil.getColumnIndexOrThrow(_cursor, "honey_thick");
      final int _cursorIndexOfExtraGravy = CursorUtil.getColumnIndexOrThrow(_cursor, "extra_gravy");
      final int _cursorIndexOfMeatsOnly = CursorUtil.getColumnIndexOrThrow(_cursor, "meats_only");
      final int _cursorIndexOfBreakfastComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_complete");
      final int _cursorIndexOfLunchComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_complete");
      final int _cursorIndexOfDinnerComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_complete");
      final int _cursorIndexOfBreakfastNPO = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_npo");
      final int _cursorIndexOfLunchNPO = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_npo");
      final int _cursorIndexOfDinnerNPO = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_npo");
      final int _cursorIndexOfBreakfastItems = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_items");
      final int _cursorIndexOfLunchItems = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_items");
      final int _cursorIndexOfDinnerItems = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_items");
      final int _cursorIndexOfBreakfastJuices = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_juices");
      final int _cursorIndexOfLunchJuices = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_juices");
      final int _cursorIndexOfDinnerJuices = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_juices");
      final int _cursorIndexOfBreakfastDrinks = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_drinks");
      final int _cursorIndexOfLunchDrinks = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_drinks");
      final int _cursorIndexOfDinnerDrinks = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_drinks");
      final int _cursorIndexOfCreatedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "created_date");
      final int _cursorIndexOfModifiedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "modified_date");
      final List<PatientEntity> _result = new ArrayList<PatientEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final PatientEntity _item;
        _item = new PatientEntity();
        final long _tmpPatientId;
        _tmpPatientId = _cursor.getLong(_cursorIndexOfPatientId);
        _item.setPatientId(_tmpPatientId);
        final String _tmpPatientFirstName;
        if (_cursor.isNull(_cursorIndexOfPatientFirstName)) {
          _tmpPatientFirstName = null;
        } else {
          _tmpPatientFirstName = _cursor.getString(_cursorIndexOfPatientFirstName);
        }
        _item.setPatientFirstName(_tmpPatientFirstName);
        final String _tmpPatientLastName;
        if (_cursor.isNull(_cursorIndexOfPatientLastName)) {
          _tmpPatientLastName = null;
        } else {
          _tmpPatientLastName = _cursor.getString(_cursorIndexOfPatientLastName);
        }
        _item.setPatientLastName(_tmpPatientLastName);
        final String _tmpWing;
        if (_cursor.isNull(_cursorIndexOfWing)) {
          _tmpWing = null;
        } else {
          _tmpWing = _cursor.getString(_cursorIndexOfWing);
        }
        _item.setWing(_tmpWing);
        final String _tmpRoomNumber;
        if (_cursor.isNull(_cursorIndexOfRoomNumber)) {
          _tmpRoomNumber = null;
        } else {
          _tmpRoomNumber = _cursor.getString(_cursorIndexOfRoomNumber);
        }
        _item.setRoomNumber(_tmpRoomNumber);
        final String _tmpDietType;
        if (_cursor.isNull(_cursorIndexOfDietType)) {
          _tmpDietType = null;
        } else {
          _tmpDietType = _cursor.getString(_cursorIndexOfDietType);
        }
        _item.setDietType(_tmpDietType);
        final String _tmpDiet;
        if (_cursor.isNull(_cursorIndexOfDiet)) {
          _tmpDiet = null;
        } else {
          _tmpDiet = _cursor.getString(_cursorIndexOfDiet);
        }
        _item.setDiet(_tmpDiet);
        final boolean _tmpAdaDiet;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfAdaDiet);
        _tmpAdaDiet = _tmp != 0;
        _item.setAdaDiet(_tmpAdaDiet);
        final String _tmpFluidRestriction;
        if (_cursor.isNull(_cursorIndexOfFluidRestriction)) {
          _tmpFluidRestriction = null;
        } else {
          _tmpFluidRestriction = _cursor.getString(_cursorIndexOfFluidRestriction);
        }
        _item.setFluidRestriction(_tmpFluidRestriction);
        final String _tmpTextureModifications;
        if (_cursor.isNull(_cursorIndexOfTextureModifications)) {
          _tmpTextureModifications = null;
        } else {
          _tmpTextureModifications = _cursor.getString(_cursorIndexOfTextureModifications);
        }
        _item.setTextureModifications(_tmpTextureModifications);
        final boolean _tmpMechanicalChopped;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfMechanicalChopped);
        _tmpMechanicalChopped = _tmp_1 != 0;
        _item.setMechanicalChopped(_tmpMechanicalChopped);
        final boolean _tmpMechanicalGround;
        final int _tmp_2;
        _tmp_2 = _cursor.getInt(_cursorIndexOfMechanicalGround);
        _tmpMechanicalGround = _tmp_2 != 0;
        _item.setMechanicalGround(_tmpMechanicalGround);
        final boolean _tmpBiteSize;
        final int _tmp_3;
        _tmp_3 = _cursor.getInt(_cursorIndexOfBiteSize);
        _tmpBiteSize = _tmp_3 != 0;
        _item.setBiteSize(_tmpBiteSize);
        final boolean _tmpBreadOK;
        final int _tmp_4;
        _tmp_4 = _cursor.getInt(_cursorIndexOfBreadOK);
        _tmpBreadOK = _tmp_4 != 0;
        _item.setBreadOK(_tmpBreadOK);
        final boolean _tmpNectarThick;
        final int _tmp_5;
        _tmp_5 = _cursor.getInt(_cursorIndexOfNectarThick);
        _tmpNectarThick = _tmp_5 != 0;
        _item.setNectarThick(_tmpNectarThick);
        final boolean _tmpPuddingThick;
        final int _tmp_6;
        _tmp_6 = _cursor.getInt(_cursorIndexOfPuddingThick);
        _tmpPuddingThick = _tmp_6 != 0;
        _item.setPuddingThick(_tmpPuddingThick);
        final boolean _tmpHoneyThick;
        final int _tmp_7;
        _tmp_7 = _cursor.getInt(_cursorIndexOfHoneyThick);
        _tmpHoneyThick = _tmp_7 != 0;
        _item.setHoneyThick(_tmpHoneyThick);
        final boolean _tmpExtraGravy;
        final int _tmp_8;
        _tmp_8 = _cursor.getInt(_cursorIndexOfExtraGravy);
        _tmpExtraGravy = _tmp_8 != 0;
        _item.setExtraGravy(_tmpExtraGravy);
        final boolean _tmpMeatsOnly;
        final int _tmp_9;
        _tmp_9 = _cursor.getInt(_cursorIndexOfMeatsOnly);
        _tmpMeatsOnly = _tmp_9 != 0;
        _item.setMeatsOnly(_tmpMeatsOnly);
        final boolean _tmpBreakfastComplete;
        final int _tmp_10;
        _tmp_10 = _cursor.getInt(_cursorIndexOfBreakfastComplete);
        _tmpBreakfastComplete = _tmp_10 != 0;
        _item.setBreakfastComplete(_tmpBreakfastComplete);
        final boolean _tmpLunchComplete;
        final int _tmp_11;
        _tmp_11 = _cursor.getInt(_cursorIndexOfLunchComplete);
        _tmpLunchComplete = _tmp_11 != 0;
        _item.setLunchComplete(_tmpLunchComplete);
        final boolean _tmpDinnerComplete;
        final int _tmp_12;
        _tmp_12 = _cursor.getInt(_cursorIndexOfDinnerComplete);
        _tmpDinnerComplete = _tmp_12 != 0;
        _item.setDinnerComplete(_tmpDinnerComplete);
        final boolean _tmpBreakfastNPO;
        final int _tmp_13;
        _tmp_13 = _cursor.getInt(_cursorIndexOfBreakfastNPO);
        _tmpBreakfastNPO = _tmp_13 != 0;
        _item.setBreakfastNPO(_tmpBreakfastNPO);
        final boolean _tmpLunchNPO;
        final int _tmp_14;
        _tmp_14 = _cursor.getInt(_cursorIndexOfLunchNPO);
        _tmpLunchNPO = _tmp_14 != 0;
        _item.setLunchNPO(_tmpLunchNPO);
        final boolean _tmpDinnerNPO;
        final int _tmp_15;
        _tmp_15 = _cursor.getInt(_cursorIndexOfDinnerNPO);
        _tmpDinnerNPO = _tmp_15 != 0;
        _item.setDinnerNPO(_tmpDinnerNPO);
        final String _tmpBreakfastItems;
        if (_cursor.isNull(_cursorIndexOfBreakfastItems)) {
          _tmpBreakfastItems = null;
        } else {
          _tmpBreakfastItems = _cursor.getString(_cursorIndexOfBreakfastItems);
        }
        _item.setBreakfastItems(_tmpBreakfastItems);
        final String _tmpLunchItems;
        if (_cursor.isNull(_cursorIndexOfLunchItems)) {
          _tmpLunchItems = null;
        } else {
          _tmpLunchItems = _cursor.getString(_cursorIndexOfLunchItems);
        }
        _item.setLunchItems(_tmpLunchItems);
        final String _tmpDinnerItems;
        if (_cursor.isNull(_cursorIndexOfDinnerItems)) {
          _tmpDinnerItems = null;
        } else {
          _tmpDinnerItems = _cursor.getString(_cursorIndexOfDinnerItems);
        }
        _item.setDinnerItems(_tmpDinnerItems);
        final String _tmpBreakfastJuices;
        if (_cursor.isNull(_cursorIndexOfBreakfastJuices)) {
          _tmpBreakfastJuices = null;
        } else {
          _tmpBreakfastJuices = _cursor.getString(_cursorIndexOfBreakfastJuices);
        }
        _item.setBreakfastJuices(_tmpBreakfastJuices);
        final String _tmpLunchJuices;
        if (_cursor.isNull(_cursorIndexOfLunchJuices)) {
          _tmpLunchJuices = null;
        } else {
          _tmpLunchJuices = _cursor.getString(_cursorIndexOfLunchJuices);
        }
        _item.setLunchJuices(_tmpLunchJuices);
        final String _tmpDinnerJuices;
        if (_cursor.isNull(_cursorIndexOfDinnerJuices)) {
          _tmpDinnerJuices = null;
        } else {
          _tmpDinnerJuices = _cursor.getString(_cursorIndexOfDinnerJuices);
        }
        _item.setDinnerJuices(_tmpDinnerJuices);
        final String _tmpBreakfastDrinks;
        if (_cursor.isNull(_cursorIndexOfBreakfastDrinks)) {
          _tmpBreakfastDrinks = null;
        } else {
          _tmpBreakfastDrinks = _cursor.getString(_cursorIndexOfBreakfastDrinks);
        }
        _item.setBreakfastDrinks(_tmpBreakfastDrinks);
        final String _tmpLunchDrinks;
        if (_cursor.isNull(_cursorIndexOfLunchDrinks)) {
          _tmpLunchDrinks = null;
        } else {
          _tmpLunchDrinks = _cursor.getString(_cursorIndexOfLunchDrinks);
        }
        _item.setLunchDrinks(_tmpLunchDrinks);
        final String _tmpDinnerDrinks;
        if (_cursor.isNull(_cursorIndexOfDinnerDrinks)) {
          _tmpDinnerDrinks = null;
        } else {
          _tmpDinnerDrinks = _cursor.getString(_cursorIndexOfDinnerDrinks);
        }
        _item.setDinnerDrinks(_tmpDinnerDrinks);
        final Date _tmpCreatedDate;
        final Long _tmp_16;
        if (_cursor.isNull(_cursorIndexOfCreatedDate)) {
          _tmp_16 = null;
        } else {
          _tmp_16 = _cursor.getLong(_cursorIndexOfCreatedDate);
        }
        _tmpCreatedDate = DateConverter.fromTimestamp(_tmp_16);
        _item.setCreatedDate(_tmpCreatedDate);
        final Date _tmpModifiedDate;
        final Long _tmp_17;
        if (_cursor.isNull(_cursorIndexOfModifiedDate)) {
          _tmp_17 = null;
        } else {
          _tmp_17 = _cursor.getLong(_cursorIndexOfModifiedDate);
        }
        _tmpModifiedDate = DateConverter.fromTimestamp(_tmp_17);
        _item.setModifiedDate(_tmpModifiedDate);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public LiveData<PatientEntity> getPatientByIdLive(final long patientId) {
    final String _sql = "SELECT * FROM patient_info WHERE patient_id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, patientId);
    return __db.getInvalidationTracker().createLiveData(new String[] {"patient_info"}, false, new Callable<PatientEntity>() {
      @Override
      @Nullable
      public PatientEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfPatientId = CursorUtil.getColumnIndexOrThrow(_cursor, "patient_id");
          final int _cursorIndexOfPatientFirstName = CursorUtil.getColumnIndexOrThrow(_cursor, "patient_first_name");
          final int _cursorIndexOfPatientLastName = CursorUtil.getColumnIndexOrThrow(_cursor, "patient_last_name");
          final int _cursorIndexOfWing = CursorUtil.getColumnIndexOrThrow(_cursor, "wing");
          final int _cursorIndexOfRoomNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "room_number");
          final int _cursorIndexOfDietType = CursorUtil.getColumnIndexOrThrow(_cursor, "diet_type");
          final int _cursorIndexOfDiet = CursorUtil.getColumnIndexOrThrow(_cursor, "diet");
          final int _cursorIndexOfAdaDiet = CursorUtil.getColumnIndexOrThrow(_cursor, "ada_diet");
          final int _cursorIndexOfFluidRestriction = CursorUtil.getColumnIndexOrThrow(_cursor, "fluid_restriction");
          final int _cursorIndexOfTextureModifications = CursorUtil.getColumnIndexOrThrow(_cursor, "texture_modifications");
          final int _cursorIndexOfMechanicalChopped = CursorUtil.getColumnIndexOrThrow(_cursor, "mechanical_chopped");
          final int _cursorIndexOfMechanicalGround = CursorUtil.getColumnIndexOrThrow(_cursor, "mechanical_ground");
          final int _cursorIndexOfBiteSize = CursorUtil.getColumnIndexOrThrow(_cursor, "bite_size");
          final int _cursorIndexOfBreadOK = CursorUtil.getColumnIndexOrThrow(_cursor, "bread_ok");
          final int _cursorIndexOfNectarThick = CursorUtil.getColumnIndexOrThrow(_cursor, "nectar_thick");
          final int _cursorIndexOfPuddingThick = CursorUtil.getColumnIndexOrThrow(_cursor, "pudding_thick");
          final int _cursorIndexOfHoneyThick = CursorUtil.getColumnIndexOrThrow(_cursor, "honey_thick");
          final int _cursorIndexOfExtraGravy = CursorUtil.getColumnIndexOrThrow(_cursor, "extra_gravy");
          final int _cursorIndexOfMeatsOnly = CursorUtil.getColumnIndexOrThrow(_cursor, "meats_only");
          final int _cursorIndexOfBreakfastComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_complete");
          final int _cursorIndexOfLunchComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_complete");
          final int _cursorIndexOfDinnerComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_complete");
          final int _cursorIndexOfBreakfastNPO = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_npo");
          final int _cursorIndexOfLunchNPO = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_npo");
          final int _cursorIndexOfDinnerNPO = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_npo");
          final int _cursorIndexOfBreakfastItems = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_items");
          final int _cursorIndexOfLunchItems = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_items");
          final int _cursorIndexOfDinnerItems = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_items");
          final int _cursorIndexOfBreakfastJuices = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_juices");
          final int _cursorIndexOfLunchJuices = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_juices");
          final int _cursorIndexOfDinnerJuices = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_juices");
          final int _cursorIndexOfBreakfastDrinks = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_drinks");
          final int _cursorIndexOfLunchDrinks = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_drinks");
          final int _cursorIndexOfDinnerDrinks = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_drinks");
          final int _cursorIndexOfCreatedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "created_date");
          final int _cursorIndexOfModifiedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "modified_date");
          final PatientEntity _result;
          if (_cursor.moveToFirst()) {
            _result = new PatientEntity();
            final long _tmpPatientId;
            _tmpPatientId = _cursor.getLong(_cursorIndexOfPatientId);
            _result.setPatientId(_tmpPatientId);
            final String _tmpPatientFirstName;
            if (_cursor.isNull(_cursorIndexOfPatientFirstName)) {
              _tmpPatientFirstName = null;
            } else {
              _tmpPatientFirstName = _cursor.getString(_cursorIndexOfPatientFirstName);
            }
            _result.setPatientFirstName(_tmpPatientFirstName);
            final String _tmpPatientLastName;
            if (_cursor.isNull(_cursorIndexOfPatientLastName)) {
              _tmpPatientLastName = null;
            } else {
              _tmpPatientLastName = _cursor.getString(_cursorIndexOfPatientLastName);
            }
            _result.setPatientLastName(_tmpPatientLastName);
            final String _tmpWing;
            if (_cursor.isNull(_cursorIndexOfWing)) {
              _tmpWing = null;
            } else {
              _tmpWing = _cursor.getString(_cursorIndexOfWing);
            }
            _result.setWing(_tmpWing);
            final String _tmpRoomNumber;
            if (_cursor.isNull(_cursorIndexOfRoomNumber)) {
              _tmpRoomNumber = null;
            } else {
              _tmpRoomNumber = _cursor.getString(_cursorIndexOfRoomNumber);
            }
            _result.setRoomNumber(_tmpRoomNumber);
            final String _tmpDietType;
            if (_cursor.isNull(_cursorIndexOfDietType)) {
              _tmpDietType = null;
            } else {
              _tmpDietType = _cursor.getString(_cursorIndexOfDietType);
            }
            _result.setDietType(_tmpDietType);
            final String _tmpDiet;
            if (_cursor.isNull(_cursorIndexOfDiet)) {
              _tmpDiet = null;
            } else {
              _tmpDiet = _cursor.getString(_cursorIndexOfDiet);
            }
            _result.setDiet(_tmpDiet);
            final boolean _tmpAdaDiet;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfAdaDiet);
            _tmpAdaDiet = _tmp != 0;
            _result.setAdaDiet(_tmpAdaDiet);
            final String _tmpFluidRestriction;
            if (_cursor.isNull(_cursorIndexOfFluidRestriction)) {
              _tmpFluidRestriction = null;
            } else {
              _tmpFluidRestriction = _cursor.getString(_cursorIndexOfFluidRestriction);
            }
            _result.setFluidRestriction(_tmpFluidRestriction);
            final String _tmpTextureModifications;
            if (_cursor.isNull(_cursorIndexOfTextureModifications)) {
              _tmpTextureModifications = null;
            } else {
              _tmpTextureModifications = _cursor.getString(_cursorIndexOfTextureModifications);
            }
            _result.setTextureModifications(_tmpTextureModifications);
            final boolean _tmpMechanicalChopped;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfMechanicalChopped);
            _tmpMechanicalChopped = _tmp_1 != 0;
            _result.setMechanicalChopped(_tmpMechanicalChopped);
            final boolean _tmpMechanicalGround;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfMechanicalGround);
            _tmpMechanicalGround = _tmp_2 != 0;
            _result.setMechanicalGround(_tmpMechanicalGround);
            final boolean _tmpBiteSize;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfBiteSize);
            _tmpBiteSize = _tmp_3 != 0;
            _result.setBiteSize(_tmpBiteSize);
            final boolean _tmpBreadOK;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfBreadOK);
            _tmpBreadOK = _tmp_4 != 0;
            _result.setBreadOK(_tmpBreadOK);
            final boolean _tmpNectarThick;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfNectarThick);
            _tmpNectarThick = _tmp_5 != 0;
            _result.setNectarThick(_tmpNectarThick);
            final boolean _tmpPuddingThick;
            final int _tmp_6;
            _tmp_6 = _cursor.getInt(_cursorIndexOfPuddingThick);
            _tmpPuddingThick = _tmp_6 != 0;
            _result.setPuddingThick(_tmpPuddingThick);
            final boolean _tmpHoneyThick;
            final int _tmp_7;
            _tmp_7 = _cursor.getInt(_cursorIndexOfHoneyThick);
            _tmpHoneyThick = _tmp_7 != 0;
            _result.setHoneyThick(_tmpHoneyThick);
            final boolean _tmpExtraGravy;
            final int _tmp_8;
            _tmp_8 = _cursor.getInt(_cursorIndexOfExtraGravy);
            _tmpExtraGravy = _tmp_8 != 0;
            _result.setExtraGravy(_tmpExtraGravy);
            final boolean _tmpMeatsOnly;
            final int _tmp_9;
            _tmp_9 = _cursor.getInt(_cursorIndexOfMeatsOnly);
            _tmpMeatsOnly = _tmp_9 != 0;
            _result.setMeatsOnly(_tmpMeatsOnly);
            final boolean _tmpBreakfastComplete;
            final int _tmp_10;
            _tmp_10 = _cursor.getInt(_cursorIndexOfBreakfastComplete);
            _tmpBreakfastComplete = _tmp_10 != 0;
            _result.setBreakfastComplete(_tmpBreakfastComplete);
            final boolean _tmpLunchComplete;
            final int _tmp_11;
            _tmp_11 = _cursor.getInt(_cursorIndexOfLunchComplete);
            _tmpLunchComplete = _tmp_11 != 0;
            _result.setLunchComplete(_tmpLunchComplete);
            final boolean _tmpDinnerComplete;
            final int _tmp_12;
            _tmp_12 = _cursor.getInt(_cursorIndexOfDinnerComplete);
            _tmpDinnerComplete = _tmp_12 != 0;
            _result.setDinnerComplete(_tmpDinnerComplete);
            final boolean _tmpBreakfastNPO;
            final int _tmp_13;
            _tmp_13 = _cursor.getInt(_cursorIndexOfBreakfastNPO);
            _tmpBreakfastNPO = _tmp_13 != 0;
            _result.setBreakfastNPO(_tmpBreakfastNPO);
            final boolean _tmpLunchNPO;
            final int _tmp_14;
            _tmp_14 = _cursor.getInt(_cursorIndexOfLunchNPO);
            _tmpLunchNPO = _tmp_14 != 0;
            _result.setLunchNPO(_tmpLunchNPO);
            final boolean _tmpDinnerNPO;
            final int _tmp_15;
            _tmp_15 = _cursor.getInt(_cursorIndexOfDinnerNPO);
            _tmpDinnerNPO = _tmp_15 != 0;
            _result.setDinnerNPO(_tmpDinnerNPO);
            final String _tmpBreakfastItems;
            if (_cursor.isNull(_cursorIndexOfBreakfastItems)) {
              _tmpBreakfastItems = null;
            } else {
              _tmpBreakfastItems = _cursor.getString(_cursorIndexOfBreakfastItems);
            }
            _result.setBreakfastItems(_tmpBreakfastItems);
            final String _tmpLunchItems;
            if (_cursor.isNull(_cursorIndexOfLunchItems)) {
              _tmpLunchItems = null;
            } else {
              _tmpLunchItems = _cursor.getString(_cursorIndexOfLunchItems);
            }
            _result.setLunchItems(_tmpLunchItems);
            final String _tmpDinnerItems;
            if (_cursor.isNull(_cursorIndexOfDinnerItems)) {
              _tmpDinnerItems = null;
            } else {
              _tmpDinnerItems = _cursor.getString(_cursorIndexOfDinnerItems);
            }
            _result.setDinnerItems(_tmpDinnerItems);
            final String _tmpBreakfastJuices;
            if (_cursor.isNull(_cursorIndexOfBreakfastJuices)) {
              _tmpBreakfastJuices = null;
            } else {
              _tmpBreakfastJuices = _cursor.getString(_cursorIndexOfBreakfastJuices);
            }
            _result.setBreakfastJuices(_tmpBreakfastJuices);
            final String _tmpLunchJuices;
            if (_cursor.isNull(_cursorIndexOfLunchJuices)) {
              _tmpLunchJuices = null;
            } else {
              _tmpLunchJuices = _cursor.getString(_cursorIndexOfLunchJuices);
            }
            _result.setLunchJuices(_tmpLunchJuices);
            final String _tmpDinnerJuices;
            if (_cursor.isNull(_cursorIndexOfDinnerJuices)) {
              _tmpDinnerJuices = null;
            } else {
              _tmpDinnerJuices = _cursor.getString(_cursorIndexOfDinnerJuices);
            }
            _result.setDinnerJuices(_tmpDinnerJuices);
            final String _tmpBreakfastDrinks;
            if (_cursor.isNull(_cursorIndexOfBreakfastDrinks)) {
              _tmpBreakfastDrinks = null;
            } else {
              _tmpBreakfastDrinks = _cursor.getString(_cursorIndexOfBreakfastDrinks);
            }
            _result.setBreakfastDrinks(_tmpBreakfastDrinks);
            final String _tmpLunchDrinks;
            if (_cursor.isNull(_cursorIndexOfLunchDrinks)) {
              _tmpLunchDrinks = null;
            } else {
              _tmpLunchDrinks = _cursor.getString(_cursorIndexOfLunchDrinks);
            }
            _result.setLunchDrinks(_tmpLunchDrinks);
            final String _tmpDinnerDrinks;
            if (_cursor.isNull(_cursorIndexOfDinnerDrinks)) {
              _tmpDinnerDrinks = null;
            } else {
              _tmpDinnerDrinks = _cursor.getString(_cursorIndexOfDinnerDrinks);
            }
            _result.setDinnerDrinks(_tmpDinnerDrinks);
            final Date _tmpCreatedDate;
            final Long _tmp_16;
            if (_cursor.isNull(_cursorIndexOfCreatedDate)) {
              _tmp_16 = null;
            } else {
              _tmp_16 = _cursor.getLong(_cursorIndexOfCreatedDate);
            }
            _tmpCreatedDate = DateConverter.fromTimestamp(_tmp_16);
            _result.setCreatedDate(_tmpCreatedDate);
            final Date _tmpModifiedDate;
            final Long _tmp_17;
            if (_cursor.isNull(_cursorIndexOfModifiedDate)) {
              _tmp_17 = null;
            } else {
              _tmp_17 = _cursor.getLong(_cursorIndexOfModifiedDate);
            }
            _tmpModifiedDate = DateConverter.fromTimestamp(_tmp_17);
            _result.setModifiedDate(_tmpModifiedDate);
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
  public PatientEntity getPatientById(final long patientId) {
    final String _sql = "SELECT * FROM patient_info WHERE patient_id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, patientId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfPatientId = CursorUtil.getColumnIndexOrThrow(_cursor, "patient_id");
      final int _cursorIndexOfPatientFirstName = CursorUtil.getColumnIndexOrThrow(_cursor, "patient_first_name");
      final int _cursorIndexOfPatientLastName = CursorUtil.getColumnIndexOrThrow(_cursor, "patient_last_name");
      final int _cursorIndexOfWing = CursorUtil.getColumnIndexOrThrow(_cursor, "wing");
      final int _cursorIndexOfRoomNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "room_number");
      final int _cursorIndexOfDietType = CursorUtil.getColumnIndexOrThrow(_cursor, "diet_type");
      final int _cursorIndexOfDiet = CursorUtil.getColumnIndexOrThrow(_cursor, "diet");
      final int _cursorIndexOfAdaDiet = CursorUtil.getColumnIndexOrThrow(_cursor, "ada_diet");
      final int _cursorIndexOfFluidRestriction = CursorUtil.getColumnIndexOrThrow(_cursor, "fluid_restriction");
      final int _cursorIndexOfTextureModifications = CursorUtil.getColumnIndexOrThrow(_cursor, "texture_modifications");
      final int _cursorIndexOfMechanicalChopped = CursorUtil.getColumnIndexOrThrow(_cursor, "mechanical_chopped");
      final int _cursorIndexOfMechanicalGround = CursorUtil.getColumnIndexOrThrow(_cursor, "mechanical_ground");
      final int _cursorIndexOfBiteSize = CursorUtil.getColumnIndexOrThrow(_cursor, "bite_size");
      final int _cursorIndexOfBreadOK = CursorUtil.getColumnIndexOrThrow(_cursor, "bread_ok");
      final int _cursorIndexOfNectarThick = CursorUtil.getColumnIndexOrThrow(_cursor, "nectar_thick");
      final int _cursorIndexOfPuddingThick = CursorUtil.getColumnIndexOrThrow(_cursor, "pudding_thick");
      final int _cursorIndexOfHoneyThick = CursorUtil.getColumnIndexOrThrow(_cursor, "honey_thick");
      final int _cursorIndexOfExtraGravy = CursorUtil.getColumnIndexOrThrow(_cursor, "extra_gravy");
      final int _cursorIndexOfMeatsOnly = CursorUtil.getColumnIndexOrThrow(_cursor, "meats_only");
      final int _cursorIndexOfBreakfastComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_complete");
      final int _cursorIndexOfLunchComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_complete");
      final int _cursorIndexOfDinnerComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_complete");
      final int _cursorIndexOfBreakfastNPO = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_npo");
      final int _cursorIndexOfLunchNPO = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_npo");
      final int _cursorIndexOfDinnerNPO = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_npo");
      final int _cursorIndexOfBreakfastItems = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_items");
      final int _cursorIndexOfLunchItems = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_items");
      final int _cursorIndexOfDinnerItems = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_items");
      final int _cursorIndexOfBreakfastJuices = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_juices");
      final int _cursorIndexOfLunchJuices = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_juices");
      final int _cursorIndexOfDinnerJuices = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_juices");
      final int _cursorIndexOfBreakfastDrinks = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_drinks");
      final int _cursorIndexOfLunchDrinks = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_drinks");
      final int _cursorIndexOfDinnerDrinks = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_drinks");
      final int _cursorIndexOfCreatedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "created_date");
      final int _cursorIndexOfModifiedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "modified_date");
      final PatientEntity _result;
      if (_cursor.moveToFirst()) {
        _result = new PatientEntity();
        final long _tmpPatientId;
        _tmpPatientId = _cursor.getLong(_cursorIndexOfPatientId);
        _result.setPatientId(_tmpPatientId);
        final String _tmpPatientFirstName;
        if (_cursor.isNull(_cursorIndexOfPatientFirstName)) {
          _tmpPatientFirstName = null;
        } else {
          _tmpPatientFirstName = _cursor.getString(_cursorIndexOfPatientFirstName);
        }
        _result.setPatientFirstName(_tmpPatientFirstName);
        final String _tmpPatientLastName;
        if (_cursor.isNull(_cursorIndexOfPatientLastName)) {
          _tmpPatientLastName = null;
        } else {
          _tmpPatientLastName = _cursor.getString(_cursorIndexOfPatientLastName);
        }
        _result.setPatientLastName(_tmpPatientLastName);
        final String _tmpWing;
        if (_cursor.isNull(_cursorIndexOfWing)) {
          _tmpWing = null;
        } else {
          _tmpWing = _cursor.getString(_cursorIndexOfWing);
        }
        _result.setWing(_tmpWing);
        final String _tmpRoomNumber;
        if (_cursor.isNull(_cursorIndexOfRoomNumber)) {
          _tmpRoomNumber = null;
        } else {
          _tmpRoomNumber = _cursor.getString(_cursorIndexOfRoomNumber);
        }
        _result.setRoomNumber(_tmpRoomNumber);
        final String _tmpDietType;
        if (_cursor.isNull(_cursorIndexOfDietType)) {
          _tmpDietType = null;
        } else {
          _tmpDietType = _cursor.getString(_cursorIndexOfDietType);
        }
        _result.setDietType(_tmpDietType);
        final String _tmpDiet;
        if (_cursor.isNull(_cursorIndexOfDiet)) {
          _tmpDiet = null;
        } else {
          _tmpDiet = _cursor.getString(_cursorIndexOfDiet);
        }
        _result.setDiet(_tmpDiet);
        final boolean _tmpAdaDiet;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfAdaDiet);
        _tmpAdaDiet = _tmp != 0;
        _result.setAdaDiet(_tmpAdaDiet);
        final String _tmpFluidRestriction;
        if (_cursor.isNull(_cursorIndexOfFluidRestriction)) {
          _tmpFluidRestriction = null;
        } else {
          _tmpFluidRestriction = _cursor.getString(_cursorIndexOfFluidRestriction);
        }
        _result.setFluidRestriction(_tmpFluidRestriction);
        final String _tmpTextureModifications;
        if (_cursor.isNull(_cursorIndexOfTextureModifications)) {
          _tmpTextureModifications = null;
        } else {
          _tmpTextureModifications = _cursor.getString(_cursorIndexOfTextureModifications);
        }
        _result.setTextureModifications(_tmpTextureModifications);
        final boolean _tmpMechanicalChopped;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfMechanicalChopped);
        _tmpMechanicalChopped = _tmp_1 != 0;
        _result.setMechanicalChopped(_tmpMechanicalChopped);
        final boolean _tmpMechanicalGround;
        final int _tmp_2;
        _tmp_2 = _cursor.getInt(_cursorIndexOfMechanicalGround);
        _tmpMechanicalGround = _tmp_2 != 0;
        _result.setMechanicalGround(_tmpMechanicalGround);
        final boolean _tmpBiteSize;
        final int _tmp_3;
        _tmp_3 = _cursor.getInt(_cursorIndexOfBiteSize);
        _tmpBiteSize = _tmp_3 != 0;
        _result.setBiteSize(_tmpBiteSize);
        final boolean _tmpBreadOK;
        final int _tmp_4;
        _tmp_4 = _cursor.getInt(_cursorIndexOfBreadOK);
        _tmpBreadOK = _tmp_4 != 0;
        _result.setBreadOK(_tmpBreadOK);
        final boolean _tmpNectarThick;
        final int _tmp_5;
        _tmp_5 = _cursor.getInt(_cursorIndexOfNectarThick);
        _tmpNectarThick = _tmp_5 != 0;
        _result.setNectarThick(_tmpNectarThick);
        final boolean _tmpPuddingThick;
        final int _tmp_6;
        _tmp_6 = _cursor.getInt(_cursorIndexOfPuddingThick);
        _tmpPuddingThick = _tmp_6 != 0;
        _result.setPuddingThick(_tmpPuddingThick);
        final boolean _tmpHoneyThick;
        final int _tmp_7;
        _tmp_7 = _cursor.getInt(_cursorIndexOfHoneyThick);
        _tmpHoneyThick = _tmp_7 != 0;
        _result.setHoneyThick(_tmpHoneyThick);
        final boolean _tmpExtraGravy;
        final int _tmp_8;
        _tmp_8 = _cursor.getInt(_cursorIndexOfExtraGravy);
        _tmpExtraGravy = _tmp_8 != 0;
        _result.setExtraGravy(_tmpExtraGravy);
        final boolean _tmpMeatsOnly;
        final int _tmp_9;
        _tmp_9 = _cursor.getInt(_cursorIndexOfMeatsOnly);
        _tmpMeatsOnly = _tmp_9 != 0;
        _result.setMeatsOnly(_tmpMeatsOnly);
        final boolean _tmpBreakfastComplete;
        final int _tmp_10;
        _tmp_10 = _cursor.getInt(_cursorIndexOfBreakfastComplete);
        _tmpBreakfastComplete = _tmp_10 != 0;
        _result.setBreakfastComplete(_tmpBreakfastComplete);
        final boolean _tmpLunchComplete;
        final int _tmp_11;
        _tmp_11 = _cursor.getInt(_cursorIndexOfLunchComplete);
        _tmpLunchComplete = _tmp_11 != 0;
        _result.setLunchComplete(_tmpLunchComplete);
        final boolean _tmpDinnerComplete;
        final int _tmp_12;
        _tmp_12 = _cursor.getInt(_cursorIndexOfDinnerComplete);
        _tmpDinnerComplete = _tmp_12 != 0;
        _result.setDinnerComplete(_tmpDinnerComplete);
        final boolean _tmpBreakfastNPO;
        final int _tmp_13;
        _tmp_13 = _cursor.getInt(_cursorIndexOfBreakfastNPO);
        _tmpBreakfastNPO = _tmp_13 != 0;
        _result.setBreakfastNPO(_tmpBreakfastNPO);
        final boolean _tmpLunchNPO;
        final int _tmp_14;
        _tmp_14 = _cursor.getInt(_cursorIndexOfLunchNPO);
        _tmpLunchNPO = _tmp_14 != 0;
        _result.setLunchNPO(_tmpLunchNPO);
        final boolean _tmpDinnerNPO;
        final int _tmp_15;
        _tmp_15 = _cursor.getInt(_cursorIndexOfDinnerNPO);
        _tmpDinnerNPO = _tmp_15 != 0;
        _result.setDinnerNPO(_tmpDinnerNPO);
        final String _tmpBreakfastItems;
        if (_cursor.isNull(_cursorIndexOfBreakfastItems)) {
          _tmpBreakfastItems = null;
        } else {
          _tmpBreakfastItems = _cursor.getString(_cursorIndexOfBreakfastItems);
        }
        _result.setBreakfastItems(_tmpBreakfastItems);
        final String _tmpLunchItems;
        if (_cursor.isNull(_cursorIndexOfLunchItems)) {
          _tmpLunchItems = null;
        } else {
          _tmpLunchItems = _cursor.getString(_cursorIndexOfLunchItems);
        }
        _result.setLunchItems(_tmpLunchItems);
        final String _tmpDinnerItems;
        if (_cursor.isNull(_cursorIndexOfDinnerItems)) {
          _tmpDinnerItems = null;
        } else {
          _tmpDinnerItems = _cursor.getString(_cursorIndexOfDinnerItems);
        }
        _result.setDinnerItems(_tmpDinnerItems);
        final String _tmpBreakfastJuices;
        if (_cursor.isNull(_cursorIndexOfBreakfastJuices)) {
          _tmpBreakfastJuices = null;
        } else {
          _tmpBreakfastJuices = _cursor.getString(_cursorIndexOfBreakfastJuices);
        }
        _result.setBreakfastJuices(_tmpBreakfastJuices);
        final String _tmpLunchJuices;
        if (_cursor.isNull(_cursorIndexOfLunchJuices)) {
          _tmpLunchJuices = null;
        } else {
          _tmpLunchJuices = _cursor.getString(_cursorIndexOfLunchJuices);
        }
        _result.setLunchJuices(_tmpLunchJuices);
        final String _tmpDinnerJuices;
        if (_cursor.isNull(_cursorIndexOfDinnerJuices)) {
          _tmpDinnerJuices = null;
        } else {
          _tmpDinnerJuices = _cursor.getString(_cursorIndexOfDinnerJuices);
        }
        _result.setDinnerJuices(_tmpDinnerJuices);
        final String _tmpBreakfastDrinks;
        if (_cursor.isNull(_cursorIndexOfBreakfastDrinks)) {
          _tmpBreakfastDrinks = null;
        } else {
          _tmpBreakfastDrinks = _cursor.getString(_cursorIndexOfBreakfastDrinks);
        }
        _result.setBreakfastDrinks(_tmpBreakfastDrinks);
        final String _tmpLunchDrinks;
        if (_cursor.isNull(_cursorIndexOfLunchDrinks)) {
          _tmpLunchDrinks = null;
        } else {
          _tmpLunchDrinks = _cursor.getString(_cursorIndexOfLunchDrinks);
        }
        _result.setLunchDrinks(_tmpLunchDrinks);
        final String _tmpDinnerDrinks;
        if (_cursor.isNull(_cursorIndexOfDinnerDrinks)) {
          _tmpDinnerDrinks = null;
        } else {
          _tmpDinnerDrinks = _cursor.getString(_cursorIndexOfDinnerDrinks);
        }
        _result.setDinnerDrinks(_tmpDinnerDrinks);
        final Date _tmpCreatedDate;
        final Long _tmp_16;
        if (_cursor.isNull(_cursorIndexOfCreatedDate)) {
          _tmp_16 = null;
        } else {
          _tmp_16 = _cursor.getLong(_cursorIndexOfCreatedDate);
        }
        _tmpCreatedDate = DateConverter.fromTimestamp(_tmp_16);
        _result.setCreatedDate(_tmpCreatedDate);
        final Date _tmpModifiedDate;
        final Long _tmp_17;
        if (_cursor.isNull(_cursorIndexOfModifiedDate)) {
          _tmp_17 = null;
        } else {
          _tmp_17 = _cursor.getLong(_cursorIndexOfModifiedDate);
        }
        _tmpModifiedDate = DateConverter.fromTimestamp(_tmp_17);
        _result.setModifiedDate(_tmpModifiedDate);
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
  public LiveData<List<PatientEntity>> getPendingPatientsLive() {
    final String _sql = "SELECT * FROM patient_info WHERE (breakfast_complete = 0 OR lunch_complete = 0 OR dinner_complete = 0) ORDER BY wing, CAST(room_number AS INTEGER)";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"patient_info"}, false, new Callable<List<PatientEntity>>() {
      @Override
      @Nullable
      public List<PatientEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfPatientId = CursorUtil.getColumnIndexOrThrow(_cursor, "patient_id");
          final int _cursorIndexOfPatientFirstName = CursorUtil.getColumnIndexOrThrow(_cursor, "patient_first_name");
          final int _cursorIndexOfPatientLastName = CursorUtil.getColumnIndexOrThrow(_cursor, "patient_last_name");
          final int _cursorIndexOfWing = CursorUtil.getColumnIndexOrThrow(_cursor, "wing");
          final int _cursorIndexOfRoomNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "room_number");
          final int _cursorIndexOfDietType = CursorUtil.getColumnIndexOrThrow(_cursor, "diet_type");
          final int _cursorIndexOfDiet = CursorUtil.getColumnIndexOrThrow(_cursor, "diet");
          final int _cursorIndexOfAdaDiet = CursorUtil.getColumnIndexOrThrow(_cursor, "ada_diet");
          final int _cursorIndexOfFluidRestriction = CursorUtil.getColumnIndexOrThrow(_cursor, "fluid_restriction");
          final int _cursorIndexOfTextureModifications = CursorUtil.getColumnIndexOrThrow(_cursor, "texture_modifications");
          final int _cursorIndexOfMechanicalChopped = CursorUtil.getColumnIndexOrThrow(_cursor, "mechanical_chopped");
          final int _cursorIndexOfMechanicalGround = CursorUtil.getColumnIndexOrThrow(_cursor, "mechanical_ground");
          final int _cursorIndexOfBiteSize = CursorUtil.getColumnIndexOrThrow(_cursor, "bite_size");
          final int _cursorIndexOfBreadOK = CursorUtil.getColumnIndexOrThrow(_cursor, "bread_ok");
          final int _cursorIndexOfNectarThick = CursorUtil.getColumnIndexOrThrow(_cursor, "nectar_thick");
          final int _cursorIndexOfPuddingThick = CursorUtil.getColumnIndexOrThrow(_cursor, "pudding_thick");
          final int _cursorIndexOfHoneyThick = CursorUtil.getColumnIndexOrThrow(_cursor, "honey_thick");
          final int _cursorIndexOfExtraGravy = CursorUtil.getColumnIndexOrThrow(_cursor, "extra_gravy");
          final int _cursorIndexOfMeatsOnly = CursorUtil.getColumnIndexOrThrow(_cursor, "meats_only");
          final int _cursorIndexOfBreakfastComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_complete");
          final int _cursorIndexOfLunchComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_complete");
          final int _cursorIndexOfDinnerComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_complete");
          final int _cursorIndexOfBreakfastNPO = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_npo");
          final int _cursorIndexOfLunchNPO = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_npo");
          final int _cursorIndexOfDinnerNPO = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_npo");
          final int _cursorIndexOfBreakfastItems = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_items");
          final int _cursorIndexOfLunchItems = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_items");
          final int _cursorIndexOfDinnerItems = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_items");
          final int _cursorIndexOfBreakfastJuices = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_juices");
          final int _cursorIndexOfLunchJuices = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_juices");
          final int _cursorIndexOfDinnerJuices = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_juices");
          final int _cursorIndexOfBreakfastDrinks = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_drinks");
          final int _cursorIndexOfLunchDrinks = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_drinks");
          final int _cursorIndexOfDinnerDrinks = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_drinks");
          final int _cursorIndexOfCreatedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "created_date");
          final int _cursorIndexOfModifiedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "modified_date");
          final List<PatientEntity> _result = new ArrayList<PatientEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PatientEntity _item;
            _item = new PatientEntity();
            final long _tmpPatientId;
            _tmpPatientId = _cursor.getLong(_cursorIndexOfPatientId);
            _item.setPatientId(_tmpPatientId);
            final String _tmpPatientFirstName;
            if (_cursor.isNull(_cursorIndexOfPatientFirstName)) {
              _tmpPatientFirstName = null;
            } else {
              _tmpPatientFirstName = _cursor.getString(_cursorIndexOfPatientFirstName);
            }
            _item.setPatientFirstName(_tmpPatientFirstName);
            final String _tmpPatientLastName;
            if (_cursor.isNull(_cursorIndexOfPatientLastName)) {
              _tmpPatientLastName = null;
            } else {
              _tmpPatientLastName = _cursor.getString(_cursorIndexOfPatientLastName);
            }
            _item.setPatientLastName(_tmpPatientLastName);
            final String _tmpWing;
            if (_cursor.isNull(_cursorIndexOfWing)) {
              _tmpWing = null;
            } else {
              _tmpWing = _cursor.getString(_cursorIndexOfWing);
            }
            _item.setWing(_tmpWing);
            final String _tmpRoomNumber;
            if (_cursor.isNull(_cursorIndexOfRoomNumber)) {
              _tmpRoomNumber = null;
            } else {
              _tmpRoomNumber = _cursor.getString(_cursorIndexOfRoomNumber);
            }
            _item.setRoomNumber(_tmpRoomNumber);
            final String _tmpDietType;
            if (_cursor.isNull(_cursorIndexOfDietType)) {
              _tmpDietType = null;
            } else {
              _tmpDietType = _cursor.getString(_cursorIndexOfDietType);
            }
            _item.setDietType(_tmpDietType);
            final String _tmpDiet;
            if (_cursor.isNull(_cursorIndexOfDiet)) {
              _tmpDiet = null;
            } else {
              _tmpDiet = _cursor.getString(_cursorIndexOfDiet);
            }
            _item.setDiet(_tmpDiet);
            final boolean _tmpAdaDiet;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfAdaDiet);
            _tmpAdaDiet = _tmp != 0;
            _item.setAdaDiet(_tmpAdaDiet);
            final String _tmpFluidRestriction;
            if (_cursor.isNull(_cursorIndexOfFluidRestriction)) {
              _tmpFluidRestriction = null;
            } else {
              _tmpFluidRestriction = _cursor.getString(_cursorIndexOfFluidRestriction);
            }
            _item.setFluidRestriction(_tmpFluidRestriction);
            final String _tmpTextureModifications;
            if (_cursor.isNull(_cursorIndexOfTextureModifications)) {
              _tmpTextureModifications = null;
            } else {
              _tmpTextureModifications = _cursor.getString(_cursorIndexOfTextureModifications);
            }
            _item.setTextureModifications(_tmpTextureModifications);
            final boolean _tmpMechanicalChopped;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfMechanicalChopped);
            _tmpMechanicalChopped = _tmp_1 != 0;
            _item.setMechanicalChopped(_tmpMechanicalChopped);
            final boolean _tmpMechanicalGround;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfMechanicalGround);
            _tmpMechanicalGround = _tmp_2 != 0;
            _item.setMechanicalGround(_tmpMechanicalGround);
            final boolean _tmpBiteSize;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfBiteSize);
            _tmpBiteSize = _tmp_3 != 0;
            _item.setBiteSize(_tmpBiteSize);
            final boolean _tmpBreadOK;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfBreadOK);
            _tmpBreadOK = _tmp_4 != 0;
            _item.setBreadOK(_tmpBreadOK);
            final boolean _tmpNectarThick;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfNectarThick);
            _tmpNectarThick = _tmp_5 != 0;
            _item.setNectarThick(_tmpNectarThick);
            final boolean _tmpPuddingThick;
            final int _tmp_6;
            _tmp_6 = _cursor.getInt(_cursorIndexOfPuddingThick);
            _tmpPuddingThick = _tmp_6 != 0;
            _item.setPuddingThick(_tmpPuddingThick);
            final boolean _tmpHoneyThick;
            final int _tmp_7;
            _tmp_7 = _cursor.getInt(_cursorIndexOfHoneyThick);
            _tmpHoneyThick = _tmp_7 != 0;
            _item.setHoneyThick(_tmpHoneyThick);
            final boolean _tmpExtraGravy;
            final int _tmp_8;
            _tmp_8 = _cursor.getInt(_cursorIndexOfExtraGravy);
            _tmpExtraGravy = _tmp_8 != 0;
            _item.setExtraGravy(_tmpExtraGravy);
            final boolean _tmpMeatsOnly;
            final int _tmp_9;
            _tmp_9 = _cursor.getInt(_cursorIndexOfMeatsOnly);
            _tmpMeatsOnly = _tmp_9 != 0;
            _item.setMeatsOnly(_tmpMeatsOnly);
            final boolean _tmpBreakfastComplete;
            final int _tmp_10;
            _tmp_10 = _cursor.getInt(_cursorIndexOfBreakfastComplete);
            _tmpBreakfastComplete = _tmp_10 != 0;
            _item.setBreakfastComplete(_tmpBreakfastComplete);
            final boolean _tmpLunchComplete;
            final int _tmp_11;
            _tmp_11 = _cursor.getInt(_cursorIndexOfLunchComplete);
            _tmpLunchComplete = _tmp_11 != 0;
            _item.setLunchComplete(_tmpLunchComplete);
            final boolean _tmpDinnerComplete;
            final int _tmp_12;
            _tmp_12 = _cursor.getInt(_cursorIndexOfDinnerComplete);
            _tmpDinnerComplete = _tmp_12 != 0;
            _item.setDinnerComplete(_tmpDinnerComplete);
            final boolean _tmpBreakfastNPO;
            final int _tmp_13;
            _tmp_13 = _cursor.getInt(_cursorIndexOfBreakfastNPO);
            _tmpBreakfastNPO = _tmp_13 != 0;
            _item.setBreakfastNPO(_tmpBreakfastNPO);
            final boolean _tmpLunchNPO;
            final int _tmp_14;
            _tmp_14 = _cursor.getInt(_cursorIndexOfLunchNPO);
            _tmpLunchNPO = _tmp_14 != 0;
            _item.setLunchNPO(_tmpLunchNPO);
            final boolean _tmpDinnerNPO;
            final int _tmp_15;
            _tmp_15 = _cursor.getInt(_cursorIndexOfDinnerNPO);
            _tmpDinnerNPO = _tmp_15 != 0;
            _item.setDinnerNPO(_tmpDinnerNPO);
            final String _tmpBreakfastItems;
            if (_cursor.isNull(_cursorIndexOfBreakfastItems)) {
              _tmpBreakfastItems = null;
            } else {
              _tmpBreakfastItems = _cursor.getString(_cursorIndexOfBreakfastItems);
            }
            _item.setBreakfastItems(_tmpBreakfastItems);
            final String _tmpLunchItems;
            if (_cursor.isNull(_cursorIndexOfLunchItems)) {
              _tmpLunchItems = null;
            } else {
              _tmpLunchItems = _cursor.getString(_cursorIndexOfLunchItems);
            }
            _item.setLunchItems(_tmpLunchItems);
            final String _tmpDinnerItems;
            if (_cursor.isNull(_cursorIndexOfDinnerItems)) {
              _tmpDinnerItems = null;
            } else {
              _tmpDinnerItems = _cursor.getString(_cursorIndexOfDinnerItems);
            }
            _item.setDinnerItems(_tmpDinnerItems);
            final String _tmpBreakfastJuices;
            if (_cursor.isNull(_cursorIndexOfBreakfastJuices)) {
              _tmpBreakfastJuices = null;
            } else {
              _tmpBreakfastJuices = _cursor.getString(_cursorIndexOfBreakfastJuices);
            }
            _item.setBreakfastJuices(_tmpBreakfastJuices);
            final String _tmpLunchJuices;
            if (_cursor.isNull(_cursorIndexOfLunchJuices)) {
              _tmpLunchJuices = null;
            } else {
              _tmpLunchJuices = _cursor.getString(_cursorIndexOfLunchJuices);
            }
            _item.setLunchJuices(_tmpLunchJuices);
            final String _tmpDinnerJuices;
            if (_cursor.isNull(_cursorIndexOfDinnerJuices)) {
              _tmpDinnerJuices = null;
            } else {
              _tmpDinnerJuices = _cursor.getString(_cursorIndexOfDinnerJuices);
            }
            _item.setDinnerJuices(_tmpDinnerJuices);
            final String _tmpBreakfastDrinks;
            if (_cursor.isNull(_cursorIndexOfBreakfastDrinks)) {
              _tmpBreakfastDrinks = null;
            } else {
              _tmpBreakfastDrinks = _cursor.getString(_cursorIndexOfBreakfastDrinks);
            }
            _item.setBreakfastDrinks(_tmpBreakfastDrinks);
            final String _tmpLunchDrinks;
            if (_cursor.isNull(_cursorIndexOfLunchDrinks)) {
              _tmpLunchDrinks = null;
            } else {
              _tmpLunchDrinks = _cursor.getString(_cursorIndexOfLunchDrinks);
            }
            _item.setLunchDrinks(_tmpLunchDrinks);
            final String _tmpDinnerDrinks;
            if (_cursor.isNull(_cursorIndexOfDinnerDrinks)) {
              _tmpDinnerDrinks = null;
            } else {
              _tmpDinnerDrinks = _cursor.getString(_cursorIndexOfDinnerDrinks);
            }
            _item.setDinnerDrinks(_tmpDinnerDrinks);
            final Date _tmpCreatedDate;
            final Long _tmp_16;
            if (_cursor.isNull(_cursorIndexOfCreatedDate)) {
              _tmp_16 = null;
            } else {
              _tmp_16 = _cursor.getLong(_cursorIndexOfCreatedDate);
            }
            _tmpCreatedDate = DateConverter.fromTimestamp(_tmp_16);
            _item.setCreatedDate(_tmpCreatedDate);
            final Date _tmpModifiedDate;
            final Long _tmp_17;
            if (_cursor.isNull(_cursorIndexOfModifiedDate)) {
              _tmp_17 = null;
            } else {
              _tmp_17 = _cursor.getLong(_cursorIndexOfModifiedDate);
            }
            _tmpModifiedDate = DateConverter.fromTimestamp(_tmp_17);
            _item.setModifiedDate(_tmpModifiedDate);
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
  public List<PatientEntity> getPendingPatients() {
    final String _sql = "SELECT * FROM patient_info WHERE (breakfast_complete = 0 OR lunch_complete = 0 OR dinner_complete = 0) ORDER BY wing, CAST(room_number AS INTEGER)";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfPatientId = CursorUtil.getColumnIndexOrThrow(_cursor, "patient_id");
      final int _cursorIndexOfPatientFirstName = CursorUtil.getColumnIndexOrThrow(_cursor, "patient_first_name");
      final int _cursorIndexOfPatientLastName = CursorUtil.getColumnIndexOrThrow(_cursor, "patient_last_name");
      final int _cursorIndexOfWing = CursorUtil.getColumnIndexOrThrow(_cursor, "wing");
      final int _cursorIndexOfRoomNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "room_number");
      final int _cursorIndexOfDietType = CursorUtil.getColumnIndexOrThrow(_cursor, "diet_type");
      final int _cursorIndexOfDiet = CursorUtil.getColumnIndexOrThrow(_cursor, "diet");
      final int _cursorIndexOfAdaDiet = CursorUtil.getColumnIndexOrThrow(_cursor, "ada_diet");
      final int _cursorIndexOfFluidRestriction = CursorUtil.getColumnIndexOrThrow(_cursor, "fluid_restriction");
      final int _cursorIndexOfTextureModifications = CursorUtil.getColumnIndexOrThrow(_cursor, "texture_modifications");
      final int _cursorIndexOfMechanicalChopped = CursorUtil.getColumnIndexOrThrow(_cursor, "mechanical_chopped");
      final int _cursorIndexOfMechanicalGround = CursorUtil.getColumnIndexOrThrow(_cursor, "mechanical_ground");
      final int _cursorIndexOfBiteSize = CursorUtil.getColumnIndexOrThrow(_cursor, "bite_size");
      final int _cursorIndexOfBreadOK = CursorUtil.getColumnIndexOrThrow(_cursor, "bread_ok");
      final int _cursorIndexOfNectarThick = CursorUtil.getColumnIndexOrThrow(_cursor, "nectar_thick");
      final int _cursorIndexOfPuddingThick = CursorUtil.getColumnIndexOrThrow(_cursor, "pudding_thick");
      final int _cursorIndexOfHoneyThick = CursorUtil.getColumnIndexOrThrow(_cursor, "honey_thick");
      final int _cursorIndexOfExtraGravy = CursorUtil.getColumnIndexOrThrow(_cursor, "extra_gravy");
      final int _cursorIndexOfMeatsOnly = CursorUtil.getColumnIndexOrThrow(_cursor, "meats_only");
      final int _cursorIndexOfBreakfastComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_complete");
      final int _cursorIndexOfLunchComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_complete");
      final int _cursorIndexOfDinnerComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_complete");
      final int _cursorIndexOfBreakfastNPO = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_npo");
      final int _cursorIndexOfLunchNPO = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_npo");
      final int _cursorIndexOfDinnerNPO = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_npo");
      final int _cursorIndexOfBreakfastItems = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_items");
      final int _cursorIndexOfLunchItems = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_items");
      final int _cursorIndexOfDinnerItems = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_items");
      final int _cursorIndexOfBreakfastJuices = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_juices");
      final int _cursorIndexOfLunchJuices = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_juices");
      final int _cursorIndexOfDinnerJuices = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_juices");
      final int _cursorIndexOfBreakfastDrinks = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_drinks");
      final int _cursorIndexOfLunchDrinks = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_drinks");
      final int _cursorIndexOfDinnerDrinks = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_drinks");
      final int _cursorIndexOfCreatedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "created_date");
      final int _cursorIndexOfModifiedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "modified_date");
      final List<PatientEntity> _result = new ArrayList<PatientEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final PatientEntity _item;
        _item = new PatientEntity();
        final long _tmpPatientId;
        _tmpPatientId = _cursor.getLong(_cursorIndexOfPatientId);
        _item.setPatientId(_tmpPatientId);
        final String _tmpPatientFirstName;
        if (_cursor.isNull(_cursorIndexOfPatientFirstName)) {
          _tmpPatientFirstName = null;
        } else {
          _tmpPatientFirstName = _cursor.getString(_cursorIndexOfPatientFirstName);
        }
        _item.setPatientFirstName(_tmpPatientFirstName);
        final String _tmpPatientLastName;
        if (_cursor.isNull(_cursorIndexOfPatientLastName)) {
          _tmpPatientLastName = null;
        } else {
          _tmpPatientLastName = _cursor.getString(_cursorIndexOfPatientLastName);
        }
        _item.setPatientLastName(_tmpPatientLastName);
        final String _tmpWing;
        if (_cursor.isNull(_cursorIndexOfWing)) {
          _tmpWing = null;
        } else {
          _tmpWing = _cursor.getString(_cursorIndexOfWing);
        }
        _item.setWing(_tmpWing);
        final String _tmpRoomNumber;
        if (_cursor.isNull(_cursorIndexOfRoomNumber)) {
          _tmpRoomNumber = null;
        } else {
          _tmpRoomNumber = _cursor.getString(_cursorIndexOfRoomNumber);
        }
        _item.setRoomNumber(_tmpRoomNumber);
        final String _tmpDietType;
        if (_cursor.isNull(_cursorIndexOfDietType)) {
          _tmpDietType = null;
        } else {
          _tmpDietType = _cursor.getString(_cursorIndexOfDietType);
        }
        _item.setDietType(_tmpDietType);
        final String _tmpDiet;
        if (_cursor.isNull(_cursorIndexOfDiet)) {
          _tmpDiet = null;
        } else {
          _tmpDiet = _cursor.getString(_cursorIndexOfDiet);
        }
        _item.setDiet(_tmpDiet);
        final boolean _tmpAdaDiet;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfAdaDiet);
        _tmpAdaDiet = _tmp != 0;
        _item.setAdaDiet(_tmpAdaDiet);
        final String _tmpFluidRestriction;
        if (_cursor.isNull(_cursorIndexOfFluidRestriction)) {
          _tmpFluidRestriction = null;
        } else {
          _tmpFluidRestriction = _cursor.getString(_cursorIndexOfFluidRestriction);
        }
        _item.setFluidRestriction(_tmpFluidRestriction);
        final String _tmpTextureModifications;
        if (_cursor.isNull(_cursorIndexOfTextureModifications)) {
          _tmpTextureModifications = null;
        } else {
          _tmpTextureModifications = _cursor.getString(_cursorIndexOfTextureModifications);
        }
        _item.setTextureModifications(_tmpTextureModifications);
        final boolean _tmpMechanicalChopped;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfMechanicalChopped);
        _tmpMechanicalChopped = _tmp_1 != 0;
        _item.setMechanicalChopped(_tmpMechanicalChopped);
        final boolean _tmpMechanicalGround;
        final int _tmp_2;
        _tmp_2 = _cursor.getInt(_cursorIndexOfMechanicalGround);
        _tmpMechanicalGround = _tmp_2 != 0;
        _item.setMechanicalGround(_tmpMechanicalGround);
        final boolean _tmpBiteSize;
        final int _tmp_3;
        _tmp_3 = _cursor.getInt(_cursorIndexOfBiteSize);
        _tmpBiteSize = _tmp_3 != 0;
        _item.setBiteSize(_tmpBiteSize);
        final boolean _tmpBreadOK;
        final int _tmp_4;
        _tmp_4 = _cursor.getInt(_cursorIndexOfBreadOK);
        _tmpBreadOK = _tmp_4 != 0;
        _item.setBreadOK(_tmpBreadOK);
        final boolean _tmpNectarThick;
        final int _tmp_5;
        _tmp_5 = _cursor.getInt(_cursorIndexOfNectarThick);
        _tmpNectarThick = _tmp_5 != 0;
        _item.setNectarThick(_tmpNectarThick);
        final boolean _tmpPuddingThick;
        final int _tmp_6;
        _tmp_6 = _cursor.getInt(_cursorIndexOfPuddingThick);
        _tmpPuddingThick = _tmp_6 != 0;
        _item.setPuddingThick(_tmpPuddingThick);
        final boolean _tmpHoneyThick;
        final int _tmp_7;
        _tmp_7 = _cursor.getInt(_cursorIndexOfHoneyThick);
        _tmpHoneyThick = _tmp_7 != 0;
        _item.setHoneyThick(_tmpHoneyThick);
        final boolean _tmpExtraGravy;
        final int _tmp_8;
        _tmp_8 = _cursor.getInt(_cursorIndexOfExtraGravy);
        _tmpExtraGravy = _tmp_8 != 0;
        _item.setExtraGravy(_tmpExtraGravy);
        final boolean _tmpMeatsOnly;
        final int _tmp_9;
        _tmp_9 = _cursor.getInt(_cursorIndexOfMeatsOnly);
        _tmpMeatsOnly = _tmp_9 != 0;
        _item.setMeatsOnly(_tmpMeatsOnly);
        final boolean _tmpBreakfastComplete;
        final int _tmp_10;
        _tmp_10 = _cursor.getInt(_cursorIndexOfBreakfastComplete);
        _tmpBreakfastComplete = _tmp_10 != 0;
        _item.setBreakfastComplete(_tmpBreakfastComplete);
        final boolean _tmpLunchComplete;
        final int _tmp_11;
        _tmp_11 = _cursor.getInt(_cursorIndexOfLunchComplete);
        _tmpLunchComplete = _tmp_11 != 0;
        _item.setLunchComplete(_tmpLunchComplete);
        final boolean _tmpDinnerComplete;
        final int _tmp_12;
        _tmp_12 = _cursor.getInt(_cursorIndexOfDinnerComplete);
        _tmpDinnerComplete = _tmp_12 != 0;
        _item.setDinnerComplete(_tmpDinnerComplete);
        final boolean _tmpBreakfastNPO;
        final int _tmp_13;
        _tmp_13 = _cursor.getInt(_cursorIndexOfBreakfastNPO);
        _tmpBreakfastNPO = _tmp_13 != 0;
        _item.setBreakfastNPO(_tmpBreakfastNPO);
        final boolean _tmpLunchNPO;
        final int _tmp_14;
        _tmp_14 = _cursor.getInt(_cursorIndexOfLunchNPO);
        _tmpLunchNPO = _tmp_14 != 0;
        _item.setLunchNPO(_tmpLunchNPO);
        final boolean _tmpDinnerNPO;
        final int _tmp_15;
        _tmp_15 = _cursor.getInt(_cursorIndexOfDinnerNPO);
        _tmpDinnerNPO = _tmp_15 != 0;
        _item.setDinnerNPO(_tmpDinnerNPO);
        final String _tmpBreakfastItems;
        if (_cursor.isNull(_cursorIndexOfBreakfastItems)) {
          _tmpBreakfastItems = null;
        } else {
          _tmpBreakfastItems = _cursor.getString(_cursorIndexOfBreakfastItems);
        }
        _item.setBreakfastItems(_tmpBreakfastItems);
        final String _tmpLunchItems;
        if (_cursor.isNull(_cursorIndexOfLunchItems)) {
          _tmpLunchItems = null;
        } else {
          _tmpLunchItems = _cursor.getString(_cursorIndexOfLunchItems);
        }
        _item.setLunchItems(_tmpLunchItems);
        final String _tmpDinnerItems;
        if (_cursor.isNull(_cursorIndexOfDinnerItems)) {
          _tmpDinnerItems = null;
        } else {
          _tmpDinnerItems = _cursor.getString(_cursorIndexOfDinnerItems);
        }
        _item.setDinnerItems(_tmpDinnerItems);
        final String _tmpBreakfastJuices;
        if (_cursor.isNull(_cursorIndexOfBreakfastJuices)) {
          _tmpBreakfastJuices = null;
        } else {
          _tmpBreakfastJuices = _cursor.getString(_cursorIndexOfBreakfastJuices);
        }
        _item.setBreakfastJuices(_tmpBreakfastJuices);
        final String _tmpLunchJuices;
        if (_cursor.isNull(_cursorIndexOfLunchJuices)) {
          _tmpLunchJuices = null;
        } else {
          _tmpLunchJuices = _cursor.getString(_cursorIndexOfLunchJuices);
        }
        _item.setLunchJuices(_tmpLunchJuices);
        final String _tmpDinnerJuices;
        if (_cursor.isNull(_cursorIndexOfDinnerJuices)) {
          _tmpDinnerJuices = null;
        } else {
          _tmpDinnerJuices = _cursor.getString(_cursorIndexOfDinnerJuices);
        }
        _item.setDinnerJuices(_tmpDinnerJuices);
        final String _tmpBreakfastDrinks;
        if (_cursor.isNull(_cursorIndexOfBreakfastDrinks)) {
          _tmpBreakfastDrinks = null;
        } else {
          _tmpBreakfastDrinks = _cursor.getString(_cursorIndexOfBreakfastDrinks);
        }
        _item.setBreakfastDrinks(_tmpBreakfastDrinks);
        final String _tmpLunchDrinks;
        if (_cursor.isNull(_cursorIndexOfLunchDrinks)) {
          _tmpLunchDrinks = null;
        } else {
          _tmpLunchDrinks = _cursor.getString(_cursorIndexOfLunchDrinks);
        }
        _item.setLunchDrinks(_tmpLunchDrinks);
        final String _tmpDinnerDrinks;
        if (_cursor.isNull(_cursorIndexOfDinnerDrinks)) {
          _tmpDinnerDrinks = null;
        } else {
          _tmpDinnerDrinks = _cursor.getString(_cursorIndexOfDinnerDrinks);
        }
        _item.setDinnerDrinks(_tmpDinnerDrinks);
        final Date _tmpCreatedDate;
        final Long _tmp_16;
        if (_cursor.isNull(_cursorIndexOfCreatedDate)) {
          _tmp_16 = null;
        } else {
          _tmp_16 = _cursor.getLong(_cursorIndexOfCreatedDate);
        }
        _tmpCreatedDate = DateConverter.fromTimestamp(_tmp_16);
        _item.setCreatedDate(_tmpCreatedDate);
        final Date _tmpModifiedDate;
        final Long _tmp_17;
        if (_cursor.isNull(_cursorIndexOfModifiedDate)) {
          _tmp_17 = null;
        } else {
          _tmp_17 = _cursor.getLong(_cursorIndexOfModifiedDate);
        }
        _tmpModifiedDate = DateConverter.fromTimestamp(_tmp_17);
        _item.setModifiedDate(_tmpModifiedDate);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public LiveData<List<PatientEntity>> getCompletedPatientsLive() {
    final String _sql = "SELECT * FROM patient_info WHERE breakfast_complete = 1 AND lunch_complete = 1 AND dinner_complete = 1 ORDER BY wing, CAST(room_number AS INTEGER)";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"patient_info"}, false, new Callable<List<PatientEntity>>() {
      @Override
      @Nullable
      public List<PatientEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfPatientId = CursorUtil.getColumnIndexOrThrow(_cursor, "patient_id");
          final int _cursorIndexOfPatientFirstName = CursorUtil.getColumnIndexOrThrow(_cursor, "patient_first_name");
          final int _cursorIndexOfPatientLastName = CursorUtil.getColumnIndexOrThrow(_cursor, "patient_last_name");
          final int _cursorIndexOfWing = CursorUtil.getColumnIndexOrThrow(_cursor, "wing");
          final int _cursorIndexOfRoomNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "room_number");
          final int _cursorIndexOfDietType = CursorUtil.getColumnIndexOrThrow(_cursor, "diet_type");
          final int _cursorIndexOfDiet = CursorUtil.getColumnIndexOrThrow(_cursor, "diet");
          final int _cursorIndexOfAdaDiet = CursorUtil.getColumnIndexOrThrow(_cursor, "ada_diet");
          final int _cursorIndexOfFluidRestriction = CursorUtil.getColumnIndexOrThrow(_cursor, "fluid_restriction");
          final int _cursorIndexOfTextureModifications = CursorUtil.getColumnIndexOrThrow(_cursor, "texture_modifications");
          final int _cursorIndexOfMechanicalChopped = CursorUtil.getColumnIndexOrThrow(_cursor, "mechanical_chopped");
          final int _cursorIndexOfMechanicalGround = CursorUtil.getColumnIndexOrThrow(_cursor, "mechanical_ground");
          final int _cursorIndexOfBiteSize = CursorUtil.getColumnIndexOrThrow(_cursor, "bite_size");
          final int _cursorIndexOfBreadOK = CursorUtil.getColumnIndexOrThrow(_cursor, "bread_ok");
          final int _cursorIndexOfNectarThick = CursorUtil.getColumnIndexOrThrow(_cursor, "nectar_thick");
          final int _cursorIndexOfPuddingThick = CursorUtil.getColumnIndexOrThrow(_cursor, "pudding_thick");
          final int _cursorIndexOfHoneyThick = CursorUtil.getColumnIndexOrThrow(_cursor, "honey_thick");
          final int _cursorIndexOfExtraGravy = CursorUtil.getColumnIndexOrThrow(_cursor, "extra_gravy");
          final int _cursorIndexOfMeatsOnly = CursorUtil.getColumnIndexOrThrow(_cursor, "meats_only");
          final int _cursorIndexOfBreakfastComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_complete");
          final int _cursorIndexOfLunchComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_complete");
          final int _cursorIndexOfDinnerComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_complete");
          final int _cursorIndexOfBreakfastNPO = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_npo");
          final int _cursorIndexOfLunchNPO = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_npo");
          final int _cursorIndexOfDinnerNPO = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_npo");
          final int _cursorIndexOfBreakfastItems = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_items");
          final int _cursorIndexOfLunchItems = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_items");
          final int _cursorIndexOfDinnerItems = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_items");
          final int _cursorIndexOfBreakfastJuices = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_juices");
          final int _cursorIndexOfLunchJuices = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_juices");
          final int _cursorIndexOfDinnerJuices = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_juices");
          final int _cursorIndexOfBreakfastDrinks = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_drinks");
          final int _cursorIndexOfLunchDrinks = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_drinks");
          final int _cursorIndexOfDinnerDrinks = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_drinks");
          final int _cursorIndexOfCreatedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "created_date");
          final int _cursorIndexOfModifiedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "modified_date");
          final List<PatientEntity> _result = new ArrayList<PatientEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PatientEntity _item;
            _item = new PatientEntity();
            final long _tmpPatientId;
            _tmpPatientId = _cursor.getLong(_cursorIndexOfPatientId);
            _item.setPatientId(_tmpPatientId);
            final String _tmpPatientFirstName;
            if (_cursor.isNull(_cursorIndexOfPatientFirstName)) {
              _tmpPatientFirstName = null;
            } else {
              _tmpPatientFirstName = _cursor.getString(_cursorIndexOfPatientFirstName);
            }
            _item.setPatientFirstName(_tmpPatientFirstName);
            final String _tmpPatientLastName;
            if (_cursor.isNull(_cursorIndexOfPatientLastName)) {
              _tmpPatientLastName = null;
            } else {
              _tmpPatientLastName = _cursor.getString(_cursorIndexOfPatientLastName);
            }
            _item.setPatientLastName(_tmpPatientLastName);
            final String _tmpWing;
            if (_cursor.isNull(_cursorIndexOfWing)) {
              _tmpWing = null;
            } else {
              _tmpWing = _cursor.getString(_cursorIndexOfWing);
            }
            _item.setWing(_tmpWing);
            final String _tmpRoomNumber;
            if (_cursor.isNull(_cursorIndexOfRoomNumber)) {
              _tmpRoomNumber = null;
            } else {
              _tmpRoomNumber = _cursor.getString(_cursorIndexOfRoomNumber);
            }
            _item.setRoomNumber(_tmpRoomNumber);
            final String _tmpDietType;
            if (_cursor.isNull(_cursorIndexOfDietType)) {
              _tmpDietType = null;
            } else {
              _tmpDietType = _cursor.getString(_cursorIndexOfDietType);
            }
            _item.setDietType(_tmpDietType);
            final String _tmpDiet;
            if (_cursor.isNull(_cursorIndexOfDiet)) {
              _tmpDiet = null;
            } else {
              _tmpDiet = _cursor.getString(_cursorIndexOfDiet);
            }
            _item.setDiet(_tmpDiet);
            final boolean _tmpAdaDiet;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfAdaDiet);
            _tmpAdaDiet = _tmp != 0;
            _item.setAdaDiet(_tmpAdaDiet);
            final String _tmpFluidRestriction;
            if (_cursor.isNull(_cursorIndexOfFluidRestriction)) {
              _tmpFluidRestriction = null;
            } else {
              _tmpFluidRestriction = _cursor.getString(_cursorIndexOfFluidRestriction);
            }
            _item.setFluidRestriction(_tmpFluidRestriction);
            final String _tmpTextureModifications;
            if (_cursor.isNull(_cursorIndexOfTextureModifications)) {
              _tmpTextureModifications = null;
            } else {
              _tmpTextureModifications = _cursor.getString(_cursorIndexOfTextureModifications);
            }
            _item.setTextureModifications(_tmpTextureModifications);
            final boolean _tmpMechanicalChopped;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfMechanicalChopped);
            _tmpMechanicalChopped = _tmp_1 != 0;
            _item.setMechanicalChopped(_tmpMechanicalChopped);
            final boolean _tmpMechanicalGround;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfMechanicalGround);
            _tmpMechanicalGround = _tmp_2 != 0;
            _item.setMechanicalGround(_tmpMechanicalGround);
            final boolean _tmpBiteSize;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfBiteSize);
            _tmpBiteSize = _tmp_3 != 0;
            _item.setBiteSize(_tmpBiteSize);
            final boolean _tmpBreadOK;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfBreadOK);
            _tmpBreadOK = _tmp_4 != 0;
            _item.setBreadOK(_tmpBreadOK);
            final boolean _tmpNectarThick;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfNectarThick);
            _tmpNectarThick = _tmp_5 != 0;
            _item.setNectarThick(_tmpNectarThick);
            final boolean _tmpPuddingThick;
            final int _tmp_6;
            _tmp_6 = _cursor.getInt(_cursorIndexOfPuddingThick);
            _tmpPuddingThick = _tmp_6 != 0;
            _item.setPuddingThick(_tmpPuddingThick);
            final boolean _tmpHoneyThick;
            final int _tmp_7;
            _tmp_7 = _cursor.getInt(_cursorIndexOfHoneyThick);
            _tmpHoneyThick = _tmp_7 != 0;
            _item.setHoneyThick(_tmpHoneyThick);
            final boolean _tmpExtraGravy;
            final int _tmp_8;
            _tmp_8 = _cursor.getInt(_cursorIndexOfExtraGravy);
            _tmpExtraGravy = _tmp_8 != 0;
            _item.setExtraGravy(_tmpExtraGravy);
            final boolean _tmpMeatsOnly;
            final int _tmp_9;
            _tmp_9 = _cursor.getInt(_cursorIndexOfMeatsOnly);
            _tmpMeatsOnly = _tmp_9 != 0;
            _item.setMeatsOnly(_tmpMeatsOnly);
            final boolean _tmpBreakfastComplete;
            final int _tmp_10;
            _tmp_10 = _cursor.getInt(_cursorIndexOfBreakfastComplete);
            _tmpBreakfastComplete = _tmp_10 != 0;
            _item.setBreakfastComplete(_tmpBreakfastComplete);
            final boolean _tmpLunchComplete;
            final int _tmp_11;
            _tmp_11 = _cursor.getInt(_cursorIndexOfLunchComplete);
            _tmpLunchComplete = _tmp_11 != 0;
            _item.setLunchComplete(_tmpLunchComplete);
            final boolean _tmpDinnerComplete;
            final int _tmp_12;
            _tmp_12 = _cursor.getInt(_cursorIndexOfDinnerComplete);
            _tmpDinnerComplete = _tmp_12 != 0;
            _item.setDinnerComplete(_tmpDinnerComplete);
            final boolean _tmpBreakfastNPO;
            final int _tmp_13;
            _tmp_13 = _cursor.getInt(_cursorIndexOfBreakfastNPO);
            _tmpBreakfastNPO = _tmp_13 != 0;
            _item.setBreakfastNPO(_tmpBreakfastNPO);
            final boolean _tmpLunchNPO;
            final int _tmp_14;
            _tmp_14 = _cursor.getInt(_cursorIndexOfLunchNPO);
            _tmpLunchNPO = _tmp_14 != 0;
            _item.setLunchNPO(_tmpLunchNPO);
            final boolean _tmpDinnerNPO;
            final int _tmp_15;
            _tmp_15 = _cursor.getInt(_cursorIndexOfDinnerNPO);
            _tmpDinnerNPO = _tmp_15 != 0;
            _item.setDinnerNPO(_tmpDinnerNPO);
            final String _tmpBreakfastItems;
            if (_cursor.isNull(_cursorIndexOfBreakfastItems)) {
              _tmpBreakfastItems = null;
            } else {
              _tmpBreakfastItems = _cursor.getString(_cursorIndexOfBreakfastItems);
            }
            _item.setBreakfastItems(_tmpBreakfastItems);
            final String _tmpLunchItems;
            if (_cursor.isNull(_cursorIndexOfLunchItems)) {
              _tmpLunchItems = null;
            } else {
              _tmpLunchItems = _cursor.getString(_cursorIndexOfLunchItems);
            }
            _item.setLunchItems(_tmpLunchItems);
            final String _tmpDinnerItems;
            if (_cursor.isNull(_cursorIndexOfDinnerItems)) {
              _tmpDinnerItems = null;
            } else {
              _tmpDinnerItems = _cursor.getString(_cursorIndexOfDinnerItems);
            }
            _item.setDinnerItems(_tmpDinnerItems);
            final String _tmpBreakfastJuices;
            if (_cursor.isNull(_cursorIndexOfBreakfastJuices)) {
              _tmpBreakfastJuices = null;
            } else {
              _tmpBreakfastJuices = _cursor.getString(_cursorIndexOfBreakfastJuices);
            }
            _item.setBreakfastJuices(_tmpBreakfastJuices);
            final String _tmpLunchJuices;
            if (_cursor.isNull(_cursorIndexOfLunchJuices)) {
              _tmpLunchJuices = null;
            } else {
              _tmpLunchJuices = _cursor.getString(_cursorIndexOfLunchJuices);
            }
            _item.setLunchJuices(_tmpLunchJuices);
            final String _tmpDinnerJuices;
            if (_cursor.isNull(_cursorIndexOfDinnerJuices)) {
              _tmpDinnerJuices = null;
            } else {
              _tmpDinnerJuices = _cursor.getString(_cursorIndexOfDinnerJuices);
            }
            _item.setDinnerJuices(_tmpDinnerJuices);
            final String _tmpBreakfastDrinks;
            if (_cursor.isNull(_cursorIndexOfBreakfastDrinks)) {
              _tmpBreakfastDrinks = null;
            } else {
              _tmpBreakfastDrinks = _cursor.getString(_cursorIndexOfBreakfastDrinks);
            }
            _item.setBreakfastDrinks(_tmpBreakfastDrinks);
            final String _tmpLunchDrinks;
            if (_cursor.isNull(_cursorIndexOfLunchDrinks)) {
              _tmpLunchDrinks = null;
            } else {
              _tmpLunchDrinks = _cursor.getString(_cursorIndexOfLunchDrinks);
            }
            _item.setLunchDrinks(_tmpLunchDrinks);
            final String _tmpDinnerDrinks;
            if (_cursor.isNull(_cursorIndexOfDinnerDrinks)) {
              _tmpDinnerDrinks = null;
            } else {
              _tmpDinnerDrinks = _cursor.getString(_cursorIndexOfDinnerDrinks);
            }
            _item.setDinnerDrinks(_tmpDinnerDrinks);
            final Date _tmpCreatedDate;
            final Long _tmp_16;
            if (_cursor.isNull(_cursorIndexOfCreatedDate)) {
              _tmp_16 = null;
            } else {
              _tmp_16 = _cursor.getLong(_cursorIndexOfCreatedDate);
            }
            _tmpCreatedDate = DateConverter.fromTimestamp(_tmp_16);
            _item.setCreatedDate(_tmpCreatedDate);
            final Date _tmpModifiedDate;
            final Long _tmp_17;
            if (_cursor.isNull(_cursorIndexOfModifiedDate)) {
              _tmp_17 = null;
            } else {
              _tmp_17 = _cursor.getLong(_cursorIndexOfModifiedDate);
            }
            _tmpModifiedDate = DateConverter.fromTimestamp(_tmp_17);
            _item.setModifiedDate(_tmpModifiedDate);
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
  public List<PatientEntity> getCompletedPatients() {
    final String _sql = "SELECT * FROM patient_info WHERE breakfast_complete = 1 AND lunch_complete = 1 AND dinner_complete = 1 ORDER BY wing, CAST(room_number AS INTEGER)";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfPatientId = CursorUtil.getColumnIndexOrThrow(_cursor, "patient_id");
      final int _cursorIndexOfPatientFirstName = CursorUtil.getColumnIndexOrThrow(_cursor, "patient_first_name");
      final int _cursorIndexOfPatientLastName = CursorUtil.getColumnIndexOrThrow(_cursor, "patient_last_name");
      final int _cursorIndexOfWing = CursorUtil.getColumnIndexOrThrow(_cursor, "wing");
      final int _cursorIndexOfRoomNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "room_number");
      final int _cursorIndexOfDietType = CursorUtil.getColumnIndexOrThrow(_cursor, "diet_type");
      final int _cursorIndexOfDiet = CursorUtil.getColumnIndexOrThrow(_cursor, "diet");
      final int _cursorIndexOfAdaDiet = CursorUtil.getColumnIndexOrThrow(_cursor, "ada_diet");
      final int _cursorIndexOfFluidRestriction = CursorUtil.getColumnIndexOrThrow(_cursor, "fluid_restriction");
      final int _cursorIndexOfTextureModifications = CursorUtil.getColumnIndexOrThrow(_cursor, "texture_modifications");
      final int _cursorIndexOfMechanicalChopped = CursorUtil.getColumnIndexOrThrow(_cursor, "mechanical_chopped");
      final int _cursorIndexOfMechanicalGround = CursorUtil.getColumnIndexOrThrow(_cursor, "mechanical_ground");
      final int _cursorIndexOfBiteSize = CursorUtil.getColumnIndexOrThrow(_cursor, "bite_size");
      final int _cursorIndexOfBreadOK = CursorUtil.getColumnIndexOrThrow(_cursor, "bread_ok");
      final int _cursorIndexOfNectarThick = CursorUtil.getColumnIndexOrThrow(_cursor, "nectar_thick");
      final int _cursorIndexOfPuddingThick = CursorUtil.getColumnIndexOrThrow(_cursor, "pudding_thick");
      final int _cursorIndexOfHoneyThick = CursorUtil.getColumnIndexOrThrow(_cursor, "honey_thick");
      final int _cursorIndexOfExtraGravy = CursorUtil.getColumnIndexOrThrow(_cursor, "extra_gravy");
      final int _cursorIndexOfMeatsOnly = CursorUtil.getColumnIndexOrThrow(_cursor, "meats_only");
      final int _cursorIndexOfBreakfastComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_complete");
      final int _cursorIndexOfLunchComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_complete");
      final int _cursorIndexOfDinnerComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_complete");
      final int _cursorIndexOfBreakfastNPO = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_npo");
      final int _cursorIndexOfLunchNPO = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_npo");
      final int _cursorIndexOfDinnerNPO = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_npo");
      final int _cursorIndexOfBreakfastItems = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_items");
      final int _cursorIndexOfLunchItems = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_items");
      final int _cursorIndexOfDinnerItems = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_items");
      final int _cursorIndexOfBreakfastJuices = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_juices");
      final int _cursorIndexOfLunchJuices = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_juices");
      final int _cursorIndexOfDinnerJuices = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_juices");
      final int _cursorIndexOfBreakfastDrinks = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_drinks");
      final int _cursorIndexOfLunchDrinks = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_drinks");
      final int _cursorIndexOfDinnerDrinks = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_drinks");
      final int _cursorIndexOfCreatedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "created_date");
      final int _cursorIndexOfModifiedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "modified_date");
      final List<PatientEntity> _result = new ArrayList<PatientEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final PatientEntity _item;
        _item = new PatientEntity();
        final long _tmpPatientId;
        _tmpPatientId = _cursor.getLong(_cursorIndexOfPatientId);
        _item.setPatientId(_tmpPatientId);
        final String _tmpPatientFirstName;
        if (_cursor.isNull(_cursorIndexOfPatientFirstName)) {
          _tmpPatientFirstName = null;
        } else {
          _tmpPatientFirstName = _cursor.getString(_cursorIndexOfPatientFirstName);
        }
        _item.setPatientFirstName(_tmpPatientFirstName);
        final String _tmpPatientLastName;
        if (_cursor.isNull(_cursorIndexOfPatientLastName)) {
          _tmpPatientLastName = null;
        } else {
          _tmpPatientLastName = _cursor.getString(_cursorIndexOfPatientLastName);
        }
        _item.setPatientLastName(_tmpPatientLastName);
        final String _tmpWing;
        if (_cursor.isNull(_cursorIndexOfWing)) {
          _tmpWing = null;
        } else {
          _tmpWing = _cursor.getString(_cursorIndexOfWing);
        }
        _item.setWing(_tmpWing);
        final String _tmpRoomNumber;
        if (_cursor.isNull(_cursorIndexOfRoomNumber)) {
          _tmpRoomNumber = null;
        } else {
          _tmpRoomNumber = _cursor.getString(_cursorIndexOfRoomNumber);
        }
        _item.setRoomNumber(_tmpRoomNumber);
        final String _tmpDietType;
        if (_cursor.isNull(_cursorIndexOfDietType)) {
          _tmpDietType = null;
        } else {
          _tmpDietType = _cursor.getString(_cursorIndexOfDietType);
        }
        _item.setDietType(_tmpDietType);
        final String _tmpDiet;
        if (_cursor.isNull(_cursorIndexOfDiet)) {
          _tmpDiet = null;
        } else {
          _tmpDiet = _cursor.getString(_cursorIndexOfDiet);
        }
        _item.setDiet(_tmpDiet);
        final boolean _tmpAdaDiet;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfAdaDiet);
        _tmpAdaDiet = _tmp != 0;
        _item.setAdaDiet(_tmpAdaDiet);
        final String _tmpFluidRestriction;
        if (_cursor.isNull(_cursorIndexOfFluidRestriction)) {
          _tmpFluidRestriction = null;
        } else {
          _tmpFluidRestriction = _cursor.getString(_cursorIndexOfFluidRestriction);
        }
        _item.setFluidRestriction(_tmpFluidRestriction);
        final String _tmpTextureModifications;
        if (_cursor.isNull(_cursorIndexOfTextureModifications)) {
          _tmpTextureModifications = null;
        } else {
          _tmpTextureModifications = _cursor.getString(_cursorIndexOfTextureModifications);
        }
        _item.setTextureModifications(_tmpTextureModifications);
        final boolean _tmpMechanicalChopped;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfMechanicalChopped);
        _tmpMechanicalChopped = _tmp_1 != 0;
        _item.setMechanicalChopped(_tmpMechanicalChopped);
        final boolean _tmpMechanicalGround;
        final int _tmp_2;
        _tmp_2 = _cursor.getInt(_cursorIndexOfMechanicalGround);
        _tmpMechanicalGround = _tmp_2 != 0;
        _item.setMechanicalGround(_tmpMechanicalGround);
        final boolean _tmpBiteSize;
        final int _tmp_3;
        _tmp_3 = _cursor.getInt(_cursorIndexOfBiteSize);
        _tmpBiteSize = _tmp_3 != 0;
        _item.setBiteSize(_tmpBiteSize);
        final boolean _tmpBreadOK;
        final int _tmp_4;
        _tmp_4 = _cursor.getInt(_cursorIndexOfBreadOK);
        _tmpBreadOK = _tmp_4 != 0;
        _item.setBreadOK(_tmpBreadOK);
        final boolean _tmpNectarThick;
        final int _tmp_5;
        _tmp_5 = _cursor.getInt(_cursorIndexOfNectarThick);
        _tmpNectarThick = _tmp_5 != 0;
        _item.setNectarThick(_tmpNectarThick);
        final boolean _tmpPuddingThick;
        final int _tmp_6;
        _tmp_6 = _cursor.getInt(_cursorIndexOfPuddingThick);
        _tmpPuddingThick = _tmp_6 != 0;
        _item.setPuddingThick(_tmpPuddingThick);
        final boolean _tmpHoneyThick;
        final int _tmp_7;
        _tmp_7 = _cursor.getInt(_cursorIndexOfHoneyThick);
        _tmpHoneyThick = _tmp_7 != 0;
        _item.setHoneyThick(_tmpHoneyThick);
        final boolean _tmpExtraGravy;
        final int _tmp_8;
        _tmp_8 = _cursor.getInt(_cursorIndexOfExtraGravy);
        _tmpExtraGravy = _tmp_8 != 0;
        _item.setExtraGravy(_tmpExtraGravy);
        final boolean _tmpMeatsOnly;
        final int _tmp_9;
        _tmp_9 = _cursor.getInt(_cursorIndexOfMeatsOnly);
        _tmpMeatsOnly = _tmp_9 != 0;
        _item.setMeatsOnly(_tmpMeatsOnly);
        final boolean _tmpBreakfastComplete;
        final int _tmp_10;
        _tmp_10 = _cursor.getInt(_cursorIndexOfBreakfastComplete);
        _tmpBreakfastComplete = _tmp_10 != 0;
        _item.setBreakfastComplete(_tmpBreakfastComplete);
        final boolean _tmpLunchComplete;
        final int _tmp_11;
        _tmp_11 = _cursor.getInt(_cursorIndexOfLunchComplete);
        _tmpLunchComplete = _tmp_11 != 0;
        _item.setLunchComplete(_tmpLunchComplete);
        final boolean _tmpDinnerComplete;
        final int _tmp_12;
        _tmp_12 = _cursor.getInt(_cursorIndexOfDinnerComplete);
        _tmpDinnerComplete = _tmp_12 != 0;
        _item.setDinnerComplete(_tmpDinnerComplete);
        final boolean _tmpBreakfastNPO;
        final int _tmp_13;
        _tmp_13 = _cursor.getInt(_cursorIndexOfBreakfastNPO);
        _tmpBreakfastNPO = _tmp_13 != 0;
        _item.setBreakfastNPO(_tmpBreakfastNPO);
        final boolean _tmpLunchNPO;
        final int _tmp_14;
        _tmp_14 = _cursor.getInt(_cursorIndexOfLunchNPO);
        _tmpLunchNPO = _tmp_14 != 0;
        _item.setLunchNPO(_tmpLunchNPO);
        final boolean _tmpDinnerNPO;
        final int _tmp_15;
        _tmp_15 = _cursor.getInt(_cursorIndexOfDinnerNPO);
        _tmpDinnerNPO = _tmp_15 != 0;
        _item.setDinnerNPO(_tmpDinnerNPO);
        final String _tmpBreakfastItems;
        if (_cursor.isNull(_cursorIndexOfBreakfastItems)) {
          _tmpBreakfastItems = null;
        } else {
          _tmpBreakfastItems = _cursor.getString(_cursorIndexOfBreakfastItems);
        }
        _item.setBreakfastItems(_tmpBreakfastItems);
        final String _tmpLunchItems;
        if (_cursor.isNull(_cursorIndexOfLunchItems)) {
          _tmpLunchItems = null;
        } else {
          _tmpLunchItems = _cursor.getString(_cursorIndexOfLunchItems);
        }
        _item.setLunchItems(_tmpLunchItems);
        final String _tmpDinnerItems;
        if (_cursor.isNull(_cursorIndexOfDinnerItems)) {
          _tmpDinnerItems = null;
        } else {
          _tmpDinnerItems = _cursor.getString(_cursorIndexOfDinnerItems);
        }
        _item.setDinnerItems(_tmpDinnerItems);
        final String _tmpBreakfastJuices;
        if (_cursor.isNull(_cursorIndexOfBreakfastJuices)) {
          _tmpBreakfastJuices = null;
        } else {
          _tmpBreakfastJuices = _cursor.getString(_cursorIndexOfBreakfastJuices);
        }
        _item.setBreakfastJuices(_tmpBreakfastJuices);
        final String _tmpLunchJuices;
        if (_cursor.isNull(_cursorIndexOfLunchJuices)) {
          _tmpLunchJuices = null;
        } else {
          _tmpLunchJuices = _cursor.getString(_cursorIndexOfLunchJuices);
        }
        _item.setLunchJuices(_tmpLunchJuices);
        final String _tmpDinnerJuices;
        if (_cursor.isNull(_cursorIndexOfDinnerJuices)) {
          _tmpDinnerJuices = null;
        } else {
          _tmpDinnerJuices = _cursor.getString(_cursorIndexOfDinnerJuices);
        }
        _item.setDinnerJuices(_tmpDinnerJuices);
        final String _tmpBreakfastDrinks;
        if (_cursor.isNull(_cursorIndexOfBreakfastDrinks)) {
          _tmpBreakfastDrinks = null;
        } else {
          _tmpBreakfastDrinks = _cursor.getString(_cursorIndexOfBreakfastDrinks);
        }
        _item.setBreakfastDrinks(_tmpBreakfastDrinks);
        final String _tmpLunchDrinks;
        if (_cursor.isNull(_cursorIndexOfLunchDrinks)) {
          _tmpLunchDrinks = null;
        } else {
          _tmpLunchDrinks = _cursor.getString(_cursorIndexOfLunchDrinks);
        }
        _item.setLunchDrinks(_tmpLunchDrinks);
        final String _tmpDinnerDrinks;
        if (_cursor.isNull(_cursorIndexOfDinnerDrinks)) {
          _tmpDinnerDrinks = null;
        } else {
          _tmpDinnerDrinks = _cursor.getString(_cursorIndexOfDinnerDrinks);
        }
        _item.setDinnerDrinks(_tmpDinnerDrinks);
        final Date _tmpCreatedDate;
        final Long _tmp_16;
        if (_cursor.isNull(_cursorIndexOfCreatedDate)) {
          _tmp_16 = null;
        } else {
          _tmp_16 = _cursor.getLong(_cursorIndexOfCreatedDate);
        }
        _tmpCreatedDate = DateConverter.fromTimestamp(_tmp_16);
        _item.setCreatedDate(_tmpCreatedDate);
        final Date _tmpModifiedDate;
        final Long _tmp_17;
        if (_cursor.isNull(_cursorIndexOfModifiedDate)) {
          _tmp_17 = null;
        } else {
          _tmp_17 = _cursor.getLong(_cursorIndexOfModifiedDate);
        }
        _tmpModifiedDate = DateConverter.fromTimestamp(_tmp_17);
        _item.setModifiedDate(_tmpModifiedDate);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public LiveData<List<PatientEntity>> getPatientsByWingLive(final String wing) {
    final String _sql = "SELECT * FROM patient_info WHERE wing = ? ORDER BY CAST(room_number AS INTEGER)";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (wing == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, wing);
    }
    return __db.getInvalidationTracker().createLiveData(new String[] {"patient_info"}, false, new Callable<List<PatientEntity>>() {
      @Override
      @Nullable
      public List<PatientEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfPatientId = CursorUtil.getColumnIndexOrThrow(_cursor, "patient_id");
          final int _cursorIndexOfPatientFirstName = CursorUtil.getColumnIndexOrThrow(_cursor, "patient_first_name");
          final int _cursorIndexOfPatientLastName = CursorUtil.getColumnIndexOrThrow(_cursor, "patient_last_name");
          final int _cursorIndexOfWing = CursorUtil.getColumnIndexOrThrow(_cursor, "wing");
          final int _cursorIndexOfRoomNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "room_number");
          final int _cursorIndexOfDietType = CursorUtil.getColumnIndexOrThrow(_cursor, "diet_type");
          final int _cursorIndexOfDiet = CursorUtil.getColumnIndexOrThrow(_cursor, "diet");
          final int _cursorIndexOfAdaDiet = CursorUtil.getColumnIndexOrThrow(_cursor, "ada_diet");
          final int _cursorIndexOfFluidRestriction = CursorUtil.getColumnIndexOrThrow(_cursor, "fluid_restriction");
          final int _cursorIndexOfTextureModifications = CursorUtil.getColumnIndexOrThrow(_cursor, "texture_modifications");
          final int _cursorIndexOfMechanicalChopped = CursorUtil.getColumnIndexOrThrow(_cursor, "mechanical_chopped");
          final int _cursorIndexOfMechanicalGround = CursorUtil.getColumnIndexOrThrow(_cursor, "mechanical_ground");
          final int _cursorIndexOfBiteSize = CursorUtil.getColumnIndexOrThrow(_cursor, "bite_size");
          final int _cursorIndexOfBreadOK = CursorUtil.getColumnIndexOrThrow(_cursor, "bread_ok");
          final int _cursorIndexOfNectarThick = CursorUtil.getColumnIndexOrThrow(_cursor, "nectar_thick");
          final int _cursorIndexOfPuddingThick = CursorUtil.getColumnIndexOrThrow(_cursor, "pudding_thick");
          final int _cursorIndexOfHoneyThick = CursorUtil.getColumnIndexOrThrow(_cursor, "honey_thick");
          final int _cursorIndexOfExtraGravy = CursorUtil.getColumnIndexOrThrow(_cursor, "extra_gravy");
          final int _cursorIndexOfMeatsOnly = CursorUtil.getColumnIndexOrThrow(_cursor, "meats_only");
          final int _cursorIndexOfBreakfastComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_complete");
          final int _cursorIndexOfLunchComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_complete");
          final int _cursorIndexOfDinnerComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_complete");
          final int _cursorIndexOfBreakfastNPO = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_npo");
          final int _cursorIndexOfLunchNPO = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_npo");
          final int _cursorIndexOfDinnerNPO = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_npo");
          final int _cursorIndexOfBreakfastItems = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_items");
          final int _cursorIndexOfLunchItems = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_items");
          final int _cursorIndexOfDinnerItems = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_items");
          final int _cursorIndexOfBreakfastJuices = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_juices");
          final int _cursorIndexOfLunchJuices = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_juices");
          final int _cursorIndexOfDinnerJuices = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_juices");
          final int _cursorIndexOfBreakfastDrinks = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_drinks");
          final int _cursorIndexOfLunchDrinks = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_drinks");
          final int _cursorIndexOfDinnerDrinks = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_drinks");
          final int _cursorIndexOfCreatedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "created_date");
          final int _cursorIndexOfModifiedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "modified_date");
          final List<PatientEntity> _result = new ArrayList<PatientEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PatientEntity _item;
            _item = new PatientEntity();
            final long _tmpPatientId;
            _tmpPatientId = _cursor.getLong(_cursorIndexOfPatientId);
            _item.setPatientId(_tmpPatientId);
            final String _tmpPatientFirstName;
            if (_cursor.isNull(_cursorIndexOfPatientFirstName)) {
              _tmpPatientFirstName = null;
            } else {
              _tmpPatientFirstName = _cursor.getString(_cursorIndexOfPatientFirstName);
            }
            _item.setPatientFirstName(_tmpPatientFirstName);
            final String _tmpPatientLastName;
            if (_cursor.isNull(_cursorIndexOfPatientLastName)) {
              _tmpPatientLastName = null;
            } else {
              _tmpPatientLastName = _cursor.getString(_cursorIndexOfPatientLastName);
            }
            _item.setPatientLastName(_tmpPatientLastName);
            final String _tmpWing;
            if (_cursor.isNull(_cursorIndexOfWing)) {
              _tmpWing = null;
            } else {
              _tmpWing = _cursor.getString(_cursorIndexOfWing);
            }
            _item.setWing(_tmpWing);
            final String _tmpRoomNumber;
            if (_cursor.isNull(_cursorIndexOfRoomNumber)) {
              _tmpRoomNumber = null;
            } else {
              _tmpRoomNumber = _cursor.getString(_cursorIndexOfRoomNumber);
            }
            _item.setRoomNumber(_tmpRoomNumber);
            final String _tmpDietType;
            if (_cursor.isNull(_cursorIndexOfDietType)) {
              _tmpDietType = null;
            } else {
              _tmpDietType = _cursor.getString(_cursorIndexOfDietType);
            }
            _item.setDietType(_tmpDietType);
            final String _tmpDiet;
            if (_cursor.isNull(_cursorIndexOfDiet)) {
              _tmpDiet = null;
            } else {
              _tmpDiet = _cursor.getString(_cursorIndexOfDiet);
            }
            _item.setDiet(_tmpDiet);
            final boolean _tmpAdaDiet;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfAdaDiet);
            _tmpAdaDiet = _tmp != 0;
            _item.setAdaDiet(_tmpAdaDiet);
            final String _tmpFluidRestriction;
            if (_cursor.isNull(_cursorIndexOfFluidRestriction)) {
              _tmpFluidRestriction = null;
            } else {
              _tmpFluidRestriction = _cursor.getString(_cursorIndexOfFluidRestriction);
            }
            _item.setFluidRestriction(_tmpFluidRestriction);
            final String _tmpTextureModifications;
            if (_cursor.isNull(_cursorIndexOfTextureModifications)) {
              _tmpTextureModifications = null;
            } else {
              _tmpTextureModifications = _cursor.getString(_cursorIndexOfTextureModifications);
            }
            _item.setTextureModifications(_tmpTextureModifications);
            final boolean _tmpMechanicalChopped;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfMechanicalChopped);
            _tmpMechanicalChopped = _tmp_1 != 0;
            _item.setMechanicalChopped(_tmpMechanicalChopped);
            final boolean _tmpMechanicalGround;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfMechanicalGround);
            _tmpMechanicalGround = _tmp_2 != 0;
            _item.setMechanicalGround(_tmpMechanicalGround);
            final boolean _tmpBiteSize;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfBiteSize);
            _tmpBiteSize = _tmp_3 != 0;
            _item.setBiteSize(_tmpBiteSize);
            final boolean _tmpBreadOK;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfBreadOK);
            _tmpBreadOK = _tmp_4 != 0;
            _item.setBreadOK(_tmpBreadOK);
            final boolean _tmpNectarThick;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfNectarThick);
            _tmpNectarThick = _tmp_5 != 0;
            _item.setNectarThick(_tmpNectarThick);
            final boolean _tmpPuddingThick;
            final int _tmp_6;
            _tmp_6 = _cursor.getInt(_cursorIndexOfPuddingThick);
            _tmpPuddingThick = _tmp_6 != 0;
            _item.setPuddingThick(_tmpPuddingThick);
            final boolean _tmpHoneyThick;
            final int _tmp_7;
            _tmp_7 = _cursor.getInt(_cursorIndexOfHoneyThick);
            _tmpHoneyThick = _tmp_7 != 0;
            _item.setHoneyThick(_tmpHoneyThick);
            final boolean _tmpExtraGravy;
            final int _tmp_8;
            _tmp_8 = _cursor.getInt(_cursorIndexOfExtraGravy);
            _tmpExtraGravy = _tmp_8 != 0;
            _item.setExtraGravy(_tmpExtraGravy);
            final boolean _tmpMeatsOnly;
            final int _tmp_9;
            _tmp_9 = _cursor.getInt(_cursorIndexOfMeatsOnly);
            _tmpMeatsOnly = _tmp_9 != 0;
            _item.setMeatsOnly(_tmpMeatsOnly);
            final boolean _tmpBreakfastComplete;
            final int _tmp_10;
            _tmp_10 = _cursor.getInt(_cursorIndexOfBreakfastComplete);
            _tmpBreakfastComplete = _tmp_10 != 0;
            _item.setBreakfastComplete(_tmpBreakfastComplete);
            final boolean _tmpLunchComplete;
            final int _tmp_11;
            _tmp_11 = _cursor.getInt(_cursorIndexOfLunchComplete);
            _tmpLunchComplete = _tmp_11 != 0;
            _item.setLunchComplete(_tmpLunchComplete);
            final boolean _tmpDinnerComplete;
            final int _tmp_12;
            _tmp_12 = _cursor.getInt(_cursorIndexOfDinnerComplete);
            _tmpDinnerComplete = _tmp_12 != 0;
            _item.setDinnerComplete(_tmpDinnerComplete);
            final boolean _tmpBreakfastNPO;
            final int _tmp_13;
            _tmp_13 = _cursor.getInt(_cursorIndexOfBreakfastNPO);
            _tmpBreakfastNPO = _tmp_13 != 0;
            _item.setBreakfastNPO(_tmpBreakfastNPO);
            final boolean _tmpLunchNPO;
            final int _tmp_14;
            _tmp_14 = _cursor.getInt(_cursorIndexOfLunchNPO);
            _tmpLunchNPO = _tmp_14 != 0;
            _item.setLunchNPO(_tmpLunchNPO);
            final boolean _tmpDinnerNPO;
            final int _tmp_15;
            _tmp_15 = _cursor.getInt(_cursorIndexOfDinnerNPO);
            _tmpDinnerNPO = _tmp_15 != 0;
            _item.setDinnerNPO(_tmpDinnerNPO);
            final String _tmpBreakfastItems;
            if (_cursor.isNull(_cursorIndexOfBreakfastItems)) {
              _tmpBreakfastItems = null;
            } else {
              _tmpBreakfastItems = _cursor.getString(_cursorIndexOfBreakfastItems);
            }
            _item.setBreakfastItems(_tmpBreakfastItems);
            final String _tmpLunchItems;
            if (_cursor.isNull(_cursorIndexOfLunchItems)) {
              _tmpLunchItems = null;
            } else {
              _tmpLunchItems = _cursor.getString(_cursorIndexOfLunchItems);
            }
            _item.setLunchItems(_tmpLunchItems);
            final String _tmpDinnerItems;
            if (_cursor.isNull(_cursorIndexOfDinnerItems)) {
              _tmpDinnerItems = null;
            } else {
              _tmpDinnerItems = _cursor.getString(_cursorIndexOfDinnerItems);
            }
            _item.setDinnerItems(_tmpDinnerItems);
            final String _tmpBreakfastJuices;
            if (_cursor.isNull(_cursorIndexOfBreakfastJuices)) {
              _tmpBreakfastJuices = null;
            } else {
              _tmpBreakfastJuices = _cursor.getString(_cursorIndexOfBreakfastJuices);
            }
            _item.setBreakfastJuices(_tmpBreakfastJuices);
            final String _tmpLunchJuices;
            if (_cursor.isNull(_cursorIndexOfLunchJuices)) {
              _tmpLunchJuices = null;
            } else {
              _tmpLunchJuices = _cursor.getString(_cursorIndexOfLunchJuices);
            }
            _item.setLunchJuices(_tmpLunchJuices);
            final String _tmpDinnerJuices;
            if (_cursor.isNull(_cursorIndexOfDinnerJuices)) {
              _tmpDinnerJuices = null;
            } else {
              _tmpDinnerJuices = _cursor.getString(_cursorIndexOfDinnerJuices);
            }
            _item.setDinnerJuices(_tmpDinnerJuices);
            final String _tmpBreakfastDrinks;
            if (_cursor.isNull(_cursorIndexOfBreakfastDrinks)) {
              _tmpBreakfastDrinks = null;
            } else {
              _tmpBreakfastDrinks = _cursor.getString(_cursorIndexOfBreakfastDrinks);
            }
            _item.setBreakfastDrinks(_tmpBreakfastDrinks);
            final String _tmpLunchDrinks;
            if (_cursor.isNull(_cursorIndexOfLunchDrinks)) {
              _tmpLunchDrinks = null;
            } else {
              _tmpLunchDrinks = _cursor.getString(_cursorIndexOfLunchDrinks);
            }
            _item.setLunchDrinks(_tmpLunchDrinks);
            final String _tmpDinnerDrinks;
            if (_cursor.isNull(_cursorIndexOfDinnerDrinks)) {
              _tmpDinnerDrinks = null;
            } else {
              _tmpDinnerDrinks = _cursor.getString(_cursorIndexOfDinnerDrinks);
            }
            _item.setDinnerDrinks(_tmpDinnerDrinks);
            final Date _tmpCreatedDate;
            final Long _tmp_16;
            if (_cursor.isNull(_cursorIndexOfCreatedDate)) {
              _tmp_16 = null;
            } else {
              _tmp_16 = _cursor.getLong(_cursorIndexOfCreatedDate);
            }
            _tmpCreatedDate = DateConverter.fromTimestamp(_tmp_16);
            _item.setCreatedDate(_tmpCreatedDate);
            final Date _tmpModifiedDate;
            final Long _tmp_17;
            if (_cursor.isNull(_cursorIndexOfModifiedDate)) {
              _tmp_17 = null;
            } else {
              _tmp_17 = _cursor.getLong(_cursorIndexOfModifiedDate);
            }
            _tmpModifiedDate = DateConverter.fromTimestamp(_tmp_17);
            _item.setModifiedDate(_tmpModifiedDate);
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
  public List<PatientEntity> getPatientsByWing(final String wing) {
    final String _sql = "SELECT * FROM patient_info WHERE wing = ? ORDER BY CAST(room_number AS INTEGER)";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (wing == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, wing);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfPatientId = CursorUtil.getColumnIndexOrThrow(_cursor, "patient_id");
      final int _cursorIndexOfPatientFirstName = CursorUtil.getColumnIndexOrThrow(_cursor, "patient_first_name");
      final int _cursorIndexOfPatientLastName = CursorUtil.getColumnIndexOrThrow(_cursor, "patient_last_name");
      final int _cursorIndexOfWing = CursorUtil.getColumnIndexOrThrow(_cursor, "wing");
      final int _cursorIndexOfRoomNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "room_number");
      final int _cursorIndexOfDietType = CursorUtil.getColumnIndexOrThrow(_cursor, "diet_type");
      final int _cursorIndexOfDiet = CursorUtil.getColumnIndexOrThrow(_cursor, "diet");
      final int _cursorIndexOfAdaDiet = CursorUtil.getColumnIndexOrThrow(_cursor, "ada_diet");
      final int _cursorIndexOfFluidRestriction = CursorUtil.getColumnIndexOrThrow(_cursor, "fluid_restriction");
      final int _cursorIndexOfTextureModifications = CursorUtil.getColumnIndexOrThrow(_cursor, "texture_modifications");
      final int _cursorIndexOfMechanicalChopped = CursorUtil.getColumnIndexOrThrow(_cursor, "mechanical_chopped");
      final int _cursorIndexOfMechanicalGround = CursorUtil.getColumnIndexOrThrow(_cursor, "mechanical_ground");
      final int _cursorIndexOfBiteSize = CursorUtil.getColumnIndexOrThrow(_cursor, "bite_size");
      final int _cursorIndexOfBreadOK = CursorUtil.getColumnIndexOrThrow(_cursor, "bread_ok");
      final int _cursorIndexOfNectarThick = CursorUtil.getColumnIndexOrThrow(_cursor, "nectar_thick");
      final int _cursorIndexOfPuddingThick = CursorUtil.getColumnIndexOrThrow(_cursor, "pudding_thick");
      final int _cursorIndexOfHoneyThick = CursorUtil.getColumnIndexOrThrow(_cursor, "honey_thick");
      final int _cursorIndexOfExtraGravy = CursorUtil.getColumnIndexOrThrow(_cursor, "extra_gravy");
      final int _cursorIndexOfMeatsOnly = CursorUtil.getColumnIndexOrThrow(_cursor, "meats_only");
      final int _cursorIndexOfBreakfastComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_complete");
      final int _cursorIndexOfLunchComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_complete");
      final int _cursorIndexOfDinnerComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_complete");
      final int _cursorIndexOfBreakfastNPO = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_npo");
      final int _cursorIndexOfLunchNPO = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_npo");
      final int _cursorIndexOfDinnerNPO = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_npo");
      final int _cursorIndexOfBreakfastItems = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_items");
      final int _cursorIndexOfLunchItems = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_items");
      final int _cursorIndexOfDinnerItems = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_items");
      final int _cursorIndexOfBreakfastJuices = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_juices");
      final int _cursorIndexOfLunchJuices = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_juices");
      final int _cursorIndexOfDinnerJuices = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_juices");
      final int _cursorIndexOfBreakfastDrinks = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_drinks");
      final int _cursorIndexOfLunchDrinks = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_drinks");
      final int _cursorIndexOfDinnerDrinks = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_drinks");
      final int _cursorIndexOfCreatedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "created_date");
      final int _cursorIndexOfModifiedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "modified_date");
      final List<PatientEntity> _result = new ArrayList<PatientEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final PatientEntity _item;
        _item = new PatientEntity();
        final long _tmpPatientId;
        _tmpPatientId = _cursor.getLong(_cursorIndexOfPatientId);
        _item.setPatientId(_tmpPatientId);
        final String _tmpPatientFirstName;
        if (_cursor.isNull(_cursorIndexOfPatientFirstName)) {
          _tmpPatientFirstName = null;
        } else {
          _tmpPatientFirstName = _cursor.getString(_cursorIndexOfPatientFirstName);
        }
        _item.setPatientFirstName(_tmpPatientFirstName);
        final String _tmpPatientLastName;
        if (_cursor.isNull(_cursorIndexOfPatientLastName)) {
          _tmpPatientLastName = null;
        } else {
          _tmpPatientLastName = _cursor.getString(_cursorIndexOfPatientLastName);
        }
        _item.setPatientLastName(_tmpPatientLastName);
        final String _tmpWing;
        if (_cursor.isNull(_cursorIndexOfWing)) {
          _tmpWing = null;
        } else {
          _tmpWing = _cursor.getString(_cursorIndexOfWing);
        }
        _item.setWing(_tmpWing);
        final String _tmpRoomNumber;
        if (_cursor.isNull(_cursorIndexOfRoomNumber)) {
          _tmpRoomNumber = null;
        } else {
          _tmpRoomNumber = _cursor.getString(_cursorIndexOfRoomNumber);
        }
        _item.setRoomNumber(_tmpRoomNumber);
        final String _tmpDietType;
        if (_cursor.isNull(_cursorIndexOfDietType)) {
          _tmpDietType = null;
        } else {
          _tmpDietType = _cursor.getString(_cursorIndexOfDietType);
        }
        _item.setDietType(_tmpDietType);
        final String _tmpDiet;
        if (_cursor.isNull(_cursorIndexOfDiet)) {
          _tmpDiet = null;
        } else {
          _tmpDiet = _cursor.getString(_cursorIndexOfDiet);
        }
        _item.setDiet(_tmpDiet);
        final boolean _tmpAdaDiet;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfAdaDiet);
        _tmpAdaDiet = _tmp != 0;
        _item.setAdaDiet(_tmpAdaDiet);
        final String _tmpFluidRestriction;
        if (_cursor.isNull(_cursorIndexOfFluidRestriction)) {
          _tmpFluidRestriction = null;
        } else {
          _tmpFluidRestriction = _cursor.getString(_cursorIndexOfFluidRestriction);
        }
        _item.setFluidRestriction(_tmpFluidRestriction);
        final String _tmpTextureModifications;
        if (_cursor.isNull(_cursorIndexOfTextureModifications)) {
          _tmpTextureModifications = null;
        } else {
          _tmpTextureModifications = _cursor.getString(_cursorIndexOfTextureModifications);
        }
        _item.setTextureModifications(_tmpTextureModifications);
        final boolean _tmpMechanicalChopped;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfMechanicalChopped);
        _tmpMechanicalChopped = _tmp_1 != 0;
        _item.setMechanicalChopped(_tmpMechanicalChopped);
        final boolean _tmpMechanicalGround;
        final int _tmp_2;
        _tmp_2 = _cursor.getInt(_cursorIndexOfMechanicalGround);
        _tmpMechanicalGround = _tmp_2 != 0;
        _item.setMechanicalGround(_tmpMechanicalGround);
        final boolean _tmpBiteSize;
        final int _tmp_3;
        _tmp_3 = _cursor.getInt(_cursorIndexOfBiteSize);
        _tmpBiteSize = _tmp_3 != 0;
        _item.setBiteSize(_tmpBiteSize);
        final boolean _tmpBreadOK;
        final int _tmp_4;
        _tmp_4 = _cursor.getInt(_cursorIndexOfBreadOK);
        _tmpBreadOK = _tmp_4 != 0;
        _item.setBreadOK(_tmpBreadOK);
        final boolean _tmpNectarThick;
        final int _tmp_5;
        _tmp_5 = _cursor.getInt(_cursorIndexOfNectarThick);
        _tmpNectarThick = _tmp_5 != 0;
        _item.setNectarThick(_tmpNectarThick);
        final boolean _tmpPuddingThick;
        final int _tmp_6;
        _tmp_6 = _cursor.getInt(_cursorIndexOfPuddingThick);
        _tmpPuddingThick = _tmp_6 != 0;
        _item.setPuddingThick(_tmpPuddingThick);
        final boolean _tmpHoneyThick;
        final int _tmp_7;
        _tmp_7 = _cursor.getInt(_cursorIndexOfHoneyThick);
        _tmpHoneyThick = _tmp_7 != 0;
        _item.setHoneyThick(_tmpHoneyThick);
        final boolean _tmpExtraGravy;
        final int _tmp_8;
        _tmp_8 = _cursor.getInt(_cursorIndexOfExtraGravy);
        _tmpExtraGravy = _tmp_8 != 0;
        _item.setExtraGravy(_tmpExtraGravy);
        final boolean _tmpMeatsOnly;
        final int _tmp_9;
        _tmp_9 = _cursor.getInt(_cursorIndexOfMeatsOnly);
        _tmpMeatsOnly = _tmp_9 != 0;
        _item.setMeatsOnly(_tmpMeatsOnly);
        final boolean _tmpBreakfastComplete;
        final int _tmp_10;
        _tmp_10 = _cursor.getInt(_cursorIndexOfBreakfastComplete);
        _tmpBreakfastComplete = _tmp_10 != 0;
        _item.setBreakfastComplete(_tmpBreakfastComplete);
        final boolean _tmpLunchComplete;
        final int _tmp_11;
        _tmp_11 = _cursor.getInt(_cursorIndexOfLunchComplete);
        _tmpLunchComplete = _tmp_11 != 0;
        _item.setLunchComplete(_tmpLunchComplete);
        final boolean _tmpDinnerComplete;
        final int _tmp_12;
        _tmp_12 = _cursor.getInt(_cursorIndexOfDinnerComplete);
        _tmpDinnerComplete = _tmp_12 != 0;
        _item.setDinnerComplete(_tmpDinnerComplete);
        final boolean _tmpBreakfastNPO;
        final int _tmp_13;
        _tmp_13 = _cursor.getInt(_cursorIndexOfBreakfastNPO);
        _tmpBreakfastNPO = _tmp_13 != 0;
        _item.setBreakfastNPO(_tmpBreakfastNPO);
        final boolean _tmpLunchNPO;
        final int _tmp_14;
        _tmp_14 = _cursor.getInt(_cursorIndexOfLunchNPO);
        _tmpLunchNPO = _tmp_14 != 0;
        _item.setLunchNPO(_tmpLunchNPO);
        final boolean _tmpDinnerNPO;
        final int _tmp_15;
        _tmp_15 = _cursor.getInt(_cursorIndexOfDinnerNPO);
        _tmpDinnerNPO = _tmp_15 != 0;
        _item.setDinnerNPO(_tmpDinnerNPO);
        final String _tmpBreakfastItems;
        if (_cursor.isNull(_cursorIndexOfBreakfastItems)) {
          _tmpBreakfastItems = null;
        } else {
          _tmpBreakfastItems = _cursor.getString(_cursorIndexOfBreakfastItems);
        }
        _item.setBreakfastItems(_tmpBreakfastItems);
        final String _tmpLunchItems;
        if (_cursor.isNull(_cursorIndexOfLunchItems)) {
          _tmpLunchItems = null;
        } else {
          _tmpLunchItems = _cursor.getString(_cursorIndexOfLunchItems);
        }
        _item.setLunchItems(_tmpLunchItems);
        final String _tmpDinnerItems;
        if (_cursor.isNull(_cursorIndexOfDinnerItems)) {
          _tmpDinnerItems = null;
        } else {
          _tmpDinnerItems = _cursor.getString(_cursorIndexOfDinnerItems);
        }
        _item.setDinnerItems(_tmpDinnerItems);
        final String _tmpBreakfastJuices;
        if (_cursor.isNull(_cursorIndexOfBreakfastJuices)) {
          _tmpBreakfastJuices = null;
        } else {
          _tmpBreakfastJuices = _cursor.getString(_cursorIndexOfBreakfastJuices);
        }
        _item.setBreakfastJuices(_tmpBreakfastJuices);
        final String _tmpLunchJuices;
        if (_cursor.isNull(_cursorIndexOfLunchJuices)) {
          _tmpLunchJuices = null;
        } else {
          _tmpLunchJuices = _cursor.getString(_cursorIndexOfLunchJuices);
        }
        _item.setLunchJuices(_tmpLunchJuices);
        final String _tmpDinnerJuices;
        if (_cursor.isNull(_cursorIndexOfDinnerJuices)) {
          _tmpDinnerJuices = null;
        } else {
          _tmpDinnerJuices = _cursor.getString(_cursorIndexOfDinnerJuices);
        }
        _item.setDinnerJuices(_tmpDinnerJuices);
        final String _tmpBreakfastDrinks;
        if (_cursor.isNull(_cursorIndexOfBreakfastDrinks)) {
          _tmpBreakfastDrinks = null;
        } else {
          _tmpBreakfastDrinks = _cursor.getString(_cursorIndexOfBreakfastDrinks);
        }
        _item.setBreakfastDrinks(_tmpBreakfastDrinks);
        final String _tmpLunchDrinks;
        if (_cursor.isNull(_cursorIndexOfLunchDrinks)) {
          _tmpLunchDrinks = null;
        } else {
          _tmpLunchDrinks = _cursor.getString(_cursorIndexOfLunchDrinks);
        }
        _item.setLunchDrinks(_tmpLunchDrinks);
        final String _tmpDinnerDrinks;
        if (_cursor.isNull(_cursorIndexOfDinnerDrinks)) {
          _tmpDinnerDrinks = null;
        } else {
          _tmpDinnerDrinks = _cursor.getString(_cursorIndexOfDinnerDrinks);
        }
        _item.setDinnerDrinks(_tmpDinnerDrinks);
        final Date _tmpCreatedDate;
        final Long _tmp_16;
        if (_cursor.isNull(_cursorIndexOfCreatedDate)) {
          _tmp_16 = null;
        } else {
          _tmp_16 = _cursor.getLong(_cursorIndexOfCreatedDate);
        }
        _tmpCreatedDate = DateConverter.fromTimestamp(_tmp_16);
        _item.setCreatedDate(_tmpCreatedDate);
        final Date _tmpModifiedDate;
        final Long _tmp_17;
        if (_cursor.isNull(_cursorIndexOfModifiedDate)) {
          _tmp_17 = null;
        } else {
          _tmp_17 = _cursor.getLong(_cursorIndexOfModifiedDate);
        }
        _tmpModifiedDate = DateConverter.fromTimestamp(_tmp_17);
        _item.setModifiedDate(_tmpModifiedDate);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public LiveData<List<PatientEntity>> searchPatientsLive(final String searchTerm) {
    final String _sql = "SELECT * FROM patient_info WHERE LOWER(patient_first_name) LIKE LOWER(?) OR LOWER(patient_last_name) LIKE LOWER(?) OR room_number LIKE ? ORDER BY wing, CAST(room_number AS INTEGER)";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
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
    _argIndex = 3;
    if (searchTerm == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, searchTerm);
    }
    return __db.getInvalidationTracker().createLiveData(new String[] {"patient_info"}, false, new Callable<List<PatientEntity>>() {
      @Override
      @Nullable
      public List<PatientEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfPatientId = CursorUtil.getColumnIndexOrThrow(_cursor, "patient_id");
          final int _cursorIndexOfPatientFirstName = CursorUtil.getColumnIndexOrThrow(_cursor, "patient_first_name");
          final int _cursorIndexOfPatientLastName = CursorUtil.getColumnIndexOrThrow(_cursor, "patient_last_name");
          final int _cursorIndexOfWing = CursorUtil.getColumnIndexOrThrow(_cursor, "wing");
          final int _cursorIndexOfRoomNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "room_number");
          final int _cursorIndexOfDietType = CursorUtil.getColumnIndexOrThrow(_cursor, "diet_type");
          final int _cursorIndexOfDiet = CursorUtil.getColumnIndexOrThrow(_cursor, "diet");
          final int _cursorIndexOfAdaDiet = CursorUtil.getColumnIndexOrThrow(_cursor, "ada_diet");
          final int _cursorIndexOfFluidRestriction = CursorUtil.getColumnIndexOrThrow(_cursor, "fluid_restriction");
          final int _cursorIndexOfTextureModifications = CursorUtil.getColumnIndexOrThrow(_cursor, "texture_modifications");
          final int _cursorIndexOfMechanicalChopped = CursorUtil.getColumnIndexOrThrow(_cursor, "mechanical_chopped");
          final int _cursorIndexOfMechanicalGround = CursorUtil.getColumnIndexOrThrow(_cursor, "mechanical_ground");
          final int _cursorIndexOfBiteSize = CursorUtil.getColumnIndexOrThrow(_cursor, "bite_size");
          final int _cursorIndexOfBreadOK = CursorUtil.getColumnIndexOrThrow(_cursor, "bread_ok");
          final int _cursorIndexOfNectarThick = CursorUtil.getColumnIndexOrThrow(_cursor, "nectar_thick");
          final int _cursorIndexOfPuddingThick = CursorUtil.getColumnIndexOrThrow(_cursor, "pudding_thick");
          final int _cursorIndexOfHoneyThick = CursorUtil.getColumnIndexOrThrow(_cursor, "honey_thick");
          final int _cursorIndexOfExtraGravy = CursorUtil.getColumnIndexOrThrow(_cursor, "extra_gravy");
          final int _cursorIndexOfMeatsOnly = CursorUtil.getColumnIndexOrThrow(_cursor, "meats_only");
          final int _cursorIndexOfBreakfastComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_complete");
          final int _cursorIndexOfLunchComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_complete");
          final int _cursorIndexOfDinnerComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_complete");
          final int _cursorIndexOfBreakfastNPO = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_npo");
          final int _cursorIndexOfLunchNPO = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_npo");
          final int _cursorIndexOfDinnerNPO = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_npo");
          final int _cursorIndexOfBreakfastItems = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_items");
          final int _cursorIndexOfLunchItems = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_items");
          final int _cursorIndexOfDinnerItems = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_items");
          final int _cursorIndexOfBreakfastJuices = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_juices");
          final int _cursorIndexOfLunchJuices = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_juices");
          final int _cursorIndexOfDinnerJuices = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_juices");
          final int _cursorIndexOfBreakfastDrinks = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_drinks");
          final int _cursorIndexOfLunchDrinks = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_drinks");
          final int _cursorIndexOfDinnerDrinks = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_drinks");
          final int _cursorIndexOfCreatedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "created_date");
          final int _cursorIndexOfModifiedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "modified_date");
          final List<PatientEntity> _result = new ArrayList<PatientEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PatientEntity _item;
            _item = new PatientEntity();
            final long _tmpPatientId;
            _tmpPatientId = _cursor.getLong(_cursorIndexOfPatientId);
            _item.setPatientId(_tmpPatientId);
            final String _tmpPatientFirstName;
            if (_cursor.isNull(_cursorIndexOfPatientFirstName)) {
              _tmpPatientFirstName = null;
            } else {
              _tmpPatientFirstName = _cursor.getString(_cursorIndexOfPatientFirstName);
            }
            _item.setPatientFirstName(_tmpPatientFirstName);
            final String _tmpPatientLastName;
            if (_cursor.isNull(_cursorIndexOfPatientLastName)) {
              _tmpPatientLastName = null;
            } else {
              _tmpPatientLastName = _cursor.getString(_cursorIndexOfPatientLastName);
            }
            _item.setPatientLastName(_tmpPatientLastName);
            final String _tmpWing;
            if (_cursor.isNull(_cursorIndexOfWing)) {
              _tmpWing = null;
            } else {
              _tmpWing = _cursor.getString(_cursorIndexOfWing);
            }
            _item.setWing(_tmpWing);
            final String _tmpRoomNumber;
            if (_cursor.isNull(_cursorIndexOfRoomNumber)) {
              _tmpRoomNumber = null;
            } else {
              _tmpRoomNumber = _cursor.getString(_cursorIndexOfRoomNumber);
            }
            _item.setRoomNumber(_tmpRoomNumber);
            final String _tmpDietType;
            if (_cursor.isNull(_cursorIndexOfDietType)) {
              _tmpDietType = null;
            } else {
              _tmpDietType = _cursor.getString(_cursorIndexOfDietType);
            }
            _item.setDietType(_tmpDietType);
            final String _tmpDiet;
            if (_cursor.isNull(_cursorIndexOfDiet)) {
              _tmpDiet = null;
            } else {
              _tmpDiet = _cursor.getString(_cursorIndexOfDiet);
            }
            _item.setDiet(_tmpDiet);
            final boolean _tmpAdaDiet;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfAdaDiet);
            _tmpAdaDiet = _tmp != 0;
            _item.setAdaDiet(_tmpAdaDiet);
            final String _tmpFluidRestriction;
            if (_cursor.isNull(_cursorIndexOfFluidRestriction)) {
              _tmpFluidRestriction = null;
            } else {
              _tmpFluidRestriction = _cursor.getString(_cursorIndexOfFluidRestriction);
            }
            _item.setFluidRestriction(_tmpFluidRestriction);
            final String _tmpTextureModifications;
            if (_cursor.isNull(_cursorIndexOfTextureModifications)) {
              _tmpTextureModifications = null;
            } else {
              _tmpTextureModifications = _cursor.getString(_cursorIndexOfTextureModifications);
            }
            _item.setTextureModifications(_tmpTextureModifications);
            final boolean _tmpMechanicalChopped;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfMechanicalChopped);
            _tmpMechanicalChopped = _tmp_1 != 0;
            _item.setMechanicalChopped(_tmpMechanicalChopped);
            final boolean _tmpMechanicalGround;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfMechanicalGround);
            _tmpMechanicalGround = _tmp_2 != 0;
            _item.setMechanicalGround(_tmpMechanicalGround);
            final boolean _tmpBiteSize;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfBiteSize);
            _tmpBiteSize = _tmp_3 != 0;
            _item.setBiteSize(_tmpBiteSize);
            final boolean _tmpBreadOK;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfBreadOK);
            _tmpBreadOK = _tmp_4 != 0;
            _item.setBreadOK(_tmpBreadOK);
            final boolean _tmpNectarThick;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfNectarThick);
            _tmpNectarThick = _tmp_5 != 0;
            _item.setNectarThick(_tmpNectarThick);
            final boolean _tmpPuddingThick;
            final int _tmp_6;
            _tmp_6 = _cursor.getInt(_cursorIndexOfPuddingThick);
            _tmpPuddingThick = _tmp_6 != 0;
            _item.setPuddingThick(_tmpPuddingThick);
            final boolean _tmpHoneyThick;
            final int _tmp_7;
            _tmp_7 = _cursor.getInt(_cursorIndexOfHoneyThick);
            _tmpHoneyThick = _tmp_7 != 0;
            _item.setHoneyThick(_tmpHoneyThick);
            final boolean _tmpExtraGravy;
            final int _tmp_8;
            _tmp_8 = _cursor.getInt(_cursorIndexOfExtraGravy);
            _tmpExtraGravy = _tmp_8 != 0;
            _item.setExtraGravy(_tmpExtraGravy);
            final boolean _tmpMeatsOnly;
            final int _tmp_9;
            _tmp_9 = _cursor.getInt(_cursorIndexOfMeatsOnly);
            _tmpMeatsOnly = _tmp_9 != 0;
            _item.setMeatsOnly(_tmpMeatsOnly);
            final boolean _tmpBreakfastComplete;
            final int _tmp_10;
            _tmp_10 = _cursor.getInt(_cursorIndexOfBreakfastComplete);
            _tmpBreakfastComplete = _tmp_10 != 0;
            _item.setBreakfastComplete(_tmpBreakfastComplete);
            final boolean _tmpLunchComplete;
            final int _tmp_11;
            _tmp_11 = _cursor.getInt(_cursorIndexOfLunchComplete);
            _tmpLunchComplete = _tmp_11 != 0;
            _item.setLunchComplete(_tmpLunchComplete);
            final boolean _tmpDinnerComplete;
            final int _tmp_12;
            _tmp_12 = _cursor.getInt(_cursorIndexOfDinnerComplete);
            _tmpDinnerComplete = _tmp_12 != 0;
            _item.setDinnerComplete(_tmpDinnerComplete);
            final boolean _tmpBreakfastNPO;
            final int _tmp_13;
            _tmp_13 = _cursor.getInt(_cursorIndexOfBreakfastNPO);
            _tmpBreakfastNPO = _tmp_13 != 0;
            _item.setBreakfastNPO(_tmpBreakfastNPO);
            final boolean _tmpLunchNPO;
            final int _tmp_14;
            _tmp_14 = _cursor.getInt(_cursorIndexOfLunchNPO);
            _tmpLunchNPO = _tmp_14 != 0;
            _item.setLunchNPO(_tmpLunchNPO);
            final boolean _tmpDinnerNPO;
            final int _tmp_15;
            _tmp_15 = _cursor.getInt(_cursorIndexOfDinnerNPO);
            _tmpDinnerNPO = _tmp_15 != 0;
            _item.setDinnerNPO(_tmpDinnerNPO);
            final String _tmpBreakfastItems;
            if (_cursor.isNull(_cursorIndexOfBreakfastItems)) {
              _tmpBreakfastItems = null;
            } else {
              _tmpBreakfastItems = _cursor.getString(_cursorIndexOfBreakfastItems);
            }
            _item.setBreakfastItems(_tmpBreakfastItems);
            final String _tmpLunchItems;
            if (_cursor.isNull(_cursorIndexOfLunchItems)) {
              _tmpLunchItems = null;
            } else {
              _tmpLunchItems = _cursor.getString(_cursorIndexOfLunchItems);
            }
            _item.setLunchItems(_tmpLunchItems);
            final String _tmpDinnerItems;
            if (_cursor.isNull(_cursorIndexOfDinnerItems)) {
              _tmpDinnerItems = null;
            } else {
              _tmpDinnerItems = _cursor.getString(_cursorIndexOfDinnerItems);
            }
            _item.setDinnerItems(_tmpDinnerItems);
            final String _tmpBreakfastJuices;
            if (_cursor.isNull(_cursorIndexOfBreakfastJuices)) {
              _tmpBreakfastJuices = null;
            } else {
              _tmpBreakfastJuices = _cursor.getString(_cursorIndexOfBreakfastJuices);
            }
            _item.setBreakfastJuices(_tmpBreakfastJuices);
            final String _tmpLunchJuices;
            if (_cursor.isNull(_cursorIndexOfLunchJuices)) {
              _tmpLunchJuices = null;
            } else {
              _tmpLunchJuices = _cursor.getString(_cursorIndexOfLunchJuices);
            }
            _item.setLunchJuices(_tmpLunchJuices);
            final String _tmpDinnerJuices;
            if (_cursor.isNull(_cursorIndexOfDinnerJuices)) {
              _tmpDinnerJuices = null;
            } else {
              _tmpDinnerJuices = _cursor.getString(_cursorIndexOfDinnerJuices);
            }
            _item.setDinnerJuices(_tmpDinnerJuices);
            final String _tmpBreakfastDrinks;
            if (_cursor.isNull(_cursorIndexOfBreakfastDrinks)) {
              _tmpBreakfastDrinks = null;
            } else {
              _tmpBreakfastDrinks = _cursor.getString(_cursorIndexOfBreakfastDrinks);
            }
            _item.setBreakfastDrinks(_tmpBreakfastDrinks);
            final String _tmpLunchDrinks;
            if (_cursor.isNull(_cursorIndexOfLunchDrinks)) {
              _tmpLunchDrinks = null;
            } else {
              _tmpLunchDrinks = _cursor.getString(_cursorIndexOfLunchDrinks);
            }
            _item.setLunchDrinks(_tmpLunchDrinks);
            final String _tmpDinnerDrinks;
            if (_cursor.isNull(_cursorIndexOfDinnerDrinks)) {
              _tmpDinnerDrinks = null;
            } else {
              _tmpDinnerDrinks = _cursor.getString(_cursorIndexOfDinnerDrinks);
            }
            _item.setDinnerDrinks(_tmpDinnerDrinks);
            final Date _tmpCreatedDate;
            final Long _tmp_16;
            if (_cursor.isNull(_cursorIndexOfCreatedDate)) {
              _tmp_16 = null;
            } else {
              _tmp_16 = _cursor.getLong(_cursorIndexOfCreatedDate);
            }
            _tmpCreatedDate = DateConverter.fromTimestamp(_tmp_16);
            _item.setCreatedDate(_tmpCreatedDate);
            final Date _tmpModifiedDate;
            final Long _tmp_17;
            if (_cursor.isNull(_cursorIndexOfModifiedDate)) {
              _tmp_17 = null;
            } else {
              _tmp_17 = _cursor.getLong(_cursorIndexOfModifiedDate);
            }
            _tmpModifiedDate = DateConverter.fromTimestamp(_tmp_17);
            _item.setModifiedDate(_tmpModifiedDate);
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
  public List<PatientEntity> searchPatients(final String searchTerm) {
    final String _sql = "SELECT * FROM patient_info WHERE LOWER(patient_first_name) LIKE LOWER(?) OR LOWER(patient_last_name) LIKE LOWER(?) OR room_number LIKE ? ORDER BY wing, CAST(room_number AS INTEGER)";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
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
    _argIndex = 3;
    if (searchTerm == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, searchTerm);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfPatientId = CursorUtil.getColumnIndexOrThrow(_cursor, "patient_id");
      final int _cursorIndexOfPatientFirstName = CursorUtil.getColumnIndexOrThrow(_cursor, "patient_first_name");
      final int _cursorIndexOfPatientLastName = CursorUtil.getColumnIndexOrThrow(_cursor, "patient_last_name");
      final int _cursorIndexOfWing = CursorUtil.getColumnIndexOrThrow(_cursor, "wing");
      final int _cursorIndexOfRoomNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "room_number");
      final int _cursorIndexOfDietType = CursorUtil.getColumnIndexOrThrow(_cursor, "diet_type");
      final int _cursorIndexOfDiet = CursorUtil.getColumnIndexOrThrow(_cursor, "diet");
      final int _cursorIndexOfAdaDiet = CursorUtil.getColumnIndexOrThrow(_cursor, "ada_diet");
      final int _cursorIndexOfFluidRestriction = CursorUtil.getColumnIndexOrThrow(_cursor, "fluid_restriction");
      final int _cursorIndexOfTextureModifications = CursorUtil.getColumnIndexOrThrow(_cursor, "texture_modifications");
      final int _cursorIndexOfMechanicalChopped = CursorUtil.getColumnIndexOrThrow(_cursor, "mechanical_chopped");
      final int _cursorIndexOfMechanicalGround = CursorUtil.getColumnIndexOrThrow(_cursor, "mechanical_ground");
      final int _cursorIndexOfBiteSize = CursorUtil.getColumnIndexOrThrow(_cursor, "bite_size");
      final int _cursorIndexOfBreadOK = CursorUtil.getColumnIndexOrThrow(_cursor, "bread_ok");
      final int _cursorIndexOfNectarThick = CursorUtil.getColumnIndexOrThrow(_cursor, "nectar_thick");
      final int _cursorIndexOfPuddingThick = CursorUtil.getColumnIndexOrThrow(_cursor, "pudding_thick");
      final int _cursorIndexOfHoneyThick = CursorUtil.getColumnIndexOrThrow(_cursor, "honey_thick");
      final int _cursorIndexOfExtraGravy = CursorUtil.getColumnIndexOrThrow(_cursor, "extra_gravy");
      final int _cursorIndexOfMeatsOnly = CursorUtil.getColumnIndexOrThrow(_cursor, "meats_only");
      final int _cursorIndexOfBreakfastComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_complete");
      final int _cursorIndexOfLunchComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_complete");
      final int _cursorIndexOfDinnerComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_complete");
      final int _cursorIndexOfBreakfastNPO = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_npo");
      final int _cursorIndexOfLunchNPO = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_npo");
      final int _cursorIndexOfDinnerNPO = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_npo");
      final int _cursorIndexOfBreakfastItems = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_items");
      final int _cursorIndexOfLunchItems = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_items");
      final int _cursorIndexOfDinnerItems = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_items");
      final int _cursorIndexOfBreakfastJuices = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_juices");
      final int _cursorIndexOfLunchJuices = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_juices");
      final int _cursorIndexOfDinnerJuices = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_juices");
      final int _cursorIndexOfBreakfastDrinks = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_drinks");
      final int _cursorIndexOfLunchDrinks = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_drinks");
      final int _cursorIndexOfDinnerDrinks = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_drinks");
      final int _cursorIndexOfCreatedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "created_date");
      final int _cursorIndexOfModifiedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "modified_date");
      final List<PatientEntity> _result = new ArrayList<PatientEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final PatientEntity _item;
        _item = new PatientEntity();
        final long _tmpPatientId;
        _tmpPatientId = _cursor.getLong(_cursorIndexOfPatientId);
        _item.setPatientId(_tmpPatientId);
        final String _tmpPatientFirstName;
        if (_cursor.isNull(_cursorIndexOfPatientFirstName)) {
          _tmpPatientFirstName = null;
        } else {
          _tmpPatientFirstName = _cursor.getString(_cursorIndexOfPatientFirstName);
        }
        _item.setPatientFirstName(_tmpPatientFirstName);
        final String _tmpPatientLastName;
        if (_cursor.isNull(_cursorIndexOfPatientLastName)) {
          _tmpPatientLastName = null;
        } else {
          _tmpPatientLastName = _cursor.getString(_cursorIndexOfPatientLastName);
        }
        _item.setPatientLastName(_tmpPatientLastName);
        final String _tmpWing;
        if (_cursor.isNull(_cursorIndexOfWing)) {
          _tmpWing = null;
        } else {
          _tmpWing = _cursor.getString(_cursorIndexOfWing);
        }
        _item.setWing(_tmpWing);
        final String _tmpRoomNumber;
        if (_cursor.isNull(_cursorIndexOfRoomNumber)) {
          _tmpRoomNumber = null;
        } else {
          _tmpRoomNumber = _cursor.getString(_cursorIndexOfRoomNumber);
        }
        _item.setRoomNumber(_tmpRoomNumber);
        final String _tmpDietType;
        if (_cursor.isNull(_cursorIndexOfDietType)) {
          _tmpDietType = null;
        } else {
          _tmpDietType = _cursor.getString(_cursorIndexOfDietType);
        }
        _item.setDietType(_tmpDietType);
        final String _tmpDiet;
        if (_cursor.isNull(_cursorIndexOfDiet)) {
          _tmpDiet = null;
        } else {
          _tmpDiet = _cursor.getString(_cursorIndexOfDiet);
        }
        _item.setDiet(_tmpDiet);
        final boolean _tmpAdaDiet;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfAdaDiet);
        _tmpAdaDiet = _tmp != 0;
        _item.setAdaDiet(_tmpAdaDiet);
        final String _tmpFluidRestriction;
        if (_cursor.isNull(_cursorIndexOfFluidRestriction)) {
          _tmpFluidRestriction = null;
        } else {
          _tmpFluidRestriction = _cursor.getString(_cursorIndexOfFluidRestriction);
        }
        _item.setFluidRestriction(_tmpFluidRestriction);
        final String _tmpTextureModifications;
        if (_cursor.isNull(_cursorIndexOfTextureModifications)) {
          _tmpTextureModifications = null;
        } else {
          _tmpTextureModifications = _cursor.getString(_cursorIndexOfTextureModifications);
        }
        _item.setTextureModifications(_tmpTextureModifications);
        final boolean _tmpMechanicalChopped;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfMechanicalChopped);
        _tmpMechanicalChopped = _tmp_1 != 0;
        _item.setMechanicalChopped(_tmpMechanicalChopped);
        final boolean _tmpMechanicalGround;
        final int _tmp_2;
        _tmp_2 = _cursor.getInt(_cursorIndexOfMechanicalGround);
        _tmpMechanicalGround = _tmp_2 != 0;
        _item.setMechanicalGround(_tmpMechanicalGround);
        final boolean _tmpBiteSize;
        final int _tmp_3;
        _tmp_3 = _cursor.getInt(_cursorIndexOfBiteSize);
        _tmpBiteSize = _tmp_3 != 0;
        _item.setBiteSize(_tmpBiteSize);
        final boolean _tmpBreadOK;
        final int _tmp_4;
        _tmp_4 = _cursor.getInt(_cursorIndexOfBreadOK);
        _tmpBreadOK = _tmp_4 != 0;
        _item.setBreadOK(_tmpBreadOK);
        final boolean _tmpNectarThick;
        final int _tmp_5;
        _tmp_5 = _cursor.getInt(_cursorIndexOfNectarThick);
        _tmpNectarThick = _tmp_5 != 0;
        _item.setNectarThick(_tmpNectarThick);
        final boolean _tmpPuddingThick;
        final int _tmp_6;
        _tmp_6 = _cursor.getInt(_cursorIndexOfPuddingThick);
        _tmpPuddingThick = _tmp_6 != 0;
        _item.setPuddingThick(_tmpPuddingThick);
        final boolean _tmpHoneyThick;
        final int _tmp_7;
        _tmp_7 = _cursor.getInt(_cursorIndexOfHoneyThick);
        _tmpHoneyThick = _tmp_7 != 0;
        _item.setHoneyThick(_tmpHoneyThick);
        final boolean _tmpExtraGravy;
        final int _tmp_8;
        _tmp_8 = _cursor.getInt(_cursorIndexOfExtraGravy);
        _tmpExtraGravy = _tmp_8 != 0;
        _item.setExtraGravy(_tmpExtraGravy);
        final boolean _tmpMeatsOnly;
        final int _tmp_9;
        _tmp_9 = _cursor.getInt(_cursorIndexOfMeatsOnly);
        _tmpMeatsOnly = _tmp_9 != 0;
        _item.setMeatsOnly(_tmpMeatsOnly);
        final boolean _tmpBreakfastComplete;
        final int _tmp_10;
        _tmp_10 = _cursor.getInt(_cursorIndexOfBreakfastComplete);
        _tmpBreakfastComplete = _tmp_10 != 0;
        _item.setBreakfastComplete(_tmpBreakfastComplete);
        final boolean _tmpLunchComplete;
        final int _tmp_11;
        _tmp_11 = _cursor.getInt(_cursorIndexOfLunchComplete);
        _tmpLunchComplete = _tmp_11 != 0;
        _item.setLunchComplete(_tmpLunchComplete);
        final boolean _tmpDinnerComplete;
        final int _tmp_12;
        _tmp_12 = _cursor.getInt(_cursorIndexOfDinnerComplete);
        _tmpDinnerComplete = _tmp_12 != 0;
        _item.setDinnerComplete(_tmpDinnerComplete);
        final boolean _tmpBreakfastNPO;
        final int _tmp_13;
        _tmp_13 = _cursor.getInt(_cursorIndexOfBreakfastNPO);
        _tmpBreakfastNPO = _tmp_13 != 0;
        _item.setBreakfastNPO(_tmpBreakfastNPO);
        final boolean _tmpLunchNPO;
        final int _tmp_14;
        _tmp_14 = _cursor.getInt(_cursorIndexOfLunchNPO);
        _tmpLunchNPO = _tmp_14 != 0;
        _item.setLunchNPO(_tmpLunchNPO);
        final boolean _tmpDinnerNPO;
        final int _tmp_15;
        _tmp_15 = _cursor.getInt(_cursorIndexOfDinnerNPO);
        _tmpDinnerNPO = _tmp_15 != 0;
        _item.setDinnerNPO(_tmpDinnerNPO);
        final String _tmpBreakfastItems;
        if (_cursor.isNull(_cursorIndexOfBreakfastItems)) {
          _tmpBreakfastItems = null;
        } else {
          _tmpBreakfastItems = _cursor.getString(_cursorIndexOfBreakfastItems);
        }
        _item.setBreakfastItems(_tmpBreakfastItems);
        final String _tmpLunchItems;
        if (_cursor.isNull(_cursorIndexOfLunchItems)) {
          _tmpLunchItems = null;
        } else {
          _tmpLunchItems = _cursor.getString(_cursorIndexOfLunchItems);
        }
        _item.setLunchItems(_tmpLunchItems);
        final String _tmpDinnerItems;
        if (_cursor.isNull(_cursorIndexOfDinnerItems)) {
          _tmpDinnerItems = null;
        } else {
          _tmpDinnerItems = _cursor.getString(_cursorIndexOfDinnerItems);
        }
        _item.setDinnerItems(_tmpDinnerItems);
        final String _tmpBreakfastJuices;
        if (_cursor.isNull(_cursorIndexOfBreakfastJuices)) {
          _tmpBreakfastJuices = null;
        } else {
          _tmpBreakfastJuices = _cursor.getString(_cursorIndexOfBreakfastJuices);
        }
        _item.setBreakfastJuices(_tmpBreakfastJuices);
        final String _tmpLunchJuices;
        if (_cursor.isNull(_cursorIndexOfLunchJuices)) {
          _tmpLunchJuices = null;
        } else {
          _tmpLunchJuices = _cursor.getString(_cursorIndexOfLunchJuices);
        }
        _item.setLunchJuices(_tmpLunchJuices);
        final String _tmpDinnerJuices;
        if (_cursor.isNull(_cursorIndexOfDinnerJuices)) {
          _tmpDinnerJuices = null;
        } else {
          _tmpDinnerJuices = _cursor.getString(_cursorIndexOfDinnerJuices);
        }
        _item.setDinnerJuices(_tmpDinnerJuices);
        final String _tmpBreakfastDrinks;
        if (_cursor.isNull(_cursorIndexOfBreakfastDrinks)) {
          _tmpBreakfastDrinks = null;
        } else {
          _tmpBreakfastDrinks = _cursor.getString(_cursorIndexOfBreakfastDrinks);
        }
        _item.setBreakfastDrinks(_tmpBreakfastDrinks);
        final String _tmpLunchDrinks;
        if (_cursor.isNull(_cursorIndexOfLunchDrinks)) {
          _tmpLunchDrinks = null;
        } else {
          _tmpLunchDrinks = _cursor.getString(_cursorIndexOfLunchDrinks);
        }
        _item.setLunchDrinks(_tmpLunchDrinks);
        final String _tmpDinnerDrinks;
        if (_cursor.isNull(_cursorIndexOfDinnerDrinks)) {
          _tmpDinnerDrinks = null;
        } else {
          _tmpDinnerDrinks = _cursor.getString(_cursorIndexOfDinnerDrinks);
        }
        _item.setDinnerDrinks(_tmpDinnerDrinks);
        final Date _tmpCreatedDate;
        final Long _tmp_16;
        if (_cursor.isNull(_cursorIndexOfCreatedDate)) {
          _tmp_16 = null;
        } else {
          _tmp_16 = _cursor.getLong(_cursorIndexOfCreatedDate);
        }
        _tmpCreatedDate = DateConverter.fromTimestamp(_tmp_16);
        _item.setCreatedDate(_tmpCreatedDate);
        final Date _tmpModifiedDate;
        final Long _tmp_17;
        if (_cursor.isNull(_cursorIndexOfModifiedDate)) {
          _tmp_17 = null;
        } else {
          _tmp_17 = _cursor.getLong(_cursorIndexOfModifiedDate);
        }
        _tmpModifiedDate = DateConverter.fromTimestamp(_tmp_17);
        _item.setModifiedDate(_tmpModifiedDate);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public LiveData<List<PatientEntity>> getPatientsByDietTypeLive(final String dietType) {
    final String _sql = "SELECT * FROM patient_info WHERE diet_type = ? ORDER BY wing, CAST(room_number AS INTEGER)";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (dietType == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, dietType);
    }
    return __db.getInvalidationTracker().createLiveData(new String[] {"patient_info"}, false, new Callable<List<PatientEntity>>() {
      @Override
      @Nullable
      public List<PatientEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfPatientId = CursorUtil.getColumnIndexOrThrow(_cursor, "patient_id");
          final int _cursorIndexOfPatientFirstName = CursorUtil.getColumnIndexOrThrow(_cursor, "patient_first_name");
          final int _cursorIndexOfPatientLastName = CursorUtil.getColumnIndexOrThrow(_cursor, "patient_last_name");
          final int _cursorIndexOfWing = CursorUtil.getColumnIndexOrThrow(_cursor, "wing");
          final int _cursorIndexOfRoomNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "room_number");
          final int _cursorIndexOfDietType = CursorUtil.getColumnIndexOrThrow(_cursor, "diet_type");
          final int _cursorIndexOfDiet = CursorUtil.getColumnIndexOrThrow(_cursor, "diet");
          final int _cursorIndexOfAdaDiet = CursorUtil.getColumnIndexOrThrow(_cursor, "ada_diet");
          final int _cursorIndexOfFluidRestriction = CursorUtil.getColumnIndexOrThrow(_cursor, "fluid_restriction");
          final int _cursorIndexOfTextureModifications = CursorUtil.getColumnIndexOrThrow(_cursor, "texture_modifications");
          final int _cursorIndexOfMechanicalChopped = CursorUtil.getColumnIndexOrThrow(_cursor, "mechanical_chopped");
          final int _cursorIndexOfMechanicalGround = CursorUtil.getColumnIndexOrThrow(_cursor, "mechanical_ground");
          final int _cursorIndexOfBiteSize = CursorUtil.getColumnIndexOrThrow(_cursor, "bite_size");
          final int _cursorIndexOfBreadOK = CursorUtil.getColumnIndexOrThrow(_cursor, "bread_ok");
          final int _cursorIndexOfNectarThick = CursorUtil.getColumnIndexOrThrow(_cursor, "nectar_thick");
          final int _cursorIndexOfPuddingThick = CursorUtil.getColumnIndexOrThrow(_cursor, "pudding_thick");
          final int _cursorIndexOfHoneyThick = CursorUtil.getColumnIndexOrThrow(_cursor, "honey_thick");
          final int _cursorIndexOfExtraGravy = CursorUtil.getColumnIndexOrThrow(_cursor, "extra_gravy");
          final int _cursorIndexOfMeatsOnly = CursorUtil.getColumnIndexOrThrow(_cursor, "meats_only");
          final int _cursorIndexOfBreakfastComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_complete");
          final int _cursorIndexOfLunchComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_complete");
          final int _cursorIndexOfDinnerComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_complete");
          final int _cursorIndexOfBreakfastNPO = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_npo");
          final int _cursorIndexOfLunchNPO = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_npo");
          final int _cursorIndexOfDinnerNPO = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_npo");
          final int _cursorIndexOfBreakfastItems = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_items");
          final int _cursorIndexOfLunchItems = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_items");
          final int _cursorIndexOfDinnerItems = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_items");
          final int _cursorIndexOfBreakfastJuices = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_juices");
          final int _cursorIndexOfLunchJuices = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_juices");
          final int _cursorIndexOfDinnerJuices = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_juices");
          final int _cursorIndexOfBreakfastDrinks = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_drinks");
          final int _cursorIndexOfLunchDrinks = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_drinks");
          final int _cursorIndexOfDinnerDrinks = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_drinks");
          final int _cursorIndexOfCreatedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "created_date");
          final int _cursorIndexOfModifiedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "modified_date");
          final List<PatientEntity> _result = new ArrayList<PatientEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PatientEntity _item;
            _item = new PatientEntity();
            final long _tmpPatientId;
            _tmpPatientId = _cursor.getLong(_cursorIndexOfPatientId);
            _item.setPatientId(_tmpPatientId);
            final String _tmpPatientFirstName;
            if (_cursor.isNull(_cursorIndexOfPatientFirstName)) {
              _tmpPatientFirstName = null;
            } else {
              _tmpPatientFirstName = _cursor.getString(_cursorIndexOfPatientFirstName);
            }
            _item.setPatientFirstName(_tmpPatientFirstName);
            final String _tmpPatientLastName;
            if (_cursor.isNull(_cursorIndexOfPatientLastName)) {
              _tmpPatientLastName = null;
            } else {
              _tmpPatientLastName = _cursor.getString(_cursorIndexOfPatientLastName);
            }
            _item.setPatientLastName(_tmpPatientLastName);
            final String _tmpWing;
            if (_cursor.isNull(_cursorIndexOfWing)) {
              _tmpWing = null;
            } else {
              _tmpWing = _cursor.getString(_cursorIndexOfWing);
            }
            _item.setWing(_tmpWing);
            final String _tmpRoomNumber;
            if (_cursor.isNull(_cursorIndexOfRoomNumber)) {
              _tmpRoomNumber = null;
            } else {
              _tmpRoomNumber = _cursor.getString(_cursorIndexOfRoomNumber);
            }
            _item.setRoomNumber(_tmpRoomNumber);
            final String _tmpDietType;
            if (_cursor.isNull(_cursorIndexOfDietType)) {
              _tmpDietType = null;
            } else {
              _tmpDietType = _cursor.getString(_cursorIndexOfDietType);
            }
            _item.setDietType(_tmpDietType);
            final String _tmpDiet;
            if (_cursor.isNull(_cursorIndexOfDiet)) {
              _tmpDiet = null;
            } else {
              _tmpDiet = _cursor.getString(_cursorIndexOfDiet);
            }
            _item.setDiet(_tmpDiet);
            final boolean _tmpAdaDiet;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfAdaDiet);
            _tmpAdaDiet = _tmp != 0;
            _item.setAdaDiet(_tmpAdaDiet);
            final String _tmpFluidRestriction;
            if (_cursor.isNull(_cursorIndexOfFluidRestriction)) {
              _tmpFluidRestriction = null;
            } else {
              _tmpFluidRestriction = _cursor.getString(_cursorIndexOfFluidRestriction);
            }
            _item.setFluidRestriction(_tmpFluidRestriction);
            final String _tmpTextureModifications;
            if (_cursor.isNull(_cursorIndexOfTextureModifications)) {
              _tmpTextureModifications = null;
            } else {
              _tmpTextureModifications = _cursor.getString(_cursorIndexOfTextureModifications);
            }
            _item.setTextureModifications(_tmpTextureModifications);
            final boolean _tmpMechanicalChopped;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfMechanicalChopped);
            _tmpMechanicalChopped = _tmp_1 != 0;
            _item.setMechanicalChopped(_tmpMechanicalChopped);
            final boolean _tmpMechanicalGround;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfMechanicalGround);
            _tmpMechanicalGround = _tmp_2 != 0;
            _item.setMechanicalGround(_tmpMechanicalGround);
            final boolean _tmpBiteSize;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfBiteSize);
            _tmpBiteSize = _tmp_3 != 0;
            _item.setBiteSize(_tmpBiteSize);
            final boolean _tmpBreadOK;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfBreadOK);
            _tmpBreadOK = _tmp_4 != 0;
            _item.setBreadOK(_tmpBreadOK);
            final boolean _tmpNectarThick;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfNectarThick);
            _tmpNectarThick = _tmp_5 != 0;
            _item.setNectarThick(_tmpNectarThick);
            final boolean _tmpPuddingThick;
            final int _tmp_6;
            _tmp_6 = _cursor.getInt(_cursorIndexOfPuddingThick);
            _tmpPuddingThick = _tmp_6 != 0;
            _item.setPuddingThick(_tmpPuddingThick);
            final boolean _tmpHoneyThick;
            final int _tmp_7;
            _tmp_7 = _cursor.getInt(_cursorIndexOfHoneyThick);
            _tmpHoneyThick = _tmp_7 != 0;
            _item.setHoneyThick(_tmpHoneyThick);
            final boolean _tmpExtraGravy;
            final int _tmp_8;
            _tmp_8 = _cursor.getInt(_cursorIndexOfExtraGravy);
            _tmpExtraGravy = _tmp_8 != 0;
            _item.setExtraGravy(_tmpExtraGravy);
            final boolean _tmpMeatsOnly;
            final int _tmp_9;
            _tmp_9 = _cursor.getInt(_cursorIndexOfMeatsOnly);
            _tmpMeatsOnly = _tmp_9 != 0;
            _item.setMeatsOnly(_tmpMeatsOnly);
            final boolean _tmpBreakfastComplete;
            final int _tmp_10;
            _tmp_10 = _cursor.getInt(_cursorIndexOfBreakfastComplete);
            _tmpBreakfastComplete = _tmp_10 != 0;
            _item.setBreakfastComplete(_tmpBreakfastComplete);
            final boolean _tmpLunchComplete;
            final int _tmp_11;
            _tmp_11 = _cursor.getInt(_cursorIndexOfLunchComplete);
            _tmpLunchComplete = _tmp_11 != 0;
            _item.setLunchComplete(_tmpLunchComplete);
            final boolean _tmpDinnerComplete;
            final int _tmp_12;
            _tmp_12 = _cursor.getInt(_cursorIndexOfDinnerComplete);
            _tmpDinnerComplete = _tmp_12 != 0;
            _item.setDinnerComplete(_tmpDinnerComplete);
            final boolean _tmpBreakfastNPO;
            final int _tmp_13;
            _tmp_13 = _cursor.getInt(_cursorIndexOfBreakfastNPO);
            _tmpBreakfastNPO = _tmp_13 != 0;
            _item.setBreakfastNPO(_tmpBreakfastNPO);
            final boolean _tmpLunchNPO;
            final int _tmp_14;
            _tmp_14 = _cursor.getInt(_cursorIndexOfLunchNPO);
            _tmpLunchNPO = _tmp_14 != 0;
            _item.setLunchNPO(_tmpLunchNPO);
            final boolean _tmpDinnerNPO;
            final int _tmp_15;
            _tmp_15 = _cursor.getInt(_cursorIndexOfDinnerNPO);
            _tmpDinnerNPO = _tmp_15 != 0;
            _item.setDinnerNPO(_tmpDinnerNPO);
            final String _tmpBreakfastItems;
            if (_cursor.isNull(_cursorIndexOfBreakfastItems)) {
              _tmpBreakfastItems = null;
            } else {
              _tmpBreakfastItems = _cursor.getString(_cursorIndexOfBreakfastItems);
            }
            _item.setBreakfastItems(_tmpBreakfastItems);
            final String _tmpLunchItems;
            if (_cursor.isNull(_cursorIndexOfLunchItems)) {
              _tmpLunchItems = null;
            } else {
              _tmpLunchItems = _cursor.getString(_cursorIndexOfLunchItems);
            }
            _item.setLunchItems(_tmpLunchItems);
            final String _tmpDinnerItems;
            if (_cursor.isNull(_cursorIndexOfDinnerItems)) {
              _tmpDinnerItems = null;
            } else {
              _tmpDinnerItems = _cursor.getString(_cursorIndexOfDinnerItems);
            }
            _item.setDinnerItems(_tmpDinnerItems);
            final String _tmpBreakfastJuices;
            if (_cursor.isNull(_cursorIndexOfBreakfastJuices)) {
              _tmpBreakfastJuices = null;
            } else {
              _tmpBreakfastJuices = _cursor.getString(_cursorIndexOfBreakfastJuices);
            }
            _item.setBreakfastJuices(_tmpBreakfastJuices);
            final String _tmpLunchJuices;
            if (_cursor.isNull(_cursorIndexOfLunchJuices)) {
              _tmpLunchJuices = null;
            } else {
              _tmpLunchJuices = _cursor.getString(_cursorIndexOfLunchJuices);
            }
            _item.setLunchJuices(_tmpLunchJuices);
            final String _tmpDinnerJuices;
            if (_cursor.isNull(_cursorIndexOfDinnerJuices)) {
              _tmpDinnerJuices = null;
            } else {
              _tmpDinnerJuices = _cursor.getString(_cursorIndexOfDinnerJuices);
            }
            _item.setDinnerJuices(_tmpDinnerJuices);
            final String _tmpBreakfastDrinks;
            if (_cursor.isNull(_cursorIndexOfBreakfastDrinks)) {
              _tmpBreakfastDrinks = null;
            } else {
              _tmpBreakfastDrinks = _cursor.getString(_cursorIndexOfBreakfastDrinks);
            }
            _item.setBreakfastDrinks(_tmpBreakfastDrinks);
            final String _tmpLunchDrinks;
            if (_cursor.isNull(_cursorIndexOfLunchDrinks)) {
              _tmpLunchDrinks = null;
            } else {
              _tmpLunchDrinks = _cursor.getString(_cursorIndexOfLunchDrinks);
            }
            _item.setLunchDrinks(_tmpLunchDrinks);
            final String _tmpDinnerDrinks;
            if (_cursor.isNull(_cursorIndexOfDinnerDrinks)) {
              _tmpDinnerDrinks = null;
            } else {
              _tmpDinnerDrinks = _cursor.getString(_cursorIndexOfDinnerDrinks);
            }
            _item.setDinnerDrinks(_tmpDinnerDrinks);
            final Date _tmpCreatedDate;
            final Long _tmp_16;
            if (_cursor.isNull(_cursorIndexOfCreatedDate)) {
              _tmp_16 = null;
            } else {
              _tmp_16 = _cursor.getLong(_cursorIndexOfCreatedDate);
            }
            _tmpCreatedDate = DateConverter.fromTimestamp(_tmp_16);
            _item.setCreatedDate(_tmpCreatedDate);
            final Date _tmpModifiedDate;
            final Long _tmp_17;
            if (_cursor.isNull(_cursorIndexOfModifiedDate)) {
              _tmp_17 = null;
            } else {
              _tmp_17 = _cursor.getLong(_cursorIndexOfModifiedDate);
            }
            _tmpModifiedDate = DateConverter.fromTimestamp(_tmp_17);
            _item.setModifiedDate(_tmpModifiedDate);
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
  public LiveData<List<PatientEntity>> getAdaDietPatientsLive() {
    final String _sql = "SELECT * FROM patient_info WHERE ada_diet = 1 ORDER BY wing, CAST(room_number AS INTEGER)";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"patient_info"}, false, new Callable<List<PatientEntity>>() {
      @Override
      @Nullable
      public List<PatientEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfPatientId = CursorUtil.getColumnIndexOrThrow(_cursor, "patient_id");
          final int _cursorIndexOfPatientFirstName = CursorUtil.getColumnIndexOrThrow(_cursor, "patient_first_name");
          final int _cursorIndexOfPatientLastName = CursorUtil.getColumnIndexOrThrow(_cursor, "patient_last_name");
          final int _cursorIndexOfWing = CursorUtil.getColumnIndexOrThrow(_cursor, "wing");
          final int _cursorIndexOfRoomNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "room_number");
          final int _cursorIndexOfDietType = CursorUtil.getColumnIndexOrThrow(_cursor, "diet_type");
          final int _cursorIndexOfDiet = CursorUtil.getColumnIndexOrThrow(_cursor, "diet");
          final int _cursorIndexOfAdaDiet = CursorUtil.getColumnIndexOrThrow(_cursor, "ada_diet");
          final int _cursorIndexOfFluidRestriction = CursorUtil.getColumnIndexOrThrow(_cursor, "fluid_restriction");
          final int _cursorIndexOfTextureModifications = CursorUtil.getColumnIndexOrThrow(_cursor, "texture_modifications");
          final int _cursorIndexOfMechanicalChopped = CursorUtil.getColumnIndexOrThrow(_cursor, "mechanical_chopped");
          final int _cursorIndexOfMechanicalGround = CursorUtil.getColumnIndexOrThrow(_cursor, "mechanical_ground");
          final int _cursorIndexOfBiteSize = CursorUtil.getColumnIndexOrThrow(_cursor, "bite_size");
          final int _cursorIndexOfBreadOK = CursorUtil.getColumnIndexOrThrow(_cursor, "bread_ok");
          final int _cursorIndexOfNectarThick = CursorUtil.getColumnIndexOrThrow(_cursor, "nectar_thick");
          final int _cursorIndexOfPuddingThick = CursorUtil.getColumnIndexOrThrow(_cursor, "pudding_thick");
          final int _cursorIndexOfHoneyThick = CursorUtil.getColumnIndexOrThrow(_cursor, "honey_thick");
          final int _cursorIndexOfExtraGravy = CursorUtil.getColumnIndexOrThrow(_cursor, "extra_gravy");
          final int _cursorIndexOfMeatsOnly = CursorUtil.getColumnIndexOrThrow(_cursor, "meats_only");
          final int _cursorIndexOfBreakfastComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_complete");
          final int _cursorIndexOfLunchComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_complete");
          final int _cursorIndexOfDinnerComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_complete");
          final int _cursorIndexOfBreakfastNPO = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_npo");
          final int _cursorIndexOfLunchNPO = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_npo");
          final int _cursorIndexOfDinnerNPO = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_npo");
          final int _cursorIndexOfBreakfastItems = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_items");
          final int _cursorIndexOfLunchItems = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_items");
          final int _cursorIndexOfDinnerItems = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_items");
          final int _cursorIndexOfBreakfastJuices = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_juices");
          final int _cursorIndexOfLunchJuices = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_juices");
          final int _cursorIndexOfDinnerJuices = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_juices");
          final int _cursorIndexOfBreakfastDrinks = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfast_drinks");
          final int _cursorIndexOfLunchDrinks = CursorUtil.getColumnIndexOrThrow(_cursor, "lunch_drinks");
          final int _cursorIndexOfDinnerDrinks = CursorUtil.getColumnIndexOrThrow(_cursor, "dinner_drinks");
          final int _cursorIndexOfCreatedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "created_date");
          final int _cursorIndexOfModifiedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "modified_date");
          final List<PatientEntity> _result = new ArrayList<PatientEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PatientEntity _item;
            _item = new PatientEntity();
            final long _tmpPatientId;
            _tmpPatientId = _cursor.getLong(_cursorIndexOfPatientId);
            _item.setPatientId(_tmpPatientId);
            final String _tmpPatientFirstName;
            if (_cursor.isNull(_cursorIndexOfPatientFirstName)) {
              _tmpPatientFirstName = null;
            } else {
              _tmpPatientFirstName = _cursor.getString(_cursorIndexOfPatientFirstName);
            }
            _item.setPatientFirstName(_tmpPatientFirstName);
            final String _tmpPatientLastName;
            if (_cursor.isNull(_cursorIndexOfPatientLastName)) {
              _tmpPatientLastName = null;
            } else {
              _tmpPatientLastName = _cursor.getString(_cursorIndexOfPatientLastName);
            }
            _item.setPatientLastName(_tmpPatientLastName);
            final String _tmpWing;
            if (_cursor.isNull(_cursorIndexOfWing)) {
              _tmpWing = null;
            } else {
              _tmpWing = _cursor.getString(_cursorIndexOfWing);
            }
            _item.setWing(_tmpWing);
            final String _tmpRoomNumber;
            if (_cursor.isNull(_cursorIndexOfRoomNumber)) {
              _tmpRoomNumber = null;
            } else {
              _tmpRoomNumber = _cursor.getString(_cursorIndexOfRoomNumber);
            }
            _item.setRoomNumber(_tmpRoomNumber);
            final String _tmpDietType;
            if (_cursor.isNull(_cursorIndexOfDietType)) {
              _tmpDietType = null;
            } else {
              _tmpDietType = _cursor.getString(_cursorIndexOfDietType);
            }
            _item.setDietType(_tmpDietType);
            final String _tmpDiet;
            if (_cursor.isNull(_cursorIndexOfDiet)) {
              _tmpDiet = null;
            } else {
              _tmpDiet = _cursor.getString(_cursorIndexOfDiet);
            }
            _item.setDiet(_tmpDiet);
            final boolean _tmpAdaDiet;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfAdaDiet);
            _tmpAdaDiet = _tmp != 0;
            _item.setAdaDiet(_tmpAdaDiet);
            final String _tmpFluidRestriction;
            if (_cursor.isNull(_cursorIndexOfFluidRestriction)) {
              _tmpFluidRestriction = null;
            } else {
              _tmpFluidRestriction = _cursor.getString(_cursorIndexOfFluidRestriction);
            }
            _item.setFluidRestriction(_tmpFluidRestriction);
            final String _tmpTextureModifications;
            if (_cursor.isNull(_cursorIndexOfTextureModifications)) {
              _tmpTextureModifications = null;
            } else {
              _tmpTextureModifications = _cursor.getString(_cursorIndexOfTextureModifications);
            }
            _item.setTextureModifications(_tmpTextureModifications);
            final boolean _tmpMechanicalChopped;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfMechanicalChopped);
            _tmpMechanicalChopped = _tmp_1 != 0;
            _item.setMechanicalChopped(_tmpMechanicalChopped);
            final boolean _tmpMechanicalGround;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfMechanicalGround);
            _tmpMechanicalGround = _tmp_2 != 0;
            _item.setMechanicalGround(_tmpMechanicalGround);
            final boolean _tmpBiteSize;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfBiteSize);
            _tmpBiteSize = _tmp_3 != 0;
            _item.setBiteSize(_tmpBiteSize);
            final boolean _tmpBreadOK;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfBreadOK);
            _tmpBreadOK = _tmp_4 != 0;
            _item.setBreadOK(_tmpBreadOK);
            final boolean _tmpNectarThick;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfNectarThick);
            _tmpNectarThick = _tmp_5 != 0;
            _item.setNectarThick(_tmpNectarThick);
            final boolean _tmpPuddingThick;
            final int _tmp_6;
            _tmp_6 = _cursor.getInt(_cursorIndexOfPuddingThick);
            _tmpPuddingThick = _tmp_6 != 0;
            _item.setPuddingThick(_tmpPuddingThick);
            final boolean _tmpHoneyThick;
            final int _tmp_7;
            _tmp_7 = _cursor.getInt(_cursorIndexOfHoneyThick);
            _tmpHoneyThick = _tmp_7 != 0;
            _item.setHoneyThick(_tmpHoneyThick);
            final boolean _tmpExtraGravy;
            final int _tmp_8;
            _tmp_8 = _cursor.getInt(_cursorIndexOfExtraGravy);
            _tmpExtraGravy = _tmp_8 != 0;
            _item.setExtraGravy(_tmpExtraGravy);
            final boolean _tmpMeatsOnly;
            final int _tmp_9;
            _tmp_9 = _cursor.getInt(_cursorIndexOfMeatsOnly);
            _tmpMeatsOnly = _tmp_9 != 0;
            _item.setMeatsOnly(_tmpMeatsOnly);
            final boolean _tmpBreakfastComplete;
            final int _tmp_10;
            _tmp_10 = _cursor.getInt(_cursorIndexOfBreakfastComplete);
            _tmpBreakfastComplete = _tmp_10 != 0;
            _item.setBreakfastComplete(_tmpBreakfastComplete);
            final boolean _tmpLunchComplete;
            final int _tmp_11;
            _tmp_11 = _cursor.getInt(_cursorIndexOfLunchComplete);
            _tmpLunchComplete = _tmp_11 != 0;
            _item.setLunchComplete(_tmpLunchComplete);
            final boolean _tmpDinnerComplete;
            final int _tmp_12;
            _tmp_12 = _cursor.getInt(_cursorIndexOfDinnerComplete);
            _tmpDinnerComplete = _tmp_12 != 0;
            _item.setDinnerComplete(_tmpDinnerComplete);
            final boolean _tmpBreakfastNPO;
            final int _tmp_13;
            _tmp_13 = _cursor.getInt(_cursorIndexOfBreakfastNPO);
            _tmpBreakfastNPO = _tmp_13 != 0;
            _item.setBreakfastNPO(_tmpBreakfastNPO);
            final boolean _tmpLunchNPO;
            final int _tmp_14;
            _tmp_14 = _cursor.getInt(_cursorIndexOfLunchNPO);
            _tmpLunchNPO = _tmp_14 != 0;
            _item.setLunchNPO(_tmpLunchNPO);
            final boolean _tmpDinnerNPO;
            final int _tmp_15;
            _tmp_15 = _cursor.getInt(_cursorIndexOfDinnerNPO);
            _tmpDinnerNPO = _tmp_15 != 0;
            _item.setDinnerNPO(_tmpDinnerNPO);
            final String _tmpBreakfastItems;
            if (_cursor.isNull(_cursorIndexOfBreakfastItems)) {
              _tmpBreakfastItems = null;
            } else {
              _tmpBreakfastItems = _cursor.getString(_cursorIndexOfBreakfastItems);
            }
            _item.setBreakfastItems(_tmpBreakfastItems);
            final String _tmpLunchItems;
            if (_cursor.isNull(_cursorIndexOfLunchItems)) {
              _tmpLunchItems = null;
            } else {
              _tmpLunchItems = _cursor.getString(_cursorIndexOfLunchItems);
            }
            _item.setLunchItems(_tmpLunchItems);
            final String _tmpDinnerItems;
            if (_cursor.isNull(_cursorIndexOfDinnerItems)) {
              _tmpDinnerItems = null;
            } else {
              _tmpDinnerItems = _cursor.getString(_cursorIndexOfDinnerItems);
            }
            _item.setDinnerItems(_tmpDinnerItems);
            final String _tmpBreakfastJuices;
            if (_cursor.isNull(_cursorIndexOfBreakfastJuices)) {
              _tmpBreakfastJuices = null;
            } else {
              _tmpBreakfastJuices = _cursor.getString(_cursorIndexOfBreakfastJuices);
            }
            _item.setBreakfastJuices(_tmpBreakfastJuices);
            final String _tmpLunchJuices;
            if (_cursor.isNull(_cursorIndexOfLunchJuices)) {
              _tmpLunchJuices = null;
            } else {
              _tmpLunchJuices = _cursor.getString(_cursorIndexOfLunchJuices);
            }
            _item.setLunchJuices(_tmpLunchJuices);
            final String _tmpDinnerJuices;
            if (_cursor.isNull(_cursorIndexOfDinnerJuices)) {
              _tmpDinnerJuices = null;
            } else {
              _tmpDinnerJuices = _cursor.getString(_cursorIndexOfDinnerJuices);
            }
            _item.setDinnerJuices(_tmpDinnerJuices);
            final String _tmpBreakfastDrinks;
            if (_cursor.isNull(_cursorIndexOfBreakfastDrinks)) {
              _tmpBreakfastDrinks = null;
            } else {
              _tmpBreakfastDrinks = _cursor.getString(_cursorIndexOfBreakfastDrinks);
            }
            _item.setBreakfastDrinks(_tmpBreakfastDrinks);
            final String _tmpLunchDrinks;
            if (_cursor.isNull(_cursorIndexOfLunchDrinks)) {
              _tmpLunchDrinks = null;
            } else {
              _tmpLunchDrinks = _cursor.getString(_cursorIndexOfLunchDrinks);
            }
            _item.setLunchDrinks(_tmpLunchDrinks);
            final String _tmpDinnerDrinks;
            if (_cursor.isNull(_cursorIndexOfDinnerDrinks)) {
              _tmpDinnerDrinks = null;
            } else {
              _tmpDinnerDrinks = _cursor.getString(_cursorIndexOfDinnerDrinks);
            }
            _item.setDinnerDrinks(_tmpDinnerDrinks);
            final Date _tmpCreatedDate;
            final Long _tmp_16;
            if (_cursor.isNull(_cursorIndexOfCreatedDate)) {
              _tmp_16 = null;
            } else {
              _tmp_16 = _cursor.getLong(_cursorIndexOfCreatedDate);
            }
            _tmpCreatedDate = DateConverter.fromTimestamp(_tmp_16);
            _item.setCreatedDate(_tmpCreatedDate);
            final Date _tmpModifiedDate;
            final Long _tmp_17;
            if (_cursor.isNull(_cursorIndexOfModifiedDate)) {
              _tmp_17 = null;
            } else {
              _tmp_17 = _cursor.getLong(_cursorIndexOfModifiedDate);
            }
            _tmpModifiedDate = DateConverter.fromTimestamp(_tmp_17);
            _item.setModifiedDate(_tmpModifiedDate);
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
  public LiveData<Integer> getPatientCountLive() {
    final String _sql = "SELECT COUNT(*) FROM patient_info";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"patient_info"}, false, new Callable<Integer>() {
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
  public int getPatientCount() {
    final String _sql = "SELECT COUNT(*) FROM patient_info";
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
  public LiveData<Integer> getPendingCountLive() {
    final String _sql = "SELECT COUNT(*) FROM patient_info WHERE (breakfast_complete = 0 OR lunch_complete = 0 OR dinner_complete = 0)";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"patient_info"}, false, new Callable<Integer>() {
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
  public LiveData<Integer> getCompletedCountLive() {
    final String _sql = "SELECT COUNT(*) FROM patient_info WHERE breakfast_complete = 1 AND lunch_complete = 1 AND dinner_complete = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"patient_info"}, false, new Callable<Integer>() {
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
  public int isRoomOccupied(final String wing, final String roomNumber) {
    final String _sql = "SELECT COUNT(*) FROM patient_info WHERE wing = ? AND room_number = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (wing == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, wing);
    }
    _argIndex = 2;
    if (roomNumber == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, roomNumber);
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
