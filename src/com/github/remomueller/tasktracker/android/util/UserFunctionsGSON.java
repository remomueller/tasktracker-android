package com.github.remomueller.tasktracker.android.util;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

// import org.apache.http.NameValuePair;
// import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.util.Log;

import java.io.InputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
// import java.net.MalformedURLException;
import java.net.URL;
import java.net.HttpURLConnection;
// import java.text.SimpleDateFormat;
// import java.util.ArrayList;
// import java.util.Date;

// From libs directory
import org.apache.commons.io.IOUtils;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

// import com.github.remomueller.tasktracker.android.util.Base64;



public class UserFunctionsGSON {

    public final static String HOST_URL = "https://sleepepi.partners.org/edge/tasktracker";

    private static final String TAG = "TaskTrackerAndroid";

    private Gson gson;

    // constructor
    public UserFunctionsGSON(){
        gson = new Gson();
    }


    // public String loginUser(String email, String password){
    //     String result = '';
    //     String[] values = { email, password };
    //     result = gson.toJson(values);
    //     // // Building Parameters
    //     // List<NameValuePair> params = new ArrayList<NameValuePair>();
    //     // params.add(new BasicNameValuePair("tag", login_tag));
    //     // params.add(new BasicNameValuePair("email", email));
    //     // params.add(new BasicNameValuePair("password", password));
    //     // JSONObject json = jsonParser.getJSONFromUrl(loginURL, params);
    //     // // return json
    //     // // Log.e("JSON", json.toString());
    //     return result;
    // }



    public boolean canAuthenticateUser(String site_url, String email, String password) {
        InputStream is = null;
        boolean authenticated = false;
        try {
            // Date date = new Date();
            // SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
            // String s = formatter.format(date);
            // String due_date_end_date = s;

            URL url = new URL(site_url + "/stickies.json");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET"); /* Can be POST */
            conn.setDoInput(true);
            conn.setRequestProperty("Accept-Charset", "UTF-8");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("WWW-Authenticate", "Basic realm='Application'");

            String decoded = email+":"+password;
            String encoded = Base64.encodeBytes( decoded.getBytes() );

            conn.setRequestProperty("Authorization", "Basic "+encoded);

            // Starts the query
            conn.connect();

            int response = conn.getResponseCode();
            Log.d(TAG, "The response is: " + response);

            is = conn.getInputStream();

            authenticated = (response == 200);
            // Convert the InputStream into a string
            // String contentAsString = readIt(is);
        } catch (IOException e) {
            Log.d(TAG, "IOException: " + e);
            return false;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                   Log.d(TAG, "IOException: " + e);
                    return false;
                }

            }
        }

        return authenticated;
    }

    // // Reads an InputStream and converts it to a String.
    // public String readIt(InputStream stream) throws IOException, UnsupportedEncodingException {
    //     String encoding = "UTF-8";
    //     StringWriter writer = new StringWriter();
    //     IOUtils.copy(stream, writer, encoding);
    //     return new String(writer.toString());
    // }

    public String getSiteURL(Context context){
        DatabaseHandler db = new DatabaseHandler(context);
        HashMap<String,String> user;
        user = db.getUserDetails();
        return user.get("site_url");
    }

    public String getEmail(Context context){
        DatabaseHandler db = new DatabaseHandler(context);
        HashMap<String,String> user;
        user = db.getUserDetails();
        return user.get("email");
    }

    public String getPassword(Context context){
        DatabaseHandler db = new DatabaseHandler(context);
        HashMap<String,String> user;
        user = db.getUserDetails();
        return user.get("password");
    }


    /**
     * Function get Login status
     * */
    public boolean isUserLoggedIn(Context context){
        DatabaseHandler db = new DatabaseHandler(context);
        int count = db.getRowCount();
        if(count > 0){
            // user logged in
            return true;
        }
        return false;
    }

    /**
     * Function to logout user
     * Reset Database
     * */
    public boolean logoutUser(Context context){
        DatabaseHandler db = new DatabaseHandler(context);
        db.resetTables();
        return true;
    }


    // /**
    //  * function make Login Request
    //  * @param name
    //  * @param email
    //  * @param password
    //  * */
    // public JSONObject registerUser(String name, String email, String password){
    //     // Building Parameters
    //     List<NameValuePair> params = new ArrayList<NameValuePair>();
    //     params.add(new BasicNameValuePair("tag", register_tag));
    //     params.add(new BasicNameValuePair("name", name));
    //     params.add(new BasicNameValuePair("email", email));
    //     params.add(new BasicNameValuePair("password", password));

    //     // getting JSON Object
    //     JSONObject json = jsonParser.getJSONFromUrl(registerURL, params);
    //     // return json
    //     return json;
    // }
}
