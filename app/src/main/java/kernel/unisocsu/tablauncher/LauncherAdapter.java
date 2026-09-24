package kernel.unisocsu.tablauncher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.List;

public class LauncherAdapter extends BaseAdapter {
    private final Context context;
    private final PackageManager pm;
    private final List<LauncherItem> items;

    public LauncherAdapter(Context context, List<LauncherItem> items) {
        this.context = context;
        this.pm = context.getPackageManager();
        this.items = items;
    }

    public int getCount() { return items.size(); }
    public Object getItem(int position) { return items.get(position); }
    public long getItemId(int position) { return position; }

    public View getView(final int position, View convertView, ViewGroup parent) {
        LinearLayout cell = new LinearLayout(context);
        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setGravity(17);
        cell.setPadding(12, 12, 12, 12);
        cell.setBackgroundDrawable(new ColorDrawable(Color.argb(55, 255, 255, 255)));

        ImageView icon = new ImageView(context);
        try { icon.setImageDrawable(pm.getApplicationIcon(items.get(position).packageName)); } catch (Exception ignored) {}
        cell.addView(icon, new LinearLayout.LayoutParams(-1, 80));

        TextView title = new TextView(context);
        title.setText(items.get(position).title);
        title.setTextSize(16);
        title.setGravity(17);
        title.setSingleLine(true);
        cell.addView(title, new LinearLayout.LayoutParams(-1, -2));

        cell.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent launch = pm.getLaunchIntentForPackage(items.get(position).packageName);
                if (launch != null) context.startActivity(launch);
            }
        });
        return cell;
    }
}
