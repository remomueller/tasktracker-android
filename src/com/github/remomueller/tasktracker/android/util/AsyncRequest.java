package com.github.remomueller.tasktracker.android.util;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.HttpURLConnection;

// From libs directory
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import android.content.pm.PackageManager.NameNotFoundException;

import com.github.remomueller.tasktracker.android.User;

import com.github.remomueller.tasktracker.android.util.Base64;
import com.github.remomueller.tasktracker.android.util.DatabaseHandler;

// GET /stickies.json                                           // Get JSON Array of Stickies
// GET /stickies/1.json                                         // Get JSON Sticky Object
// POST /stickies.json?description=New+Sticky&project_id=1      // Create Sticky (returns JSON Sticky object)
// PUT /stickies/1.json?description=Update+Sticky&project_id=1  // Update Sticky (returns JSON Sticky object) May be updated to PATCH in the future
// DELETE /stickies/1.json                                      // Delete Sticky (returns empty JSON)


public class AsyncRequest extends AsyncTask<String, Void, String> {
    private static final String TAG = "TaskTrackerAndroid";

    private Context context;
    private String method;
    private String path;
    private String params;
    private boolean doOutput;
    private boolean doInput;
    private final AsyncRequestFinishedListener finishedListener;

    // String params = URLEncoder.encode("project[name]", "UTF-8") + "=" + URLEncoder.encode(project.name, "UTF-8");
    // params += "&" + URLEncoder.encode("project[description]", "UTF-8") + "=" + URLEncoder.encode(project.description, "UTF-8");
    // params += "&" + URLEncoder.encode("project[status]", "UTF-8") + "=" + URLEncoder.encode(project.status, "UTF-8");
    // params += "&" + URLEncoder.encode("project[start_date]", "UTF-8") + "=" + URLEncoder.encode(project.start_date, "UTF-8");
    // params += "&" + URLEncoder.encode("project[end_date]", "UTF-8") + "=" + URLEncoder.encode(project.end_date, "UTF-8");

    public interface AsyncRequestFinishedListener {
        void onTaskFinished(String json); // If you want to pass something back to the listener add a param to this method
    }

    public AsyncRequest(Context context, String method, String path, String params, AsyncRequestFinishedListener finishedListener) {
        this.context = context;
        this.method = method;
        this.path = path;
        this.doInput = true;
        this.doOutput = false;
        if(method.equals("POST") || method.equals("PUT") || method.equals("PATCH")) doOutput = true;

        this.params = params;
        this.finishedListener = finishedListener;
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            return webRequest();
        } catch (IOException e) {
            return "Unable to Connect: Make sure you have an active network connection." + e;
        }
    }

    @Override
    protected void onPostExecute(String json) {
        super.onPostExecute(json);
        finishedListener.onTaskFinished(json);
   }

    private String webRequest() throws IOException {
      InputStream is = null;
      OutputStreamWriter wr = null;
      BufferedReader rd = null;
      int len = 1000;

      try {
        User current_user = new User(context);

        String decoded = current_user.email+":"+current_user.password;
        String encoded = Base64.encodeBytes( decoded.getBytes() );

        URL url = new URL(current_user.site_url + path);

        String versionName = "";
        try {
            versionName = " " + context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            Log.d(TAG, e.getMessage());
        }

        // Log.d(TAG, "--------------");
        // Log.d(TAG, "- ");
        // Log.d(TAG, "- Version" + versionName);
        // Log.d(TAG, "- ");
        // Log.d(TAG, "--------------");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty(      "User-Agent", "Task Tracker Android" + versionName);
        conn.setRequestProperty(  "Accept-Charset", "UTF-8");
        conn.setRequestProperty("WWW-Authenticate", "Basic realm='Application'");
        conn.setRequestProperty(   "Authorization", "Basic "+encoded);
        conn.setRequestProperty(    "Content-Type", "application/x-www-form-urlencoded");
        conn.setReadTimeout(10000);    /* milliseconds, 10 seconds */
        conn.setConnectTimeout(15000); /* milliseconds, 15 seconds */
        conn.setUseCaches(false);
        conn.setRequestMethod(method);
        conn.setDoInput(doInput);
        conn.setDoOutput(doOutput);

        if(doOutput){
            conn.setRequestProperty("Content-Length", "" + Integer.toString(params.getBytes().length));
            wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(params);
            wr.flush();
        } else {
            conn.connect();
        }

        int response = conn.getResponseCode();

        if(response >= 400)
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        else
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        String line;
        String contentAsString = "";
        while ((line = rd.readLine()) != null) {
            contentAsString = contentAsString + line;
        }

        return contentAsString;

      } finally {
        if (is != null) is.close();
        if (wr != null) wr.close();
        if (rd != null) rd.close();
      }
    }
}
