package com.github.remomueller.tasktracker.android.util;

import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 2;

    // Database Name
    private static final String DATABASE_NAME = "android_api";

    // Login table name
    private static final String TABLE_LOGIN = "login";

    // Login Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_SITE_URL = "site_url";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_COOKIE = "cookie";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        // String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_LOGIN + "("
        //         + KEY_ID + " INTEGER PRIMARY KEY,"
        //         + KEY_SITE_URL + " TEXT,"
        //         + KEY_EMAIL + " TEXT UNIQUE,"
        //         + KEY_PASSWORD + " TEXT,"
        //         + KEY_COOKIE + " TEXT" + ")";
        // db.execSQL(CREATE_LOGIN_TABLE);
        migration0001(db);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion <= 0) oldVersion = 0;
        for(int i = oldVersion + 1; i <= newVersion; i++){
            if(i == 1) migration0001(db);
            if(i == 2) migration0002(db);
            // Add more migrations here.
        }
    }

    private void migration0001(SQLiteDatabase db){
        String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_LOGIN + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_SITE_URL + " TEXT,"
                + KEY_EMAIL + " TEXT UNIQUE,"
                + KEY_PASSWORD + " TEXT,"
                + KEY_COOKIE + " TEXT" + ")";
        db.execSQL(CREATE_LOGIN_TABLE);
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

    /**
     * Storing user details in database
     * */
    public void addUser(String site_url, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_SITE_URL, site_url); // Site URL
        values.put(KEY_EMAIL, email); // Email
        values.put(KEY_PASSWORD, password); // Password
        values.put(KEY_COOKIE, "cookie"); // Cookie Placeholder

        // Inserting Row
        db.insert(TABLE_LOGIN, null, values);
        db.close(); // Closing database connection
    }

    /**
     * Getting user data from database
     * */
    public HashMap<String, String> getUserDetails(){
        HashMap<String,String> user = new HashMap<String,String>();
        String selectQuery = "SELECT  * FROM " + TABLE_LOGIN;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){
            user.put("site_url", cursor.getString(1));
            user.put("email", cursor.getString(2));
            user.put("password", cursor.getString(3));
            user.put("cookie", cursor.getString(4));
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
        String countQuery = "SELECT  * FROM " + TABLE_LOGIN;
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
    public void resetTables(){
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_LOGIN, null, null);
        db.close();
    }

}
