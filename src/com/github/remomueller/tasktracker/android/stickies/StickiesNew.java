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


public class StickiesNew extends Activity {
    private static final String TAG = "TaskTrackerAndroid";

    private int mYear;
    private int mMonth;
    private int mDay;

    private TextView mDateDisplay;
    private Button mPickDate;

    private EditText stickyDescription;
    private Button btnStickyCreate;

    Project current_project;

    static final int DATE_DIALOG_ID = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.stickies_new);

        mDateDisplay = (TextView) findViewById(R.id.due_date_show);
        mPickDate = (Button) findViewById(R.id.due_date_btn);

        mPickDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(DATE_DIALOG_ID);
            }
        });

        // get the current date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        // display the current date
        updateDisplay();

        Intent intent = getIntent();
        current_project = new Project();

        TextView projectNameTV = (TextView) findViewById(R.id.project_name);

        if(intent.getStringExtra(Project.PROJECT_ID) != null)
        {

            current_project.id = Integer.parseInt( intent.getStringExtra(Project.PROJECT_ID) );
            current_project.name = intent.getStringExtra(Project.PROJECT_NAME);
            // current_project.color = intent.getStringExtra(Project.PROJECT_COLOR);
            // actionBar.setDisplayHomeAsUpEnabled(true);
            // actionBar.setTitle(current_project.name);
            projectNameTV.setText(current_project.name);
            // actionBar.setTextColor(Color.parseColor(current_project.color));
        }

        stickyDescription = (EditText) findViewById(R.id.description);
        btnStickyCreate = (Button) findViewById(R.id.sticky_create);
        // loginErrorMsg = (TextView) findViewById(R.id.sticky_error);

        // Login button Click Event
        btnStickyCreate.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                String description = stickyDescription.getText().toString();
                String due_date = Integer.toString(mMonth + 1) + "/" + Integer.toString(mDay) + "/" + Integer.toString(mYear);

                new CreateSticky().execute(description, due_date, Integer.toString(current_project.id));
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
                sticky.description = params[0];
                sticky.due_date = params[1];
                sticky.project_id = Integer.parseInt(params[2]);
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

            if(json != null)
            {
                Log.d(TAG, "[StickyCreate] Response: " + json);
                if(json == "{\"description\":[\"can't be blank\"],\"project_id\":[\"can't be blank\"]}")
                    message = "Description can't be blank";
                else if(json == "{\"project_id\":[\"can't be blank\"]}")
                    message = "Project can't be blank";
                Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();
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
        User current_user = new User();
        String email = current_user.getEmail(getApplicationContext());
        String password = current_user.getPassword(getApplicationContext());
        String site_url = current_user.getSiteURL(getApplicationContext());

        // String params = "";

        String params = URLEncoder.encode("sticky[description]", "UTF-8") + "=" + URLEncoder.encode(sticky.description, "UTF-8");
        params += "&" + URLEncoder.encode("sticky[due_date]", "UTF-8") + "=" + URLEncoder.encode(sticky.due_date, "UTF-8");
        if(sticky.project_id > 0)
            params += "&" + URLEncoder.encode("sticky[project_id]", "UTF-8") + "=" + URLEncoder.encode(Integer.toString(sticky.project_id), "UTF-8");

        // params = "sticky[description]="+sticky.description+"&sticky[due_date]="+sticky.due_date;

        // if(sticky.project_id > 0)
        //     params = params + "&sticky[project_id]=" + Integer.toString(sticky.project_id);

        // // Filter by project if project is selected
        // if(current_project != null && current_project.id > 0)
        //     params = params + "&project_id=" + current_project.id;


        // URL url = new URL(site_url + "/stickies.json?" + params);
        // HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        // conn.setReadTimeout(10000 /* milliseconds */);
        // conn.setConnectTimeout(15000 /* milliseconds */);
        // conn.setRequestMethod("POST"); /* Can be POST */
        // conn.setDoInput(true);
        // conn.setRequestProperty("Accept-Charset", "UTF-8");
        // conn.setRequestProperty("Content-Type", "application/json");
        // conn.setRequestProperty("WWW-Authenticate", "Basic realm='Application'");


        // String decoded = email+":"+password;
        // String encoded = Base64.encodeBytes( decoded.getBytes() );

        // conn.setRequestProperty("Authorization", "Basic "+encoded);

        // // Starts the query
        // conn.connect();


        URL url = new URL(site_url + "/stickies.json?" + params);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        // conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("User-Agent", "Task Tracker Android");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        // conn.setRequestProperty("charset", "utf-8");
        conn.setRequestProperty("Accept-Charset", "UTF-8");
        conn.setRequestProperty("Content-Length", "" + Integer.toString(params.getBytes().length));
        conn.setRequestProperty("WWW-Authenticate", "Basic realm='Application'");
        conn.setUseCaches(false);

        String decoded = email+":"+password;
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
