package com.github.remomueller.tasktracker.android;

// import com.actionbarsherlock.app.SherlockActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.github.remomueller.tasktracker.android.util.UserFunctionsGSON;

public class DashboardActivity extends Activity {
    UserFunctionsGSON userFunctionsGSON;
    Button btnLogout;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**
         * Dashboard Screen for the application
         * */
        // Check login status in database
        userFunctionsGSON = new UserFunctionsGSON();
        if(userFunctionsGSON.isUserLoggedIn(getApplicationContext())){

            Intent intent = new Intent(getApplicationContext(), StickiesIndex.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();

       // // user already logged in show dashboard
       //      setContentView(R.layout.dashboard);
       //      btnLogout = (Button) findViewById(R.id.btnLogout);

       //      btnLogout.setOnClickListener(new View.OnClickListener() {

       //          public void onClick(View arg0) {
       //              // TODO Auto-generated method stub
       //              userFunctionsGSON.logoutUser(getApplicationContext());
       //              Intent login = new Intent(getApplicationContext(), LoginActivity.class);
       //              login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
       //              startActivity(login);
       //              // Closing dashboard screen
       //              finish();
       //          }
       //      });

        }else{
            // user is not logged in show login screen
            Intent login = new Intent(getApplicationContext(), LoginActivity.class);
            login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(login);
            // Closing dashboard screen
            finish();
        }
    }
}
