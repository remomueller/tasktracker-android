package com.github.remomueller.tasktracker.android;


import android.app.Application;
import android.content.Context;

import android.database.sqlite.SQLiteDatabase;
import com.github.remomueller.tasktracker.android.util.DatabaseHandler;

public class TaskTracker extends Application {

    private DatabaseHandler dbHandler;

    private static TaskTracker mSelf;

    public synchronized static SQLiteDatabase db() {
        if(self().dbHandler == null) {
            self().dbHandler = new DatabaseHandler(context());
        }
        return self().dbHandler.getWritableDatabase();
    }

    public static Context context() {
        return self();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mSelf = this;
    }

    private static TaskTracker self() {
        return mSelf;
    }

    // public synchronized static DatabaseHandler db() {
    //     if(this.dbHandler == null) {
    //         this.dbHandler = new DatabaseHandler();
    //     }
    //     return this.dbHandler.getWritableDatabase();
    // }

}
