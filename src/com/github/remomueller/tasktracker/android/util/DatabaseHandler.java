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
import com.github.remomueller.tasktracker.android.Tag;

import java.util.ArrayList;

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final String TAG                         = "TaskTrackerAndroid";

    // Database Version
    private static final int    DATABASE_VERSION            = 3;

    // Database Name
    private static final String DATABASE_NAME               = "android_api";

    private final static String DROP_LOGIN_TABLE            = "DROP TABLE IF EXISTS login";
    private final static String DROP_PROJECTS_TABLE         = "DROP TABLE IF EXISTS projects";
    private final static String DROP_STICKIES_TABLE         = "DROP TABLE IF EXISTS stickies";
    private final static String DROP_STICKIES_TAGS_TABLE    = "DROP TABLE IF EXISTS stickies_tags";
    private final static String DROP_TAGS_TABLE             = "DROP TABLE IF EXISTS tags";


    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public String getVersion() {
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
        migration0003(db);
        // Add more migrations here
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion <= 0) oldVersion = 0;
        for(int i = oldVersion + 1; i <= newVersion; i++) {
            if(i == 1) migration0001(db);
            if(i == 2) migration0002(db);
            if(i == 3) migration0003(db);
            // Add more migrations here
        }
    }

    @Override
    public void onDowngrade (SQLiteDatabase db, int oldVersion, int newVersion) {
        for(int i = oldVersion; i > newVersion; i--) {
            if(i == 1) rollback0001(db);
            if(i == 2) rollback0002(db);
            if(i == 3) rollback0003(db);
            // Add more rollbacks here
        }
    }

    private void migration0001(SQLiteDatabase db) {
        String CREATE_LOGIN_TABLE = "CREATE TABLE login("
                + "id INTEGER PRIMARY KEY,"
                + "site_url TEXT,"
                + "email TEXT UNIQUE,"
                + "password TEXT,"
                + "cookie TEXT" + ")";
        db.execSQL(CREATE_LOGIN_TABLE);
    }

    private void rollback0001(SQLiteDatabase db) {
        db.execSQL(DROP_LOGIN_TABLE);
    }

    private void migration0002(SQLiteDatabase db) {
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

    private void rollback0002(SQLiteDatabase db) {
        db.execSQL(DROP_STICKIES_TAGS_TABLE);
        db.execSQL(DROP_TAGS_TABLE);
        db.execSQL(DROP_STICKIES_TABLE);
        db.execSQL(DROP_PROJECTS_TABLE);
    }

    private void migration0003(SQLiteDatabase db) {
        db.execSQL(DROP_PROJECTS_TABLE);
        String CREATE_PROJECTS_TABLE = "CREATE TABLE projects("
                + "android_id INTEGER PRIMARY KEY,"
                + "id INTEGER UNIQUE,"
                + "user_id INTEGER,"
                + "name STRING,"
                + "description TEXT,"
                + "status STRING,"
                + "start_date TEXT,"
                + "end_date TEXT,"
                + "color STRING,"
                + "favorited INTEGER" + ")";
        db.execSQL(CREATE_PROJECTS_TABLE);
    }

    private void rollback0003(SQLiteDatabase db) {
        db.execSQL(DROP_PROJECTS_TABLE);
        String CREATE_PROJECTS_TABLE = "CREATE TABLE projects("
                + "android_id INTEGER PRIMARY KEY,"
                + "id INTEGER UNIQUE,"
                + "name STRING,"
                + "description TEXT,"
                + "color STRING" + ")";
        db.execSQL(CREATE_PROJECTS_TABLE);
    }

    public void addOrUpdateSticky(Sticky sticky) {
        // Log.d(TAG, "Inserting Sticky " + sticky.name());
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

        for(int i = 0; i < sticky.tags.length; i++){
            ContentValues tag_values = new ContentValues();

            tag_values.put("id", Integer.toString(sticky.tags[i].id));
            tag_values.put("name", sticky.tags[i].name);
            tag_values.put("description", sticky.tags[i].description);
            tag_values.put("color", sticky.tags[i].color);
            tag_values.put("user_id", Integer.toString(sticky.tags[i].user_id));
            tag_values.put("project_id", Integer.toString(sticky.id));

            db.insertWithOnConflict("tags", null, tag_values, android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE); // Ignore since it already exists... Only updated when projects are refreshed

            ContentValues sticky_tag_values = new ContentValues();
            sticky_tag_values.put("sticky_id", Integer.toString(sticky.id));
            sticky_tag_values.put("tag_id", Integer.toString(sticky.tags[i].id));

            db.insertWithOnConflict("stickies_tags", null, sticky_tag_values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE);
        }

        db.insertWithOnConflict("stickies", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public Sticky findStickyByID(int id) {
        Sticky sticky = new Sticky();
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT * FROM stickies where stickies.id = " + Integer.toString(id) + " LIMIT 1";

        Cursor cursor = db.rawQuery(selectQuery, null);
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

        sticky.tags = findAllTagsArray("tags.id IN (select stickies_tags.tag_id from stickies_tags where stickies_tags.sticky_id = " + Integer.toString(sticky.id) + ")");

        return sticky;
    }

    public void deleteStickyByID(int id) {
        Sticky sticky = new Sticky();
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT * FROM stickies where stickies.id = " + Integer.toString(id) + " LIMIT 1";

        db.delete("stickies", "id = ?", new String[] { Integer.toString(id) });

        db.close();
    }

    public ArrayList<Sticky> findAllStickies(String conditions) {
        ArrayList<Sticky> stickies = new ArrayList<Sticky>();

        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT * FROM stickies";
        if(conditions == null || conditions.equals("")) conditions = "1 = 1";
        selectQuery += " WHERE " + conditions;

        Cursor cursor = db.rawQuery(selectQuery, null);
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
        // Log.d(TAG, "Inserting Project " + project.name);
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("id", Integer.toString(project.id));
        values.put("user_id", Integer.toString(project.user_id));
        values.put("name", project.name);
        values.put("description", project.description);
        values.put("status", project.status);
        values.put("start_date", project.start_date);
        values.put("end_date", project.end_date);
        values.put("color", project.color);
        values.put("favorited", (project.favorited ? "1" : "0"));

        db.insertWithOnConflict("projects", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE);

        for(int i = 0; i < project.tags.length; i++){
            ContentValues tag_values = new ContentValues();

            tag_values.put("id", Integer.toString(project.tags[i].id));
            tag_values.put("name", project.tags[i].name);
            tag_values.put("description", project.tags[i].description);
            tag_values.put("color", project.tags[i].color);
            tag_values.put("user_id", Integer.toString(project.tags[i].user_id));
            tag_values.put("project_id", Integer.toString(project.id));

            db.insertWithOnConflict("tags", null, tag_values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE);
        }


        db.close();
    }

    public Project findProjectByID(int id) {
        Project project = new Project();
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT * FROM projects where projects.id = " + Integer.toString(id) + " LIMIT 1";

        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        if(cursor.getCount() > 0) {
            project.id = Integer.parseInt(cursor.getString(cursor.getColumnIndex("id")));
            project.user_id = Integer.parseInt(cursor.getString(cursor.getColumnIndex("user_id")));
            project.name = cursor.getString(cursor.getColumnIndex("name"));
            project.description = cursor.getString(cursor.getColumnIndex("description"));
            project.status = cursor.getString(cursor.getColumnIndex("status"));
            project.start_date = cursor.getString(cursor.getColumnIndex("start_date"));
            project.end_date = cursor.getString(cursor.getColumnIndex("end_date"));
            project.color = cursor.getString(cursor.getColumnIndex("color"));
            project.favorited = cursor.getString(cursor.getColumnIndex("favorited")).equals("1");
        }
        cursor.close();
        db.close();

        project.tags = findAllTagsArray("project_id = " + Integer.toString(project.id));

        return project;
    }

    public ArrayList<Project> findAllProjects(String conditions) {
        ArrayList<Project> projects = new ArrayList<Project>();

        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT * FROM projects";
        if(conditions == null || conditions.equals("")) conditions = "1 = 1";
        selectQuery += " WHERE " + conditions;
        selectQuery += " ORDER BY favorited DESC, name";
        selectQuery += " COLLATE NOCASE";

        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            Project project = new Project();
            project.id = Integer.parseInt(cursor.getString(cursor.getColumnIndex("id")));
            project.user_id = Integer.parseInt(cursor.getString(cursor.getColumnIndex("user_id")));
            project.name = cursor.getString(cursor.getColumnIndex("name"));
            project.description = cursor.getString(cursor.getColumnIndex("description"));
            project.status = cursor.getString(cursor.getColumnIndex("status"));
            project.start_date = cursor.getString(cursor.getColumnIndex("start_date"));
            project.end_date = cursor.getString(cursor.getColumnIndex("end_date"));
            project.color = cursor.getString(cursor.getColumnIndex("color"));
            project.favorited = cursor.getString(cursor.getColumnIndex("favorited")).equals("1");
            projects.add(project);
            cursor.moveToNext();
        }
        cursor.close();
        db.close();

        // Temporarily removed due to performance. Use individual findProjectByID if you want to get project tags also
        // for(int i = 0; i < projects.size(); i++){
        //     projects.get(i).tags = findAllTagsArray("project_id = " + Integer.toString(projects.get(i).id));
        // }

        return projects;
    }

    public Tag[] findAllTagsArray(String conditions) {
        ArrayList<Tag> tags = findAllTags(conditions);
        Tag[] array = new Tag[tags.size()];
        for(int i = 0; i < tags.size(); i++) {
            array[i] = tags.get(i);
        }
        return array;
    }

    public ArrayList<Tag> findAllTags(String conditions) {
        ArrayList<Tag> tags = new ArrayList<Tag>();

        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT * FROM tags";
        if(conditions == null || conditions.equals("")) conditions = "1 = 1";
        selectQuery += " WHERE " + conditions;
        selectQuery += " ORDER BY name";
        selectQuery += " COLLATE NOCASE";

        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            Tag tag = new Tag();
            tag.id = Integer.parseInt(cursor.getString(cursor.getColumnIndex("id")));
            tag.user_id = Integer.parseInt(cursor.getString(cursor.getColumnIndex("user_id")));
            tag.name = cursor.getString(cursor.getColumnIndex("name"));
            tag.description = cursor.getString(cursor.getColumnIndex("description"));
            tag.color = cursor.getString(cursor.getColumnIndex("color"));
            tags.add(tag);
            cursor.moveToNext();
        }
        cursor.close();
        db.close();

        return tags;
    }

    // Store Current User Information
    public void addLogin(int id, String first_name, String last_name, String email, String password, String site_url, String authentication_token) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("rowid", "1"); // Always adding into the first row
        // values.put("id", Integer.toString(id));
        values.put("cookie", Integer.toString(id)); // Placing
        // values.put("first_name", first_name); // Put in with migration 4
        // values.put("last_name", last_name); // Put in with migration 4
        values.put("email", email);
        values.put("password", password);
        values.put("site_url", site_url);
        // values.put("authentication_token", authentication_token); // Put in with migration 4

        db.insertWithOnConflict("login", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    // Retrieve Current User
    public HashMap<String, String> getLogin() {
        HashMap<String,String> user = new HashMap<String,String>();
        String selectQuery = "SELECT * FROM login";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0) {
            user.put("id", cursor.getString(0));
            user.put("site_url", cursor.getString(1));
            user.put("email", cursor.getString(2));
            user.put("password", cursor.getString(3));
            user.put("cookie", cursor.getString(4)); // user.put("cookie", cursor.getString(4));
            user.put("first_name", ""); // user.put("first_name", cursor.getString(5)); // Put in with migration 4
            user.put("last_name", ""); // user.put("last_name", cursor.getString(6)); // Put in with migration 4
            user.put("authentication_token", ""); // user.put("authentication_token", cursor.getString(7)); // Put in with migration 4
        }
        cursor.close();
        db.close();

        return user;
    }

    // Check if Current User is signed in
    public int getRowCount() {
        String countQuery = "SELECT * FROM login WHERE password IS NOT NULL and password != ''";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int rowCount = cursor.getCount();
        db.close();
        cursor.close();

        return rowCount;
    }

    // Logout the current_user if one exists
    // Set password to NULL.
    public void resetLogin(String email, String site_url) {
        if(email == null) email = "";
        if(site_url == null) site_url = "";

        ArrayList<Object> tables = getTables();

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("rowid", "1"); // Always adding into the first row
        values.put("password", "");
        values.put("email", email);
        values.put("site_url", site_url);

        // Updating Row
        db.insertWithOnConflict("login", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE);

        if(tables.contains("projects")) db.delete("projects", null, null);
        if(tables.contains("stickies")) db.delete("stickies", null, null);
        if(tables.contains("stickies_tags")) db.delete("stickies_tags", null, null);
        if(tables.contains("tags")) db.delete("tags", null, null);

        db.close();
    }

}
