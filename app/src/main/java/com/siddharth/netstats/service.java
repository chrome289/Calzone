package com.siddharth.netstats;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.IBinder;
import android.text.format.Time;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class service extends Service
{
    String date;
    static int counter = 0;
    SQLiteDatabase db;
    Handler h = new Handler();
    static long temp_rx, temp_tx;
    SharedPreferences prefs;
    Notification notification, n2;
    NotificationManager notificationManger, nm2;
    Notification.Builder builder, builder1;
    SharedPreferences.Editor editor;

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    public void onDestroy()
    {
        try
        {
            //cutting all links
            db.close();
            notificationManger.cancel(1);
            h.removeCallbacksAndMessages(null);
        }
        catch (NullPointerException e)
        {
        }

    }

    @Override
    public void onStart(Intent intent, int startid)
    {
        prefs = getApplicationContext().getSharedPreferences("setting", Context.MODE_PRIVATE);
        editor = prefs.edit();

        boolean boot, app, dnd;
        if (!prefs.contains("noti_visible_serv"))
            editor.putBoolean("noti_visible_serv", false);
        if (!prefs.contains("start_at_boot"))
            editor.putBoolean("start_at_boot", false);
        if (!prefs.contains("dnd"))
            editor.putBoolean("dnd", false);
        if (!prefs.contains("is_app_open"))
            editor.putBoolean("is_app_open", true);
        if (!prefs.contains("cur_month"))
            editor.putInt("cur_month", 1);
        if (!prefs.contains("noti_visible"))
            editor.putBoolean("noti_visible", false);
        if (!prefs.contains("noti_visible2"))
            editor.putBoolean("noti_visible2", false);
        if (!prefs.contains("not_pers"))
            editor.putBoolean("not_pers", false);
        if (!prefs.contains("flimit"))
            editor.putLong("flimit", 0);
        if (!prefs.contains("limit"))
            editor.putString("limit", "");


        editor.putBoolean("noti_visible2", false);
        editor.commit();

        dnd = prefs.getBoolean("dnd", false);
        boot = prefs.getBoolean("start_at_boot", false);
        app = prefs.getBoolean("is_app_open", true);


        //only start app if start on boot is enabled
        if (boot && !app && !dnd)
        {
            Intent intents = new Intent(getBaseContext(), MainActivity.class);
            intents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intents);
        }

        //checking if app has quit and monitoring is required
        if (dnd)
        {
            Time now = new Time();
            now.setToNow();
            date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

            //building the notification
            Intent notificationIntent = new Intent(this, MainActivity.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder = new Notification.Builder(getApplicationContext());
            builder.setContentTitle("NetStats (Service)");
            builder.setContentText("Down : 0 KBPS         " + "Up : 0 KBPS");
            builder.setSmallIcon(R.drawable.no);
            builder.setAutoCancel(true);
            builder.setPriority(0);
            Intent intent2 = new Intent(this, notification.class);
            PendingIntent pendintIntent = PendingIntent.getBroadcast(this, 0, intent2, 0);
            builder.setDeleteIntent(pendintIntent);
            if (prefs.getBoolean("not_pers", false))
                builder.setOngoing(true);
            builder.setContentIntent(pendingIntent);

            notification = builder.build();

            notificationManger = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManger.notify(1, notification);
            editor.putBoolean("noti_visible_serv", true);

            if (prefs.getBoolean("mobile_en", false) == false)
            {
                temp_rx = TrafficStats.getTotalRxBytes();
                temp_tx = TrafficStats.getTotalTxBytes();
            }
            else
            {
                temp_rx = TrafficStats.getMobileRxBytes();
                temp_tx = TrafficStats.getMobileTxBytes();
            }
            temp_rx = temp_rx / (1024);
            temp_tx = temp_tx / (1024);
            db = openOrCreateDatabase("database", Context.MODE_PRIVATE, null);
            db.execSQL("CREATE TABLE IF NOT EXISTS transfer_week('date' VARCHAR NOT NULL UNIQUE,'down_transfer' integer,'up_transfer' integer,'down_transfer_mob' integer,'up_transfer_mob' integer);");
            db.execSQL("create table if not exists transfer_hour('hour' integer not null unique,'down' integer,'up' integer,'down_mob' integer,'up_mob' integer);");
            db.execSQL("create table if not exists this_month('package' varchar not null ,'date' varchar not null,'app' varchar,'down' integer,'up' integer,'down_mob' integer,'up_mob' integer);");
            db.execSQL("create table if not exists app('package' varchar not null ,'down' integer,'up' integer,'down_mob' integer,'up_mob' integer);");

            System.gc();
            h.postDelayed(runnable, 1000);
        }
    }

    private Runnable runnable = new Runnable()
    {
        @Override
        public void run()
        {
            boolean mob = prefs.getBoolean("mobile_en", false);
            db = openOrCreateDatabase("database", Context.MODE_PRIVATE, null);
            Log.v("run", "ning");

            //getting current stats
            long rx, tx;
            if (mob == false)
            {
                rx = TrafficStats.getTotalRxBytes();
                tx = TrafficStats.getTotalTxBytes();
            }
            else
            {
                rx = TrafficStats.getMobileRxBytes();
                tx = TrafficStats.getMobileTxBytes();
            }
            rx = rx / (1024);
            tx = tx / (1024);
            long speed_rx = rx - temp_rx;
            long speed_tx = tx - temp_tx;
            temp_tx = tx;
            temp_rx = rx;

            //updating daily values
            if (prefs.getBoolean("mobile_en", false) == false)
            {
                editor.putLong("d_today", prefs.getLong("d_today", 0) + speed_rx);
                editor.putLong("u_today", prefs.getLong("u_today", 0) + speed_tx);
            }
            else
            {
                editor.putLong("d_today_mob", prefs.getLong("d_today", 0) + speed_rx);
                editor.putLong("u_today_mob", prefs.getLong("u_today", 0) + speed_tx);
            }
            editor.commit();

            //checking for gc
            if (counter == 30)
            {
                System.gc();
                counter = 0;
            }
            else
                counter++;

            //checking if date has changed
            Time now = new Time();
            now.setToNow();
            String temp = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            int temp3 = now.month + 1;
            Cursor c = db.rawQuery("select * from transfer_hour where hour=\"" + String.valueOf(now.hour) + "\";", null);
            if (c.getCount() == 0)
                db.execSQL("insert into transfer_hour values(\"" + String.valueOf(now.hour) + "\",0,0,0,0);");
            if (mob == false)
                db.execSQL("update transfer_hour set down=down+" + speed_rx + " , up=up+" + speed_tx + " where hour = '" + String.valueOf(now.hour) + "';");
            else
                db.execSQL("update transfer_hour set down_mob=down_mob+" + speed_rx + " , up_mob=up_mob+" + speed_tx + " where hour = '" + String.valueOf(now.hour) + "';");

            if (temp.compareTo(date) != 0)
            {
                prefs = getApplicationContext().getSharedPreferences("setting", Context.MODE_PRIVATE);
                editor = prefs.edit();

                Log.v("change", "date");

                if (mob == false)
                    db.execSQL("update transfer_week set down_transfer=" + prefs.getLong("d_today", 0) + " , up_transfer=" + prefs.getLong("u_today", 0) + " where date = '" + date + "';");
                else
                    db.execSQL("update transfer_week set down_transfer_mob=" + prefs.getLong("d_today", 0) + " , up_transfer_mob=" + prefs.getLong("u_today", 0) + " where date = '" + date + "';");

                if (mob == false)
                {
                    editor.putLong("d_today", 0);
                    editor.putLong("u_today", 0);
                }
                else
                {
                    editor.putLong("d_today_mob", 0);
                    editor.putLong("u_today_mob", 0);
                }
                editor.commit();
                date = temp;

                System.gc();
                counter = 0;
            }

            //limit
            editor.putLong("flimit", prefs.getLong("flimit", 0) + speed_rx + speed_tx);
            editor.commit();

            if (prefs.getLong("flimit", 0) >= (Long.parseLong(prefs.getString("limit", "0")) * 1024) && prefs.getBoolean("noti_visible2", false) == false)
            {
                builder1 = new Notification.Builder(getApplicationContext());
                builder1.setContentTitle("Warning");
                builder1.setContentText("You have reached the Monthly limit");
                builder1.setSmallIcon(R.drawable.no);
                builder1.setAutoCancel(true);
                builder1.setPriority(0);
                builder1.setOngoing(false);
                n2 = builder1.build();
                nm2 = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                nm2.notify(2, n2);
                editor.putBoolean("noti_visible2", true);
            }

            //reseting data used on month change
            if (temp3 != prefs.getInt("cur_month", 0))
            {
                editor.putLong("flimit", 0);
                editor.putInt("cur_month", temp3);
                editor.putBoolean("noti_visible2", false);
                editor.commit();
                nm2.cancel(2);
            }
            //updating the notification

            if (!prefs.getBoolean("not_pers", false))
                builder.setOngoing(false);
            else
            {
                builder.setOngoing(true);
                editor.putBoolean("noti_visible_serv", true);
                editor.commit();
            }

            if (prefs.getBoolean("noti_visible_serv", false))
            {
                builder.setContentText("Down : " + speed_rx + " KBPS         " + "Up : " + speed_tx + " KBPS");
                notificationManger.notify(1, builder.build());
            }

            db.close();
            h.postDelayed(this, 1000);
        }
    };
}