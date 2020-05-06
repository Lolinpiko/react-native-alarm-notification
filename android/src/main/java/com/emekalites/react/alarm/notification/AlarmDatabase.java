package com.emekalites.react.alarm.notification;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;

public class AlarmDatabase extends SQLiteOpenHelper {
  private static final String TAG = AlarmDatabase.class.getSimpleName();

  private static final int DATABASE_VERSION = 1;
  private static final String DATABASE_NAME = "rnandb";

  private static final String TABLE_NAME = "alarmtbl";
  private static final String COL_ID = "id";
  private static final String COL_DATA = "gson_data";
  private static final String COL_ACTIVE = "active";

  private String CREATE_TABLE_ALARM = "CREATE TABLE " + TABLE_NAME + " (" + COL_ID
    + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_DATA + " TEXT, " + COL_ACTIVE + " INTEGER) ";

  private static AlarmDatabase mInstance = null;

  public static AlarmDatabase getInstance(Context ctx) {

    // Use the application context, which will ensure that you
    // don't accidentally leak an Activity's context.
    // See this article for more information: http://bit.ly/6LRzfx
    if (mInstance == null) {
      mInstance = new AlarmDatabase(ctx.getApplicationContext());
    }
    return mInstance;
  }

  private AlarmDatabase(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL(CREATE_TABLE_ALARM);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    db.execSQL(String.format(" DROP TABLE IF EXISTS %s", CREATE_TABLE_ALARM));
    onCreate(db);
  }

  AlarmModel getAlarm(int _id) {
    SQLiteDatabase db = this.getReadableDatabase();
    AlarmModel alarm = null;

    String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + COL_ID + "=" + _id;

    Cursor cursor = db.rawQuery(selectQuery, null);

    try {
      cursor.moveToFirst();

      int id = cursor.getInt(0);
      String data = cursor.getString(1);
      int active = cursor.getInt(2);

      Gson gson = new Gson();

      alarm = gson.fromJson(data, AlarmModel.class);
      alarm.setId(id);
      alarm.setActive(active);
    } catch (Exception e) {
      Log.e(TAG, "getAlarm: exception cause " + e.getCause() + " message " + e.getMessage());
      e.printStackTrace();
    } finally {
      if (cursor != null) {
        cursor.close();
      }

      db.close();
    }

    return alarm;
  }

  int insert(AlarmModel alarm) {
    SQLiteDatabase db = null;
    int alarmID = 0;

    try {
      db = this.getWritableDatabase();

      ContentValues values = new ContentValues();

      Gson gson = new Gson();

      String data = gson.toJson(alarm);

      values.put(COL_DATA, data);
      values.put(COL_ACTIVE, alarm.getActive());

      alarmID = (int) db.insert(TABLE_NAME, null, values);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (db != null) {
        db.close();
      }
    }

    return alarmID;
  }

  void delete(int id) {
    SQLiteDatabase db = null;
    String where = COL_ID + "=" + id;

    try {
      db = this.getWritableDatabase();
      db.delete(TABLE_NAME, where, null);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (db != null) {
        db.close();
      }
    }
  }

  ArrayList<AlarmModel> getAlarmList(int isActive) {
    String selectQuery = "SELECT * FROM " + TABLE_NAME;

    if (isActive == 1) {
      selectQuery += " WHERE " + COL_ACTIVE + " = " + isActive;
    }

    SQLiteDatabase db = this.getWritableDatabase();
    ArrayList<AlarmModel> alarms = new ArrayList<>();
    Cursor cursor = db.rawQuery(selectQuery, null);

    try {
      if (cursor.moveToFirst()) {
        do {
          int id = cursor.getInt(0);
          String data = cursor.getString(1);
          int active = cursor.getInt(2);

          Gson gson = new Gson();

          AlarmModel alarm = gson.fromJson(data, AlarmModel.class);
          alarm.setId(id);
          alarm.setActive(active);

          alarms.add(alarm);
        } while (cursor.moveToNext());
      }
    } catch (Exception e) {
      Log.e(TAG, "getAlarmList: exception cause " + e.getCause() + " message " + e.getMessage());
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }

    return alarms;
  }

  ArrayList<AlarmModel> getAlarmList() {
    return getAlarmList(0);
  }
}
