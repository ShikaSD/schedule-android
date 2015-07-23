package ru.shika.app.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import ru.shika.app.Lesson;
import ru.shika.app.R;

import java.util.ArrayList;

public class DrawerListAdapter extends BaseAdapter {
    LayoutInflater inflater;
    ArrayList<Lesson.DrawerItem> list;

    public DrawerListAdapter(Context ctx, ArrayList<Lesson.DrawerItem> data) {
        list = data;
        inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View v = view;
        if (v == null) {
            v = inflater.inflate(R.layout.drawer_item, viewGroup, false);
        }

        ((TextView) v.findViewById(R.id.drawerItemText)).setText(list.get(i).string);
        ((ImageView) v.findViewById(R.id.drawerItemIcon)).setImageDrawable(list.get(i).drawable);

        return v;
    }
}
