package com.github.remomueller.tasktracker.android;

import android.util.Log;

public class Sticky {
    private static final String TAG = "TaskTrackerAndroid";

    public final static String STICKY_POSITION = "com.github.remomueller.tasktracker.android.models.STICKY_POSITION";
    public final static String STICKY_ID = "com.github.remomueller.tasktracker.android.models.STICKY_ID";
    public final static String STICKY_DESCRIPTION = "com.github.remomueller.tasktracker.android.models.STICKY_DESCRIPTION";
    public final static String STICKY_GROUP_DESCRIPTION = "com.github.remomueller.tasktracker.android.models.STICKY_GROUP_DESCRIPTION";
    public final static String STICKY_DUE_DATE = "com.github.remomueller.tasktracker.android.models.STICKY_DUE_DATE";
    public final static String STICKY_COMPLETED = "com.github.remomueller.tasktracker.android.models.STICKY_COMPLETED";


    public int id;
    public boolean completed;
    public String description;
    public String due_date;
    public String group_description;
    public int group_id;
    public int owner_id;
    public int project_id;
    public int user_id;

    public Tag[] tags;

    public String name(){
        return "#" + Integer.toString(id);
    }

    public String full_description(){
        String result = "";

        if(group_description != null){
            result = description + "\n\n" + group_description;
        }else{
            result = description;
        }

        return result;
    }

    public String short_due_date(){
        String result = "";

        result = (due_date != null && due_date.length() > 10 ? due_date.substring(0, 10) : "");

        return result;
    }

    public int dueDateDay(){
        return dueDateSegment(8,10);
    }

    public int dueDateMonth(){
        return dueDateSegment(5,7);
    }

    public int dueDateYear(){
        return dueDateSegment(0,4);
    }

    private int dueDateSegment(int start, int stop){
        int segment = 0;
        String result = (short_due_date().length() == 10 ? due_date.substring(start, stop) : "");
        try {
            segment = Integer.parseInt(result);
        } catch(NumberFormatException e) {
            Log.d(TAG, "Caught NumberFormatException: " + e.getMessage());
        }
        return segment;
    }

}
