package kernel.unisocsu.tablauncher;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DesktopLayoutManager {
    private static final String PREFS = "desktop_layout";
    private final SharedPreferences prefs;

    public DesktopLayoutManager(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public List<LauncherItem> sort(List<LauncherItem> items) {
        Collections.sort(items, new Comparator<LauncherItem>() {
            public int compare(LauncherItem a, LauncherItem b) {
                return a.position - b.position;
            }
        });
        return items;
    }

    public void savePosition(String packageName, int position) {
        prefs.edit().putInt(packageName, position).apply();
    }

    public int getPosition(String packageName, int fallback) {
        return prefs.getInt(packageName, fallback);
    }

    public void remove(String packageName) {
        prefs.edit().remove(packageName).apply();
    }
}
