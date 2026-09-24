package kernel.unisocsu.tablauncher;

import java.util.ArrayList;
import java.util.List;

public class FolderItem extends LauncherItem {
    public final List<LauncherItem> children = new ArrayList<LauncherItem>();

    public FolderItem(String title, int position) {
        super("folder:" + title, title, position);
    }

    public void add(LauncherItem item) { children.add(item); }
}
