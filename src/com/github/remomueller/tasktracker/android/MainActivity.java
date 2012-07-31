package com.github.remomueller.tasktracker.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends Activity
{
    public final static String HOST_URL = "https://sleepepi.partners.org/edge/tasktracker";

    public final static String USERNAME = "com.example.mysecondapp.USERNAME";
    public final static String PASSWORD = "com.example.mysecondapp.PASSWORD";

//     private EditText usernameText;
//     private EditText passwordText;
//     private String username;
//     private String password;

//     /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
//         setContentView(R.layout.main);

//         usernameText = (EditText) findViewById(R.id.username);
//         passwordText = (EditText) findViewById(R.id.password);
    }

//     // Login
//     public void myClickHandler(View view)
//     {
//         username = usernameText.getText().toString();
//         password = passwordText.getText().toString();

//         // new DownloadWebpageText().execute(stringUrl);
//         // new DownloadJSONSTickies().execute(stringUrl);
//         Intent intent = new Intent(this, StickiesIndex.class);

//         intent.putExtra(USERNAME, username);
//         intent.putExtra(PASSWORD, password);

//         startActivity(intent);
//     }
}
