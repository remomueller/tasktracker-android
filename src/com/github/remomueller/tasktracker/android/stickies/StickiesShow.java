package com.github.remomueller.tasktracker.android;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.os.Bundle;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.widget.TextView;
import android.view.View;
import android.util.Log;

import android.widget.Toast;
import android.view.Gravity;

import android.view.KeyEvent;

import android.content.DialogInterface;
import android.app.AlertDialog;


import com.github.remomueller.tasktracker.android.util.DatabaseHandler;

import com.github.remomueller.tasktracker.android.util.AsyncRequest;
import com.github.remomueller.tasktracker.android.util.AsyncRequest.AsyncRequestFinishedListener;

public class StickiesShow extends SherlockFragmentActivity {
    private static final String TAG = "TaskTrackerAndroid";

    ActionBar actionBar;

    Project current_project;
    Sticky sticky;

    DatabaseHandler db;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(getApplicationContext(), StickiesIndex.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            if (current_project.id > 0) {
                intent.putExtra(Project.PROJECT_ID, Integer.toString(current_project.id));
                intent.putExtra(Project.PROJECT_NAME, current_project.name);
            }
            startActivity(intent);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.sticky_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
            case R.id.stickies:
                intent = new Intent(getApplicationContext(), StickiesIndex.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                if (current_project.id > 0) {
                    intent.putExtra(Project.PROJECT_ID, Integer.toString(current_project.id));
                    intent.putExtra(Project.PROJECT_NAME, current_project.name);
                }
                startActivity(intent);
                finish();
                return true;
            case R.id.edit:
                intent = new Intent(getApplicationContext(), StickiesNew.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(Sticky.STICKY_ID, Integer.toString(sticky.id));

                startActivity(intent);
                // finish();
                return true;
            case R.id.delete:
                new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Delete Sticky")
                    .setMessage("Are you sure you want to delete Sticky " + sticky.name() + "?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            AsyncRequestFinishedListener finishedListener = new AsyncRequestFinishedListener()
                            {
                                @Override
                                public void onTaskFinished(String json) {
                                    Toast toast = Toast.makeText(getApplicationContext(), "Sticky was successfully deleted.", Toast.LENGTH_LONG);
                                    toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
                                    toast.show();

                                    Intent intent = new Intent(getApplicationContext(), StickiesIndex.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                                    startActivity(intent);
                                    finish();
                                }
                            };

                            db.deleteStickyByID(sticky.id);
                            new AsyncRequest(getApplicationContext(), "DELETE", "/stickies/" + Integer.toString(sticky.id) + ".json", null, finishedListener).execute();
                        }

                    })
                    .setNegativeButton("No", null)
                    .show();

                return true;
    //         case R.id.about:
    //             intent = new Intent(getApplicationContext(), AboutActivity.class);
    //             intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    //             startActivity(intent);
    //             // finish();
    //             return true;
    //         case R.id.logout:
    //             current_user.logoutUser();
    //             intent = new Intent(getApplicationContext(), LoginActivity.class);
    //             intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    //             startActivity(intent);
    //             finish();
    //             return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        actionBar = getSupportActionBar();

        setContentView(R.layout.stickies_show);

        // Get the message from the intent
        Intent intent = getIntent();

        current_project = new Project();

        db = new DatabaseHandler(getApplicationContext());
        sticky = db.findStickyByID(Integer.parseInt( intent.getStringExtra(Sticky.STICKY_ID) ));

        // int position = Integer.parseInt( intent.getStringExtra(Sticky.STICKY_POSITION) );

        // Hopefully won't be needed in future and can access from database
        if(sticky == null || sticky.id == 0){
            sticky = new Sticky();
            sticky.id = Integer.parseInt( intent.getStringExtra(Sticky.STICKY_ID) );
            sticky.description = intent.getStringExtra(Sticky.STICKY_DESCRIPTION);
            sticky.group_description = intent.getStringExtra(Sticky.STICKY_GROUP_DESCRIPTION);
            sticky.due_date = intent.getStringExtra(Sticky.STICKY_DUE_DATE);
            sticky.completed = Boolean.parseBoolean(intent.getStringExtra(Sticky.STICKY_COMPLETED));
        }

        current_project = db.findProjectByID(sticky.project_id);

        actionBar.setDisplayHomeAsUpEnabled(true);
        if (current_project.id > 0) actionBar.setTitle(current_project.name);

        // Hopefully won't be needed in future and can access from database
        Tag tag = new Tag();
        tag.id = Integer.parseInt( intent.getStringExtra(Tag.TAG_ID) );
        tag.name = intent.getStringExtra(Tag.TAG_NAME);
        tag.color = intent.getStringExtra(Tag.TAG_COLOR);



        // Create the text view
        TextView sticky_id = (TextView) findViewById(R.id.sticky_id);
        TextView description = (TextView) findViewById(R.id.description);
        TextView due_date = (TextView) findViewById(R.id.due_date);

        sticky_id.setText(Integer.toString(sticky.id));

        if(sticky.completed){
            sticky_id.setPaintFlags(sticky_id.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }

        description.setText(sticky.full_description());

        due_date.setText(sticky.short_due_date());

        TextView single_tag = (TextView) findViewById(R.id.single_tag);
        single_tag.setText(tag.name);
        if(tag.id != 0){
            single_tag.setBackgroundColor(Color.parseColor(tag.color));
        }else{
            single_tag.setVisibility(View.GONE);
        }

    }
}
