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
    private DesktopView desktop;
    private DesktopLayoutManager desktopLayout;
    private AppWidgetHost widgetHost;
    private AppWidgetManager widgetManager;

    private final Runnable clockTick = new Runnable() {
        public void run() {
            if (clock != null) {
                clock.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()));
            }
            handler.postDelayed(this, 30000);
        }
    };

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);
        clock = (TextView) findViewById(R.id.clock);
        desktop = (DesktopView) findViewById(R.id.desktop);
        desktopLayout = new DesktopLayoutManager(this);
        widgetManager = AppWidgetManager.getInstance(this);
        widgetHost = new AppWidgetHost(this, APPWIDGET_HOST_ID);
        loadApps();
        desktop.setFolders(new DesktopStateStore(this).loadFolders());
        restoreWidget();
        clockTick.run();
    }

    private void loadApps() {
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> installed = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        List<LauncherItem> items = new ArrayList<LauncherItem>();
        int fallback = 0;

        for (ApplicationInfo info : installed) {
            if ((info.flags & ApplicationInfo.FLAG_SYSTEM) != 0 &&
                    pm.getLaunchIntentForPackage(info.packageName) == null) {
                continue;
            }
            String title = pm.getApplicationLabel(info).toString();
            int position = desktopLayout.getPosition(info.packageName, fallback);
            items.add(new LauncherItem(info.packageName, title, position));
            fallback++;
        }

        desktopLayout.sort(items);
        desktop.setApps(items);
    }

    private void addWidget() {
        int id = widgetHost.allocateAppWidgetId();
        Intent pick = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
        pick.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
        startActivityForResult(pick, REQUEST_PICK_WIDGET);
    }

    private void restoreWidget() {
        int id = getPreferences(MODE_PRIVATE).getInt("widget_id", 0);
        if (id == 0) return;

        AppWidgetProviderInfo info = widgetManager.getAppWidgetInfo(id);
        if (info == null) return;

        AppWidgetHostView view = widgetHost.createView(this, id, info);
        view.setAppWidget(id, info);
        desktop.addWidgetView(view);
    }

    private void showWidget(int id) {
        AppWidgetProviderInfo info = widgetManager.getAppWidgetInfo(id);
        if (info == null) return;

        AppWidgetHostView view = widgetHost.createView(this, id, info);
        view.setAppWidget(id, info);
        desktop.addWidgetView(view);
        getPreferences(MODE_PRIVATE).edit().putInt("widget_id", id).apply();
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null || resultCode != RESULT_OK) return;

        if (requestCode == REQUEST_PICK_WIDGET) {
            int id = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
            AppWidgetProviderInfo info = widgetManager.getAppWidgetInfo(id);
            if (info != null && info.configure != null) {
                Intent config = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
                config.setComponent(info.configure);
                config.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
                startActivityForResult(config, REQUEST_CONFIGURE_WIDGET);
            } else {
                showWidget(id);
            }
        } else if (requestCode == REQUEST_CONFIGURE_WIDGET) {
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

    @Override protected void onStart() {
        super.onStart();
        widgetHost.startListening();
    }

    @Override protected void onStop() {
        widgetHost.stopListening();
        super.onStop();
    }

    @Override protected void onDestroy() {
        handler.removeCallbacks(clockTick);
        super.onDestroy();
    }
}
