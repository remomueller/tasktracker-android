package com.github.remomueller.tasktracker.android.util;


import android.content.Context;
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
import com.github.remomueller.tasktracker.android.util.WebRequest;


public class WebRequest {
    private static final String TAG = "TaskTrackerAndroid";

    private Context context;
    private String method;
    private String path;
    private String params;
    private boolean doOutput;
    private boolean doInput;

    public WebRequest(Context context, String method, String path, String params) {
        this.context = context;
        this.method = method;
        if(method.equals("GET"))
            this.path = path + "?" + params;
        else
            this.path = path;
        this.doInput = true;
        this.doOutput = false;
        if(method.equals("POST") || method.equals("PUT") || method.equals("PATCH")) doOutput = true;

        this.params = params;
    }

    public String webRequest() throws IOException {
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
