package com.github.remomueller.tasktracker.android.util;

import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.github.remomueller.tasktracker.android.Project;
import com.github.remomueller.tasktracker.android.Sticky;

import java.util.ArrayList;

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final String TAG = "TaskTrackerAndroid";

    // Database Version
    private static final int DATABASE_VERSION = 2;

    // Database Name
    private static final String DATABASE_NAME = "android_api";


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
        while (cursor.isAfterLast() == false) {
            tableList.add(cursor.getString(0));
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
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
                + "id INTEGER PRIMARY KEY,"
                + "site_url TEXT,"
                + "email TEXT UNIQUE,"
                + "password TEXT,"
                + "cookie TEXT" + ")";
        db.execSQL(CREATE_LOGIN_TABLE);
    }

    private void rollback0001(SQLiteDatabase db){
        String DROP_LOGIN_TABLE = "DROP TABLE IF EXISTS login";
        db.execSQL(DROP_LOGIN_TABLE);
    }

    private void migration0002(SQLiteDatabase db){
        String CREATE_PROJECTS_TABLE = "CREATE TABLE projects("
                + "android_id INTEGER PRIMARY KEY,"
                + "id INTEGER UNIQUE,"
                + "name STRING,"
                + "description TEXT,"
                + "color STRING" + ")";
        String CREATE_STICKIES_TABLE = "CREATE TABLE stickies("
                + "android_id INTEGER PRIMARY KEY,"
                + "id INTEGER UNIQUE,"
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
                + "id INTEGER UNIQUE,"
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

    public void addOrUpdateSticky(Sticky sticky) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("id", Integer.toString(sticky.id));
        values.put("project_id", Integer.toString(sticky.project_id));
        values.put("user_id", Integer.toString(sticky.user_id));
        values.put("owner_id", Integer.toString(sticky.owner_id));
        values.put("group_id", Integer.toString(sticky.group_id));
        values.put("description", sticky.description);
        values.put("group_description", sticky.group_description);
        values.put("due_date", sticky.due_date);
        values.put("completed", (sticky.completed ? "1" : "0"));

        Log.d(TAG, "Inserting Sticky " + sticky.name());

        db.insertWithOnConflict("stickies", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public Sticky findStickyByID(int id){
        Sticky sticky = new Sticky();
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT * FROM stickies where stickies.id = " + Integer.toString(id) + " LIMIT 1";
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0) {
            sticky.id = Integer.parseInt(cursor.getString(cursor.getColumnIndex("id")));
            sticky.project_id = Integer.parseInt(cursor.getString(cursor.getColumnIndex("project_id")));
            sticky.user_id = Integer.parseInt(cursor.getString(cursor.getColumnIndex("user_id")));
            sticky.owner_id = Integer.parseInt(cursor.getString(cursor.getColumnIndex("owner_id")));
            sticky.group_id = Integer.parseInt(cursor.getString(cursor.getColumnIndex("group_id")));
            sticky.description = cursor.getString(cursor.getColumnIndex("description"));
            sticky.group_description = cursor.getString(cursor.getColumnIndex("group_description"));
            sticky.due_date = cursor.getString(cursor.getColumnIndex("due_date"));
            sticky.completed = cursor.getString(cursor.getColumnIndex("completed")).equals("1");
        }
        cursor.close();
        db.close();

        return sticky;
    }

    public void deleteStickyByID(int id){
        Sticky sticky = new Sticky();
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT * FROM stickies where stickies.id = " + Integer.toString(id) + " LIMIT 1";

        db.delete("stickies", "id = ?", new String[] { Integer.toString(id) });

        db.close();
    }

    public ArrayList<Sticky> findAllStickies(String conditions){
        ArrayList<Sticky> stickies = new ArrayList<Sticky>();

        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT * FROM stickies";
        if(conditions == null || conditions.equals("")) conditions = "1 = 1";
        selectQuery += " WHERE " + conditions;
        // selectQuery += " ORDER BY name";
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            Sticky sticky = new Sticky();
            sticky.id = Integer.parseInt(cursor.getString(cursor.getColumnIndex("id")));
            sticky.project_id = Integer.parseInt(cursor.getString(cursor.getColumnIndex("project_id")));
            sticky.user_id = Integer.parseInt(cursor.getString(cursor.getColumnIndex("user_id")));
            sticky.owner_id = Integer.parseInt(cursor.getString(cursor.getColumnIndex("owner_id")));
            sticky.group_id = Integer.parseInt(cursor.getString(cursor.getColumnIndex("group_id")));
            sticky.description = cursor.getString(cursor.getColumnIndex("description"));
            sticky.group_description = cursor.getString(cursor.getColumnIndex("group_description"));
            sticky.due_date = cursor.getString(cursor.getColumnIndex("due_date"));
            sticky.completed = cursor.getString(cursor.getColumnIndex("completed")).equals("1");
            stickies.add(sticky);
            cursor.moveToNext();
        }
        cursor.close();
        db.close();

        return stickies;
    }

    // Projects

    public void addOrUpdateProject(Project project) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("id", Integer.toString(project.id));
        // values.put("user_id", Integer.toString(project.user_id)); // Put in with migration 3
        values.put("name", project.name);
        values.put("description", project.description);
        // values.put("status", project.status);         // Put in with migration 3
        // values.put("start_date", project.start_date); // Put in with migration 3
        // values.put("end_date", project.end_date);     // Put in with migration 3
        values.put("color", project.color);

        Log.d(TAG, "Inserting Project " + project.name);

        db.insertWithOnConflict("projects", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public Project findProjectByID(int id){
        Project project = new Project();
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT * FROM projects where projects.id = " + Integer.toString(id) + " LIMIT 1";
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0) {
            project.id = Integer.parseInt(cursor.getString(cursor.getColumnIndex("id")));
            // project.user_id = Integer.parseInt(cursor.getString(cursor.getColumnIndex("user_id"))); // Put in with migration 3
            project.name = cursor.getString(cursor.getColumnIndex("name"));
            project.description = cursor.getString(cursor.getColumnIndex("description"));
            // project.status = cursor.getString(cursor.getColumnIndex("status"));          // Put in with migration 3
            // project.start_date = cursor.getString(cursor.getColumnIndex("start_date"));  // Put in with migration 3
            // project.end_date = cursor.getString(cursor.getColumnIndex("end_date"));      // Put in with migration 3
            project.color = cursor.getString(cursor.getColumnIndex("color"));
        }
        cursor.close();
        db.close();

        return project;
    }

    public ArrayList<Project> findAllProjects(String conditions){
        ArrayList<Project> projects = new ArrayList<Project>();

        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT * FROM projects";
        if(conditions == null || conditions.equals("")) conditions = "1 = 1";
        selectQuery += " WHERE " + conditions;
        selectQuery += " ORDER BY name";
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            Project project = new Project();
            project.id = Integer.parseInt(cursor.getString(cursor.getColumnIndex("id")));
            // project.user_id = Integer.parseInt(cursor.getString(cursor.getColumnIndex("user_id"))); // Put in with migration 3
            project.name = cursor.getString(cursor.getColumnIndex("name"));
            project.description = cursor.getString(cursor.getColumnIndex("description"));
            // project.status = cursor.getString(cursor.getColumnIndex("status"));          // Put in with migration 3
            // project.start_date = cursor.getString(cursor.getColumnIndex("start_date"));  // Put in with migration 3
            // project.end_date = cursor.getString(cursor.getColumnIndex("end_date"));      // Put in with migration 3
            project.color = cursor.getString(cursor.getColumnIndex("color"));
            projects.add(project);
            cursor.moveToNext();
        }
        cursor.close();
        db.close();

        return projects;
    }

    /**
     * Storing user details in database
     * */
    public void addLogin(int id, String first_name, String last_name, String email, String password, String site_url, String authentication_token) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("rowid", "1"); // Always adding into the first row
        // values.put("id", Integer.toString(id));
        values.put("cookie", Integer.toString(id)); // Placing
        // values.put("first_name", first_name); // Put in with migration 3
        // values.put("last_name", last_name); // Put in with migration 3
        values.put("email", email);
        values.put("password", password);
        values.put("site_url", site_url);
        // values.put("authentication_token", authentication_token); // Put in with migration 3


        db.insertWithOnConflict("login", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE);
        db.close(); // Closing database connection
    }

    /**
     * Getting user data from database
     * */
    public HashMap<String, String> getLogin(){
        HashMap<String,String> user = new HashMap<String,String>();
        String selectQuery = "SELECT * FROM login";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){
            user.put("id", cursor.getString(0));
            user.put("site_url", cursor.getString(1));
            user.put("email", cursor.getString(2));
            user.put("password", cursor.getString(3));
            user.put("cookie", cursor.getString(4)); // user.put("cookie", cursor.getString(4));
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
        String countQuery = "SELECT * FROM login WHERE password IS NOT NULL and password != ''";
        SQLiteDatabase db = this.getWritableDatabase();
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

        // TODO - truncate existing tables
        db.close();
    }

}
