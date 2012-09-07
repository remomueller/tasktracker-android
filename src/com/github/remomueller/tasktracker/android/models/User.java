package com.github.remomueller.tasktracker.android;

import android.content.Context;
import java.util.HashMap;

import com.github.remomueller.tasktracker.android.util.DatabaseHandler;

// Debug
import android.util.Log;

public class User {

    // constructor
    public User(){

    }

    public String getSiteURL(Context context){
        DatabaseHandler db = new DatabaseHandler(context);
        HashMap<String,String> user;
        user = db.getUserDetails();
        return user.get("site_url");
    }

    public String getEmail(Context context){
        DatabaseHandler db = new DatabaseHandler(context);
        HashMap<String,String> user;
        user = db.getUserDetails();
        return user.get("email");
    }

    public String getPassword(Context context){
        DatabaseHandler db = new DatabaseHandler(context);
        HashMap<String,String> user;
        user = db.getUserDetails();
        return user.get("password");
    }


    /**
     * Function get Login status
     * */
    public boolean isUserLoggedIn(Context context){
        DatabaseHandler db = new DatabaseHandler(context);
        int count = db.getRowCount();
        if(count > 0){
            // user logged in
            return true;
        }
        return false;
    }

    /**
     * Function to logout user
     * Reset Database
     * */
    public boolean logoutUser(Context context){
        DatabaseHandler db = new DatabaseHandler(context);
        db.resetTables();
        return true;
    }

}
