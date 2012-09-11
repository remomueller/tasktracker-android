package com.github.remomueller.tasktracker.android;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.net.Uri; // For Registration Link

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
// import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.HttpURLConnection;

import android.widget.Toast;

// From libs directory
import org.apache.commons.io.IOUtils;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;


import com.github.remomueller.tasktracker.android.util.DatabaseHandler;
import com.github.remomueller.tasktracker.android.util.Base64;

public class LoginActivity extends Activity {
    private static final String TAG = "TaskTrackerAndroid";

    Button btnLogin;
    Button btnLinkToRegister;
    EditText inputEmail;
    EditText inputPassword;
    TextView loginErrorMsg;
    EditText inputSiteURL;

    User current_user;

    // JSON Response node names
    private static String KEY_SUCCESS = "success";
    private static String KEY_ERROR = "error";
    private static String KEY_ERROR_MSG = "error_msg";
    private static String KEY_PASSWORD = "uid";
    private static String KEY_NAME = "name";
    private static String KEY_EMAIL = "email";
    private static String KEY_CREATED_AT = "created_at";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // Importing all assets like buttons, text fields
        inputSiteURL = (EditText) findViewById(R.id.loginSiteURL);
        inputEmail = (EditText) findViewById(R.id.loginEmail);
        inputPassword = (EditText) findViewById(R.id.loginPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLinkToRegister = (Button) findViewById(R.id.btnLinkToRegisterScreen);
        loginErrorMsg = (TextView) findViewById(R.id.login_error);

        current_user = new User(getApplicationContext());
        if(current_user.site_url != null && current_user.site_url != "")
            inputSiteURL.setText(current_user.site_url);
        if(current_user.email != null && current_user.email != "") {
            inputEmail.setText(current_user.email);
            inputPassword.requestFocus();
        } else {
            inputEmail.requestFocus();
        }


        // Login button Click Event
        btnLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                String site_url = inputSiteURL.getText().toString();
                String email = inputEmail.getText().toString();
                String password = inputPassword.getText().toString();

                new pullDataFromURL().execute(site_url, email, password);
            }
        });

        btnLinkToRegister.setOnClickListener( new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Uri uri = Uri.parse( "https://tasktracker.partners.org/users/register" );
                startActivity( new Intent( Intent.ACTION_VIEW, uri ) );
            }
        });

        // // Link to Register Screen
        // btnLinkToRegister.setOnClickListener(new View.OnClickListener() {

        //     public void onClick(View view) {
        //         Intent i = new Intent(getApplicationContext(),
        //                 RegisterActivity.class);
        //         startActivity(i);
        //         finish();
        //     }
        // });
    }

    private class pullDataFromURL extends AsyncTask<String, Void, Boolean> {
        String site_url;
        String email;
        String password;

        @Override
        protected Boolean doInBackground(String... params) {
            Boolean authenticated = false;

            site_url = params[0];
            email = params[1];
            password = params[2];

            InputStream is = null;
            int len = 1000;

            // InputStream is = null;
            OutputStreamWriter wr = null;
            BufferedReader rd = null;

            String contentAsString = "";

            try {

                String postparams = URLEncoder.encode("user[email]", "UTF-8") + "=" + URLEncoder.encode(email, "UTF-8");
                postparams += "&" + URLEncoder.encode("user[password]", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8");


                URL url = new URL(site_url + "/users/login.json");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Accept-Charset", "UTF-8");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("Content-Length", "" + Integer.toString(postparams.getBytes().length));
                // conn.setRequestProperty("WWW-Authenticate", "Basic realm='Application'");
                conn.setUseCaches(false);

                // String decoded = email+":"+password;
                // String encoded = Base64.encodeBytes( decoded.getBytes() );

                // conn.setRequestProperty("Authorization", "Basic "+encoded);

                // Starts the query
                // conn.connect();

                // int response = conn.getResponseCode();

                wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(postparams);
                wr.flush();

                int response = conn.getResponseCode();

                if(response >= 400)
                    rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                else
                    rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));


                Log.d(TAG, "The response is: " + response);

                is = conn.getInputStream();

                authenticated = (response == 200 || response == 201);


                String line;
                while ((line = rd.readLine()) != null) {
                    // Process line...
                    contentAsString = contentAsString + line;
                }



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

                if (wr != null) {
                    try {
                        wr.close();
                    } catch (IOException e) {
                       Log.d(TAG, "IOException: " + e);
                        return false;
                    }
                }

                if (rd != null) {
                    try {
                        rd.close();
                    } catch (IOException e) {
                       Log.d(TAG, "IOException: " + e);
                        return false;
                    }
                }

                // if (is != null) is.close();
                // if (wr != null) wr.close();
                // if (rd != null) rd.close();
            }

            if(authenticated){
                Gson gson = new Gson();

                User user;

                try {
                    Log.d(TAG, "JSON RETURNED: " + contentAsString);
                    String user_json = contentAsString.replaceAll(".*?\\\"user\\\"\\:", "").replaceAll("\\}\\}", "}");
                    Log.d(TAG, "JSON Substring: " + user_json);
                    user = gson.fromJson(user_json, User.class);
                } catch (JsonParseException e) {
                    user = new User(getApplicationContext());
                }

                DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                db.addUser(user.id, user.first_name, user.last_name, email, password, site_url, user.authentication_token);
            }

            return authenticated;
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                Intent intent = new Intent(getApplicationContext(), StickiesIndex.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }else{
                Toast toast = Toast.makeText(getApplicationContext(), "Login Failed: Incorrect email or password!", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();
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
