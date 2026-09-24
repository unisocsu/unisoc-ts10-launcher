package kernel.unisocsu.tablauncher;

public class LauncherItem {
    public String packageName;
    public String title;
    public int position;

    public LauncherItem(String packageName, String title, int position) {
        this.packageName = packageName;
        this.title = title;
        this.position = position;
    }
}
