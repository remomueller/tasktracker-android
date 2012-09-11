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

// From libs directory
import org.apache.commons.io.IOUtils;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import com.github.remomueller.tasktracker.android.util.Base64;

public class ProjectsIndex extends SherlockActivity {
    private static final String TAG = "TaskTrackerAndroid";

    ActionBar actionBar;

    ListView list;

    public ArrayList<Project> projects = new ArrayList<Project>();
    ProjectAdapter projectAdapter;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
      MenuInflater inflater = getSupportMenuInflater();
      inflater.inflate(R.menu.main_menu, menu);

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

        actionBar = getSupportActionBar();

        setContentView(R.layout.projects_index);

        list = (ListView)findViewById(R.id.projects_list);

        projectAdapter = new ProjectAdapter(this, projects);
        list.setAdapter(projectAdapter);

        new GetProjects().execute(MainActivity.HOST_URL);

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

    }

    private class GetProjects extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                return getProjectsFromDB();
            } catch (IOException e) {
                return "Unable to Connect: Make sure you have an active network connection.";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String json) {
            // String result = "";

            Gson gson = new Gson();

            Project[] projects_array;

            try {
                projects_array = gson.fromJson(json, Project[].class);
                if(projects_array == null){
                    projects_array = new Project[0];
                }
                // result = projects_array.length + " Stick" + (projects_array.length == 1 ? "y" : "ies");
                // selection.setText(result);
            } catch (JsonParseException e) {
                projects_array = new Project[0];
                // result = "Login Failed: Please make sure your email and password are correct.";
                // selection.setText(result);
            }

            for(int i = 0; i < projects_array.length; i++){
                projects.add(projects_array[i]);
            }

            projectAdapter.notifyDataSetChanged();
       }
    }

    private String getProjectsFromDB() throws IOException {
      InputStream is = null;
      int len = 1000;

      try {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        String s = formatter.format(date);
        String due_date_end_date = s;

        User current_user = new User(getApplicationContext());

        // String email = current_user.email;
        // String password = current_user.password;
        // String site_url = current_user.site_url;

        URL url = new URL(current_user.site_url + "/projects.json");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET"); /* Can be POST */
        conn.setDoInput(true);
        conn.setRequestProperty("Accept-Charset", "UTF-8");
        // conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("WWW-Authenticate", "Basic realm='Application'");


        String decoded = current_user.email+":"+current_user.password;
        String encoded = Base64.encodeBytes( decoded.getBytes() );

        conn.setRequestProperty("Authorization", "Basic "+encoded);

        // Starts the query
        conn.connect();

        int response = conn.getResponseCode();
        // Log.d(TAG, "The response is: " + response);
        is = conn.getInputStream();

        // Convert the InputStream into a string
        String contentAsString = "";
        if(is != null){
           contentAsString = readIt(is, len);
        }

        if(contentAsString == null){
            contentAsString = "";
        }

        return contentAsString;

      } finally {
        if (is != null) {
            is.close();
        }
      }
    }

    // Reads an InputStream and converts it to a String.
    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        String encoding = "UTF-8";
        StringWriter writer = new StringWriter();
        IOUtils.copy(stream, writer, encoding);
        return new String(writer.toString());
    }

}
