package jm.plugin.androidnotification;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;

import android.content.Context;
import android.content.Intent;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import android.util.Log;

import com.unity3d.player.UnityPlayer;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PushNotificationManager extends BroadcastReceiver
{
    private String mChannelId = "channel";
    private String mChannelName = "channelName";
    private static HashMap<String , NotificationData> mNotificationMap = new HashMap<String , NotificationData>();
    private static Context mContext = null;

    static class NotificationData
    {
        public int Id;
        public int Color;
        public String Bundle;
        public Date NotiTime;
        public String Title;
        public String Message;
        public String Ticker;
        public String Small_Icon;
        public String Large_Icon;
        public String SoundName;
        public boolean IsSound;
        public boolean IsVibrate;
        public boolean IsLight;
        public boolean IsRepeat;

        public long RepeatMS;

        public NotificationData()
        {
            Id = -1;
            Bundle = new String();
            NotiTime = Calendar.getInstance().getTime();
            Title = new String();
            Message = new String();
            Ticker = new String();
            Small_Icon = new String();
            Large_Icon = new String();
            SoundName = new String();
            IsSound = true;
            IsVibrate = true;
            IsLight = true;
            IsRepeat = false;
        }
    }

    public static void SetNotificationOnce(int id, String bundle, long delayMS, String title, String message, String ticker,
                                           String icon, int color, boolean isSmall_Icon, boolean isSound, String soundName, boolean isVibrate,
                                           boolean isLight)
    {
        long nowTime = System.currentTimeMillis();
        long notificationTime = nowTime + delayMS;
        Date tempDate = new Date(notificationTime);
        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = date.format(tempDate);

        Intent intent = new Intent(mContext, PushNotificationManager.class);
        AlarmManager alarmManager = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);

        //===================== Send Data To BroadCast ================/
        intent.putExtra("id", id);
        intent.putExtra("bundle", bundle);
        intent.putExtra("title", title);
        intent.putExtra("message", message);
        intent.putExtra("ticker", ticker);
        intent.putExtra("smallIcon", "");
        intent.putExtra("LargeIcon", "");
        if (isSmall_Icon) {
            intent.putExtra("smallIcon", icon);
        } else {
            intent.putExtra("LargeIcon", icon);
        }

        intent.putExtra("color", color);
        intent.putExtra("bisSound", isSound);
        intent.putExtra("soundName", soundName);
        intent.putExtra("bisVibrate", isVibrate);
        intent.putExtra("bisLight", isLight);
        //=============================================================/

        //=================== Repeat Data ================================/
        intent.putExtra("bIsRepeat", false);
        intent.putExtra("RepeatSeconds", 0);
        //=============================================================/

        //===== Set Delay TIme ====/
        // Sec -> MSec
        long delayTime = System.currentTimeMillis() + delayMS;
        //=========================/

        PendingIntent sender = PendingIntent.getBroadcast(mContext, id, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        PutNotificationData(id, bundle, dateString, title, message, ticker, icon, color, isSmall_Icon, isSound, soundName, isVibrate, isLight, true, 0);

        if (mNotificationMap.get(Integer.toString((id))) != null)
        {
            SetStringArrayNotificationData(mContext, mNotificationMap.get(Integer.toString(id)));
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M)
        {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, delayTime, sender);
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, delayTime, sender);
        }
        else
        {
            alarmManager.set(AlarmManager.RTC_WAKEUP, delayTime, sender);
        }
    }

    public static void SetNotificationRepeating(int id, String bundle, long delayMS, long repeatMS, String title, String message, String ticker,
                                                String icon, int color, boolean isSmall_Icon, boolean isSound, String soundName, boolean isVibrate,
                                                boolean isLight)
    {
        long nowTime = System.currentTimeMillis();
        long notificationTime = nowTime + delayMS;
        Date tempDate = new Date(notificationTime);
        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = date.format(tempDate);

        Intent intent = new Intent(mContext, PushNotificationManager.class);
        AlarmManager alarmManager = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);

        //===================== Send Data To BroadCast ================/
        intent.putExtra("id", id);
        intent.putExtra("bundle", bundle);

        intent.putExtra("title", title);
        intent.putExtra("message", message);
        intent.putExtra("ticker", ticker);
        intent.putExtra("smallIcon", "");
        intent.putExtra("LargeIcon", "");

        if (isSmall_Icon) {
            intent.putExtra("smallIcon", icon);
        } else {
            intent.putExtra("LargeIcon", icon);
        }


        intent.putExtra("color", color);
        intent.putExtra("bisSound", isSound);
        intent.putExtra("soundName", soundName);
        intent.putExtra("bisVibrate", isVibrate);
        intent.putExtra("bisLight", isLight);
        //=============================================================/

        //====================== Repeat Data =================/
        intent.putExtra("bIsRepeat", true);
        intent.putExtra("RepeatMillis", repeatMS);
        //=============================================================/

        //===== Set TIme ====/
        // Sec -> MSec
        long delayTime = System.currentTimeMillis() + delayMS;
        //=========================/

        PendingIntent sender = PendingIntent.getBroadcast(mContext, id, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        PutNotificationData(id, bundle, dateString, title, message, ticker, icon, color, isSmall_Icon, isSound, soundName, isVibrate, isLight, true, repeatMS);

        if (mNotificationMap.get(Integer.toString((id))) != null)
        {
            SetStringArrayNotificationData(mContext, mNotificationMap.get(Integer.toString(id)));
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M)
        {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, delayTime, sender);
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, delayTime, sender);
        }
        else
        {
            alarmManager.set(AlarmManager.RTC_WAKEUP, delayTime, sender);
        }
    }

    private static void ReRegistrationNotification(int id, String bundle, long repeatTime, String title, String message, String ticker,
                                            String smallIcon, String largeIcon, int color, boolean isSound, String soundName, boolean isVibrate, boolean isLight)
    {
        CancelPendingNotification(id);

        Intent intent = new Intent(mContext, PushNotificationManager.class);
        AlarmManager alarmManager = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);

        //===================== Send Data To BroadCast ================/
        intent.putExtra("id", id);
        intent.putExtra("bundle", bundle);

        intent.putExtra("title", title);
        intent.putExtra("message", message);
        intent.putExtra("ticker", ticker);
        intent.putExtra("smallIcon", smallIcon);
        intent.putExtra("LargeIcon", largeIcon);

        intent.putExtra("color", color);

        intent.putExtra("bisSound", isSound);
        intent.putExtra("soundName", soundName);
        intent.putExtra("bisVibrate", isVibrate);
        intent.putExtra("bisLight", isLight);
        //=============================================================/

        //====================== Repeat Data =================/
        intent.putExtra("bIsRepeat", true);
        intent.putExtra("RepeatMillis", repeatTime);
        //=============================================================/

        long delayTime = System.currentTimeMillis() + repeatTime;

        PendingIntent sender = PendingIntent.getBroadcast(mContext, id, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M)
        {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, delayTime , sender);
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, delayTime , sender);
        }
        else
        {
            alarmManager.set(AlarmManager.RTC_WAKEUP, delayTime , sender);
        }
    }

    public static void ReCallNotification()
    {
        long nowTime = System.currentTimeMillis();
        Iterator<String> keys =  mNotificationMap.keySet().iterator();
        int index = 0;

        while( keys.hasNext() )
        {
            String key = keys.next();
            NotificationData tempData = mNotificationMap.get(key);

            long notificationTime = tempData.NotiTime.getTime();

            String icon;

            if (tempData.IsRepeat == false)
            {
                if (nowTime > notificationTime)
                {
                    continue;
                }
                else
                {
                    long delayTime = notificationTime - nowTime;

                    if (tempData.Small_Icon.isEmpty()) {
                        icon = tempData.Large_Icon;
                    } else {
                        icon = tempData.Small_Icon;
                    }

                    SetNotificationOnce(index, tempData.Bundle, delayTime, tempData.Title, tempData.Message, tempData.Ticker, icon, tempData.Color, tempData.Large_Icon.isEmpty(),
                            tempData.IsSound, tempData.SoundName, tempData.IsVibrate, tempData.IsLight);
                }
            }
            else
            {
                long delayTime = 0;

                if (nowTime > notificationTime)
                {
                    notificationTime += tempData.RepeatMS;
                    delayTime = notificationTime - nowTime;
                }
                else
                {
                    delayTime = notificationTime - nowTime;
                }


                if (tempData.Small_Icon.isEmpty()) {
                    icon = tempData.Large_Icon;
                } else {
                    icon = tempData.Small_Icon;
                }

                SetNotificationRepeating(index, tempData.Bundle, delayTime, tempData.RepeatMS, tempData.Title, tempData.Message, tempData.Ticker, icon,
                        tempData.Color, tempData.Large_Icon.isEmpty(),  tempData.IsSound, tempData.SoundName, tempData.IsVibrate, tempData.IsLight);
            }

            index++;
        }
    }

    public void onReceive(Context context, Intent intent) {
        mContext = context;

        if (intent.getAction() != null)
        {
            if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
            {
                LoadNotificationData(context);
                ReCallNotification();
            }
        }
        else
        {
            //=========== Get Notification Data =====================/
            int id = intent.getIntExtra("id", 0);
            String bundle = intent.getStringExtra("bundle");
            String title = intent.getStringExtra("title");
            String message = intent.getStringExtra("message");
            String ticker = intent.getStringExtra("ticker");
            String smallIcon = intent.getStringExtra("smallIcon");
            String largeIcon = intent.getStringExtra("LargeIcon");

            int color = intent.getIntExtra("color", 0);

            Boolean bIsSound = intent.getBooleanExtra("bisSound", false);
            String soundName = intent.getStringExtra("soundName");
            Boolean bIsVibrate = intent.getBooleanExtra("bisVibrate", false);
            Boolean bIsLight = intent.getBooleanExtra("bisLight", false);

            Boolean bIsRepeat = intent.getBooleanExtra("bIsRepeat", false);
            long repeatSec = intent.getLongExtra("RepeatMillis", 0);

            //==================================================/

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            // Android Oreo
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel notificationChannel = new NotificationChannel(mChannelId, mChannelName, importance);
                notificationManager.createNotificationChannel(notificationChannel);
            }

            Resources resources = context.getResources();

            Intent notificationIntent = context.getPackageManager().getLaunchIntentForPackage(bundle);

            TaskStackBuilder stackBuilder = androidx.core.app.TaskStackBuilder.create(context);
            stackBuilder.addNextIntent(notificationIntent);

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                    notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context.getApplicationContext(), mChannelId);

            builder.setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setContentTitle(title)
                    .setContentText(message);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder.setColor(color);
            }

            if (ticker.length() > 0) {
                builder.setTicker(ticker);
            }

            if (!smallIcon.isEmpty()) {
                builder.setSmallIcon(resources.getIdentifier(smallIcon, "drawable", context.getPackageName()));
            } else if (!largeIcon.isEmpty()) {
                builder.setLargeIcon(BitmapFactory.decodeResource(resources, resources.getIdentifier(largeIcon, "drawable",
                        context.getPackageName())));
            }

            if (bIsSound == true) {
                if (soundName != null && soundName.length() > 0) {
                    int identifier = resources.getIdentifier("raw/" + soundName, null, context.getPackageName());
                    builder.setSound(Uri.parse("android.resource://" + bundle + "/" + identifier));
                } else {
                    builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                }
            }

            if (bIsVibrate == true) {
                builder.setVibrate(new long[]{
                        1000, 1000
                });
            }

            if (bIsLight == true) {
                builder.setLights(Color.BLUE, 3000, 3000);
            }

            Notification notification = builder.build();
            notificationManager.notify(id, notification);

            if (bIsRepeat == true)
            {
                ReRegistrationNotification(id, bundle, repeatSec, title, message, ticker, smallIcon, largeIcon, color, bIsSound, soundName, bIsVibrate, bIsLight);
            }
        }
    }

    public static void CancelPendingNotificationAll()
    {
        Iterator iterator = mNotificationMap.entrySet().iterator();

        while (iterator.hasNext())
        {
            HashMap.Entry<String, NotificationData> tempMap = (HashMap.Entry<String, NotificationData>) iterator.next();
            CancelPendingNotification(Integer.parseInt(tempMap.getKey()));
        }
    }

    public static void CancelPendingNotification(int id)
    {
        AlarmManager alarmManager = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(mContext, PushNotificationManager.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, id, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.cancel(pendingIntent);
    }

    public static  void ClearShowingNotification()
    {
        Activity currentActivity = UnityPlayer.currentActivity;
        NotificationManager notificationManager = (NotificationManager)currentActivity.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    private static void PutNotificationData(int id, String bundle, String time, String title, String message, String ticker, String icon, int color,
                                            boolean isSmall_Icon, boolean isSound, String soundName, boolean isVibrate, boolean isLight, boolean isRepeat, long repeatMS)
    {
        NotificationData tempData = new NotificationData();
        tempData.Id = id;
        tempData.Color = color;

        Date to = null;
        String from =  time;

        try {
            SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            to = transFormat.parse(from);
        } catch (ParseException e)
        {
            e.printStackTrace();
        }

        tempData.NotiTime = to;

        tempData.Bundle = bundle;
        tempData.Title = title;
        tempData.Message = message;
        tempData.Ticker = ticker;

        if (isSmall_Icon) {
            tempData.Small_Icon = icon;
        } else {
            tempData.Large_Icon = icon;
        }

        tempData.SoundName = soundName;
        tempData.IsSound = isSound;
        tempData.IsVibrate = isVibrate;
        tempData.IsLight = isLight;
        tempData.IsRepeat = isRepeat;
        tempData.RepeatMS = repeatMS;

        mNotificationMap.put(Integer.toString(id), tempData);
    }

    private static void SetStringArrayNotificationData(Context context, NotificationData values)
    {
        SharedPreferences prefs = context.getSharedPreferences("AndroidNotificationData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("ID", values.Id);
            jsonObject.put("Color", values.Color);
            jsonObject.put("Bundle", values.Bundle);

            Date from = values.NotiTime;
            SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String to = transFormat.format(from);

            jsonObject.put("Time", to);
            jsonObject.put("Title", values.Title);
            jsonObject.put("Message", values.Message);
            jsonObject.put("Ticker", values.Ticker);
            jsonObject.put("Small_Icon", values.Small_Icon);
            jsonObject.put("Large_Icon", values.Large_Icon);
            jsonObject.put("SoundName", values.SoundName);
            jsonObject.put("IsSound", values.IsSound);
            jsonObject.put("IsVibrate", values.IsVibrate);
            jsonObject.put("IsLight", values.IsLight);
            jsonObject.put("IsRepeat", values.IsRepeat);
            jsonObject.put("RepeatMillis", values.RepeatMS);

        } catch (JSONException e)
        {
                e.printStackTrace();
        }

        if (values != null)
        {
            editor.putString(Integer.toString(values.Id), jsonObject.toString());
        }

        editor.apply();
    }

    private static NotificationData GetStringArrayNotificationData(Context context, String key)
    {
        SharedPreferences prefs = context.getSharedPreferences("AndroidNotificationData", Context.MODE_PRIVATE);
        String json = prefs.getString(key, null);

        NotificationData notiData = new NotificationData();

        if (json != null)
        {
            try
            {
                JSONObject tempJson = new JSONObject(json);
                notiData.Id = tempJson.getInt("ID");
                notiData.Color = tempJson.getInt("Color");
                notiData.Bundle = tempJson.getString("Bundle");

                Date to = null;

                try {
                    String from = tempJson.getString("Time");
                    SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    to = transFormat.parse(from);
                } catch (ParseException e)
                {
                    e.printStackTrace();
                }

                notiData.NotiTime = to;

                notiData.Title = tempJson.getString("Title");
                notiData.Message = tempJson.getString("Message");
                notiData.Small_Icon = tempJson.getString("Small_Icon");
                notiData.Large_Icon = tempJson.getString("Large_Icon");
                notiData.SoundName = tempJson.getString("SoundName");
                notiData.IsSound = tempJson.getBoolean("IsSound");
                notiData.IsVibrate = tempJson.getBoolean("IsVibrate");
                notiData.IsLight = tempJson.getBoolean("IsLight");
                notiData.IsRepeat = tempJson.getBoolean("IsRepeat");
                notiData.RepeatMS = tempJson.getLong("RepeatMillis");
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            return null;
        }

        return notiData;
    }

    private static void LoadNotificationData(Context context)
    {
        SharedPreferences prefs = context.getSharedPreferences("AndroidNotificationData", Context.MODE_PRIVATE);
        Map<String, ?> keys = prefs.getAll();

        mNotificationMap.clear();

        for (Map.Entry<String, ?> entry : keys.entrySet())
        {
            mNotificationMap.put(entry.getKey(), GetStringArrayNotificationData(context, entry.getKey()));
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.commit();
    }

    public static void LoadNotificationStatic()
    {
        Activity currentActivity = UnityPlayer.currentActivity;
        Intent intent = new Intent(currentActivity,PushNotificationManager.class);
        mContext = UnityPlayer.currentActivity;
        LoadNotificationData(mContext);
    }
}
