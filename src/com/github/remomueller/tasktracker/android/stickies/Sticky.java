package com.github.remomueller.tasktracker.android;

public class Sticky {
  public int id;
  public String description;
  public String due_date;

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
