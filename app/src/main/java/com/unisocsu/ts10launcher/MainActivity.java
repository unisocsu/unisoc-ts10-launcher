package com.unisocsu.ts10launcher;

import android.app.Activity;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int APPWIDGET_HOST_ID = 1024;
    private final Handler handler = new Handler();
    private TextView clock;
    private AppWidgetHost widgetHost;
    private final Runnable clockTick = new Runnable() {
        public void run() {
            if (clock != null) clock.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()));
            handler.postDelayed(this, 30000);
        }
    };

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);
        clock = (TextView) findViewById(R.id.clock);
        widgetHost = new AppWidgetHost(this, APPWIDGET_HOST_ID);
        loadApplications();
        clockTick.run();
    }

    private void loadApplications() {
        GridView grid = (GridView) findViewById(R.id.app_grid);
        final PackageManager pm = getPackageManager();
        final List<ApplicationInfo> apps = new ArrayList<ApplicationInfo>();
        for (ApplicationInfo info : pm.getInstalledApplications(PackageManager.GET_META_DATA)) {
            if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0 || pm.getLaunchIntentForPackage(info.packageName) != null) apps.add(info);
        }
        java.util.Collections.sort(apps, new java.util.Comparator<ApplicationInfo>() {
            public int compare(ApplicationInfo a, ApplicationInfo b) { return pm.getApplicationLabel(a).toString().compareToIgnoreCase(pm.getApplicationLabel(b).toString()); }
        });
        List<String> names = new ArrayList<String>();
        for (ApplicationInfo a : apps) names.add(pm.getApplicationLabel(a).toString());
        grid.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, names));
        grid.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            public void onItemClick(android.widget.AdapterView<?> p, View v, int position, long id) {
                Intent launch = pm.getLaunchIntentForPackage(apps.get(position).packageName);
                if (launch != null) startActivity(launch);
            }
        });
    }

    @Override protected void onStart() { super.onStart(); widgetHost.startListening(); }
    @Override protected void onStop() { widgetHost.stopListening(); super.onStop(); }
    @Override protected void onDestroy() { handler.removeCallbacks(clockTick); super.onDestroy(); }
}
