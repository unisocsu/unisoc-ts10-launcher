package kernel.unisocsu.tablauncher;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SettingsActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 32, 32, 32);
        TextView title = new TextView(this);
        title.setText("TS10 Launcher Settings");
        title.setTextSize(26);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));
        TextView info = new TextView(this);
        info.setText("Launcher settings will be expanded here. API 19 compatible.");
        info.setTextSize(18);
        root.addView(info, new LinearLayout.LayoutParams(-1, -2));
        Button done = new Button(this);
        done.setText("Done");
        done.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { finish(); }});
        root.addView(done, new LinearLayout.LayoutParams(-1, -2));
        setContentView(root);
    }
}