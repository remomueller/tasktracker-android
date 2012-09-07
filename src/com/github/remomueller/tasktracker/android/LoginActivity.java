package com.github.remomueller.tasktracker.android;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.net.Uri; // For Registration Link

import android.os.AsyncTask;

import java.io.InputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
// import java.net.MalformedURLException;
import java.net.URL;
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

            try {

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

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                User current_user = new User();
                DatabaseHandler db = new DatabaseHandler(getApplicationContext());

                // Clear all previous data in database
                current_user.logoutUser(getApplicationContext());
                db.addUser(site_url, email, password);

                Intent intent = new Intent(getApplicationContext(), StickiesIndex.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

                finish();
            }else{
                Toast.makeText(getApplicationContext(), "Login Failed: Incorrect email or password!", Toast.LENGTH_LONG).show();
            //     loginErrorMsg.setText("Incorrect username/password");
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
