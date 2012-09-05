package com.github.remomueller.tasktracker.android;

public class Project {
    public int id;
    public String name;
    public String description;
    public String color;

    public Project() {
        // For GSON deserialization
        color = "#2C2C2C";
    }
}
