package com.siddharth.netstats;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends ActionBarActivity
{
    boolean cfrag1_is_enabled = false,first_time=true;
    String[] menu;
    String temp1="0 KB", temp2="0 KB", temp3="0 KBPS", temp4="0 KBPS",date;
    public Handler handler = new Handler();
    public long rx, tx, temp_rx, temp_tx;
    DrawerLayout dLayout;
    ListView dList;
    ArrayAdapter<String> adapter;
    cfrag1 frag;cfrag2 frag2;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cfrag1 newFragment = new cfrag1();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, newFragment, "cfrag1");
        //ft.addToBackStack("cfrag1");
        ft.commit();
        getSupportFragmentManager().executePendingTransactions();
        cfrag1_is_enabled = true;
        frag = (cfrag1) getSupportFragmentManager().findFragmentByTag("cfrag1");

        menu = new String[]{"Home", "Charts"};
        dLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        dList = (ListView) findViewById(R.id.left_drawer);
        adapter = new ArrayAdapter<String>(this, R.layout.nav_menu, R.id.textview, menu);
        dList.setAdapter(adapter);
        dList.setSelector(android.R.color.holo_blue_dark);

        dList.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position, long id)
            {
                dLayout.closeDrawers();
                if (position == 0)
                {
                    cfrag1 newFragment = new cfrag1();
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.content_frame, newFragment, "cfrag1");
                    //ft.addToBackStack("cfrag1");
                    ft.commit();
                    getSupportFragmentManager().executePendingTransactions();   //fucking important

                    frag = (cfrag1) getSupportFragmentManager().findFragmentByTag("cfrag1");
                    if (frag != null)
                    {
                        cfrag1_is_enabled = true;
                        frag.go(temp1, temp2, temp3, temp4);
                    }
                }
                else
                {
                    cfrag1_is_enabled = false;
                    cfrag2 newFragment = new cfrag2();
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.content_frame, newFragment, "cfrag2");
                    //ft.addToBackStack("cfrag1");
                    ft.commit();
                    getSupportFragmentManager().executePendingTransactions();   //fucking important

                    frag2 = (cfrag2) getSupportFragmentManager().findFragmentByTag("cfrag2");
                    if (frag != null)
                    {
                        setdata();
                    }
                }
            }
        });
        prog();
    }

    public void setdata()
    {

        //charting
        ArrayList<String> xVals = new ArrayList<String>();
        int count=10;
        int range=45;
        for (int i = 1; i <= count; i++)
        {
            xVals.add(i % 5 + "");
        }
        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();

        for (int i = 1; i <= count; i++)
        {
            yVals1.add(new BarEntry(i, i));
        }
        BarDataSet set1 = new BarDataSet(yVals1, "DataSet");
        set1.setBarSpacePercent(35f);
        ArrayList<BarDataSet> dataSets = new ArrayList<BarDataSet>();
        dataSets.add(set1);
        frag2.go(xVals,dataSets);
    }

    private void prog()
    {
        db = openOrCreateDatabase("database", Context.MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS transfer_day('date' VARCHAR NOT NULL UNIQUE,'down_transfer' integer);");
        Time now = new Time();
        now.setToNow();
        date= new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        Cursor c=db.rawQuery("select * from transfer_day where date=\"" + date + "\";",null);
        if(c.getCount()==0)
            db.execSQL("insert into transfer_day values(\""+date+"\",0);");
        handler.postDelayed(runnable, 1000);
    }

    private Runnable runnable = new Runnable()
    {
        @Override
        public void run()
        {

            if(first_time==true)
            {
                rx = TrafficStats.getTotalRxBytes();
                rx = rx / (1024);
                tx = TrafficStats.getTotalTxBytes();
                tx = tx / (1024);
                temp_tx = tx;
                temp_rx = rx;
                frag.go(temp1, temp2, temp3, temp4);
                first_time=false;
            }
            try
            {

                if (cfrag1_is_enabled == true && frag != null)
                    frag.go(temp1, temp2, temp3, temp4);
                long rx1 = TrafficStats.getTotalRxBytes();
                rx1 = rx1 / (1024);
                long tx1 = TrafficStats.getTotalTxBytes();
                tx1 = tx1 / (1024);
                long down_speed = rx1 - temp_rx, up_speed = tx1 - temp_tx, down_data = rx1 - rx, up_data = tx1 - tx;

                temp1 = Long.toString(down_data) + " KB";
                temp2 = Long.toString(up_data) + " KB";
                temp3 = Long.toString(down_speed) + " KBPS";
                temp4 = Long.toString(up_speed) + " KBPS";

                temp_tx = tx1;
                temp_rx = rx1;
                //Log.v("sfs","update transfer_day set down_transfer=down_transfer+"+down_speed+" where date = '"+date+"';");
                db.execSQL("update transfer_day set down_transfer=down_transfer+"+down_speed+" where date = '"+date+"';");

                handler.postDelayed(this, 1000);
            }
            catch (NullPointerException n)
            {
                Log.v("piss","off");
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }
}