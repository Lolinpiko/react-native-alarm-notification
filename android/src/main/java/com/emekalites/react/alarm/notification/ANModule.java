package com.emekalites.react.alarm.notification;

import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ANModule extends ReactContextBaseJavaModule {
  private final static String TAG = ANModule.class.getCanonicalName();
  private AlarmUtil alarmUtil;
  private static ReactApplicationContext mReactContext;

  ANModule(ReactApplicationContext reactContext) {
    super(reactContext);
    mReactContext = reactContext;
    alarmUtil = new AlarmUtil((Application) reactContext.getApplicationContext());
  }

  static ReactApplicationContext getReactAppContext() {
    return mReactContext;
  }

  @Override
  public String getName() {
    return "RNAlarmNotification";
  }

  private AlarmDatabase getAlarmDB() {
    return AlarmDatabase.getInstance(mReactContext);
  }

  @ReactMethod
  public void scheduleAlarm(ReadableMap details) throws ParseException {
    Bundle bundle = Arguments.toBundle(details);

    AlarmModel alarm = new AlarmModel();

    alarm.setActive(1);
    alarm.setChannel(bundle.getString("channel", "my_channel_id"));

    Bundle data = bundle.getBundle("data");
    alarm.setData(bundle2string(data));

    alarm.setInterval(bundle.getInt("repeat_interval", 1));
    alarm.setMessage(bundle.getString("message", "My Notification Message"));
    alarm.setSmallIcon(bundle.getString("small_icon", "ic_launcher"));
    alarm.setTag(bundle.getString("tag", ""));
    alarm.setTitle(bundle.getString("title", "My Notification Title"));

    try {
      int alarmId = Integer.parseInt(bundle.getString("alarm_id"));
      if (alarmId == 0) {
        alarmId = (int) System.currentTimeMillis();
      }
      alarm.setAlarmId(alarmId);
    } catch (Exception e) {
      alarm.setAlarmId((int) System.currentTimeMillis());
    }

    String datetime = bundle.getString("fire_date");
    if (datetime == null || datetime.equals("")) {
      FileLogger.e(mReactContext, "failed to schedule notification because fire date is missing");
      Log.e(TAG, "failed to schedule notification because fire date is missing");
      return;
    }

    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", getReactAppContext().getResources().getConfiguration().locale);
    Date mDate = sdf.parse(datetime);

    alarm.setAlarmFireTime(mDate.getTime());

    Log.d(TAG, "Alarm scheduled");
    try {
      int id = getAlarmDB().insert(alarm);
      alarm.setId(id);
      FileLogger.d(mReactContext, "ANMODULE: SCHEDULE ALARM\n" + alarm.toString());

      alarmUtil.setAlarm(alarm);
    } catch (Exception e) {
      FileLogger.e(mReactContext, "ANMODULE: SCHEDULE ALARM ERROR\n" + e.toString());
      e.printStackTrace();
    }
  }

  @ReactMethod
  public void deleteAlarm(String alarmID) {
    alarmUtil.doCancelAlarm(alarmID);
  }

  @ReactMethod
  public void stopAlarm() {

  }

  @ReactMethod
  public void removeFiredNotification(String alarm_id) {
    alarmUtil.removeFiredNotification(Integer.parseInt(alarm_id));
  }

  @ReactMethod
  public void removeAllFiredNotifications() {
    alarmUtil.removeAllFiredNotifications();
  }

  @ReactMethod
  public void getScheduledAlarms(Promise promise) throws JSONException {
    ArrayList<AlarmModel> alarms = alarmUtil.getAlarms();
    WritableArray array = Arguments.createArray();
    Gson gson = new Gson();
    for (AlarmModel alarm : alarms) {
      WritableMap alarmMap = alarmUtil.convertJsonToMap(new JSONObject(gson.toJson(alarm)));
      array.pushMap(alarmMap);
    }
    promise.resolve(array);
  }

  private static String bundle2string(Bundle bundle) {
    if (bundle == null) {
      return null;
    }
    StringBuilder string = new StringBuilder();
    for (String key : bundle.keySet()) {
      string.append(key).append("==>").append(bundle.get(key)).append(";;");
    }
    return string.toString();
  }
}
