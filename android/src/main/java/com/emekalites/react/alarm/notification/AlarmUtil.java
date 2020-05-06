package com.emekalites.react.alarm.notification;

import android.app.AlarmManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;

import static com.emekalites.react.alarm.notification.Constants.ADD_INTENT;
import static com.emekalites.react.alarm.notification.Constants.NOTIFICATION_ACTION_DISMISS;
import static com.emekalites.react.alarm.notification.Constants.NOTIFICATION_ACTION_SNOOZE;

class AlarmUtil {
  private static final String TAG = AlarmUtil.class.getSimpleName();

  private Context mContext;

  AlarmUtil(Application context) {
    mContext = context;

  }

  private Class getMainActivityClass() {
    String packageName = mContext.getPackageName();
    Intent launchIntent = mContext.getPackageManager().getLaunchIntentForPackage(packageName);
    String className = launchIntent.getComponent().getClassName();
    Log.e(TAG, "main activity classname: " + className);
    try {
      return Class.forName(className);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      return null;
    }
  }

  private AlarmManager getAlarmManager() {
    return (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
  }

  private AlarmDatabase getAlarmDB() {
    return AlarmDatabase.getInstance(mContext);
  }

  private NotificationManager getNotificationManager() {
    return (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
  }

  void setBootReceiver() {
    ArrayList<AlarmModel> alarms = getAlarmDB().getAlarmList(1);
    if (alarms.size() > 0) {
      enableBootReceiver(mContext);
    } else {
      disableBootReceiver(mContext);
    }
  }

  void setAlarm(AlarmModel alarm) {
    int alarmId = alarm.getAlarmId();

    Intent intent = new Intent(mContext, AlarmReceiver.class);
    intent.putExtra("intentType", ADD_INTENT);
    intent.putExtra("PendingId", alarm.getId());

    PendingIntent alarmIntent = PendingIntent.getBroadcast(mContext, alarmId, intent, 0);
    AlarmManager alarmManager = this.getAlarmManager();

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarm.getAlarmFireTime(), alarmIntent);
    } else {
      alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarm.getAlarmFireTime(), alarmIntent);
    }

    this.setBootReceiver();
  }

  void doCancelAlarm(String id) {
    try {
      AlarmModel alarm = getAlarmDB().getAlarm(Integer.parseInt(id));
      this.cancelAlarm(alarm);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  void cancelAlarm(AlarmModel alarm) {
    AlarmManager alarmManager = this.getAlarmManager();

    int alarmId = alarm.getAlarmId();
    getAlarmDB().delete(alarmId);

    Intent intent = new Intent(mContext, AlarmReceiver.class);
    PendingIntent alarmIntent = PendingIntent.getBroadcast(mContext, alarmId, intent,
      PendingIntent.FLAG_UPDATE_CURRENT);
    alarmManager.cancel(alarmIntent);

    this.setBootReceiver();
  }

  private void enableBootReceiver(Context context) {
    ComponentName receiver = new ComponentName(context, AlarmBootReceiver.class);
    PackageManager pm = context.getPackageManager();

    int setting = pm.getComponentEnabledSetting(receiver);
    if (setting == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
      pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
        PackageManager.DONT_KILL_APP);
    }
  }

  private void disableBootReceiver(Context context) {
    ComponentName receiver = new ComponentName(context, AlarmBootReceiver.class);
    PackageManager pm = context.getPackageManager();

    pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
      PackageManager.DONT_KILL_APP);
  }

  void sendNotification(AlarmModel alarm) {
    try {
      Class intentClass = getMainActivityClass();
      if (intentClass == null) {
        Log.e(TAG, "No activity class found for the notification");
        return;
      }

      NotificationManager mNotificationManager = getNotificationManager();
      int notificationID = alarm.getAlarmId();

      // title
      String title = alarm.getTitle();
      if (title == null || title.equals("")) {
        ApplicationInfo appInfo = mContext.getApplicationInfo();
        title = mContext.getPackageManager().getApplicationLabel(appInfo).toString();
      }

      // message
      String message = alarm.getMessage();
      if (message == null || message.equals("")) {
        Log.d(TAG, "Cannot send to notification centre because there is no 'message' found");
        return;
      }

      // channel
      String channelID = alarm.getChannel();
      if (channelID == null || channelID.equals("")) {
        Log.d(TAG, "Cannot send to notification centre because there is no 'channel' found");
        return;
      }

      Resources res = mContext.getResources();
      String packageName = mContext.getPackageName();

      // icon
      int smallIconResId;
      String smallIcon = alarm.getSmallIcon();
      if (smallIcon != null && !smallIcon.equals("")) {
        smallIconResId = res.getIdentifier(smallIcon, "mipmap", packageName);
      } else {
        smallIconResId = res.getIdentifier("ic_launcher", "mipmap", packageName);
      }

      Intent intent = new Intent(mContext, intentClass);
      intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

      Bundle bundle = new Bundle();
      if (alarm.getData() != null && !alarm.getData().equals("")) {
        String[] datum = alarm.getData().split(";;");
        for (String item : datum) {
          String[] data = item.split("==>");
          bundle.putString(data[0], data[1]);
        }

        intent.putExtras(bundle);
      }

      PendingIntent pendingIntent = PendingIntent.getActivity(mContext, notificationID, intent,
        PendingIntent.FLAG_UPDATE_CURRENT);

      NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext, channelID)
        .setSmallIcon(smallIconResId).setContentTitle(title).setContentText(message)
        .setDefaults(NotificationCompat.DEFAULT_ALL)
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .setCategory(NotificationCompat.CATEGORY_ALARM);

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        mBuilder.setChannelId(channelID);
      }

      mBuilder.setContentIntent(pendingIntent);

      // set tag and push notification
      Notification notification = mBuilder.build();

      Log.e(TAG, "notification done");
      mNotificationManager.notify(notificationID, notification);
    } catch (Exception e) {
      Log.e(TAG, "failed to send notification", e);
    }
  }

  void removeFiredNotification(int notificationId) {
    getNotificationManager().cancel(notificationId);
  }

  void removeAllFiredNotifications() {
    getNotificationManager().cancelAll();
  }

  ArrayList<AlarmModel> getAlarms() {
    return getAlarmDB().getAlarmList(1);
  }

  WritableMap convertJsonToMap(JSONObject jsonObject) throws JSONException {
    WritableMap map = new WritableNativeMap();

    Iterator<String> iterator = jsonObject.keys();
    while (iterator.hasNext()) {
      String key = iterator.next();
      Object value = jsonObject.get(key);
      if (value instanceof JSONObject) {
        map.putMap(key, convertJsonToMap((JSONObject) value));
      } else if (value instanceof Boolean) {
        map.putBoolean(key, (Boolean) value);
      } else if (value instanceof Integer) {
        map.putInt(key, (Integer) value);
      } else if (value instanceof Double) {
        map.putDouble(key, (Double) value);
      } else if (value instanceof String) {
        map.putString(key, (String) value);
      } else {
        map.putString(key, value.toString());
      }
    }
    return map;
  }
}
