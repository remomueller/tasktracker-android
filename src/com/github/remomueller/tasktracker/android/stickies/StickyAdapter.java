package com.github.remomueller.tasktracker.android;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class StickyAdapter extends BaseAdapter {

    private Activity activity;
    private ArrayList<Sticky> data;
    private static LayoutInflater inflater=null;

    public StickyAdapter(Activity a, ArrayList<Sticky> d) {
        activity = a;
        data=d;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.stickies_index_item, null);

        TextView sticky_id = (TextView)vi.findViewById(R.id.sticky_id);
        TextView description = (TextView)vi.findViewById(R.id.description);
        TextView due_date = (TextView)vi.findViewById(R.id.due_date);

        Sticky sticky = new Sticky();
        sticky = data.get(position);

        sticky_id.setText("" + sticky.id);
        description.setText(sticky.description);
        due_date.setText(sticky.due_date.length() > 10 ? sticky.due_date.substring(0, 10) : "");

        return vi;
    }
}
