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
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.view.View;
import android.util.Log;

import android.graphics.Typeface;

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

    private LinearLayout stickyTagsLL;

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

        if(intent.getStringExtra(Project.PROJECT_ID) != null) {
            current_project = db.findProjectByID(Integer.parseInt( intent.getStringExtra(Project.PROJECT_ID) ));
        }

        Project project = db.findProjectByID(sticky.project_id);

        actionBar.setDisplayHomeAsUpEnabled(true);

        if (current_project.id > 0) {
            actionBar.setTitle(current_project.name);
        } else if (project.id > 0) {
            actionBar.setTitle(project.name);
        }

        stickyTagsLL = (LinearLayout) findViewById(R.id.sticky_tags);

        for(int i = 0; i < sticky.tags.length; i++) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                (Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM));
            // lp.setPadding(1, 2, 1, 2);
            lp.setMargins(0, 2, 0, 2);

            TextView tagTV = new TextView(getApplicationContext());
            tagTV.setText(sticky.tags[i].name);
            tagTV.setTypeface(null, Typeface.BOLD);
            tagTV.setGravity(Gravity.CENTER | Gravity.BOTTOM);
            tagTV.setCompoundDrawablePadding(2);
            // tagTV.setTextStyle("bold");
            // tagTV.setMarginLeft("2px");
            // tagTV.setMarginRight("2px");
            // tagTV.setShadowDx("1.2");
            // tagTV.setShadowColor(Color.parseColor("#333333"));
            // tagTV.setShadowRadius("1.2");
            tagTV.setBackgroundColor(Color.parseColor(sticky.tags[i].color));
            tagTV.setTextColor(Color.parseColor("#ffffff"));
            stickyTagsLL.addView(tagTV, lp);
        }

        // // Hopefully won't be needed in future and can access from database
        // Tag tag = new Tag();
        // tag.id = Integer.parseInt( intent.getStringExtra(Tag.TAG_ID) );
        // tag.name = intent.getStringExtra(Tag.TAG_NAME);
        // tag.color = intent.getStringExtra(Tag.TAG_COLOR);

        // TextView single_tag = (TextView) findViewById(R.id.single_tag);
        // single_tag.setText(tag.name);
        // if(tag.id != 0){
        //     single_tag.setBackgroundColor(Color.parseColor(tag.color));
        // }else{
        //     single_tag.setVisibility(View.GONE);
        // }



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

    }
}
