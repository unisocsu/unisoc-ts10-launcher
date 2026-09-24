package kernel.unisocsu.tablauncher;

import java.util.ArrayList;
import java.util.List;

public class FolderItem {
    public final String id;
    public String title;
    public final List<LauncherItem> items = new ArrayList<LauncherItem>();

    public FolderItem(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public void add(LauncherItem item) {
        if (item != null && !items.contains(item)) items.add(item);
    }
}
