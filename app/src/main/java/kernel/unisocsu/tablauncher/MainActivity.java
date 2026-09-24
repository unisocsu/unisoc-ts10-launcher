package kernel.unisocsu.tablauncher;

import android.app.Activity;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int APPWIDGET_HOST_ID = 1024;
    private static final int REQUEST_PICK_WIDGET = 2001;
    private static final int REQUEST_CONFIGURE_WIDGET = 2002;
    private static final int MENU_SETTINGS = 3001;
    private static final int MENU_ADD_WIDGET = 3002;

    private final Handler handler = new Handler();
    private TextView clock;
    private DesktopLayoutManager desktopLayout;
    private AppWidgetHost widgetHost;
    private AppWidgetManager widgetManager;
    private android.widget.FrameLayout navigationContainer;
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
        navigationContainer = (android.widget.FrameLayout) findViewById(R.id.navigation_container);
        widgetManager = AppWidgetManager.getInstance(this);
        widgetHost = new AppWidgetHost(this, APPWIDGET_HOST_ID);
        desktopLayout = new DesktopLayoutManager(this);
        loadApplications();
        restoreWidget();
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
            public int compare(ApplicationInfo a, ApplicationInfo b) {
                return pm.getApplicationLabel(a).toString().compareToIgnoreCase(pm.getApplicationLabel(b).toString());
            }
        });

        final List<LauncherItem> items = new ArrayList<LauncherItem>();
        for (int i = 0; i < apps.size(); i++) {
            ApplicationInfo a = apps.get(i);
            items.add(new LauncherItem(a.packageName, pm.getApplicationLabel(a).toString(),
                    desktopLayout.getPosition(a.packageName, i)));
        }
        desktopLayout.sort(items);
        grid.setAdapter(new LauncherAdapter(this, items));
    }

    private void addWidget() {
        int appWidgetId = widgetHost.allocateAppWidgetId();
        Intent pick = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
        pick.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        startActivityForResult(pick, REQUEST_PICK_WIDGET);
    }

    private void restoreWidget() {
        int widgetId = getPreferences(MODE_PRIVATE).getInt("widget_id", 0);
        if (widgetId == 0) return;
        AppWidgetProviderInfo info = widgetManager.getAppWidgetInfo(widgetId);
        if (info == null) return;
        AppWidgetHostView view = widgetHost.createView(this, widgetId, info);
        view.setAppWidget(widgetId, info);
        navigationContainer.removeAllViews();
        navigationContainer.addView(view, new android.widget.FrameLayout.LayoutParams(-1, -1));
    }

    private void showWidget(int widgetId) {
        AppWidgetProviderInfo info = widgetManager.getAppWidgetInfo(widgetId);
        if (info == null) return;
        AppWidgetHostView view = widgetHost.createView(this, widgetId, info);
        view.setAppWidget(widgetId, info);
        navigationContainer.removeAllViews();
        navigationContainer.addView(view, new android.widget.FrameLayout.LayoutParams(-1, -1));
        getPreferences(MODE_PRIVATE).edit().putInt("widget_id", widgetId).apply();
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) return;
        if (requestCode == REQUEST_PICK_WIDGET && resultCode == RESULT_OK) {
            int widgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
            AppWidgetProviderInfo info = widgetManager.getAppWidgetInfo(widgetId);
            if (info != null && info.configure != null) {
                Intent config = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
                config.setComponent(info.configure);
                config.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
                startActivityForResult(config, REQUEST_CONFIGURE_WIDGET);
            } else {
                showWidget(widgetId);
            }
        } else if (requestCode == REQUEST_CONFIGURE_WIDGET && resultCode == RESULT_OK) {
            showWidget(data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0));
        }
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_ADD_WIDGET, 0, "Add Widget");
        menu.add(0, MENU_SETTINGS, 1, "Settings");
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == MENU_ADD_WIDGET) {
            addWidget();
            return true;
        }
        if (item.getItemId() == MENU_SETTINGS) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override protected void onStart() { super.onStart(); widgetHost.startListening(); }
    @Override protected void onStop() { widgetHost.stopListening(); super.onStop(); }
    @Override protected void onDestroy() { handler.removeCallbacks(clockTick); super.onDestroy(); }
}
