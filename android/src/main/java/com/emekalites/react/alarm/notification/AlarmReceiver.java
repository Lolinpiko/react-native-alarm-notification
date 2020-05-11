package com.emekalites.react.alarm.notification;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.ArrayList;

public class AlarmReceiver extends BroadcastReceiver {
  private static final String TAG = AlarmReceiver.class.getSimpleName();

  AlarmModel alarm;

  int id;

  @Override
  public void onReceive(Context context, Intent intent) {
    if (intent != null) {
      FileLogger.d(context, "AlarmReceiver: ALARM RECEIVED\n");
      final AlarmDatabase alarmDB = AlarmDatabase.getInstance(context);
      AlarmUtil alarmUtil = new AlarmUtil((Application) context.getApplicationContext());

      try {
        String intentType = intent.getExtras().getString("intentType");
        if (Constants.ADD_INTENT.equals(intentType)) {
          id = intent.getExtras().getInt("PendingId");

          try {
            alarm = alarmDB.getAlarm(id);

            FileLogger.d(context, "AlarmReceiver: HANDLE ALARM\n" + alarm.toString());

            alarmUtil.sendNotification(alarm);

            alarmDB.delete(id);

            alarmUtil.setBootReceiver();
          } catch (Exception e) {
            Log.e(TAG, "ERROR");
            e.printStackTrace();
          }
        }

      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
