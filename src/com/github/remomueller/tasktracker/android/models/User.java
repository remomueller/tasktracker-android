package com.github.remomueller.tasktracker.android;

import android.content.Context;
import java.util.HashMap;

import com.github.remomueller.tasktracker.android.util.DatabaseHandler;

// Debug
import android.util.Log;

public class User {

    public int id = 0;
    public String first_name;
    public String last_name;
    public String email;
    public String password;

    public String site_url;
    public String auth_token;

    // constructor
    public User(Context context){
        DatabaseHandler db = new DatabaseHandler(context);
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
    }

    // public String getSiteURL(Context context){
    //     DatabaseHandler db = new DatabaseHandler(context);
    //     HashMap<String,String> user;
    //     user = db.getUserDetails();
    //     return user.get("site_url");
    // }

    // public String getEmail(Context context){
    //     DatabaseHandler db = new DatabaseHandler(context);
    //     HashMap<String,String> user;
    //     user = db.getUserDetails();
    //     return user.get("email");
    // }

    // public String getPassword(Context context){
    //     DatabaseHandler db = new DatabaseHandler(context);
    //     HashMap<String,String> user;
    //     user = db.getUserDetails();
    //     return user.get("password");
    // }


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
