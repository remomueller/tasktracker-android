package com.github.remomueller.tasktracker.android;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import android.widget.TextView;
import com.github.remomueller.tasktracker.android.util.DatabaseHandler;

import android.content.pm.PackageManager.NameNotFoundException;

import java.util.ArrayList;

public class AboutActivity extends SherlockActivity {
    private static final String TAG = "TaskTrackerAndroid";

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
            case R.id.stickies:
                intent = new Intent(getApplicationContext(), StickiesIndex.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        DatabaseHandler db = new DatabaseHandler(getApplicationContext());

        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            TextView app_version_tv = (TextView) findViewById(R.id.app_version);
            app_version_tv.setText(versionName);
        } catch (NameNotFoundException e) {
            Log.e("tag", e.getMessage());
        }

        TextView database_version_tv = (TextView) findViewById(R.id.database_version);
        database_version_tv.setText(db.getVersion());

        TextView database_tables_tv = (TextView) findViewById(R.id.database_tables);

        String tablesString = "";
        ArrayList<Object> tables = db.getTables();
        if(tables != null && tables.size() > 0){
            for(int i = 0; i < tables.size(); i++){
                tablesString = tablesString + tables.get(i) + "\n";
            }
        }

        database_tables_tv.setText(tablesString);

    }

}
