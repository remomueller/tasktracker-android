package com.github.remomueller.tasktracker.android;

import android.app.Activity;
import android.app.Dialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.content.Intent;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Button;
import android.view.View;

import java.lang.StringBuilder;
import java.util.Calendar;

import java.net.URLEncoder;


import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.RadioGroup;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
// import java.io.DataOutputStream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.widget.Toast;

// From libs directory
import org.apache.commons.io.IOUtils;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import com.github.remomueller.tasktracker.android.util.Base64;
import com.github.remomueller.tasktracker.android.util.DatabaseHandler;

public class ProjectsNew extends Activity {
    private static final String TAG = "TaskTrackerAndroid";

    private int mYearEndDate;
    private int mMonthEndDate;
    private int mDayEndDate;

    private int mYearStartDate;
    private int mMonthStartDate;
    private int mDayStartDate;

    private TextView mDateDisplayStartDate;
    private Button mPickDateStartDate;

    private TextView mDateDisplayEndDate;
    private Button mPickDateEndDate;

    private EditText nameET;
    private EditText descriptionET;
    private RadioGroup statusRG;
    private Button createBtn;

    Project project;

    static final int START_DATE_DIALOG_ID = 0;
    static final int END_DATE_DIALOG_ID = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.projects_new);

        project = new Project();

        // DatabaseHandler db = new DatabaseHandler(getApplicationContext());
        // if(intent.getStringExtra(Project.PROJECT_ID) != null){
        //     project = db.findProjectByID(Integer.parseInt( intent.getStringExtra(Project.PROJECT_ID) ));
        // }

        mDateDisplayStartDate = (TextView) findViewById(R.id.start_date_show);
        mPickDateStartDate = (Button) findViewById(R.id.start_date_btn);

        mPickDateStartDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(START_DATE_DIALOG_ID);
            }
        });

        mDateDisplayEndDate = (TextView) findViewById(R.id.end_date_show);
        mPickDateEndDate = (Button) findViewById(R.id.end_date_btn);

        mPickDateEndDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(END_DATE_DIALOG_ID);
            }
        });

        // get the current date
        final Calendar c = Calendar.getInstance();
        mYearStartDate = c.get(Calendar.YEAR);
        mMonthStartDate = c.get(Calendar.MONTH);
        mDayStartDate = c.get(Calendar.DAY_OF_MONTH);

        mYearEndDate = c.get(Calendar.YEAR);
        mMonthEndDate = 11; // December (0..11)
        mDayEndDate = 31;

        // display the current date
        updateDisplayStartDate();
        updateDisplayEndDate();

        // Intent intent = getIntent();
        // current_project = new Project();

        // TextView projectNameTV = (TextView) findViewById(R.id.project_name);

        // if(intent.getStringExtra(Project.PROJECT_ID) != null)
        // {

        //     current_project.id = Integer.parseInt( intent.getStringExtra(Project.PROJECT_ID) );
        //     current_project.name = intent.getStringExtra(Project.PROJECT_NAME);
        //     // current_project.color = intent.getStringExtra(Project.PROJECT_COLOR);
        //     // actionBar.setDisplayHomeAsUpEnabled(true);
        //     // actionBar.setTitle(current_project.name);
        //     projectNameTV.setText(current_project.name);
        //     // actionBar.setTextColor(Color.parseColor(current_project.color));
        // }

        nameET = (EditText) findViewById(R.id.name);
        descriptionET = (EditText) findViewById(R.id.description);
        statusRG = (RadioGroup) findViewById(R.id.status);
        statusRG.check(R.id.status_ongoing);
        createBtn = (Button) findViewById(R.id.create);

        // Login button Click Event
        createBtn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                String name = nameET.getText().toString();
                String description = descriptionET.getText().toString();
                String start_date = Integer.toString(mMonthStartDate + 1) + "/" + Integer.toString(mDayStartDate) + "/" + Integer.toString(mYearStartDate);
                String end_date = Integer.toString(mMonthEndDate + 1) + "/" + Integer.toString(mDayEndDate) + "/" + Integer.toString(mYearEndDate);
                String status = "ongoing";

                switch(statusRG.getCheckedRadioButtonId()) {
                    case R.id.status_planned:
                        status = "planned";
                        break;
                    case R.id.status_ongoing:
                        status = "ongoing";
                        break;
                    case R.id.status_completed:
                        status = "completed";
                        break;
                }

                new CreateProject().execute(name, description, status, start_date, end_date);
            }
        });
    }

    private void updateDisplayStartDate() {
        this.mDateDisplayStartDate.setText(
        new StringBuilder()
            // Month is 0 based so add 1
            .append(mMonthStartDate + 1).append("-")
            .append(mDayStartDate).append("-")
            .append(mYearStartDate).append(" "));
    }

    private void updateDisplayEndDate() {
        this.mDateDisplayEndDate.setText(
        new StringBuilder()
            // Month is 0 based so add 1
            .append(mMonthEndDate + 1).append("-")
            .append(mDayEndDate).append("-")
            .append(mYearEndDate).append(" "));
    }

    private DatePickerDialog.OnDateSetListener mDateSetListenerStartDate =
    new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            mYearStartDate = year;
            mMonthStartDate = monthOfYear;
            mDayStartDate = dayOfMonth;
            updateDisplayStartDate();
        }
    };

    private DatePickerDialog.OnDateSetListener mDateSetListenerEndDate =
    new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            mYearEndDate = year;
            mMonthEndDate = monthOfYear;
            mDayEndDate = dayOfMonth;
            updateDisplayEndDate();
        }
    };

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case START_DATE_DIALOG_ID:
                return new DatePickerDialog(this, mDateSetListenerStartDate, mYearStartDate, mMonthStartDate, mDayStartDate);
            case END_DATE_DIALOG_ID:
                return new DatePickerDialog(this, mDateSetListenerEndDate, mYearEndDate, mMonthEndDate, mDayEndDate);
        }
        return null;
    }

    private class CreateProject extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                Project project = new Project();
                project.name = params[0];
                project.description = params[1];
                project.status = params[2];
                project.start_date = params[3];
                project.end_date = params[4];
                return createProject(project);
            } catch (IOException e) {
                return "Unable to Connect: Make sure you have an active network connection." + e;
            }
        }

        @Override
        protected void onPostExecute(String json) {
            String result = "";
            String message = json;
            boolean error_found = false;

            if(json != null)
            {
                Log.d(TAG, "[ProjectCreate] Response: " + json);
                if(json.equals("{\"name\":[\"can't be blank\"]}")){
                    message = "Name can't be blank";
                    error_found = true;
                }
            }

            if(error_found){
                Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();
            }

            if(!error_found){
                // No Error found, load project, and go to project show page
                Gson gson = new Gson();
                Project server_project;

                try {
                    server_project = gson.fromJson(json, Project.class);
                    if(server_project != null) project = server_project;
                } catch (JsonParseException e) {
                    if(json != null) Log.d(TAG, json);
                    Log.d(TAG, "Caught JsonParseException: " + e.getMessage());
                    server_project = new Project();
                }

                if(project.id > 0){
                    DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                    db.addOrUpdateProject(project);
                }

                Toast toast = Toast.makeText(getApplicationContext(), "Project was successfully created.", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();

                // Intent intent = new Intent(getApplicationContext(), ProjectsShow.class);
                Intent intent = new Intent(getApplicationContext(), StickiesIndex.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(Project.PROJECT_ID, Integer.toString(project.id));
                intent.putExtra(Project.PROJECT_NAME, project.name);

                startActivity(intent);
                finish();
            }
       }
    }

    private String createProject(Project project) throws IOException {
      InputStream is = null;
      OutputStreamWriter wr = null;
      BufferedReader rd = null;
      int len = 1000;

      try {
        User current_user = new User(getApplicationContext());
        // String email = current_user.getEmail(getApplicationContext());
        // String password = current_user.getPassword(getApplicationContext());
        // String site_url = current_user.getSiteURL(getApplicationContext());

        // String params = "";

        String params = URLEncoder.encode("project[name]", "UTF-8") + "=" + URLEncoder.encode(project.name, "UTF-8");
        params += "&" + URLEncoder.encode("project[description]", "UTF-8") + "=" + URLEncoder.encode(project.description, "UTF-8");
        params += "&" + URLEncoder.encode("project[status]", "UTF-8") + "=" + URLEncoder.encode(project.status, "UTF-8");
        params += "&" + URLEncoder.encode("project[start_date]", "UTF-8") + "=" + URLEncoder.encode(project.start_date, "UTF-8");
        params += "&" + URLEncoder.encode("project[end_date]", "UTF-8") + "=" + URLEncoder.encode(project.end_date, "UTF-8");


        URL url = new URL(current_user.site_url + "/projects.json?" + params);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("User-Agent", "Task Tracker Android");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        // conn.setRequestProperty("charset", "utf-8");
        conn.setRequestProperty("Accept-Charset", "UTF-8");
        conn.setRequestProperty("Content-Length", "" + Integer.toString(params.getBytes().length));
        conn.setRequestProperty("WWW-Authenticate", "Basic realm='Application'");
        conn.setUseCaches(false);

        String decoded = current_user.email+":"+current_user.password;
        String encoded = Base64.encodeBytes( decoded.getBytes() );

        conn.setRequestProperty("Authorization", "Basic "+encoded);

        wr = new OutputStreamWriter(conn.getOutputStream());
        wr.write(params);
        wr.flush();

        int response = conn.getResponseCode();

        if(response >= 400)
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        else
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        String line;
        String contentAsString = "";
        while ((line = rd.readLine()) != null) {
            // Process line...
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
