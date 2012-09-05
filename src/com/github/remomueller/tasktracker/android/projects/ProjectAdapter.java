package com.github.remomueller.tasktracker.android;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
// import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import android.util.Log;


public class ProjectAdapter extends BaseAdapter {

    private static final String TAG = "TaskTrackerAndroid";

    private Activity activity;
    private ArrayList<Project> data;
    private static LayoutInflater inflater=null;

    public ProjectAdapter(Activity a, ArrayList<Project> d) {
        activity = a;
        data=d;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {
        return data.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null){
            vi = inflater.inflate(R.layout.projects_index_item, null);
        }

        TextView project_name = (TextView) vi.findViewById(R.id.project_name);

        Project project = new Project();
        project = data.get(position);

        project_name.setText(project.name);
        project_name.setTextColor(Color.parseColor(project.color));

        return vi;
    }
}
