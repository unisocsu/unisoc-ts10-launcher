package kernel.unisocsu.tablauncher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DesktopView extends FrameLayout {
    private final DesktopLayoutManager layout;
    private final PackageManager pm;
    private final List<LauncherItem> apps = new ArrayList<LauncherItem>();
    private final List<FolderItem> folders = new ArrayList<FolderItem>();
    private final int columns = 5;
    private int cellW;
    private int cellH = 112;

    public DesktopView(Context context) {
        super(context);
        layout = new DesktopLayoutManager(context);
        pm = context.getPackageManager();
        setBackgroundColor(Color.TRANSPARENT);

        setOnDragListener(new OnDragListener() {
            public boolean onDrag(View v, DragEvent event) {
                if (event.getAction() == DragEvent.ACTION_DROP) {
                    Object source = event.getLocalState();
                    if (source instanceof LauncherItem) {
                        moveApp((LauncherItem) source, slotAt(event.getX(), event.getY()));
                        rebuild();
                        return true;
                    }
                }
                return true;
            }
        });
    }

    public void setApps(List<LauncherItem> source) {
        apps.clear();
        if (source != null) apps.addAll(source);
        rebuild();
    }

    public void setFolders(List<FolderItem> source) {
        folders.clear();
        if (source != null) folders.addAll(source);
        rebuild();
    }

    private int slotAt(float x, float y) {
        int col = cellW <= 0 ? 0 : (int) (x / cellW);
        int row = (int) (y / cellH);
        if (col < 0) col = 0;
        if (col >= columns) col = columns - 1;
        if (row < 0) row = 0;
        return row * columns + col;
    }

    private void moveApp(LauncherItem item, int target) {
        int old = item.position;
        if (old == target) return;
        for (LauncherItem other : apps) {
            if (other != item && other.position == target) {
                other.position = old;
                layout.savePosition(other.packageName, old);
            }
        }
        item.position = target;
        layout.savePosition(item.packageName, target);
    }

    private void createFolder(LauncherItem a, LauncherItem b) {
        FolderItem folder = new FolderItem("folder_" + a.packageName + "_" + b.packageName, "Folder");
        folder.add(a);
        folder.add(b);
        folders.add(folder);
        apps.remove(a);
        apps.remove(b);
        layout.remove(a.packageName);
        layout.remove(b.packageName);
        rebuild();
    }

    private View appCell(final LauncherItem item) {
        LinearLayout cell = new LinearLayout(getContext());
        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setGravity(Gravity.CENTER);
        cell.setPadding(8, 6, 8, 6);
        cell.setBackgroundDrawable(new ColorDrawable(Color.argb(55, 255, 255, 255)));

        ImageView icon = new ImageView(getContext());
        try {
            icon.setImageDrawable(pm.getApplicationIcon(item.packageName));
        } catch (Exception ignored) {}
        cell.addView(icon, new LinearLayout.LayoutParams(-1, 66));

        TextView name = new TextView(getContext());
        name.setText(item.title);
        name.setTextSize(13);
        name.setGravity(Gravity.CENTER);
        name.setSingleLine(true);
        cell.addView(name, new LinearLayout.LayoutParams(-1, -2));

        cell.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent launch = pm.getLaunchIntentForPackage(item.packageName);
                if (launch != null) getContext().startActivity(launch);
            }
        });

        cell.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                v.startDrag(null, new DragShadowBuilder(v), item, 0);
                return true;
            }
        });

        cell.setOnDragListener(new OnDragListener() {
            public boolean onDrag(View v, DragEvent event) {
                if (event.getAction() == DragEvent.ACTION_DROP) {
                    Object source = event.getLocalState();
                    if (source instanceof LauncherItem && source != item) {
                        createFolder((LauncherItem) source, item);
                        return true;
                    }
                }
                return true;
            }
        });
        return cell;
    }

    public void addWidgetView(View widget) {
        LayoutParams lp = new LayoutParams(-1, cellH * 2);
        lp.leftMargin = 0;
        lp.topMargin = 0;
        addView(widget, lp);
        widget.setOnLongClickListener(new OnLongClickListener() {
            public boolean onLongClick(View v) {
                v.startDrag(null, new DragShadowBuilder(v), v, 0);
                return true;
            }
        });
    }

    public void rebuild() {
        removeAllViews();
        post(new Runnable() {
            public void run() {
                cellW = getWidth() > 0 ? getWidth() / columns : 160;
                Collections.sort(apps, new Comparator<LauncherItem>() {
                    public int compare(LauncherItem a, LauncherItem b) {
                        return a.position - b.position;
                    }
                });
                for (LauncherItem item : apps) {
                    View v = appCell(item);
                    int row = item.position / columns;
                    int col = item.position % columns;
                    LayoutParams lp = new LayoutParams(cellW - 8, cellH - 8);
                    lp.leftMargin = col * cellW + 4;
                    lp.topMargin = row * cellH + 4;
                    addView(v, lp);
                }
                int folderSlot = apps.size();
                for (FolderItem folder : folders) {
                    View v = new FolderView(getContext(), folder, new Runnable() {
                        public void run() { rebuild(); }
                    });
                    int row = folderSlot / columns;
                    int col = folderSlot % columns;
                    LayoutParams lp = new LayoutParams(cellW - 8, cellH - 8);
                    lp.leftMargin = col * cellW + 4;
                    lp.topMargin = row * cellH + 4;
                    addView(v, lp);
                    folderSlot++;
                }
            }
        });
    }
}
