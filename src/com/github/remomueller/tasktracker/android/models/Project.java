package com.github.remomueller.tasktracker.android;

import android.util.Log;

public class Project {
    private static final String TAG = "TaskTrackerAndroid";

    public final static String PROJECT_ID = "com.github.remomueller.tasktracker.android.models.PROJECT_ID";
    public final static String PROJECT_NAME = "com.github.remomueller.tasktracker.android.models.PROJECT_NAME";
    public final static String PROJECT_COLOR = "com.github.remomueller.tasktracker.android.models.PROJECT_COLOR";

    public int id;
    public int user_id;
    public String name;
    public String description;
    public String status;
    public String start_date;
    public String end_date;

    public String color;
    public boolean favorited;

    public Tag[] tags;

    public Project() {
        // For GSON deserialization
        id = 0;
        user_id = 0;
        status = "ongoing";

        name = "";
        color = "#2C2C2C";
        favorited = false;

        tags = new Tag[0];
    }

    public String tag_names(){
        String result = "";
        for(int i = 0; i < this.tags.length; i++) {
            if(i == 0)
                result += this.tags[i].name;
            else
                result += ", " + this.tags[i].name;
        }
        return result;
    }
}
