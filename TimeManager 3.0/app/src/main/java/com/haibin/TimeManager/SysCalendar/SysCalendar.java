package com.haibin.TimeManager.SysCalendar;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.CalendarContract;
import android.text.TextUtils;

import com.haibin.TimeManager.Todo.Date;
import com.haibin.TimeManager.Todo.Time;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class SysCalendar {
    //系统calendar content provider相关的uri
    private static String CALANDER_URL = "content://com.android.calendar/calendars";
    private static String CALANDER_EVENT_URL = "content://com.android.calendar/events";
    private static String CALANDER_REMIDER_URL = "content://com.android.calendar/reminders";
    //账户的相关信息
    private static String CALENDARS_NAME = "Todo.List";
    private static String CALENDARS_ACCOUNT_NAME = "Todo.List@gmail.com";
    private static String CALENDARS_ACCOUNT_TYPE = "com.android.exchange";
    private static String CALENDARS_DISPLAY_NAME = "Todo.List";


    //检查是否有现有存在的账户。存在则返回账户id，否则返回-1
    @SuppressLint("Range")
    private static int checkCalendarAccount(Context context) {
        Cursor userCursor = context.getContentResolver().query(Uri.parse(CALANDER_URL), null, null, null, null);
        try {
            if (userCursor == null)//查询返回空值
                return -1;
            int count = userCursor.getCount();
            if (count > 0) {//存在现有账户，取Todo.List账户的id返回
                userCursor.moveToFirst();/*CalendarContract.Calendars.ACCOUNT_NAME;*/
                do{
                    if(userCursor.getString(userCursor.getColumnIndex(CalendarContract.Calendars.ACCOUNT_NAME))
                            .equals(CALENDARS_ACCOUNT_NAME)){//找到Todo.List账户
                        return userCursor.getInt(userCursor.getColumnIndex(CalendarContract.Calendars._ID));
                    }
                }while(userCursor.moveToNext());
                return -1;//不存在Todo.List账户
            } else {
                return -1;
            }
        } finally {
            if (userCursor != null) {
                userCursor.close();
            }
        }
    }
    //添加账户。账户创建成功则返回账户id，否则返回-1
    private static long addCalendarAccount(Context context) {
        TimeZone timeZone = TimeZone.getDefault();
        ContentValues value = new ContentValues();
        value.put(CalendarContract.Calendars.NAME, CALENDARS_NAME);

        value.put(CalendarContract.Calendars.ACCOUNT_NAME, CALENDARS_ACCOUNT_NAME);
        value.put(CalendarContract.Calendars.ACCOUNT_TYPE, CALENDARS_ACCOUNT_TYPE);
        value.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, CALENDARS_DISPLAY_NAME);
        value.put(CalendarContract.Calendars.VISIBLE, 1);
        value.put(CalendarContract.Calendars.CALENDAR_COLOR, Color.BLUE);
        value.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER);
        value.put(CalendarContract.Calendars.SYNC_EVENTS, 1);
        value.put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, timeZone.getID());
        value.put(CalendarContract.Calendars.OWNER_ACCOUNT, CALENDARS_ACCOUNT_NAME);
        value.put(CalendarContract.Calendars.CAN_ORGANIZER_RESPOND, 0);

        Uri calendarUri = Uri.parse(CALANDER_URL);
        calendarUri = calendarUri.buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, CALENDARS_ACCOUNT_NAME)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, CALENDARS_ACCOUNT_TYPE)
                .build();

        Uri result = context.getContentResolver().insert(calendarUri, value);
        long id = result == null ? -1 : ContentUris.parseId(result);
        return id;
    }
    //检查是否已经添加了日历账户，如果没有添加先添加一个日历账户再查询
    private static int checkAndAddCalendarAccount(Context context) {
        int oldId = checkCalendarAccount(context);
        if (oldId >= 0) {
            return oldId;
        } else {
            long addId = addCalendarAccount(context);
            if (addId >= 0) {
                return checkCalendarAccount(context);
            } else {
                return -1;
            }
        }
    }
    //添加日历事件、日程
    public static boolean addCalendarEvent(Context context,int id,String title, String description, Date date, Time time) {
        // 获取日历账户的id
        int calId = checkAndAddCalendarAccount(context);
        if (calId < 0) {
            // 获取账户id失败直接返回，添加日历事件失败
            return false;
        }

        ContentValues event = new ContentValues();
        event.put("_id",id);
        event.put("title", title);
        event.put("description", description);
        // 插入账户的id
        event.put("calendar_id", calId);

        Calendar mCalendar = Calendar.getInstance(Locale.CHINA);
        mCalendar.set(Calendar.YEAR,date.year);//设置日期
        mCalendar.set(Calendar.MONTH,date.month-1);
        mCalendar.set(Calendar.DAY_OF_MONTH,date.day);
        mCalendar.set(Calendar.HOUR_OF_DAY, time.hour);
        mCalendar.set(Calendar.MINUTE, time.minute);
        long start = mCalendar.getTime().getTime();//设置开始时间
        mCalendar.set(Calendar.MINUTE,time.minute+5);
        long end = mCalendar.getTime().getTime();//设置终止时间

        event.put(CalendarContract.Events.DTSTART, start);
        event.put(CalendarContract.Events.DTEND, end);
        event.put(CalendarContract.Events.HAS_ALARM, 1);//设置有闹钟提醒
        event.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());  //这个是时区，必须有，
        //添加事件
        Uri newEvent = context.getContentResolver().insert(Uri.parse(CALANDER_EVENT_URL), event);
        if (newEvent == null) {
            // 添加日历事件失败直接返回
            return false;
        }
        //事件提醒的设定
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Reminders.EVENT_ID, ContentUris.parseId(newEvent));
        // 提前5分钟有提醒
        values.put(CalendarContract.Reminders.MINUTES, 5);
        values.put(CalendarContract.Reminders.METHOD, 1);
        Uri uri = context.getContentResolver().insert(Uri.parse(CALANDER_REMIDER_URL), values);
        if (uri == null) {
            // 添加闹钟提醒失败直接返回
            return false;
        }
        return true;
    }
    //删除日历事件、日程
    @SuppressLint("Range")
    public static void deleteCalendarEvent(Context context,int id){
  /*      Cursor eventCursor = context.getContentResolver().query(Uri.parse(CALANDER_EVENT_URL), null, null, null, null);
        try {
            if (eventCursor == null)//查询返回空值
                return;
            if (eventCursor.getCount() > 0) {
                //遍历所有事件，找到title跟需要查询的title一样的项
                for (eventCursor.moveToFirst(); !eventCursor.isAfterLast(); eventCursor.moveToNext()) {
                    String eventTitle = eventCursor.getString(eventCursor.getColumnIndex("title"));
                    if (!TextUtils.isEmpty(title) && title.equals(eventTitle)) {
                        int id = eventCursor.getInt(eventCursor.getColumnIndex(CalendarContract.Calendars._ID));//取得id
                        Uri deleteUri = ContentUris.withAppendedId(Uri.parse(CALANDER_EVENT_URL), id);
                        int rows = context.getContentResolver().delete(deleteUri, null, null);
                        if (rows == -1) {
                            //事件删除失败
                            return;
                        }
                    }
                }
            }
        } finally {
            if (eventCursor != null) {
                eventCursor.close();
            }
        }*/

        Cursor cr = context.getContentResolver().query(Uri.parse(CALANDER_EVENT_URL), null, null, null, null);
        if(cr==null){//查询返回值为空
            return;
        }
        //ContentValues values = new ContentValues();
        Uri deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, id);
        context.getContentResolver().delete(deleteUri, null, null);


    }
    //更新日历事件、日程
    public static void updateCalendarEvent(Context context,int id,String title, String description, Date date, Time time){
        deleteCalendarEvent(context,id);//先删除原来的event
        addCalendarEvent(context,id,title,description,date,time);//然后再添加新的event
    }
}
