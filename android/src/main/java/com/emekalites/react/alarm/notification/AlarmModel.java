package com.emekalites.react.alarm.notification;

import java.io.Serializable;

public class AlarmModel implements Serializable {
  private int id;

  private long fireTime;

  private int alarmId;
  private String title = "My Notification Title";
  private String message = "My Notification Message";
  private String channel = "my_channel_id";
  private String smallIcon = "ic_launcher";
  private int interval = 1; // in minutes
  private String tag;
  private String data;

  private int active = 1; // 1 = yes, 0 = no

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getAlarmId() {
    return alarmId;
  }

  public void setAlarmId(int alarmId) {
    this.alarmId = alarmId;
  }

  public long getAlarmFireTime() {
    return fireTime;
  }

  public void setAlarmFireTime(long time) {
    this.fireTime = time;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getChannel() {
    return channel;
  }

  public void setChannel(String channel) {
    this.channel = channel;
  }


  public String getSmallIcon() {
    return smallIcon;
  }

  public void setSmallIcon(String smallIcon) {
    this.smallIcon = smallIcon;
  }

  public int getInterval() {
    return interval;
  }

  public void setInterval(int interval) {
    this.interval = interval;
  }

  public String getTag() {
    return tag;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }

  public int getActive() {
    return active;
  }

  public void setActive(int active) {
    this.active = active;
  }

  @Override
  public String toString() {
    return "AlarmModel{" + "id=" + id + ", fireTime=" + fireTime + ", alarmId=" + alarmId + ", title='" + title + '\''
      + ", message='" + message + '\'' + ", channel='" + channel + '\'' + '\''
      + ", smallIcon='" + smallIcon + '\'' + ", tag='" + tag + '\'' + ", data='" + data + '\'' + '}';
  }
}
