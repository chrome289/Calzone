package com.siddharth.netstats;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class notification extends BroadcastReceiver
{

    @Override
    public void onReceive(Context context, Intent intent)
    {
        SharedPreferences prefs = context.getSharedPreferences("setting",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=prefs.edit();
        editor.putBoolean("noti_visible",false);
        editor.commit();
        Log.v("knkn", "646469464    "+prefs.getBoolean("noti_visible",true));
    }
}