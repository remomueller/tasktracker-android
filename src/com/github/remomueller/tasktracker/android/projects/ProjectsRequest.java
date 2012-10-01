// // This file should act as an intermediary between the app, and the database/web JSON requests.

// // Ex:
// // 1. App makes requests for all projects
// // 2. ProjectRecord checks if a refresh has been requested, or if it's been X time since last refresh
// // 3. If refresh is requested, then make a web request for projects as JSON and load them into the internal database
// // 4. Complete request by returning all projects as Project objects (Array) from database.

// // NOTE: If the pull from the external JSON server fails, it should still pull from the internal database.

package com.github.remomueller.tasktracker.android.util;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import java.util.ArrayList;

// From libs directory
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import com.github.remomueller.tasktracker.android.Project;
import com.github.remomueller.tasktracker.android.User;

// import com.github.remomueller.tasktracker.android.util.DatabaseHandler;
import com.github.remomueller.tasktracker.android.util.WebRequest;

// GET    /projects.json                                          // Get JSON Array of Projects
// GET    /projects/1.json                                        // Get JSON Project Object
// POST   /projects.json?name=New+Project                         // Create Project (returns JSON Project object)
// PUT    /projects/1.json?name=Update+Project                    // Update Project (returns JSON Project object) May be updated to PATCH in the future
// DELETE /projects/1.json                                        // Delete Project (returns empty JSON)


public class ProjectsRequest extends AsyncTask<Void, Void, ArrayList<Project>> {
    private static final String TAG = "TaskTrackerAndroid";

    private WebRequest webRequest;
    private Context context;
    // private String method;
    // private String path;
    // private String params;
    private String conditions;
    // private boolean doOutput;
    // private boolean doInput;
    private final ProjectsRequestFinishedListener finishedListener;

    DatabaseHandler db;

    // String params = URLEncoder.encode("project[name]", "UTF-8") + "=" + URLEncoder.encode(project.name, "UTF-8");
    // params += "&" + URLEncoder.encode("project[description]", "UTF-8") + "=" + URLEncoder.encode(project.description, "UTF-8");
    // params += "&" + URLEncoder.encode("project[status]", "UTF-8") + "=" + URLEncoder.encode(project.status, "UTF-8");
    // params += "&" + URLEncoder.encode("project[start_date]", "UTF-8") + "=" + URLEncoder.encode(project.start_date, "UTF-8");
    // params += "&" + URLEncoder.encode("project[end_date]", "UTF-8") + "=" + URLEncoder.encode(project.end_date, "UTF-8");

    public interface ProjectsRequestFinishedListener {
        void onTaskFinished(ArrayList<Project> projects); // If you want to pass something back to the listener add a param to this method
    }

    public ProjectsRequest(Context context, String method, String path, String params, String conditions, ProjectsRequestFinishedListener finishedListener) {
        this.webRequest = new WebRequest(context, method, path, params);
        this.conditions = conditions;
        this.db = new DatabaseHandler(context);
        this.finishedListener = finishedListener;
    }

    @Override
    protected ArrayList<Project> doInBackground(Void... params) {
        try {
            // Check if web request is required.

            // Web Request for New Projects
            String json = webRequest.webRequest();

            Gson gson = new Gson();
            Project[] array;

            try {
                array = gson.fromJson(json, Project[].class);
                if(array == null) array = new Project[0];
            } catch (JsonParseException e) {
                array = new Project[0];
            }

            for(int i = 0; i < array.length; i++) {
                db.addOrUpdateProject(array[i]);
            }

            // Load Projects from Internal Database
            ArrayList<Project> projects = db.findAllProjects(conditions);

            return projects;
        } catch (IOException e) {
            ArrayList<Project> projects = new ArrayList<Project>();
            // return "Unable to Connect: Make sure you have an active network connection." + e;
            return projects;
        }
    }

    @Override
    protected void onPostExecute(ArrayList<Project> projects) {
        super.onPostExecute(projects);
        finishedListener.onTaskFinished(projects);
   }

}
