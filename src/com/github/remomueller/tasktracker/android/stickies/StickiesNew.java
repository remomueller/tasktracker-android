package com.github.remomueller.tasktracker.android;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

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
import android.widget.CheckBox;

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

public class StickiesNew extends SherlockActivity {
    private static final String TAG = "TaskTrackerAndroid";

    ActionBar actionBar;

    private int mYear;
    private int mMonth;
    private int mDay;

    private TextView mDateDisplay;
    private Button mPickDate;

    private EditText descriptionET;
    private TextView assignedToTV;
    private EditText assignedToET;
    private CheckBox completedCB;
    private Button createBtn;

    private TextView projectNameTV;

    Project current_project;
    Sticky sticky;

    static final int DATE_DIALOG_ID = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        actionBar = getSupportActionBar();

        setContentView(R.layout.stickies_new);

        mDateDisplay = (TextView) findViewById(R.id.due_date_show);
        mPickDate = (Button) findViewById(R.id.due_date_btn);
        projectNameTV = (TextView) findViewById(R.id.project_name);
        assignedToTV = (TextView) findViewById(R.id.assigned_to);
        assignedToET = (EditText) findViewById(R.id.assigned_to_hidden);
        descriptionET = (EditText) findViewById(R.id.description);
        completedCB = (CheckBox) findViewById(R.id.completed);
        createBtn = (Button) findViewById(R.id.sticky_create);

        mPickDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(DATE_DIALOG_ID);
            }
        });

        Intent intent = getIntent();
        current_project = new Project();
        sticky = new Sticky();

        DatabaseHandler db = new DatabaseHandler(getApplicationContext());
        if(intent.getStringExtra(Sticky.STICKY_ID) != null){
            sticky = db.findStickyByID(Integer.parseInt( intent.getStringExtra(Sticky.STICKY_ID) ));
        }

        if(sticky.id > 0){
            current_project = db.findProjectByID(sticky.project_id);
            projectNameTV.setText(current_project.name);
        }
        else if(intent.getStringExtra(Project.PROJECT_ID) != null)
        {

            current_project.id = Integer.parseInt( intent.getStringExtra(Project.PROJECT_ID) );
            current_project.name = intent.getStringExtra(Project.PROJECT_NAME);
            // current_project.color = intent.getStringExtra(Project.PROJECT_COLOR);
            // actionBar.setDisplayHomeAsUpEnabled(true);
            // actionBar.setTitle(current_project.name);
            projectNameTV.setText(current_project.name);
            // actionBar.setTextColor(Color.parseColor(current_project.color));
        }


        // get the current date
        final Calendar c = Calendar.getInstance();
        if (sticky.id > 0) {
            mYear = sticky.dueDateYear();
            mMonth = sticky.dueDateMonth() - 1;
            mDay = sticky.dueDateDay();
        } else {
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);
        }

        // display the current date
        updateDisplay();

        User current_user = new User(getApplicationContext());

        if (sticky.id > 0) {
            if (sticky.owner_id > 0) {
                assignedToTV.setText("User ID: " + Integer.toString(sticky.owner_id));
                assignedToET.setText(Integer.toString(sticky.owner_id));
            }

        } else {

            if(current_user.id > 0 && current_user.name() == "")
                assignedToTV.setText("Me");
            else if(current_user.id > 0 && current_user.name() != "")
                assignedToTV.setText(current_user.name());
            assignedToET.setText(Integer.toString(current_user.id));
        }

        if (sticky.id > 0) {
            actionBar.setTitle("Edit Sticky " + sticky.name());
            descriptionET.setText(sticky.description);
            completedCB.setChecked(sticky.completed);
            createBtn.setText("Update Sticky");
        }

        // Login button Click Event
        createBtn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                String description = descriptionET.getText().toString();
                String due_date = Integer.toString(mMonth + 1) + "/" + Integer.toString(mDay) + "/" + Integer.toString(mYear);
                String owner_id = assignedToET.getText().toString();
                String completed = (completedCB.isChecked() ? "1" : "0");

                new CreateSticky().execute(Integer.toString(sticky.id), description, due_date, Integer.toString(current_project.id), owner_id, completed);
            }
        });

    }

    private void updateDisplay() {
        this.mDateDisplay.setText(
        new StringBuilder()
            // Month is 0 based so add 1
            .append(mMonth + 1).append("-")
            .append(mDay).append("-")
            .append(mYear).append(" "));
    }

    private DatePickerDialog.OnDateSetListener mDateSetListener =
    new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            mYear = year;
            mMonth = monthOfYear;
            mDay = dayOfMonth;
            updateDisplay();
        }
    };

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DATE_DIALOG_ID:
            return new DatePickerDialog(this, mDateSetListener, mYear, mMonth, mDay);
        }
        return null;
    }

    private class CreateSticky extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                Sticky sticky = new Sticky();
                sticky.id = Integer.parseInt(params[0]);
                sticky.description = params[1];
                sticky.due_date = params[2];
                sticky.project_id = Integer.parseInt(params[3]);
                sticky.owner_id = Integer.parseInt(params[4]);
                sticky.completed = params[5].equals("1");
                return createSticky(sticky);
            } catch (IOException e) {
                return "Unable to Connect: Make sure you have an active network connection." + e;
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String json) {
            String result = "";
            String message = json;
            boolean error_found = false;

            if(json != null)
            {
                Log.d(TAG, "[StickyCreate] Response: " + json);
                if(json.equals("{\"description\":[\"can't be blank\"]}")){
                    message = "Description can't be blank";
                    error_found = true;
                } else if(json.equals("{\"project_id\":[\"can't be blank\"]}") || json.equals("{\"description\":[\"can't be blank\"],\"project_id\":[\"can't be blank\"]}")){
                    message = "Project can't be blank.\nSelect a project from the Projects page then click New Sticky.";
                    error_found = true;
                }
            }

            if(error_found){
                Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();
            }

            if(!error_found){
                // No Error found, load sticky, and go to sticky show page

                boolean created = (sticky.id == 0);

                Log.d(TAG, "Sticky ID?: " + Integer.toString(sticky.id));

                Gson gson = new Gson();
                Sticky server_sticky; // Consider renaming if it uses the same name as the global....?

                try {
                    server_sticky = gson.fromJson(json, Sticky.class);
                    if(server_sticky != null) sticky = server_sticky;
                } catch (JsonParseException e) {
                    if(json != null) Log.d(TAG, json);
                    Log.d(TAG, "Caught JsonParseException: " + e.getMessage());
                    server_sticky = new Sticky();
                }

                if(sticky.id > 0){
                    DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                    db.addOrUpdateSticky(sticky);
                }

                String notice;

                if (created) {
                    notice = "Sticky was successfully created.";
                } else {
                    notice = "Sticky was successfully updated.";
                }

                Toast toast = Toast.makeText(getApplicationContext(), notice, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();

                Intent intent = new Intent(getApplicationContext(), StickiesShow.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(Sticky.STICKY_ID, Integer.toString(sticky.id));

                String tag_id = "0";
                String tag_name = "";
                String tag_color = "#80FFFFFF";

                // for(int i = 0; i < sticky.tags.length; i++){
                //     if(i == 0){
                //         tag_id = Integer.toString(sticky.tags[i].id);
                //         tag_name = sticky.tags[i].name;
                //         tag_color = sticky.tags[i].color;
                //     }
                // }

                intent.putExtra(Tag.TAG_ID, tag_id);
                intent.putExtra(Tag.TAG_NAME, tag_name);
                intent.putExtra(Tag.TAG_COLOR, tag_color);

                startActivity(intent);
                finish();
            }


            // Gson gson = new Gson();

            // Sticky[] stickies_array;

            // try {
            //     stickies_array = gson.fromJson(json, Sticky[].class);
            //     if(stickies_array == null){
            //         stickies_array = new Sticky[0];
            //     }
            //     result = stickies_array.length + " Stick" + (stickies_array.length == 1 ? "y" : "ies");
            // } catch (JsonParseException e) {
            //     stickies_array = new Sticky[0];
            //     result = "Login Failed: Please make sure your email and password are correct.";
            // }

            // for(int i = 0; i < stickies_array.length; i++){
            //     stickies.add(stickies_array[i]);
            // }

            // if(stickyAdapter != null){
            //     stickyAdapter.notifyDataSetChanged();
            // }
       }
    }

    private String createSticky(Sticky sticky) throws IOException {
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

        String params = URLEncoder.encode("sticky[description]", "UTF-8") + "=" + URLEncoder.encode(sticky.description, "UTF-8");
        params += "&" + URLEncoder.encode("sticky[due_date]", "UTF-8") + "=" + URLEncoder.encode(sticky.due_date, "UTF-8");
        params += "&" + URLEncoder.encode("sticky[completed]", "UTF-8") + "=" + URLEncoder.encode((sticky.completed ? "1" : "0"), "UTF-8");
        if(sticky.project_id > 0)
            params += "&" + URLEncoder.encode("sticky[project_id]", "UTF-8") + "=" + URLEncoder.encode(Integer.toString(sticky.project_id), "UTF-8");
        if(sticky.owner_id > 0)
            params += "&" + URLEncoder.encode("sticky[owner_id]", "UTF-8") + "=" + URLEncoder.encode(Integer.toString(sticky.owner_id), "UTF-8");

        URL url;
        if (sticky.id > 0) {
            url = new URL(current_user.site_url + "/stickies/" + Integer.toString(sticky.id) + ".json?" + params);
        } else {
            url = new URL(current_user.site_url + "/stickies.json?" + params);
        }

        // URL url = new URL(current_user.site_url + "/stickies.json?" + params);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setDoOutput(true);
        if (sticky.id > 0) {
            conn.setRequestMethod("PUT");
        } else {
            conn.setRequestMethod("POST");
        }
        // conn.setRequestProperty("Content-Type", "application/json");
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

        // DataOutputStream wr = new DataOutputStream(conn.getOutputStream ());
        // wr.writeBytes(params);
        // wr.flush();
        // wr.close();
        // conn.disconnect();


        wr = new OutputStreamWriter(conn.getOutputStream());
        wr.write(params);
        wr.flush();

        int response = conn.getResponseCode();

        if(response >= 400)
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        else
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        // Get the response
        // rd = new BufferedReader(new InputStreamReader(((HttpURLConnection) (new URL(urlString)).openConnection()).getInputStream(), Charset.forName("UTF-8")));

        String line;
        String contentAsString = "";
        while ((line = rd.readLine()) != null) {
            // Process line...
            contentAsString = contentAsString + line;
        }



        // int response = conn.getResponseCode();
        // Log.d(TAG, "The response is: " + response);
        // is = conn.getInputStream();

        // // Convert the InputStream into a string
        // String contentAsString = "";
        // if(is != null){
        //     Log.d(TAG, "Before READIT");
        //     contentAsString = readIt(is, len);
        //     Log.d(TAG, "After READIT");
        // }

        // if(contentAsString == null){
        //     contentAsString = "";
        // }

        // Log.d(TAG, "The content is: " + contentAsString);

        return contentAsString;

      } finally {
        if (is != null) is.close();
        if (wr != null) wr.close();
        if (rd != null) rd.close();
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
