package com.github.remomueller.tasktracker.android;

public class Sticky {

    public final static String STICKY_POSITION = "com.github.remomueller.tasktracker.android.models.STICKY_POSITION";
    public final static String STICKY_ID = "com.github.remomueller.tasktracker.android.models.STICKY_ID";
    public final static String STICKY_DESCRIPTION = "com.github.remomueller.tasktracker.android.models.STICKY_DESCRIPTION";
    public final static String STICKY_GROUP_DESCRIPTION = "com.github.remomueller.tasktracker.android.models.STICKY_GROUP_DESCRIPTION";
    public final static String STICKY_DUE_DATE = "com.github.remomueller.tasktracker.android.models.STICKY_DUE_DATE";
    public final static String STICKY_COMPLETED = "com.github.remomueller.tasktracker.android.models.STICKY_COMPLETED";


    public int id;
    public String description;
    public String due_date;
    public int project_id;

    public String group_description;

    public boolean completed;

    public Tag[] tags;

    public String short_due_date(){
    String result = "";

    result = (due_date.length() > 10 ? due_date.substring(0, 10) : "");

    return result;
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

}
