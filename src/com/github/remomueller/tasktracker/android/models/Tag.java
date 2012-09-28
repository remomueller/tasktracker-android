package com.github.remomueller.tasktracker.android;

public class Tag {
    public final static String TAG_ID = "com.github.remomueller.tasktracker.android.models.TAG_ID";
    public final static String TAG_NAME = "com.github.remomueller.tasktracker.android.models.TAG_NAME";
    public final static String TAG_COLOR = "com.github.remomueller.tasktracker.android.models.TAG_COLOR";

    public int id;
    public String name;
    public String description;
    public String color;
    public int user_id;

    public Tag() {
        // For GSON deserialization
    }
}
