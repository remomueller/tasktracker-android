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

import com.github.remomueller.tasktracker.android.util.DatabaseHandler;
import com.github.remomueller.tasktracker.android.util.UserFunctionsGSON;

public class LoginActivity extends Activity {
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

                UserFunctionsGSON userFunctionsGSON = new UserFunctionsGSON();

                if (userFunctionsGSON.canAuthenticateUser(site_url, email, password)) {
                    DatabaseHandler db = new DatabaseHandler(getApplicationContext());

                    // Clear all previous data in database
                    userFunctionsGSON.logoutUser(getApplicationContext());
                    db.addUser(site_url, email, password);

                    Intent intent = new Intent(getApplicationContext(), StickiesIndex.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);

                    finish();

                    // // Launch Dashboard Screen
                    // Intent dashboard = new Intent(getApplicationContext(), DashboardActivity.class);

                    // // Close all views before launching Dashboard
                    // dashboard.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    // startActivity(dashboard);

                    // finish();
                }else{
                    loginErrorMsg.setText("Incorrect username/password");
                }
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
}
