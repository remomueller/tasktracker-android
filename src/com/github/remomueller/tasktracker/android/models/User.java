package com.github.remomueller.tasktracker.android;

import android.content.Context;
import java.util.HashMap;
import android.util.Log;

import com.github.remomueller.tasktracker.android.util.DatabaseHandler;

// Debug
import android.util.Log;

public class User {
    private static final String TAG = "TaskTrackerAndroid";

    public int id = 0;
    public String first_name;
    public String last_name;
    public String email;
    public String password;

    public String site_url;
    public String auth_token;

    private DatabaseHandler db;

    // constructor
    public User(Context context){
        db = new DatabaseHandler(context);
        HashMap<String,String> user;
        user = db.getUserDetails();
        if(user.get("id") != null && user.get("id") != "")
            id = Integer.parseInt(user.get("id"));
        first_name = user.get("first_name");
        last_name = user.get("last_name");
        email = user.get("email");
        password = user.get("password");

        site_url = user.get("site_url");
        auth_token = user.get("auth_token");

        if(first_name != null) Log.d(TAG, "First Name: " + first_name);
        if(last_name != null) Log.d(TAG, "Last Name: " + last_name);
        if(email != null) Log.d(TAG, "Email: " + email);
        if(site_url != null) Log.d(TAG, "Site URL: " + site_url);
    }


    /**
     * Function get Login status
     * */
    public boolean isUserLoggedIn(){
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
    public boolean logoutUser(){
        db.resetLogin(email, site_url);
        return true;
    }

}
