package com.github.remomueller.tasktracker.android;

public class Project {

    public final static String PROJECT_ID = "com.github.remomueller.tasktracker.android.models.PROJECT_ID";
    public final static String PROJECT_NAME = "com.github.remomueller.tasktracker.android.models.PROJECT_NAME";
    public final static String PROJECT_COLOR = "com.github.remomueller.tasktracker.android.models.PROJECT_COLOR";

    public int id;
    public String name;
    public String description;
    public String color;

    public Project() {
        // For GSON deserialization
        id = 0;
        color = "#2C2C2C";
    }
}
