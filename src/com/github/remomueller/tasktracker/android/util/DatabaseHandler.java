package com.github.remomueller.tasktracker.android.util;

import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 2;

    // Database Name
    private static final String DATABASE_NAME = "android_api";

    // Login Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_SITE_URL = "site_url";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_COOKIE = "cookie";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public String getVersion(){
        return Integer.toString(DATABASE_VERSION);
    }

    public ArrayList<Object> getTables()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<Object> tableList = new ArrayList<Object>();
        String SQL_GET_ALL_TABLES = "SELECT name FROM sqlite_master WHERE type='table' ORDER BY name";
        Cursor cursor = db.rawQuery(SQL_GET_ALL_TABLES, null);
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            do {
                tableList.add(cursor.getString(0));
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        return tableList;
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        migration0001(db);
        migration0002(db);
        // Add more migrations here
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion <= 0) oldVersion = 0;
        for(int i = oldVersion + 1; i <= newVersion; i++){
            if(i == 1) migration0001(db);
            if(i == 2) migration0002(db);
            // Add more migrations here
        }
    }

    @Override
    public void onDowngrade (SQLiteDatabase db, int oldVersion, int newVersion) {
        for(int i = oldVersion; i > newVersion; i--){
            if(i == 1) rollback0001(db);
            if(i == 2) rollback0002(db);
            // Add more rollbacks here
        }
    }

    private void migration0001(SQLiteDatabase db){
        String CREATE_LOGIN_TABLE = "CREATE TABLE login("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_SITE_URL + " TEXT,"
                + KEY_EMAIL + " TEXT UNIQUE,"
                + "password TEXT,"
                + KEY_COOKIE + " TEXT" + ")";
        db.execSQL(CREATE_LOGIN_TABLE);
    }

    private void rollback0001(SQLiteDatabase db){
        String DROP_LOGIN_TABLE = "DROP TABLE IF EXISTS login";
        db.execSQL(DROP_LOGIN_TABLE);
    }

    private void migration0002(SQLiteDatabase db){
        String CREATE_PROJECTS_TABLE = "CREATE TABLE projects("
                + "android_id INTEGER PRIMARY KEY,"
                + "id INTEGER,"
                + "name STRING,"
                + "description TEXT,"
                + "color STRING" + ")";
        String CREATE_STICKIES_TABLE = "CREATE TABLE stickies("
                + "android_id INTEGER PRIMARY KEY,"
                + "id INTEGER,"
                + "project_id INTEGER,"
                + "user_id INTEGER,"
                + "owner_id INTEGER,"
                + "description TEXT,"
                + "group_description TEXT,"
                + "group_id INTEGER,"
                + "due_date TEXT,"
                + "completed INTEGER" + ")";
        String CREATE_TAGS_TABLE = "CREATE TABLE tags("
                + "android_id INTEGER PRIMARY KEY,"
                + "id INTEGER,"
                + "name STRING,"
                + "description TEXT,"
                + "color STRING,"
                + "project_id INTEGER,"
                + "user_id INTEGER" + ")";
        String CREATE_STICKIES_TAGS_TABLE = "CREATE TABLE stickies_tags("
                + "sticky_id INTEGER,"
                + "tag_id INTEGER" + ")";
        db.execSQL(CREATE_PROJECTS_TABLE);
        db.execSQL(CREATE_STICKIES_TABLE);
        db.execSQL(CREATE_TAGS_TABLE);
        db.execSQL(CREATE_STICKIES_TAGS_TABLE);
    }

    private void rollback0002(SQLiteDatabase db){
        String DROP_STICKIES_TAGS_TABLE = "DROP TABLE IF EXISTS stickies_tags";
        String DROP_TAGS_TABLE = "DROP TABLE IF EXISTS tags";
        String DROP_STICKIES_TABLE = "DROP TABLE IF EXISTS stickies";
        String DROP_PROJECTS_TABLE = "DROP TABLE IF EXISTS projects";

        db.execSQL(DROP_STICKIES_TAGS_TABLE);
        db.execSQL(DROP_TAGS_TABLE);
        db.execSQL(DROP_STICKIES_TABLE);
        db.execSQL(DROP_PROJECTS_TABLE);
    }

    /**
     * Storing user details in database
     * */
    public void addUser(int id, String first_name, String last_name, String email, String password, String site_url, String authentication_token) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("rowid", "1"); // Always adding into the first row
        values.put(KEY_COOKIE, Integer.toString(id)); // Cookie Placeholder
        // values.put("first_name", first_name); // Put in with migration 3
        // values.put("last_name", last_name); // Put in with migration 3
        values.put(KEY_EMAIL, email);
        values.put("password", password); // Password
        values.put(KEY_SITE_URL, site_url); // Site URL
        // values.put("authentication_token", authentication_token); // Put in with migration 3


        db.insertWithOnConflict("login", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE);
        db.close(); // Closing database connection
    }

    /**
     * Getting user data from database
     * */
    public HashMap<String, String> getUserDetails(){
        HashMap<String,String> user = new HashMap<String,String>();
        String selectQuery = "SELECT * FROM login";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){
            user.put("site_url", cursor.getString(1));
            user.put("email", cursor.getString(2));
            user.put("password", cursor.getString(3));
            user.put("cookie", cursor.getString(4));
            user.put("first_name", ""); // user.put("first_name", cursor.getString(5)); // Put in with migration 3
            user.put("last_name", ""); // user.put("last_name", cursor.getString(6)); // Put in with migration 3
            user.put("authentication_token", ""); // user.put("authentication_token", cursor.getString(7)); // Put in with migration 3
        }
        cursor.close();
        db.close();
        // return user
        return user;
    }

    /**
     * Getting user login status
     * return true if rows are there in table
     * */
    public int getRowCount() {
        String countQuery = "SELECT * FROM login where password IS NOT NULL and password != ''";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int rowCount = cursor.getCount();
        db.close();
        cursor.close();

        // return row count
        return rowCount;
    }

    /**
     * Re crate database
     * Delete all tables and create them again
     * */
    // public void resetTables(){
    //     SQLiteDatabase db = this.getWritableDatabase();
    //     // Delete All Rows
    //     db.delete("login", null, null);
    //     db.close();
    // }

    // Logout the current_user if one exists
    // Set password to NULL.
    public void resetLogin(String email, String site_url){
        if(email == null) email = "";
        if(site_url == null) site_url = "";

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("rowid", "1"); // Always adding into the first row
        values.put("password", "");
        values.put("email", email);
        values.put("site_url", site_url);

        // Updating Row
        db.insertWithOnConflict("login", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

}
