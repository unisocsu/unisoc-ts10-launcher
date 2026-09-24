package kernel.unisocsu.tablauncher;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class FolderDialog {
    private FolderDialog() {}

    public static void show(final Context context, final FolderItem folder, final Runnable refresh) {
        final Dialog dialog = new Dialog(context);
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 20, 24, 20);

        TextView title = new TextView(context);
        title.setText(folder.title);
        title.setTextSize(24);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        final PackageManager pm = context.getPackageManager();
        for (final LauncherItem item : folder.items) {
            TextView row = new TextView(context);
            row.setText(item.title);
            row.setTextSize(19);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(12, 16, 12, 16);
            row.setBackgroundDrawable(new ColorDrawable(0x22000000));
            row.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent launch = pm.getLaunchIntentForPackage(item.packageName);
                    if (launch != null) context.startActivity(launch);
                    dialog.dismiss();
                }
            });
            root.addView(row, new LinearLayout.LayoutParams(-1, -2));
        }
        dialog.setContentView(root);
        dialog.show();
    }
}
