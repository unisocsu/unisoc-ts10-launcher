package kernel.unisocsu.tablauncher;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FolderView extends LinearLayout {
    public FolderView(Context context, final FolderItem folder, final Runnable refresh) {
        super(context);
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);
        setPadding(8, 8, 8, 8);
        setBackgroundDrawable(new ColorDrawable(Color.argb(65, 255, 255, 255)));

        TextView icon = new TextView(context);
        icon.setText("▦");
        icon.setTextSize(34);
        icon.setGravity(Gravity.CENTER);
        addView(icon, new LayoutParams(-1, 54));

        TextView title = new TextView(context);
        title.setText(folder.title);
        title.setTextSize(14);
        title.setGravity(Gravity.CENTER);
        title.setSingleLine(true);
        addView(title, new LayoutParams(-1, -2));

        setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FolderDialog.show(getContext(), folder, refresh);
            }
        });
    }
}
