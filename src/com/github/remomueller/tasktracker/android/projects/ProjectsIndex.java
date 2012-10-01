package com.github.remomueller.tasktracker.android;


import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.InputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.widget.Toast;
import android.view.Gravity;

// From libs directory
import org.apache.commons.io.IOUtils;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import com.github.remomueller.tasktracker.android.util.Base64;

import com.github.remomueller.tasktracker.android.util.ProjectsRequest;
import com.github.remomueller.tasktracker.android.util.ProjectsRequest.ProjectsRequestFinishedListener;
import com.github.remomueller.tasktracker.android.util.DatabaseHandler;

public class ProjectsIndex extends SherlockActivity {
    private static final String TAG = "TaskTrackerAndroid";

    ActionBar actionBar;

    ListView list;

    public ArrayList<Project> projects = new ArrayList<Project>();
    ProjectAdapter projectAdapter;
    DatabaseHandler db;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
      MenuInflater inflater = getSupportMenuInflater();
      inflater.inflate(R.menu.projects_menu, menu);

      return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        Intent intent;
        switch (item.getItemId()) {
            case R.id.projects:
                intent = new Intent(getApplicationContext(), ProjectsIndex.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                return true;
            case R.id.stickies:
                intent = new Intent(getApplicationContext(), StickiesIndex.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                return true;
            case R.id.new_project:
                intent = new Intent(getApplicationContext(), ProjectsNew.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                // finish();
                return true;
            case R.id.about:
                intent = new Intent(getApplicationContext(), AboutActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                // finish();
                return true;
            case R.id.logout:
                User current_user = new User(getApplicationContext());
                current_user.logoutUser();
                intent = new Intent(getApplicationContext(), LoginActivity.class);
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

        db = new DatabaseHandler(getApplicationContext());

        actionBar = getSupportActionBar();

        setContentView(R.layout.projects_index);

        list = (ListView)findViewById(R.id.projects_list);

        projectAdapter = new ProjectAdapter(this, projects);
        list.setAdapter(projectAdapter);

        // new GetProjects().execute(MainActivity.HOST_URL);

        list.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Project project = projects.get(position);
                Intent intent = new Intent(getApplicationContext(), StickiesIndex.class);
                intent.putExtra(Project.PROJECT_ID, Integer.toString(project.id));
                intent.putExtra(Project.PROJECT_NAME, project.name);
                // intent.putExtra(Project.PROJECT_COLOR, project.color);
                startActivity(intent);
            }
        });

        list.setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Project project = projects.get(position);
                Toast toast = Toast.makeText(getApplicationContext(), "Project " + project.name + " selected.", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();
                return true;
            }
        });

        ProjectsRequestFinishedListener finishedListener = new ProjectsRequestFinishedListener()
        {
            @Override
            public void onTaskFinished(ArrayList<Project> loadedProjects) {
                // Gson gson = new Gson();
                // Project[] array;

                // try {
                //     array = gson.fromJson(json, Project[].class);
                //     if(array == null) array = new Project[0];
                // } catch (JsonParseException e) {
                //     array = new Project[0];
                // }

                // for(int i = 0; i < array.length; i++) {
                //     db.addOrUpdateProject(array[i]);
                // }

                // projects.addAll(db.findAllProjects(null));
                projects.addAll(loadedProjects);
                projectAdapter.notifyDataSetChanged();
            }
        };

        new ProjectsRequest(getApplicationContext(), "GET", "/projects.json", null, null, finishedListener).execute();
    }
}
