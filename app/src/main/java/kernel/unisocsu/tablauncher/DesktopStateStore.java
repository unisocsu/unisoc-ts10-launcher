package kernel.unisocsu.tablauncher;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class DesktopStateStore {
    private static final String PREFS = "desktop_state";
    private final SharedPreferences prefs;

    public DesktopStateStore(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void saveFolders(List<FolderItem> folders) {
        try {
            JSONArray all = new JSONArray();
            for (FolderItem folder : folders) {
                JSONObject f = new JSONObject();
                f.put("id", folder.id);
                f.put("title", folder.title);
                JSONArray items = new JSONArray();
                for (LauncherItem item : folder.items) {
                    JSONObject a = new JSONObject();
                    a.put("package", item.packageName);
                    a.put("title", item.title);
                    items.put(a);
                }
                f.put("items", items);
                all.put(f);
            }
            prefs.edit().putString("folders", all.toString()).apply();
        } catch (Exception ignored) {}
    }

    public List<FolderItem> loadFolders() {
        List<FolderItem> result = new ArrayList<FolderItem>();
        try {
            JSONArray all = new JSONArray(prefs.getString("folders", "[]"));
            for (int i = 0; i < all.length(); i++) {
                JSONObject f = all.getJSONObject(i);
                FolderItem folder = new FolderItem(f.getString("id"), f.optString("title", "Folder"));
                JSONArray items = f.optJSONArray("items");
                if (items != null) {
                    for (int j = 0; j < items.length(); j++) {
                        JSONObject a = items.getJSONObject(j);
                        folder.add(new LauncherItem(a.getString("package"), a.optString("title", a.getString("package")), j));
                    }
                }
                result.add(folder);
            }
        } catch (Exception ignored) {}
        return result;
    }

    public void saveWidgetLayout(int slot, int widthCells, int heightCells) {
        prefs.edit().putInt("widget_slot", slot)
                .putInt("widget_w", widthCells)
                .putInt("widget_h", heightCells).apply();
    }

    public int getWidgetSlot() { return prefs.getInt("widget_slot", 5); }
    public int getWidgetWidth() { return prefs.getInt("widget_w", 5); }
    public int getWidgetHeight() { return prefs.getInt("widget_h", 2); }
}
