package com.siddharth.netstats;

import android.content.Intent;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity {
    private Handler handler = new Handler();
    private long rx,tx,temp_rx,temp_tx;
    private ListView listView;private DrawerLayout mDrawerLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        String[] countryArray = {"Data", "Charts"};
        ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.nav_menu,R.id.textview, countryArray);
        listView = (ListView) findViewById(R.id.left_drawer);
        listView.setAdapter(adapter);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        listView.setOnItemClickListener(new DrawerItemClickListener());

        rx=TrafficStats.getTotalRxBytes();rx=rx/(1024);
        tx=TrafficStats.getTotalTxBytes();tx=tx/(1024);
        TextView t1=(TextView)findViewById(R.id.tv1);
        t1.setText("0 KB");
        t1=(TextView)findViewById(R.id.tv2);
        t1.setText("0 KB");
        t1=(TextView)findViewById(R.id.tv3);
        t1.setText("0 KBPS");
        t1=(TextView)findViewById(R.id.tv4);
        t1.setText("0 KBPS");
        temp_tx=tx;temp_rx=rx;
        prog();
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
            mDrawerLayout.closeDrawer(listView);
            listView.setVisibility(View.INVISIBLE);
        }
    }

    private void selectItem(int position)
    {
        if (position ==1)
        {
            Intent t = new Intent(this, cfrag2.class);

            startActivity(t);
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        }
    }


    private void prog()
    {
        handler.postDelayed(runnable, 1000);
    }
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            long rx1=TrafficStats.getTotalRxBytes();rx1=rx1/(1024);
            long tx1=TrafficStats.getTotalTxBytes();tx1=tx1/(1024);
            String temp;
            long down_speed=rx1-temp_rx,up_speed=tx1-temp_tx,down_data=rx1-rx,up_data=tx1-tx;

            TextView t1=(TextView)findViewById(R.id.tv1);
            temp=Long.toString(down_data)+" KB";
            t1.setText(temp);
            t1=(TextView)findViewById(R.id.tv2);
            temp=Long.toString(up_data)+" KB";
            t1.setText(temp);
            t1=(TextView)findViewById(R.id.tv3);
            temp=Long.toString(down_speed)+" KBPS";
            t1.setText(temp);
            t1=(TextView)findViewById(R.id.tv4);
            temp=Long.toString(up_speed)+" KBPS";
            t1.setText(temp);

            temp_tx=tx1;temp_rx=rx1;
            handler.postDelayed(this, 1000);
        }
    };
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}