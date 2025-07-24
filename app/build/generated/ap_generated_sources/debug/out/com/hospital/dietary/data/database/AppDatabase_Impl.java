package com.hospital.dietary.data.database;

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
import com.hospital.dietary.data.dao.DefaultMenuDao;
import com.hospital.dietary.data.dao.DefaultMenuDao_Impl;
import com.hospital.dietary.data.dao.FinalizedOrderDao;
import com.hospital.dietary.data.dao.FinalizedOrderDao_Impl;
import com.hospital.dietary.data.dao.ItemDao;
import com.hospital.dietary.data.dao.ItemDao_Impl;
import com.hospital.dietary.data.dao.MealOrderDao;
import com.hospital.dietary.data.dao.MealOrderDao_Impl;
import com.hospital.dietary.data.dao.OrderItemDao;
import com.hospital.dietary.data.dao.OrderItemDao_Impl;
import com.hospital.dietary.data.dao.PatientDao;
import com.hospital.dietary.data.dao.PatientDao_Impl;
import com.hospital.dietary.data.dao.UserDao;
import com.hospital.dietary.data.dao.UserDao_Impl;
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

@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile UserDao _userDao;

  private volatile PatientDao _patientDao;

  private volatile ItemDao _itemDao;

  private volatile MealOrderDao _mealOrderDao;

  private volatile OrderItemDao _orderItemDao;

  private volatile DefaultMenuDao _defaultMenuDao;

  private volatile FinalizedOrderDao _finalizedOrderDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(11) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `users` (`user_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `username` TEXT, `password` TEXT, `full_name` TEXT, `role` TEXT, `is_active` INTEGER NOT NULL DEFAULT 1, `must_change_password` INTEGER NOT NULL DEFAULT 0, `last_login` INTEGER, `created_date` INTEGER DEFAULT CURRENT_TIMESTAMP)");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_users_username` ON `users` (`username`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `patient_info` (`patient_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `patient_first_name` TEXT, `patient_last_name` TEXT, `wing` TEXT, `room_number` TEXT, `diet_type` TEXT, `diet` TEXT, `ada_diet` INTEGER NOT NULL DEFAULT 0, `fluid_restriction` TEXT, `texture_modifications` TEXT, `mechanical_chopped` INTEGER NOT NULL DEFAULT 0, `mechanical_ground` INTEGER NOT NULL DEFAULT 0, `bite_size` INTEGER NOT NULL DEFAULT 0, `bread_ok` INTEGER NOT NULL DEFAULT 0, `nectar_thick` INTEGER NOT NULL DEFAULT 0, `pudding_thick` INTEGER NOT NULL DEFAULT 0, `honey_thick` INTEGER NOT NULL DEFAULT 0, `extra_gravy` INTEGER NOT NULL DEFAULT 0, `meats_only` INTEGER NOT NULL DEFAULT 0, `is_puree` INTEGER NOT NULL DEFAULT 0, `allergies` TEXT, `likes` TEXT, `dislikes` TEXT, `comments` TEXT, `preferred_drink` TEXT, `drink_variety` TEXT, `breakfast_complete` INTEGER NOT NULL DEFAULT 0, `lunch_complete` INTEGER NOT NULL DEFAULT 0, `dinner_complete` INTEGER NOT NULL DEFAULT 0, `breakfast_npo` INTEGER NOT NULL DEFAULT 0, `lunch_npo` INTEGER NOT NULL DEFAULT 0, `dinner_npo` INTEGER NOT NULL DEFAULT 0, `breakfast_items` TEXT, `lunch_items` TEXT, `dinner_items` TEXT, `breakfast_juices` TEXT, `lunch_juices` TEXT, `dinner_juices` TEXT, `breakfast_drinks` TEXT, `lunch_drinks` TEXT, `dinner_drinks` TEXT, `created_date` INTEGER DEFAULT CURRENT_TIMESTAMP, `breakfast_diet` TEXT, `lunch_diet` TEXT, `dinner_diet` TEXT, `breakfast_ada` INTEGER NOT NULL DEFAULT 0, `lunch_ada` INTEGER NOT NULL DEFAULT 0, `dinner_ada` INTEGER NOT NULL DEFAULT 0)");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_patient_info_wing_room_number` ON `patient_info` (`wing`, `room_number`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `items` (`item_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT, `category` TEXT, `description` TEXT, `is_ada_friendly` INTEGER NOT NULL DEFAULT 0)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_items_category` ON `items` (`category`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_items_is_ada_friendly` ON `items` (`is_ada_friendly`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `meal_orders` (`order_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `patient_id` INTEGER NOT NULL, `meal` TEXT, `order_date` INTEGER, `is_complete` INTEGER NOT NULL, `created_by` TEXT, `timestamp` INTEGER)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `order_items` (`order_item_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `order_id` INTEGER NOT NULL, `item_id` INTEGER NOT NULL, `quantity` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `default_menu` (`menu_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `diet_type` TEXT, `meal_type` TEXT, `day_of_week` TEXT, `item_name` TEXT, `item_category` TEXT, `is_active` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `finalized_order` (`order_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `patient_name` TEXT, `wing` TEXT, `room` TEXT, `order_date` TEXT, `diet_type` TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '15b7fdfe5846c46b88d6e8e8ea5186bd')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `users`");
        db.execSQL("DROP TABLE IF EXISTS `patient_info`");
        db.execSQL("DROP TABLE IF EXISTS `items`");
        db.execSQL("DROP TABLE IF EXISTS `meal_orders`");
        db.execSQL("DROP TABLE IF EXISTS `order_items`");
        db.execSQL("DROP TABLE IF EXISTS `default_menu`");
        db.execSQL("DROP TABLE IF EXISTS `finalized_order`");
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
        final HashMap<String, TableInfo.Column> _columnsUsers = new HashMap<String, TableInfo.Column>(9);
        _columnsUsers.put("user_id", new TableInfo.Column("user_id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("username", new TableInfo.Column("username", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("password", new TableInfo.Column("password", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("full_name", new TableInfo.Column("full_name", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("role", new TableInfo.Column("role", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("is_active", new TableInfo.Column("is_active", "INTEGER", true, 0, "1", TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("must_change_password", new TableInfo.Column("must_change_password", "INTEGER", true, 0, "0", TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("last_login", new TableInfo.Column("last_login", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("created_date", new TableInfo.Column("created_date", "INTEGER", false, 0, "CURRENT_TIMESTAMP", TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysUsers = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesUsers = new HashSet<TableInfo.Index>(1);
        _indicesUsers.add(new TableInfo.Index("index_users_username", true, Arrays.asList("username"), Arrays.asList("ASC")));
        final TableInfo _infoUsers = new TableInfo("users", _columnsUsers, _foreignKeysUsers, _indicesUsers);
        final TableInfo _existingUsers = TableInfo.read(db, "users");
        if (!_infoUsers.equals(_existingUsers)) {
          return new RoomOpenHelper.ValidationResult(false, "users(com.hospital.dietary.data.entities.UserEntity).\n"
                  + " Expected:\n" + _infoUsers + "\n"
                  + " Found:\n" + _existingUsers);
        }
        final HashMap<String, TableInfo.Column> _columnsPatientInfo = new HashMap<String, TableInfo.Column>(48);
        _columnsPatientInfo.put("patient_id", new TableInfo.Column("patient_id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatientInfo.put("patient_first_name", new TableInfo.Column("patient_first_name", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatientInfo.put("patient_last_name", new TableInfo.Column("patient_last_name", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatientInfo.put("wing", new TableInfo.Column("wing", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatientInfo.put("room_number", new TableInfo.Column("room_number", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatientInfo.put("diet_type", new TableInfo.Column("diet_type", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatientInfo.put("diet", new TableInfo.Column("diet", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatientInfo.put("ada_diet", new TableInfo.Column("ada_diet", "INTEGER", true, 0, "0", TableInfo.CREATED_FROM_ENTITY));
        _columnsPatientInfo.put("fluid_restriction", new TableInfo.Column("fluid_restriction", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatientInfo.put("texture_modifications", new TableInfo.Column("texture_modifications", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatientInfo.put("mechanical_chopped", new TableInfo.Column("mechanical_chopped", "INTEGER", true, 0, "0", TableInfo.CREATED_FROM_ENTITY));
        _columnsPatientInfo.put("mechanical_ground", new TableInfo.Column("mechanical_ground", "INTEGER", true, 0, "0", TableInfo.CREATED_FROM_ENTITY));
        _columnsPatientInfo.put("bite_size", new TableInfo.Column("bite_size", "INTEGER", true, 0, "0", TableInfo.CREATED_FROM_ENTITY));
        _columnsPatientInfo.put("bread_ok", new TableInfo.Column("bread_ok", "INTEGER", true, 0, "0", TableInfo.CREATED_FROM_ENTITY));
        _columnsPatientInfo.put("nectar_thick", new TableInfo.Column("nectar_thick", "INTEGER", true, 0, "0", TableInfo.CREATED_FROM_ENTITY));
        _columnsPatientInfo.put("pudding_thick", new TableInfo.Column("pudding_thick", "INTEGER", true, 0, "0", TableInfo.CREATED_FROM_ENTITY));
        _columnsPatientInfo.put("honey_thick", new TableInfo.Column("honey_thick", "INTEGER", true, 0, "0", TableInfo.CREATED_FROM_ENTITY));
        _columnsPatientInfo.put("extra_gravy", new TableInfo.Column("extra_gravy", "INTEGER", true, 0, "0", TableInfo.CREATED_FROM_ENTITY));
        _columnsPatientInfo.put("meats_only", new TableInfo.Column("meats_only", "INTEGER", true, 0, "0", TableInfo.CREATED_FROM_ENTITY));
        _columnsPatientInfo.put("is_puree", new TableInfo.Column("is_puree", "INTEGER", true, 0, "0", TableInfo.CREATED_FROM_ENTITY));
        _columnsPatientInfo.put("allergies", new TableInfo.Column("allergies", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatientInfo.put("likes", new TableInfo.Column("likes", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatientInfo.put("dislikes", new TableInfo.Column("dislikes", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatientInfo.put("comments", new TableInfo.Column("comments", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatientInfo.put("preferred_drink", new TableInfo.Column("preferred_drink", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatientInfo.put("drink_variety", new TableInfo.Column("drink_variety", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatientInfo.put("breakfast_complete", new TableInfo.Column("breakfast_complete", "INTEGER", true, 0, "0", TableInfo.CREATED_FROM_ENTITY));
        _columnsPatientInfo.put("lunch_complete", new TableInfo.Column("lunch_complete", "INTEGER", true, 0, "0", TableInfo.CREATED_FROM_ENTITY));
        _columnsPatientInfo.put("dinner_complete", new TableInfo.Column("dinner_complete", "INTEGER", true, 0, "0", TableInfo.CREATED_FROM_ENTITY));
        _columnsPatientInfo.put("breakfast_npo", new TableInfo.Column("breakfast_npo", "INTEGER", true, 0, "0", TableInfo.CREATED_FROM_ENTITY));
        _columnsPatientInfo.put("lunch_npo", new TableInfo.Column("lunch_npo", "INTEGER", true, 0, "0", TableInfo.CREATED_FROM_ENTITY));
        _columnsPatientInfo.put("dinner_npo", new TableInfo.Column("dinner_npo", "INTEGER", true, 0, "0", TableInfo.CREATED_FROM_ENTITY));
        _columnsPatientInfo.put("breakfast_items", new TableInfo.Column("breakfast_items", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatientInfo.put("lunch_items", new TableInfo.Column("lunch_items", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatientInfo.put("dinner_items", new TableInfo.Column("dinner_items", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatientInfo.put("breakfast_juices", new TableInfo.Column("breakfast_juices", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatientInfo.put("lunch_juices", new TableInfo.Column("lunch_juices", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatientInfo.put("dinner_juices", new TableInfo.Column("dinner_juices", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatientInfo.put("breakfast_drinks", new TableInfo.Column("breakfast_drinks", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatientInfo.put("lunch_drinks", new TableInfo.Column("lunch_drinks", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatientInfo.put("dinner_drinks", new TableInfo.Column("dinner_drinks", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatientInfo.put("created_date", new TableInfo.Column("created_date", "INTEGER", false, 0, "CURRENT_TIMESTAMP", TableInfo.CREATED_FROM_ENTITY));
        _columnsPatientInfo.put("breakfast_diet", new TableInfo.Column("breakfast_diet", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatientInfo.put("lunch_diet", new TableInfo.Column("lunch_diet", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatientInfo.put("dinner_diet", new TableInfo.Column("dinner_diet", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatientInfo.put("breakfast_ada", new TableInfo.Column("breakfast_ada", "INTEGER", true, 0, "0", TableInfo.CREATED_FROM_ENTITY));
        _columnsPatientInfo.put("lunch_ada", new TableInfo.Column("lunch_ada", "INTEGER", true, 0, "0", TableInfo.CREATED_FROM_ENTITY));
        _columnsPatientInfo.put("dinner_ada", new TableInfo.Column("dinner_ada", "INTEGER", true, 0, "0", TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPatientInfo = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPatientInfo = new HashSet<TableInfo.Index>(1);
        _indicesPatientInfo.add(new TableInfo.Index("index_patient_info_wing_room_number", true, Arrays.asList("wing", "room_number"), Arrays.asList("ASC", "ASC")));
        final TableInfo _infoPatientInfo = new TableInfo("patient_info", _columnsPatientInfo, _foreignKeysPatientInfo, _indicesPatientInfo);
        final TableInfo _existingPatientInfo = TableInfo.read(db, "patient_info");
        if (!_infoPatientInfo.equals(_existingPatientInfo)) {
          return new RoomOpenHelper.ValidationResult(false, "patient_info(com.hospital.dietary.data.entities.PatientEntity).\n"
                  + " Expected:\n" + _infoPatientInfo + "\n"
                  + " Found:\n" + _existingPatientInfo);
        }
        final HashMap<String, TableInfo.Column> _columnsItems = new HashMap<String, TableInfo.Column>(5);
        _columnsItems.put("item_id", new TableInfo.Column("item_id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsItems.put("name", new TableInfo.Column("name", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsItems.put("category", new TableInfo.Column("category", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsItems.put("description", new TableInfo.Column("description", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsItems.put("is_ada_friendly", new TableInfo.Column("is_ada_friendly", "INTEGER", true, 0, "0", TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysItems = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesItems = new HashSet<TableInfo.Index>(2);
        _indicesItems.add(new TableInfo.Index("index_items_category", false, Arrays.asList("category"), Arrays.asList("ASC")));
        _indicesItems.add(new TableInfo.Index("index_items_is_ada_friendly", false, Arrays.asList("is_ada_friendly"), Arrays.asList("ASC")));
        final TableInfo _infoItems = new TableInfo("items", _columnsItems, _foreignKeysItems, _indicesItems);
        final TableInfo _existingItems = TableInfo.read(db, "items");
        if (!_infoItems.equals(_existingItems)) {
          return new RoomOpenHelper.ValidationResult(false, "items(com.hospital.dietary.data.entities.ItemEntity).\n"
                  + " Expected:\n" + _infoItems + "\n"
                  + " Found:\n" + _existingItems);
        }
        final HashMap<String, TableInfo.Column> _columnsMealOrders = new HashMap<String, TableInfo.Column>(7);
        _columnsMealOrders.put("order_id", new TableInfo.Column("order_id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMealOrders.put("patient_id", new TableInfo.Column("patient_id", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMealOrders.put("meal", new TableInfo.Column("meal", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMealOrders.put("order_date", new TableInfo.Column("order_date", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMealOrders.put("is_complete", new TableInfo.Column("is_complete", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMealOrders.put("created_by", new TableInfo.Column("created_by", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMealOrders.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysMealOrders = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesMealOrders = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoMealOrders = new TableInfo("meal_orders", _columnsMealOrders, _foreignKeysMealOrders, _indicesMealOrders);
        final TableInfo _existingMealOrders = TableInfo.read(db, "meal_orders");
        if (!_infoMealOrders.equals(_existingMealOrders)) {
          return new RoomOpenHelper.ValidationResult(false, "meal_orders(com.hospital.dietary.data.entities.MealOrderEntity).\n"
                  + " Expected:\n" + _infoMealOrders + "\n"
                  + " Found:\n" + _existingMealOrders);
        }
        final HashMap<String, TableInfo.Column> _columnsOrderItems = new HashMap<String, TableInfo.Column>(4);
        _columnsOrderItems.put("order_item_id", new TableInfo.Column("order_item_id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOrderItems.put("order_id", new TableInfo.Column("order_id", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOrderItems.put("item_id", new TableInfo.Column("item_id", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOrderItems.put("quantity", new TableInfo.Column("quantity", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysOrderItems = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesOrderItems = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoOrderItems = new TableInfo("order_items", _columnsOrderItems, _foreignKeysOrderItems, _indicesOrderItems);
        final TableInfo _existingOrderItems = TableInfo.read(db, "order_items");
        if (!_infoOrderItems.equals(_existingOrderItems)) {
          return new RoomOpenHelper.ValidationResult(false, "order_items(com.hospital.dietary.data.entities.OrderItemEntity).\n"
                  + " Expected:\n" + _infoOrderItems + "\n"
                  + " Found:\n" + _existingOrderItems);
        }
        final HashMap<String, TableInfo.Column> _columnsDefaultMenu = new HashMap<String, TableInfo.Column>(7);
        _columnsDefaultMenu.put("menu_id", new TableInfo.Column("menu_id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDefaultMenu.put("diet_type", new TableInfo.Column("diet_type", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDefaultMenu.put("meal_type", new TableInfo.Column("meal_type", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDefaultMenu.put("day_of_week", new TableInfo.Column("day_of_week", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDefaultMenu.put("item_name", new TableInfo.Column("item_name", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDefaultMenu.put("item_category", new TableInfo.Column("item_category", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDefaultMenu.put("is_active", new TableInfo.Column("is_active", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysDefaultMenu = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesDefaultMenu = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoDefaultMenu = new TableInfo("default_menu", _columnsDefaultMenu, _foreignKeysDefaultMenu, _indicesDefaultMenu);
        final TableInfo _existingDefaultMenu = TableInfo.read(db, "default_menu");
        if (!_infoDefaultMenu.equals(_existingDefaultMenu)) {
          return new RoomOpenHelper.ValidationResult(false, "default_menu(com.hospital.dietary.data.entities.DefaultMenuEntity).\n"
                  + " Expected:\n" + _infoDefaultMenu + "\n"
                  + " Found:\n" + _existingDefaultMenu);
        }
        final HashMap<String, TableInfo.Column> _columnsFinalizedOrder = new HashMap<String, TableInfo.Column>(6);
        _columnsFinalizedOrder.put("order_id", new TableInfo.Column("order_id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFinalizedOrder.put("patient_name", new TableInfo.Column("patient_name", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFinalizedOrder.put("wing", new TableInfo.Column("wing", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFinalizedOrder.put("room", new TableInfo.Column("room", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFinalizedOrder.put("order_date", new TableInfo.Column("order_date", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFinalizedOrder.put("diet_type", new TableInfo.Column("diet_type", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysFinalizedOrder = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesFinalizedOrder = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoFinalizedOrder = new TableInfo("finalized_order", _columnsFinalizedOrder, _foreignKeysFinalizedOrder, _indicesFinalizedOrder);
        final TableInfo _existingFinalizedOrder = TableInfo.read(db, "finalized_order");
        if (!_infoFinalizedOrder.equals(_existingFinalizedOrder)) {
          return new RoomOpenHelper.ValidationResult(false, "finalized_order(com.hospital.dietary.data.entities.FinalizedOrderEntity).\n"
                  + " Expected:\n" + _infoFinalizedOrder + "\n"
                  + " Found:\n" + _existingFinalizedOrder);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "15b7fdfe5846c46b88d6e8e8ea5186bd", "359811f9d0e86c8948055b7a96b703f8");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "users","patient_info","items","meal_orders","order_items","default_menu","finalized_order");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `users`");
      _db.execSQL("DELETE FROM `patient_info`");
      _db.execSQL("DELETE FROM `items`");
      _db.execSQL("DELETE FROM `meal_orders`");
      _db.execSQL("DELETE FROM `order_items`");
      _db.execSQL("DELETE FROM `default_menu`");
      _db.execSQL("DELETE FROM `finalized_order`");
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
    _typeConvertersMap.put(UserDao.class, UserDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(PatientDao.class, PatientDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ItemDao.class, ItemDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(MealOrderDao.class, MealOrderDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(OrderItemDao.class, OrderItemDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(DefaultMenuDao.class, DefaultMenuDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(FinalizedOrderDao.class, FinalizedOrderDao_Impl.getRequiredConverters());
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
  public UserDao userDao() {
    if (_userDao != null) {
      return _userDao;
    } else {
      synchronized(this) {
        if(_userDao == null) {
          _userDao = new UserDao_Impl(this);
        }
        return _userDao;
      }
    }
  }

  @Override
  public PatientDao patientDao() {
    if (_patientDao != null) {
      return _patientDao;
    } else {
      synchronized(this) {
        if(_patientDao == null) {
          _patientDao = new PatientDao_Impl(this);
        }
        return _patientDao;
      }
    }
  }

  @Override
  public ItemDao itemDao() {
    if (_itemDao != null) {
      return _itemDao;
    } else {
      synchronized(this) {
        if(_itemDao == null) {
          _itemDao = new ItemDao_Impl(this);
        }
        return _itemDao;
      }
    }
  }

  @Override
  public MealOrderDao mealOrderDao() {
    if (_mealOrderDao != null) {
      return _mealOrderDao;
    } else {
      synchronized(this) {
        if(_mealOrderDao == null) {
          _mealOrderDao = new MealOrderDao_Impl(this);
        }
        return _mealOrderDao;
      }
    }
  }

  @Override
  public OrderItemDao orderItemDao() {
    if (_orderItemDao != null) {
      return _orderItemDao;
    } else {
      synchronized(this) {
        if(_orderItemDao == null) {
          _orderItemDao = new OrderItemDao_Impl(this);
        }
        return _orderItemDao;
      }
    }
  }

  @Override
  public DefaultMenuDao defaultMenuDao() {
    if (_defaultMenuDao != null) {
      return _defaultMenuDao;
    } else {
      synchronized(this) {
        if(_defaultMenuDao == null) {
          _defaultMenuDao = new DefaultMenuDao_Impl(this);
        }
        return _defaultMenuDao;
      }
    }
  }

  @Override
  public FinalizedOrderDao finalizedOrderDao() {
    if (_finalizedOrderDao != null) {
      return _finalizedOrderDao;
    } else {
      synchronized(this) {
        if(_finalizedOrderDao == null) {
          _finalizedOrderDao = new FinalizedOrderDao_Impl(this);
        }
        return _finalizedOrderDao;
      }
    }
  }
}
